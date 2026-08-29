package com.simibubi.create.foundation.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.Create;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.data.loot.BlockLootSubProvider;

// registrate's per-entry loot callbacks can be skipped for some blocks, leaving holes that
// would crash datagen validation; emit an empty table instead so generation completes
@Mixin(BlockLootSubProvider.class)
public abstract class BlockLootSubProviderMixin {

	@Redirect(
		method = "generate(Ljava/util/function/BiConsumer;)V",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"
		)
	)
	private Object create$gracefulMissingLoot(Map<ResourceKey<LootTable>, LootTable.Builder> map, Object key) {
		Object builder = map.remove(key);
		if (builder == null) {
			Create.LOGGER.warn("Missing loot table for {}, generating an empty one", key);
			return LootTable.lootTable();
		}
		return builder;
	}
}
