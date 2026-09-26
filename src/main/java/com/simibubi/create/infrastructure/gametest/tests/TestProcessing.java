package com.simibubi.create.infrastructure.gametest.tests;

import java.util.List;
import java.util.function.Function;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import io.netty.buffer.Unpooled;

@GameTestGroup(path = "processing")
public class TestProcessing {
	@GameTest(template = "water_filling_bottle")
	public static void mixingTransactionsJoinTheirCaller(CreateGameTestHelper helper) {
		ItemStackHandler inventory = new ItemStackHandler(1);
		try (Transaction transaction = TransferUtil.getTransaction()) {
			inventory.insert(ItemVariant.of(Items.NETHER_WART), 1, transaction);
			transaction.commit();
		}
		try (Transaction outer = Transaction.openOuter()) {
			try (Transaction nested = TransferUtil.getTransaction()) {
				inventory.extract(ItemVariant.of(Items.NETHER_WART), 1, nested);
				nested.commit();
			}
			helper.assertTrue(inventory.getStackInSlot(0).isEmpty(), "Nested recipe transaction did not run");
		}
		helper.assertTrue(inventory.getStackInSlot(0).is(Items.NETHER_WART),
			"Aborted recipe transaction consumed its ingredient");
		helper.succeed();
	}

	@GameTest(template = "water_filling_bottle")
	public static void processingOutputCodecs(CreateGameTestHelper helper) {
		ItemStack expected = new ItemStack(Items.DIAMOND, 7);
		expected.set(DataComponents.CUSTOM_NAME, Component.literal("Recipe output"));
		ProcessingOutput output = new ProcessingOutput(expected, .5f);
		output.getStack().shrink(6);
		helper.assertTrue(ItemStack.matches(output.getStack(), expected), "Output stack mutation changed the recipe");
		var ops = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
		var encoded = ProcessingOutput.CODEC.encodeStart(ops, output).getOrThrow();
		helper.assertTrue(encoded.getAsJsonObject().has("id") && encoded.getAsJsonObject().has("components"),
			"Output encoder did not use the new component format");
		var decoded = ProcessingOutput.CODEC.parse(ops, encoded).getOrThrow();
		helper.assertTrue(ItemStack.matches(decoded.getStack(), expected) && decoded.getChance() == .5f,
			"JSON round-trip lost output data");
		var legacy = ProcessingOutput.CODEC.parse(ops,
			JsonParser.parseString("{\"item\":{\"id\":\"minecraft:diamond\"},\"count\":7,\"chance\":0.5}")).getOrThrow();
		helper.assertTrue(legacy.getStack().is(Items.DIAMOND) && legacy.getStack().getCount() == 7
			&& legacy.getChance() == .5f, "Legacy output JSON is no longer readable");
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		try {
			ProcessingOutput.STREAM_CODEC.encode(buffer, output);
			ProcessingOutput.STREAM_CODEC.encode(buffer, ProcessingOutput.EMPTY);
			var network = ProcessingOutput.STREAM_CODEC.decode(buffer);
			helper.assertTrue(ItemStack.matches(network.getStack(), expected) && network.getChance() == .5f,
				"Network round-trip lost output data");
			helper.assertTrue(ProcessingOutput.STREAM_CODEC.decode(buffer).getStack().isEmpty() && !buffer.isReadable(),
				"Empty output codec did not consume exactly its payload");
		} finally {
			buffer.release();
		}
		RandomSource expectedRandom = RandomSource.create(42);
		int count = 7;
		for (int roll = 0; roll < 7; roll++)
			if (expectedRandom.nextFloat() > .5f)
				count--;
		ItemStack rolled = output.rollOutput(RandomSource.create(42));
		helper.assertTrue(rolled.getCount() == count && ItemStack.isSameItemSameComponents(rolled, expected),
			"Output did not use the supplied random source or preserve components");
		RandomSource guaranteedRandom = RandomSource.create(42);
		new ProcessingOutput(expected, 1).rollOutput(guaranteedRandom);
		helper.assertTrue(guaranteedRandom.nextFloat() == RandomSource.create(42).nextFloat(),
			"Guaranteed output consumed random values");
		helper.succeed();
	}

