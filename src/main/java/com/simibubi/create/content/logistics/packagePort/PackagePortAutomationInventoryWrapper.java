package com.simibubi.create.content.logistics.packagePort;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class PackagePortAutomationInventoryWrapper extends ItemHandlerWrapper {

	private PackagePortBlockEntity ppbe;

	public PackagePortAutomationInventoryWrapper(Storage<ItemVariant> wrapped, PackagePortBlockEntity ppbe) {
		super(wrapped);
		this.ppbe = ppbe;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!PackageItem.isPackage(resource))
			return 0;
		String filterString = ppbe.getFilterString();
		if (filterString == null || !PackageItem.matchAddress(resource, filterString))
			return 0;

		return super.extract(resource, maxAmount, transaction);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!PackageItem.isPackage(resource))
			return 0;
		String filterString = ppbe.getFilterString();
		if (filterString != null && PackageItem.matchAddress(resource, filterString))
			return 0;

		return super.insert(resource, maxAmount, transaction);
	}
}
