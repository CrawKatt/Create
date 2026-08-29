package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.api.event.client.CameraSetupCallback;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

@Mixin(Camera.class)
public abstract class CameraSetupMixin {

	@Invoker("setRotation")
	public abstract void create$setRotation(float yRot, float xRot);

	@Inject(method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", at = @At("TAIL"))
	private void create$onCameraSetup(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci) {
		Camera self = (Camera) (Object) this;
		CameraSetupCallback.CameraInfo info = new CameraSetupCallback.CameraInfo(self.getYRot(), self.getXRot(), 0);
		CameraSetupCallback.EVENT.invoker().onCameraSetup(info);
		if (info.yaw != self.getYRot() || info.pitch != self.getXRot())
			create$setRotation(info.yaw, info.pitch);
	}
}