	@GameTest(template = "water_filling_bottle")
	public static void processingRecipeCodecs(CreateGameTestHelper helper) {
		var ops = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
		RecipeSerializer<MixingRecipe> mixingSerializer = AllRecipeTypes.MIXING.getSerializer();
		RecipeSerializer<PressingRecipe> pressingSerializer = AllRecipeTypes.PRESSING.getSerializer();
		RecipeSerializer<ManualApplicationRecipe> applicationSerializer = AllRecipeTypes.ITEM_APPLICATION.getSerializer();
		var standardJson = JsonParser.parseString("""
			{"ingredients":[{"item":"minecraft:iron_ingot"},{"type":"fluid_stack","fluid":"minecraft:water","amount":2147483772}],
			"results":[{"id":"minecraft:gold_ingot","count":2},{"id":"minecraft:water","amount":250}],
			"processing_time":20,"heat_requirement":"heated"}
			""");
		MixingRecipe standard = mixingSerializer.codec().codec().parse(ops, standardJson).getOrThrow();
		helper.assertTrue(standard.getIngredients().size() == 1 && standard.getFluidIngredients().size() == 1
			&& standard.getRollableResults().getFirst().getStack().getCount() == 2
			&& standard.getFluidResults().getFirst().getAmount() == 250,
			"Mixed processing recipe JSON lost item/fluid parameters");
		var encodedStandard = mixingSerializer.codec().codec().encodeStart(ops, standard).getOrThrow();
		helper.assertTrue(!encodedStandard.getAsJsonObject().has("id"), "Recipe params unexpectedly encoded an id");
		MixingRecipe standardJsonRoundTrip = mixingSerializer.codec().codec().parse(ops, encodedStandard).getOrThrow();
		helper.assertTrue(standardJsonRoundTrip.getIngredients().size() == 1
			&& standardJsonRoundTrip.getFluidIngredients().getFirst().getRequiredAmount() == 2_147_483_772L
			&& standardJsonRoundTrip.getRollableResults().getFirst().getStack().getCount() == 2
			&& standardJsonRoundTrip.getFluidResults().getFirst().getAmount() == 250
			&& standardJsonRoundTrip.getProcessingDuration() == 20
			&& standardJsonRoundTrip.getRequiredHeat() == HeatCondition.HEATED,
			"Mixed processing recipe JSON round-trip lost parameters");

		var invalid = JsonParser.parseString("""
			{"ingredients":[{"item":"minecraft:iron_ingot"},{"item":"minecraft:gold_ingot"}],
			"results":[{"id":"minecraft:diamond"}],"processing_time":1,"heat_requirement":"heated"}
			""");
		var invalidResult = pressingSerializer.codec().codec().parse(ops, invalid);
		helper.assertTrue(invalidResult.error().isPresent()
			&& invalidResult.error().get().message().contains("item inputs")
			&& invalidResult.error().get().message().contains("duration")
			&& invalidResult.error().get().message().contains("heat"),
			"Invalid processing params were accepted");

		Function<Boolean, com.google.gson.JsonElement> applicationJson = keep -> JsonParser.parseString("{\"ingredients\":[{\"item\":\"minecraft:iron_ingot\"},{\"item\":\"minecraft:stick\"}],\"results\":[{\"id\":\"minecraft:iron_block\"}],\"keep_held_item\":" + keep + "}");
		ManualApplicationRecipe applicationFalse = applicationSerializer.codec().codec()
			.parse(ops, applicationJson.apply(false)).getOrThrow();
		ManualApplicationRecipe applicationTrue = applicationSerializer.codec().codec()
			.parse(ops, applicationJson.apply(true)).getOrThrow();
		helper.assertTrue(!applicationFalse.shouldKeepHeldItem() && applicationTrue.shouldKeepHeldItem(),
			"Item application keep_held_item JSON was not preserved");

		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		try {
			mixingSerializer.streamCodec().encode(buffer, standard);
			applicationSerializer.streamCodec().encode(buffer, applicationFalse);
			applicationSerializer.streamCodec().encode(buffer, applicationTrue);
			MixingRecipe networkStandard = mixingSerializer.streamCodec().decode(buffer);
			ManualApplicationRecipe networkFalse = applicationSerializer.streamCodec().decode(buffer);
			ManualApplicationRecipe networkTrue = applicationSerializer.streamCodec().decode(buffer);
			helper.assertTrue(networkStandard.getFluidResults().getFirst().getAmount() == 250
				&& networkStandard.getIngredients().size() == 1
				&& networkStandard.getFluidIngredients().getFirst().getRequiredAmount() == 2_147_483_772L
				&& networkStandard.getRollableResults().getFirst().getStack().is(Items.GOLD_INGOT)
				&& networkStandard.getRollableResults().getFirst().getStack().getCount() == 2
				&& networkStandard.getProcessingDuration() == 20
				&& networkStandard.getRequiredHeat() == HeatCondition.HEATED
				&& !networkFalse.shouldKeepHeldItem() && networkTrue.shouldKeepHeldItem() && !buffer.isReadable(),
				"Processing recipe network codecs did not round-trip or consume exactly");
		} finally {
			buffer.release();
		}

		var pressingJson = JsonParser.parseString("{\"type\":\"create:pressing\",\"ingredients\":[{\"item\":\"minecraft:iron_ingot\"}],\"results\":[{\"id\":\"minecraft:iron_block\"}]}");
		var deployerJson = JsonParser.parseString("{\"type\":\"create:deploying\",\"ingredients\":[{\"item\":\"minecraft:iron_ingot\"},{\"item\":\"minecraft:stick\"}],\"results\":[{\"id\":\"minecraft:iron_block\"}]}");
		SequencedRecipe<?> pressingSequence = SequencedRecipe.CODEC.parse(ops, pressingJson).getOrThrow();
		SequencedRecipe<?> deployerSequence = SequencedRecipe.CODEC.parse(ops, deployerJson).getOrThrow();
		helper.assertTrue(pressingSequence.getRecipe() instanceof PressingRecipe
			&& deployerSequence.getRecipe() instanceof DeployerApplicationRecipe,
			"Sequenced recipe codec rejected an assembly recipe");
		var nonAssemblyJson = JsonParser.parseString("{\"type\":\"create:mixing\",\"ingredients\":[{\"item\":\"minecraft:iron_ingot\"}],\"results\":[{\"id\":\"minecraft:iron_block\"}]}");
		helper.assertTrue(SequencedRecipe.CODEC.parse(ops, nonAssemblyJson).error().isPresent(),
			"Sequenced recipe codec accepted a non-assembly recipe");

		buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
		try {
			SequencedRecipe.STREAM_CODEC.encode(buffer, pressingSequence);
			SequencedRecipe.STREAM_CODEC.encode(buffer, deployerSequence);
			SequencedRecipe<?> networkPressing = SequencedRecipe.STREAM_CODEC.decode(buffer);
			SequencedRecipe<?> networkDeployer = SequencedRecipe.STREAM_CODEC.decode(buffer);
			helper.assertTrue(networkPressing.getRecipe() instanceof PressingRecipe
				&& networkDeployer.getRecipe() instanceof DeployerApplicationRecipe && !buffer.isReadable(),
				"Sequenced recipe network codec did not round-trip exactly");
			Recipe.STREAM_CODEC.encode(buffer, standard);
			try {
				SequencedRecipe.STREAM_CODEC.decode(buffer);
				helper.fail("Sequenced recipe network codec accepted a non-assembly recipe");
			} catch (io.netty.handler.codec.DecoderException expected) {}
		} finally {
			buffer.release();
		}
		helper.succeed();
	}

