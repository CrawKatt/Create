package com.simibubi.create.infrastructure.fabric.util;

import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.network.chat.Component;

public enum FluidUnit {
	MILLIBUCKETS(81, "generic.unit.millibuckets"),
	DROPLETS(1, "generic.unit.droplets");

	public final Component name;

	private final int divisor;

	FluidUnit(int divisor, String key) {
		this.divisor = divisor;
		this.name = CreateLang.translateDirect(key);
	}

	public long convert(long droplets) {
		return droplets / this.divisor;
	}
}
