package com.simibubi.create.foundation.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.fabricators_of_create.porting_lib.item.ArmorTextureItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.CustomRenderedArmorItem;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {
	@WrapOperation(
		method = "renderArmorPiece",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ArmorMaterial$Layer;texture(Z)Lnet/minecraft/resources/ResourceLocation;"
		)
	)
	private ResourceLocation create$useCustomArmorTexture(ArmorMaterial.Layer layer, boolean innerModel,
			Operation<ResourceLocation> original, @Local(argsOnly = true) LivingEntity entity,
			@Local(argsOnly = true) EquipmentSlot slot) {
		ItemStack stack = entity.getItemBySlot(slot);
		if (stack.getItem() instanceof ArmorTextureItem item) {
			ResourceLocation texture = item.getArmorTexture(stack, entity, slot, layer, innerModel);
			if (texture != null)
				return texture;
		}
		return original.call(layer, innerModel);
	}

	@Inject(
		method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"),
		cancellable = true
	)
	private void create$onRenderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<?> model, CallbackInfo ci, @Local ItemStack stack) {
		if (stack.getItem() instanceof CustomRenderedArmorItem renderer) {
			renderer.renderArmorPiece((HumanoidArmorLayer<?, ?, ?>) (Object) this, poseStack, bufferSource, entity, slot, light, model, stack);
			ci.cancel();
		}
	}
}
