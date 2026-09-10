package com.simibubi.create.infrastructure.gametest.tests;

import java.util.UUID;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.infrastructure.command.AllCommands;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.datamaps.BlazeBurnerFuel;
import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.api.registry.CreateDataMaps;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardEditPacket;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.content.equipment.blueprint.BlueprintItem;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItemComponent;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import com.simibubi.create.content.equipment.toolbox.ToolboxMenu;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.ItemHandlerBeltSegment;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainPackageInteractionPacket;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionHelper;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.filter.FilterMenu;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute.ItemAttributeEntry;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.InTagAttribute;
import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity.SelectionMode;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.content.redstone.thresholdSwitch.ConfigureThresholdSwitchPacket;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayLayout;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import com.simibubi.create.content.trains.station.GlobalStation.GlobalPackagePort;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.CreateNBTProcessors;
import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import io.netty.buffer.Unpooled;
import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import net.createmod.catnip.math.BlockFace;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

@GameTestGroup(path = "items")
public class TestItems {
	@GameTest(template = "threshold_switch")
	public static void filterItemApi(CreateGameTestHelper helper) {
		ItemStack listFilter = AllItems.FILTER.asStack();
		ItemStack diamond = new ItemStack(Items.DIAMOND, 2);
		diamond.set(DataComponents.CUSTOM_NAME, Component.literal("Named diamond"));
		listFilter.set(AllDataComponents.FILTER_ITEMS, ItemContainerContents.fromItems(List.of(diamond)));
		listFilter.set(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, true);
		var handler = AllItems.FILTER.get().getFilterItemHandler(listFilter);
		ItemStack[] displayed = AllItems.FILTER.get().getFilterItems(listFilter);
		helper.assertTrue(handler.getSlotCount() == 18 && displayed.length == 1
			&& ItemStack.matches(displayed[0], diamond), "List filter API lost slots, counts or components");
		var listWrapper = FilterItemStack.of(listFilter.copy());
		helper.assertTrue(listWrapper instanceof FilterItemStack.ListFilterItemStack
			&& listWrapper.test(helper.getLevel(), diamond)
			&& !listWrapper.test(helper.getLevel(), Items.DIAMOND.getDefaultInstance()),
			"List filter wrapper did not preserve component-sensitive matching");
		listFilter.set(AllDataComponents.FILTER_ITEMS_BLACKLIST, true);
		helper.assertTrue(AllItems.FILTER.get().getFilterItems(listFilter).length == 0
			&& !FilterItemStack.of(listFilter.copy()).test(helper.getLevel(), diamond),
			"Blacklist displayed or accepted an excluded item");
		listFilter.set(AllDataComponents.FILTER_ITEMS, ItemContainerContents.EMPTY);
		helper.assertTrue(FilterItemStack.of(listFilter.copy()).test(helper.getLevel(), diamond),
			"Empty blacklist did not accept items");
		listFilter.set(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);
		helper.assertTrue(!FilterItemStack.of(listFilter.copy()).test(helper.getLevel(), diamond),
			"Empty allow-list accepted an item");

		ItemStack attributeFilter = AllItems.ATTRIBUTE_FILTER.asStack();
		attributeFilter.set(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);
		attributeFilter.set(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES,
			List.of(new ItemAttributeEntry(new InTagAttribute(ItemTags.PLANKS), false)));
		ItemStack[] matchingTag = AllItems.ATTRIBUTE_FILTER.get().getFilterItems(attributeFilter);
		helper.assertTrue(matchingTag.length > 0 && Stream.of(matchingTag).allMatch(stack -> stack.is(ItemTags.PLANKS))
			&& FilterItemStack.of(attributeFilter.copy()) instanceof FilterItemStack.AttributeFilterItemStack,
			"Attribute filter API did not expose its tag entries or wrapper");
		attributeFilter.set(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.BLACKLIST);
		helper.assertTrue(AllItems.ATTRIBUTE_FILTER.get().getFilterItems(attributeFilter).length == 0,
			"Attribute blacklist exposed tag entries as accepted items");

		ItemStack packageFilter = AllItems.PACKAGE_FILTER.asStack();
		packageFilter.set(AllDataComponents.PACKAGE_ADDRESS, "Station*");
		ItemStack box = PackageStyles.getDefaultBox();
		box.set(AllDataComponents.PACKAGE_ADDRESS, "Station 1");
		var packageWrapper = FilterItemStack.of(packageFilter.copy());
		helper.assertTrue(packageWrapper instanceof FilterItemStack.PackageFilterItemStack
			&& packageWrapper.test(helper.getLevel(), box)
			&& AllItems.PACKAGE_FILTER.get().getFilterItems(packageFilter).length == 0,
			"Package filter API lost its address matcher or exposed a fabricated item list");
		box.set(AllDataComponents.PACKAGE_ADDRESS, "Other");
		helper.assertTrue(!packageWrapper.test(helper.getLevel(), box), "Package filter accepted a different address");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void blueprintIngredientFilters(CreateGameTestHelper helper) {
		NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY,
			Ingredient.of(ItemTags.PLANKS),
			DefaultCustomIngredients.any(Ingredient.of(Items.DIAMOND), Ingredient.of(Items.GOLD_INGOT)));
		ShapelessRecipe recipe = new ShapelessRecipe("blueprint", CraftingBookCategory.MISC,
			new ItemStack(Items.STICK), ingredients);
		ItemStackHandler inventory = new ItemStackHandler(10);
		BlueprintItem.assignCompleteRecipe(helper.getLevel(), inventory, recipe);

		ItemStack tagFilter = inventory.getStackInSlot(0);
		FilterItemStack tagWrapper = FilterItemStack.of(tagFilter.copy());
		helper.assertTrue(tagFilter.is(AllItems.ATTRIBUTE_FILTER.get())
			&& tagWrapper instanceof FilterItemStack.AttributeFilterItemStack
			&& tagWrapper.test(helper.getLevel(), new ItemStack(Blocks.OAK_PLANKS.asItem()))
			&& !tagWrapper.test(helper.getLevel(), new ItemStack(Items.STONE)),
			"Blueprint tag ingredient was flattened instead of preserved");

		ItemStack customFilter = inventory.getStackInSlot(1);
		FilterItemStack customWrapper = FilterItemStack.of(customFilter.copy());
		helper.assertTrue(customFilter.is(AllItems.FILTER.get())
			&& customWrapper.test(helper.getLevel(), new ItemStack(Items.DIAMOND))
			&& customWrapper.test(helper.getLevel(), new ItemStack(Items.GOLD_INGOT))
			&& !customWrapper.test(helper.getLevel(), new ItemStack(Items.STONE)),
			"Blueprint custom ingredient did not preserve its matching stacks");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void blazeBurnerFuelDataMap(CreateGameTestHelper helper) {
		BlazeBurnerFuel fuel = CreateDataMaps.getSuperheated(AllItems.BLAZE_CAKE.get());
		helper.assertTrue(fuel != null && fuel.burnTime() == 3200,
			"Superheated Blaze Burner data map did not load blaze cake at 3200 ticks");
		helper.assertTrue(fuel != null && fuel.equals(CreateDataMaps.get(CreateDataMaps.SUPERHEATED_BLAZE_BURNER_FUELS,
			AllItems.BLAZE_CAKE.get())), "Data map lookup by id disagreed with the typed lookup");
		helper.assertTrue(CreateDataMaps.getRegular(AllItems.BLAZE_CAKE.get()) == null,
			"Blaze cake unexpectedly appeared in the regular fuel data map");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void clientCommandSuggestionsMerge(CreateGameTestHelper helper) throws CommandSyntaxException {
		var client = LiteralArgumentBuilder.<Boolean>literal("create")
			.then(LiteralArgumentBuilder.<Boolean>literal("overlay").executes(context -> 1))
			.then(LiteralArgumentBuilder.<Boolean>literal("rainbowDebug")
				.then(RequiredArgumentBuilder.<Boolean, Boolean>argument("status", BoolArgumentType.bool())
					.executes(context -> 1)));
		var suggestions = new CommandDispatcher<SharedSuggestionProvider>();
		for (String utilityAlias : List.of("util", "u"))
			client.then(LiteralArgumentBuilder.<Boolean>literal(utilityAlias)
				.then(LiteralArgumentBuilder.<Boolean>literal("camera")
					.then(RequiredArgumentBuilder.<Boolean, Float>argument("multiplier", FloatArgumentType.floatArg(1))
						.executes(context -> 1)))
				.then(LiteralArgumentBuilder.<Boolean>literal("angle").requires(allowed -> allowed)));
		for (String alias : List.of("create", "c")) {
			var existing = LiteralArgumentBuilder.<SharedSuggestionProvider>literal(alias)
				.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("trains"))
				.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("otherMod"));
			for (String utilityAlias : List.of("util", "u"))
				existing.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal(utilityAlias)
					.then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("replaceInCommandBlocks")));
			suggestions.register(existing);
		}
		var clientRoot = client.build();
		AllCommands.mergeClientCommandSuggestions(clientRoot, suggestions, false);
		var source = helper.getLevel().getServer().createCommandSourceStack();
		for (String alias : List.of("create", "c")) {
			var root = suggestions.getRoot().getChild(alias);
			helper.assertTrue(root.getChild("trains") != null && root.getChild("otherMod") != null,
				"Completion merge removed existing commands from /" + alias);
			helper.assertTrue(root.getChild("overlay") != null
				&& suggestions.execute(alias + " rainbowDebug true", source) == 0,
				"Client literal/boolean suggestions were lost or retained an execution callback");
			for (String utilityAlias : List.of("util", "u")) {
				var utility = root.getChild(utilityAlias);
				helper.assertTrue(utility.getChild("replaceInCommandBlocks") != null && utility.getChild("angle") == null,
					"Completion merge lost a server utility or exposed a forbidden command");
				String command = alias + " " + utilityAlias + " camera ";
				helper.assertTrue(suggestions.execute(command + "2", source) == 0
					&& !suggestions.parse(command + "0.5", source).getExceptions().isEmpty(),
					"Completion merge changed the camera argument bounds");
			}
		}
		helper.assertTrue(clientRoot.getChild("util").getChild("angle") != null,
			"Permission filtering modified the original client command tree");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void serverCommandSeparation(CreateGameTestHelper helper) {
		var dispatcher = helper.getLevel().getServer().getCommands().getDispatcher();
		for (String alias : List.of("create", "c")) {
			var root = dispatcher.getRoot().getChild(alias);
			helper.assertTrue(root != null, "Missing server command root /" + alias);
			helper.assertTrue(root.getChild("trains") != null && root.getChild("debuginfo") != null,
				"Server commands missing from /" + alias);
			for (String clientCommand : List.of("rainbowDebug", "dismissFabulousWarning", "overlay"))
				helper.assertTrue(root.getChild(clientCommand) == null,
					"Client command registered on server: /" + alias + " " + clientCommand);
			for (String utilityAlias : List.of("util", "u")) {
				var utility = root.getChild(utilityAlias);
				helper.assertTrue(utility != null && utility.getChild("replaceInCommandBlocks") != null,
					"Server utility missing from /" + alias + " " + utilityAlias);
				for (String clientCommand : List.of("clearRenderBuffers", "camera", "angle"))
					helper.assertTrue(utility.getChild(clientCommand) == null,
						"Client utility registered on server: /" + alias + " " + utilityAlias + " " + clientCommand);
			}
		}
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void configurationPacketRange(CreateGameTestHelper helper) {
		BlockPos switchLocal = new BlockPos(1, 2, 1);
		helper.setBlock(switchLocal, AllBlocks.THRESHOLD_SWITCH.getDefaultState());
		BlockPos switchPos = helper.absolutePos(switchLocal);
		ThresholdSwitchBlockEntity threshold = helper.getBlockEntity(AllBlockEntityTypes.THRESHOLD_SWITCH.get(), switchLocal);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
			new GameProfile(UUID.randomUUID(), "gametest"), ClientInformation.createDefault());
		player.moveTo(switchPos.getX() + .5, switchPos.getY() + .5, switchPos.getZ() + .5, 0, 0);
		new ConfigureThresholdSwitchPacket(switchPos, 3, 7, true, true).handle(player);
		helper.assertTrue(threshold.onWhenAbove == 7 && threshold.offWhenBelow == 3 && threshold.inStacks,
			"In-range configuration packet was not applied");
		player.moveTo(switchPos.getX() + 40.5, switchPos.getY() + .5, switchPos.getZ() + .5, 0, 0);
		new ConfigureThresholdSwitchPacket(switchPos, 99, 101, false, false).handle(player);
		helper.assertTrue(threshold.onWhenAbove == 7 && threshold.offWhenBelow == 3 && threshold.inStacks,
			"Out-of-range configuration packet changed the block entity");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void chainPackagePacketValidation(CreateGameTestHelper helper) {
		BlockPos conveyorLocal = new BlockPos(5, 2, 1);
		helper.setBlock(conveyorLocal, AllBlocks.CHAIN_CONVEYOR.getDefaultState());
		BlockPos conveyorPos = helper.absolutePos(conveyorLocal);
		ChainConveyorBlockEntity conveyor = helper.getBlockEntity(AllBlockEntityTypes.CHAIN_CONVEYOR.get(), conveyorLocal);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
			new GameProfile(UUID.randomUUID(), "gametest"), ClientInformation.createDefault());
		player.moveTo(conveyorPos.getX() + .5, conveyorPos.getY() + .5, conveyorPos.getZ() + .5, 0, 0);
		player.setItemInHand(InteractionHand.MAIN_HAND, Items.STONE.getDefaultInstance());
		new ChainPackageInteractionPacket(conveyorPos, null, 0, false).handle(player);
		helper.assertTrue(conveyor.getLoopingPackages().isEmpty() && player.getMainHandItem().is(Items.STONE),
			"Chain conveyor accepted a non-package item");
		player.setItemInHand(InteractionHand.MAIN_HAND, PackageStyles.getDefaultBox());
		new ChainPackageInteractionPacket(conveyorPos, null, 0, false).handle(player);
		helper.assertTrue(conveyor.getLoopingPackages().size() == 1 && player.getMainHandItem().isEmpty(),
			"Chain conveyor did not consume and queue a package item");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void chuteClearContent(CreateGameTestHelper helper) {
		BlockPos chuteLocal = new BlockPos(5, 2, 1);
		helper.setBlock(chuteLocal, AllBlocks.CHUTE.getDefaultState());
		ChuteBlockEntity chute = helper.getBlockEntity(AllBlockEntityTypes.CHUTE.get(), chuteLocal);
		chute.setItem(new ItemStack(Items.DIAMOND, 3));
		helper.assertTrue(chute.getItem().getCount() == 3, "Chute did not retain inserted items");
		chute.clearContent();
		helper.assertTrue(chute.getItem().isEmpty(), "Chute clearContent left items behind");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void smartChuteClearContent(CreateGameTestHelper helper) {
		BlockPos chuteLocal = new BlockPos(5, 2, 1);
		helper.setBlock(chuteLocal, AllBlocks.SMART_CHUTE.getDefaultState());
		ChuteBlockEntity chute = helper.getBlockEntity(AllBlockEntityTypes.SMART_CHUTE.get(), chuteLocal);
		FilteringBehaviour filtering = chute.getBehaviour(FilteringBehaviour.TYPE);
		filtering.setFilter(new ItemStack(Items.DIAMOND));
		chute.setItem(new ItemStack(Items.DIAMOND, 2));
		helper.assertTrue(!filtering.getFilter().isEmpty() && chute.getItem().getCount() == 2,
			"Smart chute did not retain its filter and item");
		chute.clearContent();
		helper.assertTrue(filtering.getFilter().isEmpty() && chute.getItem().isEmpty(),
			"Smart chute clearContent left filter or item behind");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void superGlueInventoryValidation(CreateGameTestHelper helper) {
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
			new GameProfile(UUID.randomUUID(), "gametest"), ClientInformation.createDefault());
		ItemStack unbreakableStone = new ItemStack(Items.STONE);
		unbreakableStone.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
		player.getInventory().items.set(0, unbreakableStone);
		helper.assertTrue(!SuperGlueSelectionHelper.collectGlueFromInventory(player, 1, true),
			"Non-glue unbreakable item satisfied a glue requirement");
		ItemStack unbreakableGlue = AllItems.SUPER_GLUE.get().getDefaultInstance();
		unbreakableGlue.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
		player.getInventory().items.set(0, unbreakableGlue);
		helper.assertTrue(SuperGlueSelectionHelper.collectGlueFromInventory(player, 1, true),
			"Unbreakable super glue did not satisfy a glue requirement");
		helper.succeed();
	}

	@GameTest(template = "depot_comparator_output")
	public static void inventoryIdentifierApi(CreateGameTestHelper helper) {
		BlockPos first = helper.absolutePos(new BlockPos(2, 1, 1));
		BlockPos second = helper.absolutePos(new BlockPos(3, 1, 1));
		BlockFace firstFace = new BlockFace(first, Direction.UP);
		BlockFace secondFace = new BlockFace(second, Direction.DOWN);
		InventoryIdentifier.Pair pair = new InventoryIdentifier.Pair(second, first);
		helper.assertTrue(pair.first().equals(first) && pair.second().equals(second)
			&& pair.contains(firstFace) && pair.contains(secondFace),
			"Inventory identifier pair did not canonicalize positions");
		InventoryIdentifier.Bounds bounds = new InventoryIdentifier.Bounds(BoundingBox.fromCorners(first, second));
		helper.assertTrue(bounds.contains(firstFace) && bounds.contains(secondFace),
			"Inventory identifier bounds did not contain both chest blocks");
		InventoryIdentifier.MultiFace multiFace = new InventoryIdentifier.MultiFace(first,
			Set.of(Direction.UP));
		helper.assertTrue(multiFace.contains(firstFace) && !multiFace.contains(secondFace),
			"Inventory identifier face filtering was incorrect");

		BlockPos chestPos = new BlockPos(1, 8, 1);
		BlockState leftChest = Blocks.CHEST.defaultBlockState()
			.setValue(ChestBlock.FACING, Direction.NORTH)
			.setValue(ChestBlock.TYPE, ChestType.LEFT);
		Direction toOther = ChestBlock.getConnectedDirection(leftChest);
		BlockPos otherChestPos = chestPos.relative(toOther);
		helper.setBlock(chestPos, leftChest);
		helper.setBlock(otherChestPos, leftChest.setValue(ChestBlock.TYPE, ChestType.RIGHT));
		InventoryIdentifier chestIdentifier = InventoryIdentifier.get(helper.getLevel(),
			new BlockFace(helper.absolutePos(chestPos), Direction.UP));
		helper.assertTrue(chestIdentifier != null
			&& chestIdentifier.contains(new BlockFace(helper.absolutePos(chestPos), Direction.UP))
			&& chestIdentifier.contains(new BlockFace(helper.absolutePos(otherChestPos), Direction.UP)),
			"Placed double chest was not identified as one inventory");

		BlockPos composterPos = new BlockPos(5, 8, 1);
		helper.setBlock(composterPos, Blocks.COMPOSTER.defaultBlockState());
		BlockPos absoluteComposterPos = helper.absolutePos(composterPos);
		InventoryIdentifier composterIdentifier = InventoryIdentifier.get(helper.getLevel(),
			new BlockFace(absoluteComposterPos, Direction.UP));
		helper.assertTrue(composterIdentifier != null
			&& composterIdentifier.contains(new BlockFace(absoluteComposterPos, Direction.UP))
			&& !composterIdentifier.contains(new BlockFace(absoluteComposterPos, Direction.NORTH)),
			"WorldlyContainer faces were not identified by their slot access");
		helper.succeed();
	}

	@GameTest(template = "depot_comparator_output")
	public static void packageOrderWithCraftsCodecs(CreateGameTestHelper helper) {
		var registries = helper.getLevel().registryAccess();
		var ops = registries.createSerializationContext(NbtOps.INSTANCE);
		BigItemStack ordered = new BigItemStack(Items.DIAMOND.getDefaultInstance(), 2);
		PackageOrderWithCrafts expected = new PackageOrderWithCrafts(new PackageOrder(List.of(ordered)),
			List.of(new PackageOrderWithCrafts.CraftingEntry(new PackageOrder(List.of(ordered)), 1)));
		var encoded = PackageOrderWithCrafts.CODEC.encodeStart(ops, expected).getOrThrow();
		var decoded = PackageOrderWithCrafts.CODEC.parse(ops, encoded).getOrThrow();
		helper.assertTrue(decoded.orderedStacks().stacks().size() == 1 && decoded.orderedCrafts().size() == 1
			&& decoded.orderedStacksMatchOrderedRecipes() && PackageOrderWithCrafts.hasCraftingInformation(decoded)
			&& !PackageOrderWithCrafts.hasCraftingInformation(null), "Package order codecs lost crafting context");
		var legacy = PackageOrder.CODEC.encodeStart(ops, new PackageOrder(List.of(ordered))).getOrThrow();
		var legacyDecoded = PackageOrderWithCrafts.CODEC.parse(ops, legacy).getOrThrow();
		var rewritten = PackageOrderWithCrafts.CODEC.encodeStart(ops, legacyDecoded).getOrThrow();
		var rewrittenDecoded = PackageOrderWithCrafts.CODEC.parse(ops, rewritten).getOrThrow();
		helper.assertTrue(legacyDecoded.orderedCrafts().isEmpty() && rewrittenDecoded.equals(legacyDecoded),
			"Legacy package order lost stacks while being rewritten");
		List<BigItemStack> duplicated = BigItemStack.duplicateWrappers(List.of(ordered));
		helper.assertTrue(duplicated.size() == 1 && duplicated.get(0).equals(ordered) && duplicated.get(0) != ordered,
			"Big item stack wrapper duplication was not independent");

		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
		try {
			PackageOrderWithCrafts.STREAM_CODEC.encode(buffer, expected);
			PackageOrderWithCrafts network = PackageOrderWithCrafts.STREAM_CODEC.decode(buffer);
			helper.assertTrue(network.equals(expected) && !buffer.isReadable(), "Package order network codec left unread data");
		} finally {
			buffer.release();
		}
		helper.succeed();
	}

	@GameTest(template = "depot_comparator_output")
	public static void clipboardContentCodecs(CreateGameTestHelper helper) {
		var registries = helper.getLevel().registryAccess();
		var ops = registries.createSerializationContext(NbtOps.INSTANCE);
		ItemStack icon = AllItems.BRASS_INGOT.asStack();
		icon.set(DataComponents.CUSTOM_NAME, Component.literal("Clipboard icon"));
		CompoundTag copiedValues = new CompoundTag();
		copiedValues.putInt("Value", 42);
		ClipboardContent content = ClipboardContent.EMPTY.setType(ClipboardType.WRITTEN)
			.setPages(List.of(List.of(new ClipboardEntry(true, Component.literal("First")).displayItem(icon, 99)),
				List.of(new ClipboardEntry(false, Component.literal("Second")))))
			.setReadOnly(true).setPreviouslyOpenedPage(1).setCopiedValues(copiedValues);
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
		try {
			for (ClipboardContent value : List.of(content, ClipboardContent.EMPTY)) {
				ItemStack clipboard = AllBlocks.CLIPBOARD.asStack();
				clipboard.set(AllDataComponents.CLIPBOARD_CONTENT, value);
				var encoded = ClipboardContent.CODEC.encodeStart(ops, value).getOrThrow();
				ItemStack restored = ItemStack.parse(registries, clipboard.save(registries)).orElseThrow();
				helper.assertTrue(encoded.equals(ClipboardContent.CODEC.encodeStart(ops,
					restored.get(AllDataComponents.CLIPBOARD_CONTENT)).getOrThrow()),
					"Clipboard persistence changed pages, item amounts, icons or settings");
				ItemStack.STREAM_CODEC.encode(buffer, clipboard);
				ClipboardContent network = ItemStack.STREAM_CODEC.decode(buffer).get(AllDataComponents.CLIPBOARD_CONTENT);
				helper.assertTrue(encoded.equals(ClipboardContent.CODEC.encodeStart(ops, network).getOrThrow())
					&& !buffer.isReadable(), "Clipboard network codec changed content or left unread bytes");
			}
		} finally {
			buffer.release();
		}
		helper.assertTrue(ClipboardContent.EMPTY.type() == ClipboardType.EMPTY && ClipboardContent.EMPTY.pages().isEmpty()
			&& !ClipboardContent.EMPTY.readOnly() && ClipboardContent.EMPTY.previouslyOpenedPage() == 0
			&& ClipboardContent.EMPTY.copiedValues().isEmpty(), "Clipboard setters mutated the empty default");
		helper.succeed();
	}

	@GameTest(template = "depot_comparator_output")
	public static void clipboardBlockEntityComponents(CreateGameTestHelper helper) {
		var registries = helper.getLevel().registryAccess();
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.CLIPBOARD.getDefaultState());
		ClipboardBlockEntity clipboard = helper.getBlockEntity(AllBlockEntityTypes.CLIPBOARD.get(), pos);
		ClipboardContent content = ClipboardContent.EMPTY.setPages(List.of(List.of(new ClipboardEntry(false, Component.literal("placed")))));
		CompoundTag legacyItem = new CompoundTag();
		legacyItem.putString("id", "create:clipboard");
		legacyItem.putInt("count", 1);
		CompoundTag legacyComponents = new CompoundTag();
		legacyComponents.put("create:clipboard_pages", ClipboardContent.PAGES_CODEC.encodeStart(
			registries.createSerializationContext(NbtOps.INSTANCE), content.pages()).getOrThrow());
		legacyComponents.put("create:clipboard_type", ClipboardType.CODEC.encodeStart(
			registries.createSerializationContext(NbtOps.INSTANCE), ClipboardType.WRITTEN).getOrThrow());
		legacyItem.put("components", legacyComponents);
		CompoundTag legacyTag = new CompoundTag();
		legacyTag.put("Item", legacyItem);
		clipboard.loadWithComponents(legacyTag, registries);
		helper.assertTrue(clipboard.components().get(AllDataComponents.CLIPBOARD_CONTENT).pages().get(0).get(0).text
			.getString().equals("placed"),
			"Clipboard block entity did not load legacy item components");
		ItemStack item = AllBlocks.CLIPBOARD.asStack();
		item.set(AllDataComponents.CLIPBOARD_CONTENT, content);
		clipboard.applyComponentsFromItemStack(item);
		helper.assertTrue(clipboard.components().get(AllDataComponents.CLIPBOARD_CONTENT).pages().get(0).get(0).text
			.getString().equals("placed"), "Clipboard placement did not apply item components");
		CompoundTag saved = clipboard.saveWithoutMetadata(registries);
		helper.assertTrue(!saved.contains("Item") && saved.contains("components")
			&& saved.getCompound("components").contains("create:clipboard_content"),
			"Clipboard block entity did not save migrated components");
		helper.succeed();
	}

	@GameTest(template = "depot_comparator_output")
	public static void clipboardLegacyMigration(CreateGameTestHelper helper) {
		var registries = helper.getLevel().registryAccess();
		var ops = registries.createSerializationContext(NbtOps.INSTANCE);
		ClipboardContent expected = ClipboardContent.EMPTY.setType(ClipboardType.WRITTEN)
			.setPages(List.of(List.of(new ClipboardEntry(true, Component.literal("legacy")).displayItem(Items.APPLE.getDefaultInstance(), 7))))
			.setReadOnly(true).setPreviouslyOpenedPage(2);
		CompoundTag oldComponents = new CompoundTag();
		oldComponents.put("create:clipboard_pages", ClipboardContent.PAGES_CODEC.encodeStart(ops, expected.pages()).getOrThrow());
		oldComponents.put("create:clipboard_type", ClipboardType.CODEC.encodeStart(ops, expected.type()).getOrThrow());
		oldComponents.put("create:clipboard_read_only", net.minecraft.util.Unit.CODEC.encodeStart(ops, net.minecraft.util.Unit.INSTANCE).getOrThrow());
		oldComponents.put("create:clipboard_copied_values", expected.setCopiedValues(new CompoundTag()).copiedValues().orElseThrow());
		oldComponents.put("create:clipboard_previously_opened_page", net.minecraft.nbt.IntTag.valueOf(2));

		CompoundTag saved = new CompoundTag();
		saved.putString("id", "create:clipboard");
		saved.putInt("count", 1);
		saved.put("components", oldComponents.copy());
		ItemStack migrated = ItemStack.parse(registries, saved).orElseThrow();
		ClipboardContent actual = migrated.get(AllDataComponents.CLIPBOARD_CONTENT);
		helper.assertTrue(actual != null && actual.type() == expected.type() && actual.pages().size() == 1
			&& actual.pages().get(0).get(0).text.getString().equals("legacy")
			&& actual.pages().get(0).get(0).itemAmount == 7 && actual.readOnly()
			&& actual.previouslyOpenedPage() == 2 && !migrated.has(AllDataComponents.CLIPBOARD_PAGES)
			&& !migrated.has(AllDataComponents.CLIPBOARD_TYPE), "Legacy clipboard components were not migrated");

		ClipboardContent canonical = ClipboardContent.EMPTY.setType(ClipboardType.EDITING)
			.setPages(List.of(List.of(new ClipboardEntry(false, Component.literal("canonical")))));
		CompoundTag mixed = saved.copy();
		CompoundTag mixedComponents = mixed.getCompound("components");
		mixedComponents.put("create:clipboard_content", ClipboardContent.CODEC.encodeStart(ops, canonical).getOrThrow());
		mixed.put("components", mixedComponents);
		ItemStack mixedStack = ItemStack.parse(registries, mixed).orElseThrow();
		// NeoForge migrates legacy components over existing clipboard content.
		helper.assertTrue(mixedStack.save(registries).equals(migrated.save(registries)),
			"Mixed clipboard components did not follow upstream legacy migration precedence");

		saved.putString("id", "minecraft:apple");
		ItemStack untouched = ItemStack.parse(registries, saved).orElseThrow();
		helper.assertTrue(untouched.has(AllDataComponents.CLIPBOARD_PAGES)
			&& !untouched.has(AllDataComponents.CLIPBOARD_CONTENT), "Non-clipboard legacy components were migrated");
		helper.succeed();
	}

	@GameTest(template = "depot_comparator_output")
	public static void clipboardPacketAndNbtSanitizers(CreateGameTestHelper helper) {
		var registries = helper.getLevel().registryAccess();
		var ops = registries.createSerializationContext(NbtOps.INSTANCE);
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
		try {
			ClipboardContent plain = ClipboardContent.EMPTY.setPages(List.of(List.of(new ClipboardEntry(false, Component.literal("ok")))));
			ClipboardEditPacket packet = new ClipboardEditPacket(0, plain, null);
			ClipboardEditPacket.STREAM_CODEC.encode(buffer, packet);
			ClipboardEditPacket decoded = ClipboardEditPacket.STREAM_CODEC.decode(buffer);
			helper.assertTrue(decoded.targetedBlock() == null && decoded.clipboardContent().pages().equals(plain.pages()) && !buffer.isReadable(),
				"Clipboard packet nullable target did not round-trip");
			ClipboardContent unsafe = plain.setPages(List.of(List.of(new ClipboardEntry(false, Component.literal("bad").withStyle(style -> style
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/say bad")))))));
			helper.assertTrue(ClipboardEditPacket.clipboardProcessor(unsafe) == null,
				"Clipboard packet sanitizer accepted a click event");
			helper.assertTrue(ClipboardEditPacket.clipboardProcessor(plain) != null,
				"Clipboard packet sanitizer rejected plain text");

			CompoundTag nbt = new CompoundTag();
			CompoundTag item = new CompoundTag();
			item.putString("id", "create:clipboard");
			item.putInt("count", 1);
			item.put("components", ClipboardContent.CODEC.encodeStart(ops, plain).map(value -> {
				CompoundTag components = new CompoundTag();
				components.put("create:clipboard_content", value);
				return components;
			}).getOrThrow());
			nbt.put("Item", item);
			helper.assertTrue(CreateNBTProcessors.clipboardProcessor(nbt) != null, "Plain clipboard NBT was rejected");
		} finally {
			buffer.release();
		}
		helper.succeed();
	}

	@GameTest(template = "depot_comparator_output")
	public static void sandpaperComponent(CreateGameTestHelper helper) {
		var registries = helper.getLevel().registryAccess();
		ItemStack input = AllItems.ROSE_QUARTZ.asStack();
		input.set(DataComponents.CUSTOM_NAME, Component.literal("Polishing input"));
		ItemStack sandpaper = AllItems.SAND_PAPER.asStack();
		sandpaper.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(input));
		ItemStack restored = ItemStack.parse(registries, sandpaper.save(registries)).orElseThrow();
		helper.assertTrue(ItemStack.matches(input, restored.get(AllDataComponents.SAND_PAPER_POLISHING).item()),
			"Sandpaper persistence lost its polishing item or components");
		var saved = (net.minecraft.nbt.CompoundTag) sandpaper.save(registries);
		helper.assertTrue(saved.getCompound("components").getCompound("create:sand_paper_polishing").contains("item"),
			"Sandpaper did not write the 6.0.10 component format");
		saved.getCompound("components").put("create:sand_paper_polishing", input.save(registries));
		ItemStack migrated = ItemStack.parse(registries, saved).orElseThrow();
		helper.assertTrue(ItemStack.matches(input, migrated.get(AllDataComponents.SAND_PAPER_POLISHING).item()),
			"Loading a 6.0.2 sandpaper stack lost the polishing item");
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
		try {
			ItemStack.STREAM_CODEC.encode(buffer, restored);
			SandPaperItemComponent.STREAM_CODEC.encode(buffer, new SandPaperItemComponent(ItemStack.EMPTY));
			helper.assertTrue(ItemStack.matches(input,
				ItemStack.STREAM_CODEC.decode(buffer).get(AllDataComponents.SAND_PAPER_POLISHING).item()),
				"Sandpaper network codec lost polishing item components");
			helper.assertTrue(SandPaperItemComponent.STREAM_CODEC.decode(buffer).item().isEmpty() && !buffer.isReadable(),
				"Sandpaper optional item codec did not consume the buffer exactly");
		} finally {
			buffer.release();
		}
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		AllItems.SAND_PAPER.get().releaseUsing(restored, helper.getLevel(), player, 10);
		helper.assertTrue(!restored.has(AllDataComponents.SAND_PAPER_POLISHING)
			&& player.getInventory().items.stream().filter(stack -> ItemStack.isSameItemSameComponents(input, stack))
				.mapToInt(ItemStack::getCount).sum() == 1,
			"Cancelling polishing did not return exactly one original item");
		helper.succeed();
	}

	@GameTest(template = "andesite_tunnel_split")
	public static void andesiteTunnelSplit(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(2, 6, 2);
		helper.pullLever(lever);
		Map<BlockPos, ItemStack> outputs = Map.of(
				new BlockPos(2, 2, 1), new ItemStack(AllItems.BRASS_INGOT.get(), 1),
				new BlockPos(3, 2, 1), new ItemStack(AllItems.BRASS_INGOT.get(), 1),
				new BlockPos(4, 2, 2), new ItemStack(AllItems.BRASS_INGOT.get(), 3)
		);
		helper.succeedWhen(() -> outputs.forEach(helper::assertContainerContains));
	}

	@GameTest(template = "arm_multi_output", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void armMultiOutput(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(2, 3, 1);
		BlockPos[] blazeBurners = IntStream.rangeClosed(6, 8)
			.boxed()
			.flatMap(x -> IntStream.rangeClosed(1, 3)
				.mapToObj(z -> new BlockPos(x, 2, z)))
			.toArray(BlockPos[]::new);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			for (BlockPos pos : blazeBurners)
				helper.assertBlockState(
					pos,
					state -> state.getValue(BlazeBurnerBlock.HEAT_LEVEL) == HeatLevel.KINDLED,
					() -> "Blaze burner isn't lit!"
				);
		});
	}

	@GameTest(template = "arm_purgatory", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void armPurgatory(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(2, 3, 2);
		BlockPos depot1Pos = new BlockPos(3, 2, 1);
		DepotBlockEntity depot1 = helper.getBlockEntity(AllBlockEntityTypes.DEPOT.get(), depot1Pos);
		BlockPos depot2Pos = new BlockPos(1, 2, 1);
		DepotBlockEntity depot2 = helper.getBlockEntity(AllBlockEntityTypes.DEPOT.get(), depot2Pos);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(5);
			ItemStack held1 = depot1.getHeldItem();
			boolean held1Empty = held1.isEmpty();
			int held1Count = held1.getCount();
			ItemStack held2 = depot2.getHeldItem();
			boolean held2Empty = held2.isEmpty();
			int held2Count = held2.getCount();
			if (held1Empty && held2Empty)
				helper.fail("No item present");
			if (!held1Empty && held1Count != 1)
				helper.fail("Unexpected count on depot 1: " + held1Count);
			if (!held2Empty && held2Count != 1)
				helper.fail("Unexpected count on depot 2: " + held2Count);
		});
	}

	@GameTest(template = "attribute_filters", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void attributeFilters(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(2, 3, 1);
		BlockPos end = new BlockPos(11, 2, 2);
		Holder<Enchantment> PROTECTION_ENCHANT = helper.getLevel().registryAccess()
				.registryOrThrow(Registries.ENCHANTMENT)
				.getHolderOrThrow(Enchantments.PROTECTION);
		Map<BlockPos, ItemStack> outputs = Map.of(
				new BlockPos(3, 2, 1), new ItemStack(AllBlocks.BRASS_BLOCK.get()),
				new BlockPos(4, 2, 1), new ItemStack(Items.APPLE),
				new BlockPos(5, 2, 1), new ItemStack(Items.WATER_BUCKET),
				new BlockPos(6, 2, 1), EnchantedBookItem.createForEnchantment(
						new EnchantmentInstance(PROTECTION_ENCHANT, 1)
				),
				new BlockPos(7, 2, 1), Util.make(
						new ItemStack(Items.NETHERITE_SWORD),
						s -> s.setDamageValue(1)
				),
				new BlockPos(8, 2, 1), new ItemStack(Items.IRON_HELMET),
				new BlockPos(9, 2, 1), new ItemStack(Items.COAL),
				new BlockPos(10, 2, 1), new ItemStack(Items.POTATO)
		);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			outputs.forEach(helper::assertContainerContains);
			helper.assertContainerEmpty(end);
		});
	}

	@GameTest(template = "belt_coaster", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void beltCoaster(CreateGameTestHelper helper) {
		BlockPos input = new BlockPos(1, 5, 6);
		BlockPos output = new BlockPos(3, 8, 6);
		BlockPos lever = new BlockPos(1, 5, 5);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			long outputItems = helper.getTotalItems(output);
			if (outputItems != 27)
				helper.fail("Expected 27 items, got " + outputItems);
			long remainingItems = helper.getTotalItems(input);
			if (remainingItems != 2)
				helper.fail("Expected 2 items remaining, got " + remainingItems);
		});
	}

	@GameTest(template = "brass_tunnel_filtering")
	public static void brassTunnelFiltering(CreateGameTestHelper helper) {
		Map<BlockPos, ItemStack> outputs = Map.of(
				new BlockPos(3, 2, 2), new ItemStack(Items.COPPER_INGOT, 13),
				new BlockPos(4, 2, 3), new ItemStack(AllItems.ZINC_INGOT.get(), 4),
				new BlockPos(4, 2, 4), new ItemStack(Items.IRON_INGOT, 2),
				new BlockPos(4, 2, 5), new ItemStack(Items.GOLD_INGOT, 24),
				new BlockPos(3, 2, 6), new ItemStack(Items.DIAMOND, 17)
		);
		BlockPos lever = new BlockPos(2, 3, 2);
		helper.pullLever(lever);
		helper.succeedWhen(() -> outputs.forEach(helper::assertContainerContains));
	}

	@GameTest(template = "brass_tunnel_prefer_nearest", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void brassTunnelPreferNearest(CreateGameTestHelper helper) {
		List<BlockPos> tunnels = List.of(
				new BlockPos(3, 3, 1),
				new BlockPos(3, 3, 2),
				new BlockPos(3, 3, 3)
		);
		List<BlockPos> out = List.of(
				new BlockPos(5, 2, 1),
				new BlockPos(5, 2, 2),
				new BlockPos(5, 2, 3)
		);
		BlockPos lever = new BlockPos(2, 3, 2);
		helper.pullLever(lever);
		// tunnels reconnect and lose their modes
		tunnels.forEach(tunnel -> helper.setTunnelMode(tunnel, SelectionMode.PREFER_NEAREST));
		helper.succeedWhen(() ->
				out.forEach(pos ->
						helper.assertContainerContains(pos, AllBlocks.BRASS_CASING.get())
				)
		);
	}

	@GameTest(template = "brass_tunnel_round_robin", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void brassTunnelRoundRobin(CreateGameTestHelper helper) {
		List<BlockPos> outputs = List.of(
				new BlockPos(7, 3, 1),
				new BlockPos(7, 3, 2),
				new BlockPos(7, 3, 3)
		);
		brassTunnelModeTest(helper, SelectionMode.ROUND_ROBIN, outputs);
	}

	@GameTest(template = "brass_tunnel_split")
	public static void brassTunnelSplit(CreateGameTestHelper helper) {
		List<BlockPos> outputs = List.of(
				new BlockPos(7, 2, 1),
				new BlockPos(7, 2, 2),
				new BlockPos(7, 2, 3)
		);
		brassTunnelModeTest(helper, SelectionMode.SPLIT, outputs);
	}

	private static void brassTunnelModeTest(CreateGameTestHelper helper, SelectionMode mode, List<BlockPos> outputs) {
		BlockPos lever = new BlockPos(2, 3, 2);
		List<BlockPos> tunnels = List.of(
				new BlockPos(3, 3, 1),
				new BlockPos(3, 3, 2),
				new BlockPos(3, 3, 3)
		);
		helper.pullLever(lever);
		tunnels.forEach(tunnel -> helper.setTunnelMode(tunnel, mode));
		helper.succeedWhen(() -> {
			long items = 0;
			for (BlockPos out : outputs) {
				helper.assertContainerContains(out, AllBlocks.BRASS_CASING.get());
				items += helper.getTotalItems(out);
			}
			if (items != 10)
				helper.fail("expected 10 items, got " + items);
		});
	}

	@GameTest(template = "brass_tunnel_sync_input", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void brassTunnelSyncInput(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(1, 3, 2);
		List<BlockPos> redstoneBlocks = List.of(
				new BlockPos(3, 4, 1),
				new BlockPos(3, 4, 2),
				new BlockPos(3, 4, 3)
		);
		List<BlockPos> tunnels = List.of(
				new BlockPos(5, 3, 1),
				new BlockPos(5, 3, 2),
				new BlockPos(5, 3, 3)
		);
		List<BlockPos> outputs = List.of(
				new BlockPos(7, 2, 1),
				new BlockPos(7, 2, 2),
				new BlockPos(7, 2, 3)
		);
		helper.pullLever(lever);
		tunnels.forEach(tunnel -> helper.setTunnelMode(tunnel, SelectionMode.SYNCHRONIZE));
		helper.succeedWhen(() -> {
			if (helper.secondsPassed() < 9) {
				helper.setBlock(redstoneBlocks.get(0), Blocks.AIR);
				helper.assertSecondsPassed(3);
				outputs.forEach(helper::assertContainerEmpty);
				helper.setBlock(redstoneBlocks.get(1), Blocks.AIR);
				helper.assertSecondsPassed(6);
				outputs.forEach(helper::assertContainerEmpty);
				helper.setBlock(redstoneBlocks.get(2), Blocks.AIR);
				helper.assertSecondsPassed(9);
			} else {
				outputs.forEach(out -> helper.assertContainerContains(out, AllBlocks.BRASS_CASING.get()));
			}
		});
	}

	@GameTest(template = "smart_observer_belt_and_funnel", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void smartObserverBeltAndFunnel(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(6, 3, 2);
		List<BlockPos> targets = List.of(
				new BlockPos(5, 2, 1), // belt
				new BlockPos(2, 4, 6) // funnel
		);
		List<BlockPos> overflows = List.of(
				new BlockPos(6, 2, 1), // belt
				new BlockPos(1, 3, 6) // funnel
		);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(9);
			targets.forEach(pos -> helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, pos));
			overflows.forEach(pos -> helper.assertBlockPresent(Blocks.AIR, pos));
		});
	}

	@GameTest(template = "smart_observer_chutes")
	public static void smartObserverChutes(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(1, 5, 2);
		BlockPos output = new BlockPos(1, 5, 3);
		helper.pullLever(lever);
		helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, output));
	}

	@GameTest(template = "smart_observer_counting")
	public static void smartObserverCounting(CreateGameTestHelper helper) {
		BlockPos chest = new BlockPos(3, 2, 1);
		long totalChestItems = helper.getTotalItems(chest);
		BlockPos chestNixiePos = new BlockPos(2, 3, 1);
		NixieTubeBlockEntity chestNixie = helper.getBlockEntity(AllBlockEntityTypes.NIXIE_TUBE.get(), chestNixiePos);

		BlockPos doubleChest = new BlockPos(2, 2, 3);
		long totalDoubleChestItems = helper.getTotalItems(doubleChest);
		BlockPos doubleChestNixiePos = new BlockPos(1, 3, 3);
		NixieTubeBlockEntity doubleChestNixie = helper.getBlockEntity(AllBlockEntityTypes.NIXIE_TUBE.get(), doubleChestNixiePos);

		helper.succeedWhen(() -> {
			String chestNixieText = chestNixie.getFullText().getString();
			long chestNixieReading = Long.parseLong(chestNixieText);
			if (chestNixieReading != totalChestItems)
				helper.fail("Chest nixie detected %s, expected %s".formatted(chestNixieReading, totalChestItems));
			String doubleChestNixieText = doubleChestNixie.getFullText().getString();
			long doubleChestNixieReading = Long.parseLong(doubleChestNixieText);
			if (doubleChestNixieReading != totalDoubleChestItems)
				helper.fail("Double chest nixie detected %s, expected %s".formatted(doubleChestNixieReading, totalDoubleChestItems));
		});
	}

	@GameTest(template = "smart_observer_filtered_storage")
	public static void smartObserverFilteredStorage(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(2, 3, 1);
		BlockPos leftLamp = new BlockPos(3, 2, 3);
		BlockPos rightLamp = new BlockPos(1, 2, 3);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertBlockProperty(leftLamp, RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(rightLamp, RedstoneLampBlock.LIT, false);
		});
	}

	@GameTest(template = "smart_observer_storage")
	public static void smartObserverStorage(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(1, 3, 2);
		BlockPos lamp = new BlockPos(1, 2, 3);
		helper.pullLever(lever);
		helper.succeedWhen(() -> helper.assertBlockProperty(lamp, RedstoneLampBlock.LIT, true));
	}

	@GameTest(template = "depot_display", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void depotDisplay(CreateGameTestHelper helper) {
		BlockPos displayPos = new BlockPos(5, 3, 1);
		List<DepotBlockEntity> depots = Stream.of(
				new BlockPos(2, 2, 1),
				new BlockPos(1, 2, 1)
		).map(pos -> helper.getBlockEntity(AllBlockEntityTypes.DEPOT.get(), pos)).toList();
		List<BlockPos> levers = List.of(
				new BlockPos(2, 5, 0),
				new BlockPos(1, 5, 0)
		);
		levers.forEach(helper::pullLever);
		FlapDisplayBlockEntity display = helper.getBlockEntity(AllBlockEntityTypes.FLAP_DISPLAY.get(), displayPos).getController();
		helper.succeedWhen(() -> {
			for (int i = 0; i < 2; i++) {
				FlapDisplayLayout line = display.getLines().get(i);
                MutableComponent textComponent = Component.empty();
				line.getSections().stream().map(FlapDisplaySection::getText).forEach(textComponent::append);
				String text = textComponent.getString().toLowerCase(Locale.ROOT).trim();

				DepotBlockEntity depot = depots.get(i);
				ItemStack item = depot.getHeldItem();
				String name = BuiltInRegistries.ITEM.getKey(item.getItem()).getPath();

				if (!name.equals(text))
					helper.fail("Text mismatch: wanted [" + name + "], got: " + text);
			}
		});
	}

	@GameTest(template = "threshold_switch")
	public static void postboxOfflineBufferRoundTrip(CreateGameTestHelper helper) {
		GlobalPackagePort port = new GlobalPackagePort();
		ItemStackHandler inventory = new ItemStackHandler(18) {
			@Override
			protected void onContentsChanged(int slot) {
				port.saveOfflineBuffer(this);
			}
		};

		port.offlineBuffer.setStackInSlot(0, new ItemStack(Items.DIAMOND));
		port.offlineBuffer.setStackInSlot(1, new ItemStack(Items.GOLD_INGOT));
		port.primed = true;
		port.restoreOfflineBuffer(inventory);
		helper.assertTrue(inventory.getStackInSlot(0).is(Items.DIAMOND), "First buffered package was not restored");
		helper.assertTrue(inventory.getStackInSlot(1).is(Items.GOLD_INGOT), "Later buffered package was overwritten");
		helper.assertTrue(!port.primed, "Restored postbox buffer remained primed");

		inventory.setStackInSlot(2, new ItemStack(Items.IRON_INGOT));
		helper.assertTrue(port.offlineBuffer.getStackInSlot(2).is(Items.IRON_INGOT),
			"Postbox change did not update its offline buffer");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void packagePortShiftClick(CreateGameTestHelper helper) {
		BlockPos portPos = new BlockPos(1, 2, 1);
		helper.setBlock(portPos, AllBlocks.PACKAGE_FROGPORT.get());
		var port = helper.getBlockEntity(AllBlockEntityTypes.PACKAGE_FROGPORT.get(), portPos);
		port.inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 32));

		int[] changes = {0};
		port.inventory.whenContentsChanged(slot -> changes[0]++);
		var player = helper.makeMockPlayer(GameType.CREATIVE);
		player.getInventory().setItem(0, new ItemStack(Items.IRON_INGOT, 16));

		PackagePortMenu menu = PackagePortMenu.create(1, player.getInventory(), port);
		ItemStack moved = menu.quickMoveStack(player, port.inventory.getSlotCount() + 27);
		menu.removed(player);

		helper.assertTrue(moved.is(Items.IRON_INGOT) && moved.getCount() == 16,
			"Shift-click did not report the moved stack");
		helper.assertTrue(port.inventory.getStackInSlot(0).getCount() == 48,
			"Shift-click did not merge into the package port");
		helper.assertTrue(player.getInventory().getItem(0).isEmpty(),
			"Shift-click left items in the player inventory");
		helper.assertTrue(changes[0] > 0, "Shift-click bypassed package port inventory callbacks");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void heldFilterMenuSlots(CreateGameTestHelper helper) {
		var player = helper.makeMockPlayer(GameType.CREATIVE);
		var inventory = player.getInventory();
		inventory.selected = 3;
		ItemStack filter = AllItems.FILTER.asStack();
		inventory.setItem(3, filter);
		inventory.setItem(9, new ItemStack(Items.DIAMOND, 12));
		inventory.setItem(0, new ItemStack(Items.GOLD_INGOT, 7));
		FilterMenu menu = FilterMenu.create(1, inventory, filter);
		helper.assertTrue(menu.getSlot(0).getItem() == inventory.getItem(9)
			&& menu.getSlot(27).getItem() == inventory.getItem(0), "Player slots are not in vanilla order");
		menu.quickMoveStack(player, 0);
		menu.quickMoveStack(player, 27);
		helper.assertTrue(menu.ghostInventory.getStackInSlot(0).is(Items.DIAMOND)
			&& menu.ghostInventory.getStackInSlot(1).is(Items.GOLD_INGOT), "Shift-click copied the wrong player slots");
		helper.assertTrue(inventory.getItem(9).getCount() == 12 && inventory.getItem(0).getCount() == 7,
			"Ghost insertion consumed real items");
		menu.clicked(30, 0, ClickType.PICKUP, player);
		helper.assertTrue(menu.getCarried().isEmpty() && inventory.getSelected() == filter,
			"Menu allowed picking up its owner item");
		helper.assertTrue(!menu.canTakeItemForPickAll(filter, menu.getSlot(30))
			&& menu.canTakeItemForPickAll(filter, menu.getSlot(0)), "Pick-all owner protection targets the wrong slot");
		helper.assertTrue(menu.stillValid(player), "Held filter menu is not valid");
		inventory.setItem(3, ItemStack.EMPTY);
		helper.assertTrue(!menu.stillValid(player), "Menu stayed valid after removing its owner");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void transferStackComponents(CreateGameTestHelper helper) {
		for (int limit : new int[] { 8, 99 }) {
			ItemStack stack = new ItemStack(Items.DIAMOND);
			stack.set(DataComponents.MAX_STACK_SIZE, limit);
			ItemVariant variant = ItemVariant.of(stack);
			BlockPos pos = helper.absolutePos(BlockPos.ZERO);
			var belt = new BeltBlockEntity(AllBlockEntityTypes.BELT.get(), pos, AllBlocks.BELT.getDefaultState());
			belt.setLevel(helper.getLevel());
			belt.setController(pos);
			belt.beltLength = 1;
			var inventory = new BeltInventory(belt);
			var segment = new ItemHandlerBeltSegment(inventory, 0);
			var deployer = new DeployerBlockEntity(AllBlockEntityTypes.DEPLOYER.get(), pos, AllBlocks.DEPLOYER.getDefaultState());
			deployer.setLevel(helper.getLevel());
			var chute = new ChuteBlockEntity(AllBlockEntityTypes.CHUTE.get(), pos, AllBlocks.CHUTE.getDefaultState());
			chute.setLevel(helper.getLevel());
			for (Storage<ItemVariant> storage : List.of(segment, deployer.getItemStorage(null), chute.getItemStorage(null))) {
				try (Transaction transaction = Transaction.openOuter()) {
					helper.assertTrue(storage.insert(variant, Long.MAX_VALUE, transaction) == limit,
						"Transfer ignored custom stack limit " + limit + " in " + storage.getClass().getSimpleName());
				}
				inventory.tick();
				helper.assertTrue(storage.iterator().next().getAmount() == 0, "Aborted insertion left items behind");
				try (Transaction transaction = Transaction.openOuter()) {
					helper.assertTrue(storage.insert(variant, Long.MAX_VALUE, transaction) == limit, "Insertion failed after rollback");
					transaction.commit();
				}
				inventory.tick();
				var view = storage.iterator().next();
				helper.assertTrue(view.getAmount() == limit && view.getCapacity() == limit,
					"Committed amount or capacity differs from component limit");
			}
		}
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void toolboxLastSlotShiftClick(CreateGameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.TOOLBOXES.get(DyeColor.BROWN).getDefaultState());
		var toolbox = helper.getBlockEntity(AllBlockEntityTypes.TOOLBOX.get(), pos);
		var inventory = (ToolboxInventory) toolbox.getItemStorage(null);
		int lastSlot = inventory.getSlotCount() - 1;
		for (int slot = 0; slot < lastSlot; slot++)
			inventory.setStackInSlot(slot, new ItemStack(Items.STONE, 64));
		var player = helper.makeMockPlayer(GameType.CREATIVE);
		player.getInventory().setItem(0, new ItemStack(Items.STONE, 64));
		ToolboxMenu menu = ToolboxMenu.create(1, player.getInventory(), toolbox);
		menu.quickMoveStack(player, inventory.getSlotCount() + 27);
		menu.removed(player);
		helper.assertTrue(inventory.getStackInSlot(lastSlot).getCount() == 64,
			"Shift-click excluded the last toolbox slot");
		helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "Shift-click left items in the player inventory");
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void toolboxInventoryRoundTrip(CreateGameTestHelper helper) {
		ToolboxInventory inventory = new ToolboxInventory(null);
		for (int slot = 0; slot < 3; slot++)
			inventory.setStackInSlot(slot, new ItemStack(Items.STONE, 64));

		var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
		var encoded = ToolboxInventory.CODEC.encodeStart(ops, inventory).getOrThrow();
		ToolboxInventory decoded = ToolboxInventory.CODEC.parse(ops, encoded).getOrThrow();
		int decodedCount = IntStream.range(0, decoded.getSlotCount())
			.map(slot -> decoded.getStackInSlot(slot).getCount())
			.sum();
		helper.assertTrue(decodedCount == 192, "Toolbox round-trip kept " + decodedCount + " of 192 items");

		decoded.setStackInSlot(ToolboxInventory.STACKS_PER_COMPARTMENT, new ItemStack(Items.GOLD_INGOT));
		helper.assertTrue(decoded.getStackInSlot(ToolboxInventory.STACKS_PER_COMPARTMENT).is(Items.GOLD_INGOT),
			"Decoded toolbox filters are not mutable");

		for (int slot = 0; slot < ToolboxInventory.STACKS_PER_COMPARTMENT; slot++)
			decoded.setStackInSlot(slot, new ItemStack(Items.IRON_INGOT, 64));
		ItemHelper.copyContents(new ItemStackHandler(decoded.getSlotCount()), decoded);
		for (int slot = 0; slot < decoded.getSlotCount(); slot++)
			helper.assertTrue(decoded.getStackInSlot(slot).isEmpty(), "Copy left stale contents in slot " + slot);
		helper.succeed();
	}

	@GameTest(template = "threshold_switch")
	public static void thresholdSwitch(CreateGameTestHelper helper) {
		BlockPos chest = new BlockPos(1, 2, 1);
		BlockPos lamp = new BlockPos(2, 3, 1);
		helper.assertBlockProperty(lamp, RedstoneLampBlock.LIT, false);
		Storage<ItemVariant> chestStorage = helper.itemStorageAt(chest);
		ItemStack diamondStack = new ItemStack(Items.DIAMOND, 64);
		for (int i = 0; i < 18; i++) { // insert 18 stacks
			TransferUtil.insert(chestStorage, diamondStack);
		}
		helper.succeedWhen(() -> helper.assertBlockProperty(lamp, RedstoneLampBlock.LIT, true));
	}

	@GameTest(template = "storages", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void storages(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(12, 3, 2);
		BlockPos startChest = new BlockPos(13, 3, 1);
		Object2LongMap<Item> originalContent = helper.getItemContent(startChest);
		BlockPos endShulker = new BlockPos(1, 3, 1);
		helper.pullLever(lever);
		helper.succeedWhen(() -> helper.assertContentPresent(originalContent, endShulker));
	}

	@GameTest(template = "vault_comparator_output")
	public static void vaultComparatorOutput(CreateGameTestHelper helper) {
		BlockPos smallInput = new BlockPos(1, 4, 1);
		BlockPos smallNixie = new BlockPos(3, 2, 1);
		helper.assertNixiePower(smallNixie, 0);
		helper.whenSecondsPassed(1, () -> helper.spawnItems(smallInput, Items.BREAD, 64 * 9));

		BlockPos medInput = new BlockPos(1, 5, 4);
		BlockPos medNixie = new BlockPos(4, 2, 4);
		helper.assertNixiePower(medNixie, 0);
		helper.whenSecondsPassed(2, () -> helper.spawnItems(medInput, Items.BREAD, 64 * 77));

		BlockPos bigInput = new BlockPos(1, 6, 8);
		BlockPos bigNixie = new BlockPos(5, 2, 7);
		helper.assertNixiePower(bigNixie, 0);
		helper.whenSecondsPassed(3, () -> helper.spawnItems(bigInput, Items.BREAD, 64 * 240));

		helper.succeedWhen(() -> {
			helper.assertNixiePower(smallNixie, 7);
			helper.assertNixiePower(medNixie, 7);
			helper.assertNixiePower(bigNixie, 7);
		});
	}

	@GameTest(template = "depot_comparator_output")
	public static void depotComparatorOutput(CreateGameTestHelper helper) {
		BlockPos swordNixie = new BlockPos(7, 2, 1);
		BlockPos diamondNixie = new BlockPos(5, 2, 1);
		BlockPos fullPearlNixie = new BlockPos(3, 2, 1);
		BlockPos halfPearlNixie = new BlockPos(1, 2, 1);

		helper.succeedWhen(() -> {
			helper.assertNixiePower(swordNixie, 15);
			helper.assertNixiePower(diamondNixie, 15);
			helper.assertNixiePower(fullPearlNixie, 15);
			helper.assertNixiePower(halfPearlNixie, 8);
		});
	}

	@GameTest(template = "fan_processing", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void fanProcessing(CreateGameTestHelper helper) {
		// why does the redstone explode
		BlockPos.betweenClosed(new BlockPos(2, 7, 3), new BlockPos(11, 7, 3)).forEach(
			pos -> helper.setBlock(pos, Blocks.REDSTONE_WIRE)
		);
		helper.pullLever(1, 7, 3);
		List<BlockPos> lamps = List.of(
			new BlockPos(1, 2, 1), new BlockPos(5, 2, 1), new BlockPos(7, 2, 1),
			new BlockPos(9, 2, 1), new BlockPos(11, 2, 1)
		);
		helper.succeedWhen(() -> {
			for (BlockPos lamp : lamps) {
				helper.assertBlockProperty(lamp, RedstoneLampBlock.LIT, true);
			}
		});
	}
}
