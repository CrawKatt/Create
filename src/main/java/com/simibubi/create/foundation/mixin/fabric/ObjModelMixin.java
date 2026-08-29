package com.simibubi.create.foundation.mixin.fabric;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.github.fabricators_of_create.porting_lib.models.obj.ObjModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

@Mixin(value = ObjModel.class, remap = false)
public class ObjModelMixin {

	@Redirect(
		method = "makeQuad",
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/MeshBuilder;build()Lnet/fabricmc/fabric/api/renderer/v1/mesh/Mesh;"
		)
	)
	private Mesh create$emitQuadBeforeBuild(MeshBuilder builder, @Local QuadEmitter quadBaker) {
		quadBaker.emit();
		return builder.build();
	}
}
