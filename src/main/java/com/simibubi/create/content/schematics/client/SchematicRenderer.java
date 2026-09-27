package com.simibubi.create.content.schematics.client;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.BlockEntityRenderHelper;

import net.createmod.catnip.client.render.model.BakedModelBufferer;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperByteBufferBuilder;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SchematicRenderer {

	private final Map<RenderType, SuperByteBuffer> bufferCache = new LinkedHashMap<>();
	private boolean active;
	private boolean changed;
	protected SchematicLevel schematic;
	private BlockPos anchor;

	public SchematicRenderer() {
		changed = false;
	}

	public void display(SchematicLevel world) {
		this.anchor = world.anchor;
		this.schematic = world;
		this.active = true;
		this.changed = true;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void update() {
		changed = true;
	}

	public void render(PoseStack ms, SuperRenderTypeBuffer buffers) {
		if (!active)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		if (changed)
			redraw();
		changed = false;

		bufferCache.forEach((layer, buffer) -> {
			buffer.renderInto(ms, buffers.getBuffer(layer));
		});
		BlockEntityRenderHelper.renderBlockEntities(schematic, schematic.getRenderedBlockEntities(), ms, buffers);
	}

	protected void redraw() {
		bufferCache.clear();

		for (RenderType layer : RenderType.chunkBufferLayers()) {
			SuperByteBuffer buffer = drawLayer(layer);
			if (!buffer.isEmpty())
				bufferCache.put(layer, buffer);
		}
	}

	protected SuperByteBuffer drawLayer(RenderType layer) {
		SchematicLevel renderWorld = schematic;
		BoundingBox bounds = renderWorld.getBounds();
		boolean oldRenderMode = renderWorld.renderMode;
		PoseStack poseStack = new PoseStack();
		SuperByteBufferBuilder builder = new SuperByteBufferBuilder();
		builder.prepare();
		poseStack.pushPose();
		poseStack.translate(-anchor.getX(), -anchor.getY(), -anchor.getZ());
		renderWorld.renderMode = true;
		try {
			BakedModelBufferer.bufferBlocks(BlockPos.betweenClosedStream(bounds)
				.map(pos -> pos.offset(anchor)).iterator(), renderWorld, poseStack, false,
				(renderType, shaded, data) -> {
					if (renderType == layer)
						builder.add(data, shaded);
				});
		} finally {
			ModelBlockRenderer.clearCache();
			renderWorld.renderMode = oldRenderMode;
			poseStack.popPose();
		}
		return builder.build();
	}

	// fabric: calling chunkBufferLayers early causes issues (#612), let the map handle its size on its own
//	private static int getLayerCount() {
//		return RenderType.chunkBufferLayers()
//			.size();
//	}

}
