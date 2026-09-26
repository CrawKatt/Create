package com.simibubi.create.content.equipment.armor;

import java.util.List;
import java.util.function.BiConsumer;

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.foundation.advancement.AllAdvancements;

import io.github.fabricators_of_create.porting_lib.entity.events.tick.EntityTickEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;

public class DivingHelmetItem extends BaseArmorItem {
	public static final EquipmentSlot SLOT = EquipmentSlot.HEAD;
	public static final ArmorItem.Type TYPE = ArmorItem.Type.HELMET;

	public DivingHelmetItem(Holder<ArmorMaterial> material, Properties properties, ResourceLocation textureLoc) {
		super(material, TYPE, properties, textureLoc);
	}

	public static void addEnchantmentModifiers(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
		BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
		if (slot != SLOT || !(stack.getItem() instanceof DivingHelmetItem))
			return;
		RegistryLookup<Enchantment> enchantments = entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		Holder<Enchantment> aquaAffinity = enchantments.getOrThrow(Enchantments.AQUA_AFFINITY);
		if (stack.getEnchantments().getLevel(aquaAffinity) > 0 || !aquaAffinity.value().matchingSlot(slot))
			return;
		for (var effect : aquaAffinity.value().getEffects(EnchantmentEffectComponents.ATTRIBUTES))
			consumer.accept(effect.attribute(), effect.getModifier(1, slot));
	}

	@Override
	public boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
		return !enchantment.is(Enchantments.AQUA_AFFINITY)
			&& super.canBeEnchantedWith(stack, enchantment, context);
	}

	public static boolean isWornBy(Entity entity) {
		return !getWornItem(entity).isEmpty();
	}

	public static ItemStack getWornItem(Entity entity) {
		if (!(entity instanceof LivingEntity livingEntity)) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = livingEntity.getItemBySlot(SLOT);
		if (!(stack.getItem() instanceof DivingHelmetItem)) {
			return ItemStack.EMPTY;
		}
		return stack;
	}

	public static void breatheUnderwater(EntityTickEvent.Pre event) {
		if (!(event.getEntity() instanceof LivingEntity entity))
			return;
		Level world = entity.level();
		boolean second = world.getGameTime() % 20 == 0;

		if (world.isClientSide)
			entity.getCustomData()
				.remove("VisualBacktankAir");

		ItemStack helmet = getWornItem(entity);
		if (helmet.isEmpty())
			return;

		boolean lavaDiving = entity.isInLava();
		if (!helmet.has(DataComponents.FIRE_RESISTANT) && lavaDiving)
			return;
		if (!entity.isEyeInFluid(AllFluidTags.DIVING_FLUIDS.tag) && !lavaDiving)
			return;
		if (!lavaDiving && (entity.canBreatheUnderwater() || MobEffectUtil.hasWaterBreathing(entity)
			|| entity instanceof Player player && player.getAbilities().invulnerable
			|| world.getBlockState(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()))
				.is(Blocks.BUBBLE_COLUMN)))
			return;
		if (entity instanceof Player && ((Player) entity).isCreative())
			return;

		List<ItemStack> backtanks = BacktankUtil.getAllWithAir(entity);
		if (backtanks.isEmpty())
			return;

		if (lavaDiving) {
			if (entity instanceof ServerPlayer sp)
				AllAdvancements.DIVING_SUIT_LAVA.awardTo(sp);
			if (backtanks.stream()
				.noneMatch(backtank -> backtank.has(DataComponents.FIRE_RESISTANT)))
				return;
		}

		if (!lavaDiving)
			entity.setAirSupply(entity.getMaxAirSupply());

		if (world.isClientSide)
			entity.getCustomData()
				.putInt("VisualBacktankAir", Math.round(backtanks.stream()
					.map(BacktankUtil::getAir)
					.reduce(0, Integer::sum)));

		if (!second)
			return;

		BacktankUtil.consumeAir(entity, backtanks.get(0), 1);

		if (lavaDiving)
			return;

		if (entity instanceof ServerPlayer sp)
			AllAdvancements.DIVING_SUIT.awardTo(sp);
	}
}
