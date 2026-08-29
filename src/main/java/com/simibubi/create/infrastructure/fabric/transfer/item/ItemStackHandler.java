package com.simibubi.create.infrastructure.fabric.transfer.item;

import com.google.common.collect.Iterators;

import com.simibubi.create.infrastructure.fabric.item.ItemUtils;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * A shim for Forge's ItemStackHandler, over-engineered to be as fast as possible with fabric transfer.
 */
public class ItemStackHandler implements SlottedStackStorage {
	private final List<Slot> slots;
	private final SortedSet<Slot> nonEmptySlots;
	private final Map<Item, SortedSet<Slot>> lookup;

	public NonNullList<ItemStack> getStacks() {
		NonNullList<ItemStack> stacks = NonNullList.withSize(slots.size(), ItemStack.EMPTY);
		for (int i = 0; i < slots.size(); i++)
			stacks.set(i, getStackInSlot(i));
		return stacks;
	}

	public ItemStackHandler() {
		this(1);
	}

	public ItemStackHandler(int stacks) {
		this(Util.make(new ItemStack[stacks], array -> Arrays.fill(array, ItemStack.EMPTY)));
	}

	public ItemStackHandler(ItemStack[] stacks) {
		this.slots = new ArrayList<>(stacks.length);
		this.nonEmptySlots = createSlotSet();
		this.lookup = new HashMap<>();
		for (int i = 0; i < stacks.length; i++) {
			ItemStack stack = stacks[i];
			// slot handles filling lookup
			this.slots.add(makeSlot(i, stack));
		}
	}

