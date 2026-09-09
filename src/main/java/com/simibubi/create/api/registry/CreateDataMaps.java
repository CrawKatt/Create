package com.simibubi.create.api.registry;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.datamaps.BlazeBurnerFuel;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

/** Fabric's resource-backed equivalent of Create's item data maps. */
public final class CreateDataMaps {

	private static final Gson GSON = new Gson();
	private static final String DIRECTORY = "data_maps/item";

	public static final ResourceLocation REGULAR_BLAZE_BURNER_FUELS =
		Create.asResource("regular_blaze_burner_fuels");
	public static final ResourceLocation SUPERHEATED_BLAZE_BURNER_FUELS =
		Create.asResource("superheated_blaze_burner_fuels");

	private static volatile Map<Item, BlazeBurnerFuel> regular = Map.of();
	private static volatile Map<Item, BlazeBurnerFuel> superheated = Map.of();

	public static final IdentifiableResourceReloadListener LISTENER = new ReloadListener();

	private static final class ReloadListener extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
		private ReloadListener() {
			super(GSON, DIRECTORY);
		}

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager,
				ProfilerFiller profiler) {
			regular = load(resources, REGULAR_BLAZE_BURNER_FUELS);
			superheated = load(resources, SUPERHEATED_BLAZE_BURNER_FUELS);
			Create.LOGGER.info("Loaded {} regular and {} superheated Blaze Burner fuels.", regular.size(), superheated.size());
		}

		@Override
		public ResourceLocation getFabricId() {
			return Create.asResource("blaze_burner_fuel_data_maps");
		}
	}

	private CreateDataMaps() {}

	@Nullable
	public static BlazeBurnerFuel getRegular(Item item) {
		return regular.get(item);
	}

	@Nullable
	public static BlazeBurnerFuel getSuperheated(Item item) {
		return superheated.get(item);
	}

	@Nullable
	public static BlazeBurnerFuel get(ResourceLocation map, Item item) {
		if (REGULAR_BLAZE_BURNER_FUELS.equals(map))
			return getRegular(item);
		if (SUPERHEATED_BLAZE_BURNER_FUELS.equals(map))
			return getSuperheated(item);
		return null;
	}

	private static Map<Item, BlazeBurnerFuel> load(Map<ResourceLocation, JsonElement> resources, ResourceLocation mapId) {
		JsonElement root = resources.get(mapId);
		if (!(root instanceof JsonObject object) || !(object.get("values") instanceof JsonObject values))
			return Map.of();

		Map<Item, BlazeBurnerFuel> fuels = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
			ResourceLocation itemId = ResourceLocation.tryParse(entry.getKey());
			if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
				Create.LOGGER.error("Unknown item {} in Blaze Burner fuel data map {}.", entry.getKey(), mapId);
				continue;
			}

			BlazeBurnerFuel.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
				.resultOrPartial(error -> Create.LOGGER.error("Invalid Blaze Burner fuel for {} in {}: {}", entry.getKey(), mapId, error))
				.ifPresent(fuel -> fuels.put(BuiltInRegistries.ITEM.get(itemId), fuel));
		}
		return Map.copyOf(fuels);
	}
}
