package com.simibubi.create.foundation.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fabricators_of_create.porting_lib.models.ExtraFaceData;
import net.minecraft.client.renderer.block.model.BlockElement;

// porting-lib's ModelBuilder compares BlockElement#getExtraFaceData against DEFAULT without
// a null guard; elements built through registrate never receive face data, crashing datagen
@Mixin(targets = "io.github.fabricators_of_create.porting_lib.models.generators.ModelBuilder")
public abstract class ModelBuilderFaceDataMixin {

	@WrapOperation(
		method = "lambda$toJson$0",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/block/model/BlockElement;port_lib$getFaceData()Lio/github/fabricators_of_create/porting_lib/models/ExtraFaceData;"
		)
	)
	private ExtraFaceData create$guardNullFaceData(BlockElement element, Operation<ExtraFaceData> original) {
		ExtraFaceData data = original.call(element);
		return data == null ? ExtraFaceData.DEFAULT : data;
	}
}
