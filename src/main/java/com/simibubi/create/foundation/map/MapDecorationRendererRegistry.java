package com.simibubi.create.foundation.map;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public class MapDecorationRendererRegistry {

	public static final Map<Holder<MapDecorationType>, MapDecorationRenderer> RENDERERS = new HashMap<>();

	public static void register(Holder<MapDecorationType> type, MapDecorationRenderer renderer) {
		RENDERERS.put(type, renderer);
	}
}
