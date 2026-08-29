package com.simibubi.create.api.event;

import java.util.List;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Fired on the client before a tooltip is rendered. Return true from the callback to cancel
 * tooltip rendering. Replaces Porting Lib's removed PreRenderTooltipCallback.
 */
public interface PreRenderTooltipCallback {

	boolean cancel(ItemStack stack, int x, int y, Font font, List<ClientTooltipComponent> tooltip);

	Event<PreRenderTooltipCallback> EVENT = EventFactory.createArrayBacked(PreRenderTooltipCallback.class,
		listeners -> (stack, x, y, font, tooltip) -> {
			for (PreRenderTooltipCallback listener : listeners) {
				if (listener.cancel(stack, x, y, font, tooltip))
					return true;
			}
			return false;
		});
}
