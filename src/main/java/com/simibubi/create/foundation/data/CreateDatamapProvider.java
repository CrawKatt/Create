package com.simibubi.create.foundation.data;

import java.util.concurrent.CompletableFuture;

import com.google.common.collect.BiMap;
import com.google.gson.JsonObject;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.CopperRegistries;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;

/**
 * fabric: weathering/waxing pairs are injected directly into the vanilla lookup
 * maps; Blaze Burner fuels use a resource-backed Fabric data map instead.
 */
public class CreateDatamapProvider implements DataProvider {
	private final PackOutput.PathProvider dataMapPath;

	public CreateDatamapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		dataMapPath = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "data_maps/item");
	}

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

		JsonObject fuel = new JsonObject();
		fuel.addProperty("burn_time", 3200);
		JsonObject values = new JsonObject();
		values.add(BuiltInRegistries.ITEM.getKey(AllItems.BLAZE_CAKE.get()).toString(), fuel);
		JsonObject dataMap = new JsonObject();
		dataMap.add("values", values);
		return DataProvider.saveStable(cachedOutput, dataMap,
			dataMapPath.json(com.simibubi.create.Create.asResource("superheated_blaze_burner_fuels")));
	}

	@Override
	public String getName() {
		return "Create datamaps";
	}
}
