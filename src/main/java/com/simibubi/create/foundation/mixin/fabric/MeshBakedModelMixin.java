package com.simibubi.create.foundation.mixin.fabric;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fabricators_of_create.porting_lib.models.MeshBakedModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(MeshBakedModel.class)
public class MeshBakedModelMixin {

	@Shadow(remap = false)
	@Final
	protected Mesh mesh;

	@Shadow(remap = false)
	@Final
	protected TextureAtlasSprite particleIcon;

	@Inject(method = "getQuads", at = @At("HEAD"), cancellable = true)
	private void create$preserveQuadSprites(BlockState state, Direction direction, RandomSource random,
		CallbackInfoReturnable<List<BakedQuad>> cir) {
		SpriteFinder spriteFinder = SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(particleIcon.atlasLocation()));
		List<BakedQuad> quads = new ArrayList<>();
		mesh.forEach(quad -> {
			if (quad.cullFace() == direction)
				quads.add(quad.toBakedQuad(spriteFinder.find(quad)));
		});
		cir.setReturnValue(quads);
	}
}
