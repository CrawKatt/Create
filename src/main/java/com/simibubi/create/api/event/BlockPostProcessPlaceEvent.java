package com.simibubi.create.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class BlockPostProcessPlaceEvent {

	public static final Event<Callback> EVENT = EventFactory.createArrayBacked(Callback.class,
		callbacks -> (context, pos, state) -> {
			for (Callback callback : callbacks)
				callback.onPostProcessPlace(context, pos, state);
		});

	@FunctionalInterface
	public interface Callback {
		void onPostProcessPlace(BlockPlaceContext context, BlockPos pos, BlockState state);
	}
}
