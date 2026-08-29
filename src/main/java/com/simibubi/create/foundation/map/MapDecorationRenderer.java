package com.simibubi.create.foundation.map;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public interface MapDecorationRenderer {
	boolean render(MapDecoration decoration, PoseStack poseStack, MultiBufferSource bufferSource, @NotNull MapItemSavedData mapData,
		MapDecorationTextureManager decorationTextures, boolean inItemFrame, int packedLight, int index);
}
