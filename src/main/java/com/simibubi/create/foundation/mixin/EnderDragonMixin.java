package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fabricators_of_create.porting_lib.block.EntityDestroyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(EnderDragon.class)
public class EnderDragonMixin {

	@ModifyExpressionValue(
		method = "checkWalls",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z",
			ordinal = 0
		)
	)
	private boolean create$respectCopycatMaterial(boolean dragonImmune, @Local BlockPos pos,
			@Local BlockState state) {
		if (!(state.getBlock() instanceof EntityDestroyBlock))
			return dragonImmune;
		EnderDragon dragon = (EnderDragon) (Object) this;
		return dragonImmune || !state.canEntityDestroy(dragon.level(), pos, dragon);
	}
}
