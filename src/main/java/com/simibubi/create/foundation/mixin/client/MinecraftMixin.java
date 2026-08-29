package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.api.event.client.AttackAirCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Shadow
	public LocalPlayer player;

	@Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
	private void create$onAttackMiss(CallbackInfoReturnable<Boolean> cir) {
		AttackAirCallback.EVENT.invoker().attackAir(player);
	}
}
