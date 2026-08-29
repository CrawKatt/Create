package com.simibubi.create.foundation.data;

import java.util.concurrent.CompletableFuture;

import com.google.common.collect.BiMap;
import com.simibubi.create.foundation.block.CopperRegistries;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;

/**
 * fabric: neoforge datamaps do not exist here, so the weathering/waxing pairs are
 * injected directly into the vanilla lookup maps instead of being datagen'd.
 */
public class CreateDatamapProvider implements DataProvider {
	public CreateDatamapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {}

	public static void registerInteractions() {
		BiMap<Block, Block> nextByBlock = WeatheringCopper.NEXT_BY_BLOCK.get();
		BiMap<Block, Block> previousByBlock = WeatheringCopper.PREVIOUS_BY_BLOCK.get();
		CopperRegistries.getWeatheringView().forEach((now, after) -> {
			nextByBlock.put(now.get(), after.get());
			previousByBlock.put(after.get(), now.get());
		});

		BiMap<Block, Block> waxables = HoneycombItem.WAXABLES.get();
		BiMap<Block, Block> waxOffByBlock = HoneycombItem.WAX_OFF_BY_BLOCK.get();
		CopperRegistries.getWaxableView().forEach((now, after) -> {
			waxables.put(now.get(), after.get());
			waxOffByBlock.put(after.get(), now.get());
		});
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		registerInteractions();
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public String getName() {
		return "Create datamaps";
	}
}
