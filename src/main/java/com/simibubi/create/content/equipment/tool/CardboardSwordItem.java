package com.simibubi.create.content.equipment.tool;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.api.item.v1.FabricItem;

import io.github.fabricators_of_create.porting_lib.enchant.CustomEnchantingBehaviorItem;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingAttackEvent;

public class CardboardSwordItem extends SwordItem implements CustomEnchantingBehaviorItem, FabricItem {


	public CardboardSwordItem(Properties pProperties) {
		super(AllToolMaterials.CARDBOARD, pProperties);
	}

	@Override
	public boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
		return enchantment.is(Enchantments.KNOCKBACK);
	}

	public static InteractionResult cardboardSwordsMakeNoiseOnClick(Player player, Level level, InteractionHand hand, BlockPos pos, Direction direction) {
		if (player.isSpectator())
			return InteractionResult.PASS;

		ItemStack itemStack = player.getItemInHand(hand);
		if (!AllItems.CARDBOARD_SWORD.isIn(itemStack))
			return InteractionResult.PASS;
		if (!level.getBlockState(pos).isAir())
			return InteractionResult.PASS;

		if (level.isClientSide)
			AllSoundEvents.CARDBOARD_SWORD.playAt(level, pos, 0.5f, 1.85f, false);
		else
			AllSoundEvents.CARDBOARD_SWORD.play(level, player, pos, 0.5f, 1.85f);

		return InteractionResult.PASS;
	}

	public static void cardboardSwordsCannotHurtYou(LivingAttackEvent event) {
		Entity attacker = event.getSource()
			.getEntity();
		LivingEntity target = event.getEntity();
		if (target == null || target.getType().is(EntityTypeTags.ARTHROPOD))
			return;
		if (!(attacker instanceof LivingEntity livingAttacker
			&& AllItems.CARDBOARD_SWORD.isIn(livingAttacker.getItemInHand(InteractionHand.MAIN_HAND))))
			return;

		AllSoundEvents.CARDBOARD_SWORD.playFrom(attacker, 0.75f, 1.85f);

		event.setCanceled(true);

		// Reference player.attack()
		// This section replicates knockback behaviour without hurting the target

		float knockbackStrength = (float) (livingAttacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK) + 2);
		if (livingAttacker instanceof Player attackingPlayer && livingAttacker.level() instanceof ServerLevel serverLevel)
			knockbackStrength = EnchantmentHelper.modifyKnockback(serverLevel, livingAttacker.getWeaponItem(), target,
				serverLevel.damageSources().playerAttack(attackingPlayer), knockbackStrength);
		if (livingAttacker instanceof Player attackingPlayer && attackingPlayer.isSprinting()
			&& attackingPlayer.getAttackStrengthScale(0.5f) > 0.9f)
			++knockbackStrength;

		if (knockbackStrength <= 0)
			return;

		float yRot = livingAttacker.getYRot();
		knockback(target, knockbackStrength, yRot);

		boolean targetIsPlayer = target instanceof Player;
		MobCategory targetType = target.getType().getCategory();

		if (target instanceof ServerPlayer sp)
			CatnipServices.NETWORK.sendToClient(sp, new KnockbackPacket(yRot, (float) knockbackStrength));

		if ((targetType == MobCategory.MISC || targetType == MobCategory.CREATURE) && !targetIsPlayer)
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9, true, false, false));

		livingAttacker.setDeltaMovement(livingAttacker.getDeltaMovement()
			.multiply(0.6D, 1.0D, 0.6D));
		livingAttacker.setSprinting(false);
	}

	public static void knockback(LivingEntity target, double knockbackStrength, float yRot) {
		target.stopRiding();
		target.knockback(knockbackStrength * 0.5F, Mth.sin(yRot * Mth.DEG_TO_RAD), -Mth.cos(yRot * Mth.DEG_TO_RAD));
	}
}
