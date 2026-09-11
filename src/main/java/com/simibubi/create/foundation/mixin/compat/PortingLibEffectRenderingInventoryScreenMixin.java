package com.simibubi.create.foundation.mixin.compat;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderingInventoryScreen.class)
public class PortingLibEffectRenderingInventoryScreenMixin {
	@Inject(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;getEffectName(Lnet/minecraft/world/effect/MobEffectInstance;)Lnet/minecraft/network/chat/Component;"))
	private void create$initializePortingLibInventoryText(GuiGraphics graphics, int x, int height, Iterable<MobEffectInstance> statusEffects, CallbackInfo ci, @Share(value = "custom", namespace = "io.github.fabricators_of_create.porting_lib.entity.mixin.client.EffectRenderingInventoryScreenMixin") LocalRef<Boolean> cancelled) {
		cancelled.set(false);
	}
}
