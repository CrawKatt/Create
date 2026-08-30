package com.simibubi.create.foundation.mixin;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.level.block.Block;

// Minecraft 1.21 validates every registered block, while Registrate's provider only owns
// the tables collected from its entry callbacks. Limit that provider to the tables it owns.
@Mixin(BlockLootSubProvider.class)
public abstract class BlockLootSubProviderMixin {

	@Redirect(
		method = "generate(Ljava/util/function/BiConsumer;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/DefaultedRegistry;iterator()Ljava/util/Iterator;"
		)
	)
	private Iterator<Block> create$iterateRegistrateBlocks(DefaultedRegistry<Block> registry) {
		if ((Object) this instanceof RegistrateBlockLootTables tables)
			return registry.stream()
				.filter(block -> tables.map.containsKey(block.getLootTable()))
				.iterator();
		return registry.iterator();
	}
}
