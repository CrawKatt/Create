package com.simibubi.create.compat.thresholdSwitch;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public class StorageDrawers implements ThresholdSwitchCompat {

	@Override
	public boolean isFromThisMod(BlockEntity blockEntity) {
		return blockEntity != null && "storagedrawers"
			.equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType())
				.getNamespace());
	}

	@Override
	public long getSpaceInSlot(StorageView<ItemVariant> inv) {
		return inv.getCapacity();
	}
}
