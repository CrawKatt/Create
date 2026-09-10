package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

@Mixin(BlockLootSubProvider.class)
public interface BlockLootSubProviderAccessor {
	@Invoker("hasSilkTouch")
	LootItemCondition.Builder create$hasSilkTouch();
}
