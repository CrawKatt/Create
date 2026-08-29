package com.simibubi.create.content.equipment.extendoGrip;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingHurtEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingKnockBackEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.AttackEntityEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.player.PlayerInteractEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.tick.EntityTickEvent;

public class ExtendoGripItem extends Item {
	public static final int MAX_DAMAGE = 200;

	public static final AttributeModifier singleRangeAttributeModifier =
		new AttributeModifier(Create.asResource("single_range_attribute_modifier"), 3,
			AttributeModifier.Operation.ADD_VALUE);
	public static final AttributeModifier doubleRangeAttributeModifier =
		new AttributeModifier(Create.asResource("double_range_attribute_modifier"), 5,
			AttributeModifier.Operation.ADD_VALUE);

	private static final Supplier<Multimap<Holder<Attribute>, AttributeModifier>> rangeModifier = Suppliers.memoize(() ->
		// Holding an ExtendoGrip
		ImmutableMultimap.of(Attributes.BLOCK_INTERACTION_RANGE, singleRangeAttributeModifier));
	private static final Supplier<Multimap<Holder<Attribute>, AttributeModifier>> doubleRangeModifier = Suppliers.memoize(() ->
		// Holding two ExtendoGrips o.O
		ImmutableMultimap.of(Attributes.BLOCK_INTERACTION_RANGE, doubleRangeAttributeModifier));

	private static DamageSource lastActiveDamageSource;

	public ExtendoGripItem(Properties properties) {
		super(properties.durability(MAX_DAMAGE));
	}

	public static final String EXTENDO_MARKER = "createExtendo";
	public static final String DUAL_EXTENDO_MARKER = "createDualExtendo";

	public static void holdingExtendoGripIncreasesRange(EntityTickEvent.Pre event) {
		if (!(event.getEntity() instanceof Player player))
			return;

		CompoundTag persistentData = player.getCustomData();
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.getOffhandItem());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.getMainHandItem());
		boolean holdingDualExtendo = inOff && inMain;
		boolean holdingExtendo = inOff ^ inMain;
		holdingExtendo &= !holdingDualExtendo;
		boolean wasHoldingExtendo = persistentData.contains(EXTENDO_MARKER);
		boolean wasHoldingDualExtendo = persistentData.contains(DUAL_EXTENDO_MARKER);

		if (holdingExtendo != wasHoldingExtendo) {
			if (!holdingExtendo) {
				player.getAttributes()
					.removeAttributeModifiers(rangeModifier.get());
				persistentData.remove(EXTENDO_MARKER);
			} else {
				AllAdvancements.EXTENDO_GRIP.awardTo(player);
				player.getAttributes()
					.addTransientAttributeModifiers(rangeModifier.get());
				persistentData.putBoolean(EXTENDO_MARKER, true);
			}
		}

		if (holdingDualExtendo != wasHoldingDualExtendo) {
			if (!holdingDualExtendo) {
				player.getAttributes()
					.removeAttributeModifiers(doubleRangeModifier.get());
				persistentData.remove(DUAL_EXTENDO_MARKER);
			} else {
				AllAdvancements.EXTENDO_GRIP_DUAL.awardTo(player);
				player.getAttributes()
					.addTransientAttributeModifiers(doubleRangeModifier.get());
				persistentData.putBoolean(DUAL_EXTENDO_MARKER, true);
			}
		}

	}

	public static void addReachToJoiningPlayersHoldingExtendo(Entity entity, @Nullable CompoundTag persistentData) {
		if (!(entity instanceof Player player) || persistentData == null)
			return;

		if (persistentData.contains(DUAL_EXTENDO_MARKER))
			player.getAttributes()
				.addTransientAttributeModifiers(doubleRangeModifier.get());
		else if (persistentData.contains(EXTENDO_MARKER))
			player.getAttributes()
				.addTransientAttributeModifiers(rangeModifier.get());
	}

	public static void consumeDurabilityOnBlockBreak(Level level, Player player, BlockPos blockPos,
		BlockState blockState, @Nullable BlockEntity blockEntity) {
		findAndDamageExtendoGrip(player);
	}

	public static void consumeDurabilityOnPlace(BlockPlaceContext context, BlockPos blockPos,
		BlockState blockState) {
		findAndDamageExtendoGrip(context.getPlayer());
	}

	private static void findAndDamageExtendoGrip(Player player) {
		if (player == null)
			return;
		if (player.level().isClientSide)
			return;
		EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
		ItemStack extendo = player.getMainHandItem();
		if (!AllItems.EXTENDO_GRIP.isIn(extendo)) {
			extendo = player.getOffhandItem();
			equipmentSlot = EquipmentSlot.OFFHAND;
		}
		if (!AllItems.EXTENDO_GRIP.isIn(extendo))
			return;
		if (!BacktankUtil.canAbsorbDamage(player, maxUses()))
			extendo.hurtAndBreak(1, player, equipmentSlot);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return BacktankUtil.isBarVisible(stack, maxUses());
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return BacktankUtil.getBarWidth(stack, maxUses());
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return BacktankUtil.getBarColor(stack, maxUses());
	}

	private static int maxUses() {
		return AllConfigs.server().equipment.maxExtendoGripActions.get();
	}

	public static void bufferLivingAttackEvent(LivingHurtEvent event) {
		// Workaround for removed patch to get the attacking entity.
		lastActiveDamageSource = event.getSource();

		DamageSource source = event.getSource();
		if (source == null)
			return;
		Entity trueSource = source.getEntity();
		if (trueSource instanceof Player)
			findAndDamageExtendoGrip((Player) trueSource);
	}

	public static void attacksByExtendoGripHaveMoreKnockback(LivingKnockBackEvent event) {
		if (lastActiveDamageSource == null)
			return;
		Entity entity = lastActiveDamageSource.getDirectEntity();
		lastActiveDamageSource = null;
		if (!(entity instanceof Player player))
			return;
		if (!isHoldingExtendoGrip(player))
			return;
		event.setStrength(event.getStrength() + 2);
	}

	private static boolean isUncaughtClientInteraction(Entity entity, Entity target) {
		// Server ignores entity interaction further than 6m
		if (entity.distanceToSqr(target) < 36)
			return false;
		if (!entity.level().isClientSide)
			return false;
		if (!(entity instanceof Player))
			return false;
		return true;
	}

	public static void notifyServerOfLongRangeAttacks(AttackEntityEvent event) {
		Entity entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		Player player = (Player) entity;
		if (isHoldingExtendoGrip(player))
			CatnipServices.NETWORK.sendToServer(new ExtendoGripInteractionPacket(target));
	}

	public static void notifyServerOfLongRangeInteractions(PlayerInteractEvent.EntityInteract event) {
		Entity entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		Player player = (Player) entity;
		if (isHoldingExtendoGrip(player))
			CatnipServices.NETWORK.sendToServer(new ExtendoGripInteractionPacket(target, event.getHand()));
	}

	public static void notifyServerOfLongRangeSpecificInteractions(PlayerInteractEvent.EntityInteractSpecific event) {
		Player entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		if (isHoldingExtendoGrip(entity))
			CatnipServices.NETWORK.sendToServer(new ExtendoGripInteractionPacket(target, event.getHand(), event.getLocalPos()));
	}

	public static boolean isHoldingExtendoGrip(Player player) {
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.getOffhandItem());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.getMainHandItem());
		boolean holdingGrip = inOff || inMain;
		return holdingGrip;
	}

}
