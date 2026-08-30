package com.simibubi.create.foundation.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;

@Mixin(LootTableProvider.class)
public interface LootTableProviderAccessor {
	@Mutable
	@Final
	@Accessor("subProviders")
	void create$setSubProviders(List<SubProviderEntry> subProviders);
}
