package com.simibubi.create.foundation.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;

import io.github.fabricators_of_create.porting_lib.models.CompositeModel;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

@Mixin(value = CompositeModel.Baked.class, remap = false)
public class CompositeModelMixin implements FabricBakedModel {

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}
}