	@GameTest(template = "brass_mixing", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void brassMixing(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(2, 3, 2);
		BlockPos chest = new BlockPos(7, 3, 1);
		helper.pullLever(lever);
		helper.succeedWhen(() -> helper.assertContainerContains(chest, AllItems.BRASS_INGOT.get()));
	}

	@GameTest(template = "brass_mixing_2", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
	public static void brassMixing2(CreateGameTestHelper helper) {
		BlockPos basinLever = new BlockPos(3, 3, 1);
		BlockPos armLever = new BlockPos(3, 3, 5);
		BlockPos output = new BlockPos(1, 2, 3);
		helper.pullLever(armLever);
		helper.whenSecondsPassed(7, () -> helper.pullLever(armLever));
		helper.whenSecondsPassed(10, () -> helper.pullLever(basinLever));
		helper.succeedWhen(() -> helper.assertContainerContains(output, AllItems.BRASS_INGOT.get()));
	}

	@GameTest(template = "crushing_wheel_crafting", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void crushingWheelCrafting(CreateGameTestHelper helper) {
		BlockPos chest = new BlockPos(1, 4, 3);
		List<BlockPos> levers = List.of(
				new BlockPos(2, 3, 2),
				new BlockPos(6, 3, 2),
				new BlockPos(3, 7, 3)
		);
		levers.forEach(helper::pullLever);
		ItemStack expected = new ItemStack(AllBlocks.CRUSHING_WHEEL.get(), 2);
		helper.succeedWhen(() -> helper.assertContainerContains(chest, expected));
	}

	@GameTest(template = "precision_mechanism_crafting", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
	public static void precisionMechanismCrafting(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(6, 3, 6);
		BlockPos output = new BlockPos(11, 3, 1);
		helper.pullLever(lever);

		SequencedAssemblyRecipe recipe = (SequencedAssemblyRecipe) helper.getLevel().getRecipeManager()
				.byKey(Create.asResource("sequenced_assembly/precision_mechanism"))
				.orElseThrow(() -> new GameTestAssertException("Precision Mechanism recipe not found")).value();
		Item result = recipe.getResultItem(helper.getLevel().registryAccess()).getItem();
		Item[] possibleResults = recipe.resultPool.stream()
				.map(ProcessingOutput::getStack)
				.map(ItemStack::getItem)
				.filter(item -> item != result)
				.toArray(Item[]::new);

		helper.succeedWhen(() -> {
			helper.assertContainerContains(output, result);
			helper.assertAnyContained(output, possibleResults);
		});
	}

	@GameTest(template = "sand_washing", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void sandWashing(CreateGameTestHelper helper) {
		BlockPos leverPos = new BlockPos(5, 3, 1);
		helper.pullLever(leverPos);
		BlockPos chestPos = new BlockPos(8, 3, 2);
		helper.succeedWhen(() -> helper.assertContainerContains(chestPos, Items.CLAY_BALL));
	}

	@GameTest(template = "stone_cobble_sand_crushing", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void stoneCobbleSandCrushing(CreateGameTestHelper helper) {
		BlockPos chest = new BlockPos(1, 6, 2);
		BlockPos lever = new BlockPos(2, 3, 1);
		helper.pullLever(lever);
		ItemStack expected = new ItemStack(Items.SAND, 5);
		helper.succeedWhen(() -> helper.assertContainerContains(chest, expected));
	}

	@GameTest(template = "track_crafting", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void trackCrafting(CreateGameTestHelper helper) {
		BlockPos output = new BlockPos(7, 3, 2);
		BlockPos lever = new BlockPos(2, 3, 1);
		helper.pullLever(lever);
		ItemStack expected = new ItemStack(AllBlocks.TRACK.get(), 6);
		helper.succeedWhen(() -> {
			helper.assertContainerContains(output, expected);
			Storage<ItemVariant> storage = helper.itemStorageAt(output);
			ItemHelper.extract(storage, ItemHelper.sameItemPredicate(expected), 6, false);
			helper.assertContainerEmpty(output);
		});
	}

	@GameTest(template = "water_filling_bottle")
	public static void waterFillingBottle(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(3, 3, 3);
		BlockPos output = new BlockPos(2, 2, 4);
		ItemStack expected = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		helper.pullLever(lever);
		helper.succeedWhen(() -> helper.assertContainerContains(output, expected));
	}

	@GameTest(template = "wheat_milling")
	public static void wheatMilling(CreateGameTestHelper helper) {
		BlockPos output = new BlockPos(1, 2, 1);
		BlockPos lever = new BlockPos(1, 7, 1);
		helper.pullLever(lever);
		ItemStack expected = new ItemStack(AllItems.WHEAT_FLOUR.get(), 3);
		helper.succeedWhen(() -> helper.assertContainerContains(output, expected));
	}
}
