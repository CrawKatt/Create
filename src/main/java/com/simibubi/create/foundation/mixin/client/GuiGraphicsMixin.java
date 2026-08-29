package com.simibubi.create.foundation.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.api.event.PreRenderTooltipCallback;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

	@Inject(method = "renderTooltipInternal", at = @At("HEAD"), cancellable = true)
	private void create$preRenderTooltip(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY,
		ClientTooltipPositioner positioner, CallbackInfo ci) {
		if (PreRenderTooltipCallback.EVENT.invoker().cancel(ItemStack.EMPTY, mouseX, mouseY, font, components))
			ci.cancel();
	}
}
