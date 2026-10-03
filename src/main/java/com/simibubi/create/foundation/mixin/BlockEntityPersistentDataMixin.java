package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fabricators_of_create.porting_lib.blocks.util.BlockEntityDataKeys;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntity.class)
public class BlockEntityPersistentDataMixin {
	@Inject(method = "loadWithComponents", at = @At("HEAD"))
	private void create$migratePersistentData(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
		// A dependency update does not change the world's Minecraft DataVersion, so DFU may not run.
		if (!tag.contains(BlockEntityDataKeys.EXTRA_DATA_KEY, Tag.TAG_COMPOUND)
			&& tag.contains(BlockEntityDataKeys.OLD_EXTRA_DATA_KEY, Tag.TAG_COMPOUND))
			tag.put(BlockEntityDataKeys.EXTRA_DATA_KEY, tag.getCompound(BlockEntityDataKeys.OLD_EXTRA_DATA_KEY).copy());
	}
}
