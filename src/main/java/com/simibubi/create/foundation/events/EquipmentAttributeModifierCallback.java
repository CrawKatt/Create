package com.simibubi.create.foundation.events;

import java.util.function.BiConsumer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public interface EquipmentAttributeModifierCallback {
	Event<EquipmentAttributeModifierCallback> EVENT = EventFactory.createArrayBacked(
		EquipmentAttributeModifierCallback.class,
		callbacks -> (entity, stack, slot, consumer) -> {
			for (EquipmentAttributeModifierCallback callback : callbacks)
				callback.addModifiers(entity, stack, slot, consumer);
		});

	void addModifiers(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
		BiConsumer<Holder<Attribute>, AttributeModifier> consumer);
}
