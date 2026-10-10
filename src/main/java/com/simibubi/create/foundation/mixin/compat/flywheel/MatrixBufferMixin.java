package com.simibubi.create.foundation.mixin.compat.flywheel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.engine_room.flywheel.backend.engine.CpuArena;
import dev.engine_room.flywheel.backend.engine.embed.EnvironmentStorage;
import dev.engine_room.flywheel.backend.engine.indirect.MatrixBuffer;

@Mixin(value = MatrixBuffer.class, remap = false)
public class MatrixBufferMixin {
	@Redirect(method = {"flush", "lambda$flush$0"}, at = @At(value = "INVOKE",
		target = "Ldev/engine_room/flywheel/backend/engine/CpuArena;byteCapacity()J"), require = 2)
	private static long create$usedMatrixBytes(CpuArena arena) {
		// GPU storage is sized for used matrices, not the arena's allocation capacity.
		return (long) arena.capacity() * EnvironmentStorage.MATRIX_SIZE_BYTES;
	}
}
