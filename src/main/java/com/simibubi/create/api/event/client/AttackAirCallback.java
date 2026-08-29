package com.simibubi.create.api.event.client;

import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class AttackAirCallback {

	public static final Event<Callback> EVENT = EventFactory.createArrayBacked(Callback.class,
		callbacks -> player -> {
			for (Callback callback : callbacks)
				callback.attackAir(player);
		});

	@FunctionalInterface
	public interface Callback {
		void attackAir(Player player);
	}
}
