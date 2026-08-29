package com.simibubi.create.compat.thresholdSwitch;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public class SophisticatedStorage implements ThresholdSwitchCompat {

	@Override
	public boolean isFromThisMod(BlockEntity be) {
		if (be == null)
			return false;

		String namespace = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType())
			.getNamespace();

		return
			"sophisticatedstorage".equals(namespace)
			|| "sophisticatedbackpacks".equals(namespace);
	}

	@Override
	public long getSpaceInSlot(StorageView<ItemVariant> inv) {
		return inv.getCapacity();
	}
}