	// core functionality

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		long inserted = 0;
		Iterator<Slot> itr = getInsertableSlotsFor(resource);
		while (itr.hasNext()) {
			Slot slot = itr.next();
			inserted += slot.insert(resource, maxAmount - inserted, transaction);
			if (inserted >= maxAmount)
				break;
		}
		return inserted;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		Item item = resource.getItem();
		SortedSet<Slot> slots = getSlotsContaining(item);
		if (slots.isEmpty())
			return 0; // no slots hold this item
		long extracted = 0;
		for (Slot slot : slots) {
			extracted += slot.extract(resource, maxAmount - extracted, transaction);
			if (extracted >= maxAmount)
				break;
		}
		return extracted;
	}

	// iteration

	@Override
	public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
		//noinspection unchecked,rawtypes
		return (Iterable) nonEmptySlots;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
		//noinspection unchecked,rawtypes
		return (Iterator) nonEmptySlots.iterator();
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		//noinspection unchecked,rawtypes
		return (Iterator) this.slots.iterator();
	}

	// slot support

	@Override
	public int getSlotCount() {
		return slots.size();
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return this.getInternalSlot(slot);
	}

	@Override
	public List<SingleSlotStorage<ItemVariant>> getSlots() {
		//noinspection unchecked,rawtypes
		return (List) slots;
	}

	// API, mostly from forge, with extras

	public ItemStack getStackInSlot(int slot) {
		return this.getInternalSlot(slot).getStack();
	}

	public void setStackInSlot(int slot, ItemStack stack) {
		this.getInternalSlot(slot).setNewStack(stack);
	}

	public ItemVariant getVariantInSlot(int slot) {
		return this.getSlot(slot).getResource();
	}

	@Override
	public int getSlotLimit(int slot) {
		return Item.ABSOLUTE_MAX_STACK_SIZE;
	}

	protected int getStackLimit(int slot, ItemVariant variant) {
		return Math.min(this.getSlotLimit(slot), ItemUtils.getMaxStackSize(variant));
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return true;
	}

	/**
	 * Once a transaction is committed, this is invoked once for each modified slot.
	 */
	protected void onContentsChanged(int slot) {
	}

	/**
	 * Called after NBT is loaded and this handler has been updated.
	 */
	protected void onLoad() {
	}

	/**
	 * True if this handler only contains empty stacks.
	 */
	public boolean empty() {
		return this.nonEmptySlots.isEmpty();
	}

	/**
	 * Resize this handler, clearing all existing content.
	 */
	public void setSize(int size) {
		this.slots.clear();
		this.nonEmptySlots.clear();
		this.lookup.clear();
		for (int i = 0; i < size; i++) {
			this.slots.add(makeSlot(i, ItemStack.EMPTY));
		}
	}

	public boolean isEmpty() {
		return this.nonEmptySlots.isEmpty();
	}

	// serialization

	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("Size", this.slots.size());

		ListTag slots = new ListTag();
		for (Slot slot : this.slots) {
			if (!slot.getStack().isEmpty()) {
				CompoundTag itemTag = new CompoundTag();
				itemTag.putInt("Slot", slot.index);
				Tag saved = slot.save(provider, itemTag);
				if (saved != null) {
					slots.add(saved);
				}
			}
		}

		nbt.put("Items", slots);
		return nbt;
	}

	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : slots.size()); // also clears
		ListTag slots = nbt.getList("Items", Tag.TAG_COMPOUND);
		for (int i = 0; i < slots.size(); i++) {
			CompoundTag slotTag = slots.getCompound(i);
			int index = slotTag.getInt("Slot");

			if (index >= 0 && index < this.slots.size()) {
				this.slots.get(index).load(provider, slotTag);
			}
		}
		onLoad();
	}

	// internals

	private Slot getInternalSlot(int index) {
		return this.slots.get(index);
	}

	private SortedSet<Slot> getSlotsContaining(Item item) {
		return this.lookup.getOrDefault(item, Collections.emptySortedSet());
	}

	private void onStackChange(Slot slot, ItemStack oldStack, ItemStack newStack) {
		if (ItemStack.isSameItem(oldStack, newStack))
			return;
		SortedSet<Slot> oldItemSlots = this.getSlotsContaining(oldStack.getItem());
		if (!oldItemSlots.isEmpty()) {
			oldItemSlots.remove(slot);
		}
		this.getSetForItem(newStack.getItem()).add(slot);
		if (oldStack.isEmpty()) { // no longer empty
			this.nonEmptySlots.add(slot);
		} else if (newStack.isEmpty()) { // became empty
			this.nonEmptySlots.remove(slot);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + slots + ']';
	}

	private Iterator<Slot> getInsertableSlotsFor(ItemVariant variant) {
		SortedSet<Slot> slots = getSlotsContaining(variant.getItem());
		SortedSet<Slot> emptySlots = getSlotsContaining(Items.AIR);
		if (slots.isEmpty()) {
			return emptySlots.isEmpty() ? Collections.emptyIterator() : emptySlots.iterator();
		} else {
			return emptySlots.isEmpty() ? slots.iterator() : Iterators.concat(
				slots.iterator(), emptySlots.iterator()
			);
		}
	}

	private SortedSet<Slot> getSetForItem(Item item) {
		return this.lookup.computeIfAbsent(item, $ -> createSlotSet());
	}

	private static SortedSet<Slot> createSlotSet() {
		return new ObjectAVLTreeSet<>(Comparator.comparingInt(slot -> slot.index));
	}

	/**
	 * Create the slot for a given index. Subclasses may return custom slots.
	 */
	protected Slot makeSlot(int index, ItemStack initialStack) {
		return new Slot(index, initialStack);
	}

	protected class Slot extends SingleStackStorage {
		private final int index;

		private ItemStack stack;
		private ItemStack lastStack; // last stack pre-transaction
		private ItemVariant variant;

		protected Slot(int index, ItemStack initial) {
			this.index = index;
			this.lastStack = initial.copy();
			this.stack = initial;
			this.variant = ItemVariant.of(initial);

			ItemStackHandler.this.getSetForItem(this.stack.getItem()).add(this);
			if (!this.stack.isEmpty()) {
				ItemStackHandler.this.nonEmptySlots.add(this);
			}
		}

		@Override
		protected int getCapacity(ItemVariant itemVariant) {
			return ItemStackHandler.this.getStackLimit(index, itemVariant);
		}

		@Override
		protected boolean canInsert(ItemVariant resource) {
			return ItemStackHandler.this.isItemValid(this.index, resource, 1);
		}

		@Override
		protected ItemStack getStack() {
			return this.stack;
		}

		/**
		 * Should only be used in transactions.
		 */
		@Override
		protected void setStack(ItemStack stack) {
			this.stack = stack;
			this.variant = ItemVariant.of(stack);
		}

		private void setNewStack(ItemStack stack) {
			this.setStack(stack);
			this.onFinalCommit();
		}

		@Override
		public ItemVariant getResource() {
			return this.variant;
		}

		@Override
		protected void onFinalCommit() {
			this.onStackChange();
			this.notifyHandlerOfChange();
		}

		private void onStackChange() {
			ItemStackHandler.this.onStackChange(this, lastStack, stack);
			this.lastStack = stack.copy();
		}

		private void notifyHandlerOfChange() {
			ItemStackHandler.this.onContentsChanged(index);
		}

		/**
		 * "Slot" is a reserved key.
		 */
		protected Tag save(HolderLookup.Provider provider, Tag tag) {
			return stack.save(provider, tag);
		}

		protected void load(HolderLookup.Provider provider, CompoundTag tag) {
			ItemStack.parse(provider, tag).ifPresent(this::setStack);
			onStackChange();
			// intentionally do not notify handler, matches forge
		}
	}
}
