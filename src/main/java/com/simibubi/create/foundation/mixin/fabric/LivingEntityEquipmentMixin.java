package com.simibubi.create.foundation.mixin.fabric;

import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.events.EquipmentAttributeModifierCallback;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEquipmentMixin {
	@WrapOperation(
		method = "collectEquipmentChanges",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"),
		require = 2
	)
	private void create$addEquipmentModifiers(ItemStack stack, EquipmentSlot slot,
		BiConsumer<Holder<Attribute>, AttributeModifier> consumer, Operation<Void> original) {
		original.call(stack, slot, consumer);
		EquipmentAttributeModifierCallback.EVENT.invoker().addModifiers((LivingEntity) (Object) this, stack, slot, consumer);
	}
}
