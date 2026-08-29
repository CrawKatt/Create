package com.simibubi.create;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.api.behaviour.spouting.CauldronSpoutingBehavior;
import com.simibubi.create.api.behaviour.spouting.StateChangingBehavior;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.botania.ApothecaryFilling;
import com.simibubi.create.compat.tconstruct.SpoutCasting;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public class AllBlockSpoutingBehaviours {

	static void registerDefaults() {
		Predicate<Fluid> isWater = fluid -> fluid.isSame(Fluids.WATER);
		BlockSpoutingBehaviour toMud = StateChangingBehavior.setTo(FluidConstants.BUCKET / 4, isWater, Blocks.MUD);

		for (Block dirt : List.of(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT)) {
			BlockSpoutingBehaviour.BY_BLOCK.register(dirt, toMud);
		}

		BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.FARMLAND, StateChangingBehavior.incrementingState(FluidConstants.BUCKET / 10, isWater, FarmBlock.MOISTURE));
		BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.WATER_CAULDRON, StateChangingBehavior.incrementingState(FluidConstants.BUCKET / 4, isWater, LayeredCauldronBlock.LEVEL));
		BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.CAULDRON, CauldronSpoutingBehavior.INSTANCE);

		if (Mods.BOTANIA.isLoaded()) {
			getTypeForCompat(Mods.BOTANIA, "altar").ifPresent(
				type -> BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(type, ApothecaryFilling.INSTANCE)
			);
		}

		if (!Mods.TCONSTRUCT.isLoaded())
			return;

		for (String name : List.of("table", "basin")) {
			getTypeForCompat(Mods.TCONSTRUCT, name).ifPresent(
				type -> BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(type, SpoutCasting.INSTANCE)
			);
		}
	}

	private static Optional<BlockEntityType<?>> getTypeForCompat(Mods mod, String name) {
		ResourceLocation id = mod.rl(name);
		BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(id);
		if (type != null) {
			return Optional.of(type);
		} else {
			Create.LOGGER.warn("Block entity {} wasn't found. Outdated compat?", id);
			return Optional.empty();
		}
	}
}
