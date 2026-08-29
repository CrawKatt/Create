package com.simibubi.create.compat.botania;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

/**
 * No-op port: Botania has no 1.21.1 build yet, so apothecary filling is disabled
 * until the mod updates. The class shape is kept for upstream parity.
 */
public enum ApothecaryFilling implements BlockSpoutingBehaviour {
	INSTANCE;

	@Override
	public long fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
		if (!enabled())
			return 0;

		return 0;
	}

	private boolean enabled() {
		return AllConfigs.server().recipes.allowFillingBySpout.get();
	}
}
