package com.simibubi.create;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public class AllMapDecorationTypes {
	public static final Holder<MapDecorationType> STATION_MAP_DECORATION = register("station",
		() -> new MapDecorationType(Create.asResource("station"), true, -1, false, true));

	private static Holder<MapDecorationType> register(String name, Supplier<MapDecorationType> factory) {
		ResourceKey<MapDecorationType> key = ResourceKey.create(Registries.MAP_DECORATION_TYPE, Create.asResource(name));
		return Registry.registerForHolder(BuiltInRegistries.MAP_DECORATION_TYPE, key, factory.get());
	}

	@Internal
	public static void register() {
	}
}
