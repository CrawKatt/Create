package com.simibubi.create.api.data.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;

/** The burn time assigned to an item by a Blaze Burner fuel data map. */
public record BlazeBurnerFuel(int burnTime) {

	public static final Codec<BlazeBurnerFuel> BURN_TIME_CODEC = ExtraCodecs.POSITIVE_INT.xmap(
		BlazeBurnerFuel::new, BlazeBurnerFuel::burnTime);
	public static final Codec<BlazeBurnerFuel> CODEC = Codec.withAlternative(
		RecordCodecBuilder.create(instance -> instance.group(
			ExtraCodecs.POSITIVE_INT.fieldOf("burn_time").forGetter(BlazeBurnerFuel::burnTime)
		).apply(instance, BlazeBurnerFuel::new)), BURN_TIME_CODEC);
}
