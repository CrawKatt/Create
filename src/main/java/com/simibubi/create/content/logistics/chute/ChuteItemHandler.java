package com.simibubi.create.content.logistics.chute;

import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.infrastructure.fabric.item.ItemUtils;

import net.minecraft.core.component.DataComponents;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ChuteItemHandler extends SingleVariantStorage<ItemVariant> {

	private ChuteBlockEntity blockEntity;

	public ChuteItemHandler(ChuteBlockEntity be) {
		this.blockEntity = be;
		update();
	}

	public void update() {
		this.variant = ItemVariant.of(blockEntity.item);
		this.amount = blockEntity.item.getCount();
	}

	@Override
	public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
		if (!blockEntity.canAcceptItem(insertedVariant.toStack()))
			return 0;
		return super.insert(insertedVariant, Math.min(maxAmount, ItemUtils.getMaxStackSize(insertedVariant)), transaction);
	}

	@Override
	protected void onFinalCommit() {
		blockEntity.setItem(variant.toStack(ItemHelper.truncateLong(amount)));
	}

	@Override
	protected long getCapacity(ItemVariant variant) {
		return variant.getComponentMap().getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
	}

	@Override
	protected ItemVariant getBlankVariant() {
		return ItemVariant.blank();
	}
}
