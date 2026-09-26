package com.simibubi.create.infrastructure.gametest.tests;

import static com.simibubi.create.infrastructure.gametest.CreateGameTestHelper.FIFTEEN_SECONDS;

import java.util.ArrayList;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.simibubi.create.content.schematics.SchematicExport;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.simibubi.create.content.trains.entity.TrainRelocationPacket;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;

import io.github.fabricators_of_create.porting_lib.entity.events.tick.EntityTickEvent;

@GameTestGroup(path = "misc")
public class TestMisc {
	@GameTest(template = "smart_observer_blocks")
	public static void missingTrainRelocationPacket(CreateGameTestHelper helper) {
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
			new GameProfile(UUID.randomUUID(), "gametest"), ClientInformation.createDefault());
		new TrainRelocationPacket(UUID.randomUUID(), BlockPos.ZERO, Vec3.ZERO, -1, false, null).handle(player);
		helper.succeed();
	}

	@GameTest(template = "smart_observer_blocks")
	public static void schematicannonLegacyOptions(CreateGameTestHelper helper) {
		SchematicannonBlockEntity cannon = new SchematicannonBlockEntity(AllBlockEntityTypes.SCHEMATICANNON.get(),
			BlockPos.ZERO, AllBlocks.SCHEMATICANNON.getDefaultState());
		CompoundTag tag = new CompoundTag();
		tag.putString("State", State.STOPPED.name());
		cannon.loadWithComponents(tag, helper.getLevel().registryAccess());
		helper.assertTrue(cannon.replaceMode == 2 && !cannon.skipMissing && !cannon.replaceBlockEntities,
			"Schematicannon legacy defaults changed when Options was absent");
		helper.succeed();
	}

	@GameTest(template = "smart_observer_blocks")
	public static void factoryGaugeTimerClampsLegacyState(CreateGameTestHelper helper) {
		FactoryPanelBlockEntity panel = new FactoryPanelBlockEntity(AllBlockEntityTypes.FACTORY_PANEL.get(), BlockPos.ZERO,
			AllBlocks.FACTORY_GAUGE.getDefaultState());
		panel.addBehaviours(new ArrayList<>());
		FactoryPanelBehaviour behaviour = panel.panels.get(PanelSlot.TOP_LEFT);
		behaviour.active = true;
		behaviour.resetTimer();

		int configured = AllConfigs.server().logistics.factoryGaugeTimer.get();
		CompoundTag saved = new CompoundTag();
		behaviour.write(saved, helper.getLevel().registryAccess(), false);
		CompoundTag panelTag = saved.getCompound("top_left");
		panelTag.putInt("Timer", configured + 50);
		behaviour.read(saved, helper.getLevel().registryAccess(), false);

		CompoundTag roundTrip = new CompoundTag();
		behaviour.write(roundTrip, helper.getLevel().registryAccess(), false);
		helper.assertTrue(configured >= 5 && roundTrip.getCompound("top_left").getInt("Timer") == configured,
			"Factory gauge timer did not use the configured interval or clamp legacy state");
		helper.succeed();
	}

	@GameTest(template = "smart_observer_blocks")
	public static void tableClothRotatesInsideContraption(CreateGameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.ANDESITE_TABLE_CLOTH.getDefaultState());
		TableClothBlockEntity cloth = helper.getBlockEntity(AllBlockEntityTypes.TABLE_CLOTH.get(), pos);
		cloth.transform(cloth, new StructureTransform(BlockPos.ZERO, Direction.Axis.Y, Rotation.CLOCKWISE_90, Mirror.NONE));
		helper.assertTrue(cloth.facing == Direction.WEST, "Table cloth did not rotate its facing with the contraption");
		helper.succeed();
	}

	@GameTest(template = "smart_observer_blocks")
	public static void packagerReadsAllSignLines(CreateGameTestHelper helper) {
		BlockPos packagerPos = new BlockPos(1, 1, 1);
		BlockPos signPos = packagerPos.relative(Direction.NORTH);
		helper.setBlock(packagerPos, AllBlocks.PACKAGER.getDefaultState());
		helper.setBlock(signPos, Blocks.OAK_SIGN.defaultBlockState());
		SignBlockEntity sign = (SignBlockEntity) helper.getBlockEntity(signPos);
		sign.setText(new SignText()
			.setMessage(0, Component.literal("A"))
			.setMessage(1, Component.literal("B")), false);
		PackagerBlockEntity packager = helper.getBlockEntity(AllBlockEntityTypes.PACKAGER.get(), packagerPos);
		packager.activate();
		helper.assertTrue("A B".equals(packager.signBasedAddress), "Packager did not combine sign lines");
		helper.succeed();
	}

	@GameTest(template = "smart_observer_blocks")
	public static void virtualNixieText(CreateGameTestHelper helper) {
		var nixie = new NixieTubeBlockEntity(AllBlockEntityTypes.NIXIE_TUBE.get(), BlockPos.ZERO,
			AllBlocks.ORANGE_NIXIE_TUBE.getDefaultState());
		nixie.markVirtual();
		CompoundTag tag = new CompoundTag();
		tag.putInt("RedstoneStrength", 13);
		nixie.loadWithComponents(tag, helper.getLevel().registryAccess());
		helper.assertTrue(nixie.getDisplayedStrings().getFirst().equals("1")
			&& nixie.getDisplayedStrings().getSecond().equals("3"), "Virtual nixie did not refresh its digits");

		tag.putString("CustomText", "\"ABCD\"");
		tag.putString("RawCustomText", "\"ABCD\"");
		tag.putInt("CustomTextIndex", 1);
		nixie.loadWithComponents(tag, helper.getLevel().registryAccess());
		helper.assertTrue(nixie.getDisplayedStrings().getFirst().equals("C")
			&& nixie.getDisplayedStrings().getSecond().equals("D"), "Virtual nixie kept stale text");
		helper.succeed();
	}

	@GameTest(template = "schematicannon", timeoutTicks = FIFTEEN_SECONDS)
	public static void schematicannon(CreateGameTestHelper helper) {
		// load the structure
		BlockPos whiteEndBottom = helper.absolutePos(new BlockPos(5, 2, 1));
		BlockPos redEndTop = helper.absolutePos(new BlockPos(5, 4, 7));
		ServerLevel level = helper.getLevel();
		SchematicExport.saveSchematic(
			SchematicExport.SCHEMATICS.resolve("uploaded/Deployer"), "schematicannon_gametest", true,
			level, whiteEndBottom, redEndTop
		);
		ItemStack schematic =
			SchematicItem.create(level, "schematicannon_gametest.nbt", "Deployer");
		// deploy to pos
		BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
		schematic.set(AllDataComponents.SCHEMATIC_DEPLOYED, true);
		schematic.set(AllDataComponents.SCHEMATIC_ANCHOR, anchor);
		// setup cannon
		BlockPos cannonPos = new BlockPos(3, 2, 6);
		SchematicannonBlockEntity cannon = helper.getBlockEntity(AllBlockEntityTypes.SCHEMATICANNON.get(), cannonPos);
		cannon.inventory.setStackInSlot(0, schematic);
		// run
		cannon.state = State.RUNNING;
		cannon.statusMsg = "running";
		helper.succeedWhen(() -> {
			if (cannon.state != State.STOPPED) {
				helper.fail("Schematicannon not done");
			}
			BlockPos lastBlock = new BlockPos(1, 4, 7);
			helper.assertBlockPresent(Blocks.RED_WOOL, lastBlock);
		});
	}

	@GameTest(template = "shearing")
	public static void shearing(CreateGameTestHelper helper) {
		BlockPos sheepPos = new BlockPos(2, 1, 2);
		Sheep sheep = helper.getFirstEntity(EntityType.SHEEP, sheepPos);
		sheep.shear(SoundSource.NEUTRAL);
		helper.succeedWhen(() -> {
			helper.assertItemEntityPresent(Items.WHITE_WOOL, sheepPos, 2);
		});
	}

	@GameTest(template = "smart_observer_blocks")
	public static void smartObserverBlocks(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(2, 2, 1);
		BlockPos leftLamp = new BlockPos(3, 4, 3);
		BlockPos rightLamp = new BlockPos(1, 4, 3);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertBlockProperty(leftLamp, RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(rightLamp, RedstoneLampBlock.LIT, false);
		});
	}

	@GameTest(template = "smart_observer_blocks")
	public static void backtanksAreNotDamageable(CreateGameTestHelper helper) {
		ArmorStand armorStand = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(1, 1, 1));
		for (ItemStack backtank : new ItemStack[] {
			new ItemStack(AllItems.COPPER_BACKTANK.get()), new ItemStack(AllItems.NETHERITE_BACKTANK.get())
		}) {
			backtank.set(AllDataComponents.BACKTANK_AIR, 42);
			helper.assertTrue(!backtank.isDamageableItem(), "Backtank must not be damageable");
			backtank.hurtAndBreak(1, armorStand, EquipmentSlot.CHEST);
			helper.assertTrue(backtank.getCount() == 1 && backtank.getOrDefault(AllDataComponents.BACKTANK_AIR, 0) == 42,
				"Backtank or its air was lost when applying damage");
		}
		helper.assertTrue(new ItemStack(AllItems.COPPER_DIVING_HELMET.get()).isDamageableItem(),
			"Diving helmet must remain damageable");
		helper.assertTrue(new ItemStack(AllItems.COPPER_DIVING_BOOTS.get()).isDamageableItem(),
			"Diving boots must remain damageable");
		helper.succeed();
	}

	@GameTest(template = "smart_observer_blocks")
	public static void divingHelmetRespectsWaterBreathing(CreateGameTestHelper helper) {
		helper.onEachTick(() -> {
			if (helper.getLevel().getGameTime() % 20 != 0)
				return;
			BlockPos pos = new BlockPos(1, 1, 1);
			for (int y = 0; y < 3; y++)
				helper.setBlock(pos.above(y), Blocks.WATER);
			var entity = helper.makeMockPlayer(GameType.SURVIVAL);
			BlockPos absolute = helper.absolutePos(pos);
			entity.moveTo(absolute.getX() + .5, absolute.getY(), absolute.getZ() + .5);
			entity.baseTick();
			helper.assertTrue(entity.isEyeInFluid(AllFluidTags.DIVING_FLUIDS.tag), "Test player must be underwater");
			ItemStack tank = AllItems.COPPER_BACKTANK.asStack();
			entity.setItemSlot(EquipmentSlot.HEAD, AllItems.COPPER_DIVING_HELMET.asStack());
			entity.setItemSlot(EquipmentSlot.CHEST, tank);
			tank.set(AllDataComponents.BACKTANK_AIR, 42);
			entity.setAirSupply(20);
			DivingHelmetItem.breatheUnderwater(new EntityTickEvent.Pre(entity));
			helper.assertTrue(entity.getAirSupply() == entity.getMaxAirSupply()
				&& tank.getOrDefault(AllDataComponents.BACKTANK_AIR, 0) == 41
				&& !entity.hasEffect(MobEffects.WATER_BREATHING), "Helmet must refill air without a synthetic potion");
			for (var effect : java.util.List.of(MobEffects.WATER_BREATHING, MobEffects.CONDUIT_POWER)) {
				tank.set(AllDataComponents.BACKTANK_AIR, 42);
				entity.setAirSupply(20);
				entity.addEffect(new MobEffectInstance(effect, 100));
				DivingHelmetItem.breatheUnderwater(new EntityTickEvent.Pre(entity));
				helper.assertTrue(entity.getAirSupply() == 20
					&& tank.getOrDefault(AllDataComponents.BACKTANK_AIR, 0) == 42
					&& entity.getEffect(effect).getDuration() == 100, "Natural breathing must preserve the tank and effect");
				entity.removeEffect(effect);
			}
			for (int y = 0; y < 3; y++)
				helper.setBlock(pos.above(y), Blocks.LAVA);
			entity.baseTick();
			helper.assertTrue(entity.isInLava(), "Test player must be in lava");
			entity.setItemSlot(EquipmentSlot.HEAD, AllItems.NETHERITE_DIVING_HELMET.asStack());
			ItemStack netheriteTank = AllItems.NETHERITE_BACKTANK.asStack();
			netheriteTank.set(AllDataComponents.BACKTANK_AIR, 42);
			entity.setItemSlot(EquipmentSlot.CHEST, netheriteTank);
			entity.setAirSupply(20);
			entity.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 100));
			DivingHelmetItem.breatheUnderwater(new EntityTickEvent.Pre(entity));
			helper.assertTrue(netheriteTank.getOrDefault(AllDataComponents.BACKTANK_AIR, 0) == 41
				&& entity.getAirSupply() == 20, "Water breathing must not bypass the tank's lava protection cost");
			helper.succeed();
		});
	}

	@GameTest(template = "threshold_switch_pulley")
	public static void thresholdSwitchPulley(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(3, 7, 1);
		BlockPos switchPos = new BlockPos(1, 6, 1);
		BlockPos finalPos = new BlockPos(2, 2, 1);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			ThresholdSwitchBlockEntity switchBe = helper.getBlockEntity(AllBlockEntityTypes.THRESHOLD_SWITCH.get(), switchPos);
			int level = switchBe.getStockLevel();
			int expectedLevel = helper.absolutePos(finalPos).getY();
			if (level != expectedLevel)
				helper.fail("Unexpected level: " + level);
		});
	}

	@GameTest(template = "netherite_backtank", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void netheriteBacktank(CreateGameTestHelper helper) {
		BlockPos lava = new BlockPos(2, 2, 3);
		BlockPos zombieSpawn = lava.above(2);
		BlockPos armorStandPos = new BlockPos(2, 2, 1);
		helper.runAtTickTime(5, () -> {
			Zombie zombie = helper.spawn(EntityType.ZOMBIE, zombieSpawn);
			ArmorStand armorStand = helper.getFirstEntity(EntityType.ARMOR_STAND, armorStandPos);
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				zombie.setItemSlot(slot, armorStand.getItemBySlot(slot).copy());
			}
		});
		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(9);
			helper.assertEntityPresent(EntityType.ZOMBIE, lava);
		});
	}
}
