package com.simibubi.create.content.contraptions.render;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.Contraption.RenderedBlocks;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedBlockAndTintGetter;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.client.render.model.BakedModelBufferer;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.catnip.render.SuperByteBufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ContraptionRenderInfo {
	public static final SuperByteBufferCache.Compartment<Pair<Contraption, RenderType>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();

	private final Contraption contraption;
	private final VirtualRenderWorld renderWorld;
	private final ContraptionMatrices matrices = new ContraptionMatrices();

	ContraptionRenderInfo(Level level, Contraption contraption) {
		this.contraption = contraption;
		this.renderWorld = setupRenderWorld(level, contraption);
	}

	public static ContraptionRenderInfo get(Contraption contraption) {
		return ContraptionRenderInfoManager.MANAGERS.get(contraption.entity.level()).getRenderInfo(contraption);
	}

	/**
	 * Reset a contraption's renderer.
	 *
	 * @param contraption The contraption to invalidate.
	 * @return true if there was a renderer associated with the given contraption.
	 */
	public static boolean invalidate(Contraption contraption) {
		return ContraptionRenderInfoManager.MANAGERS.get(contraption.entity.level()).invalidate(contraption);
	}

	public boolean isDead() {
		return !contraption.entity.isAliveOrStale();
	}

	public Contraption getContraption() {
		return contraption;
	}

	public VirtualRenderWorld getRenderWorld() {
		return renderWorld;
	}

	public ContraptionMatrices getMatrices() {
		return matrices;
	}

	public SuperByteBuffer getBuffer(RenderType renderType) {
		return SuperByteBufferCache.getInstance().get(CONTRAPTION, Pair.of(contraption, renderType), () -> buildStructureBuffer(renderType));
	}

	public void invalidate() {
		for (RenderType renderType : RenderType.chunkBufferLayers()) {
			SuperByteBufferCache.getInstance().invalidate(CONTRAPTION, Pair.of(contraption, renderType));
		}
	}

	public static VirtualRenderWorld setupRenderWorld(Level level, Contraption c) {
		ContraptionWorld contraptionWorld = c.getContraptionWorld();

		BlockPos origin = c.anchor;
		int minBuildHeight = contraptionWorld.getMinBuildHeight();
		int height = contraptionWorld.getHeight();
		VirtualRenderWorld renderWorld = new VirtualRenderWorld(level, minBuildHeight, height, origin) {
			@Override
			public boolean supportsVisualization() {
				return VisualizationManager.supportsVisualization(level);
			}

		};

		renderWorld.setBlockEntities(c.presentBlockEntities.values());
		for (StructureTemplate.StructureBlockInfo info : c.getBlocks()
			.values())
			renderWorld.setBlock(info.pos(), info.state(), 0);

		renderWorld.runLightEngine();
		return renderWorld;
	}

	private SuperByteBuffer buildStructureBuffer(RenderType layer) {
		RenderedBlocks blocks = contraption.getRenderedBlocks();
		BlockAndTintGetter modelWorld = new WrappedBlockAndTintGetter(renderWorld) {
			@Override
			public BlockState getBlockState(BlockPos pos) {
				return blocks.lookup().apply(pos);
			}
		};
		SuperByteBufferBuilder builder = new SuperByteBufferBuilder();
		builder.prepare();
		try {
			BakedModelBufferer.bufferBlocks(blocks.positions().iterator(), modelWorld, null, false,
				(renderType, shaded, data) -> {
					if (renderType == layer)
						builder.add(data, shaded);
				});
		} finally {
			ModelBlockRenderer.clearCache();
		}
		return builder.build();
	}
}
