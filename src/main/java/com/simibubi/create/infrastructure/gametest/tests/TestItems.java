package com.simibubi.create.infrastructure.gametest.tests;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.ItemHandlerBeltSegment;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.filter.FilterMenu;
import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity.SelectionMode;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayLayout;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import com.simibubi.create.content.trains.station.GlobalStation.GlobalPackagePort;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

@GameTestGroup(path = "items")
public class TestItems {
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
