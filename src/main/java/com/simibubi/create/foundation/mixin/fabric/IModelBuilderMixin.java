package com.simibubi.create.foundation.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.fabricators_of_create.porting_lib.models.IModelBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

@Mixin(value = IModelBuilder.Simple.class, remap = false)
public class IModelBuilderMixin {

	@ModifyExpressionValue(
		method = { "addCulledFace", "addUnculledFace" },
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;fromVanilla",
			remap = false
		)
	)
	private QuadEmitter create$emitVanillaQuad(QuadEmitter emitter) {
		return emitter.emit();
	}

	@ModifyExpressionValue(
		method = "addFace",
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;copyFrom",
			remap = false
		)
	)
	private QuadEmitter create$emitCopiedQuad(QuadEmitter emitter) {
		return emitter.emit();
	}
}
