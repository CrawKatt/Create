package com.simibubi.create.infrastructure.fabric.transfer;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class TransferUtil {
	public static long insert(Storage<FluidVariant> storage, FluidStack stack) {
		try (Transaction t = Transaction.openOuter()) {
			long inserted = insert(storage, stack, t);
			t.commit();
			return inserted;
		}
	}

	public static long insert(Storage<ItemVariant> storage, ItemStack stack) {
		try (Transaction t = Transaction.openOuter()) {
			long inserted = insert(storage, stack, t);
			t.commit();
			return inserted;
		}
	}

	public static long insert(Storage<FluidVariant> storage, FluidStack stack, TransactionContext ctx) {
		return storage.insert(stack.getVariant(), stack.getAmount(), ctx);
	}

	public static long insert(Storage<ItemVariant> storage, ItemStack stack, TransactionContext ctx) {
		return storage.insert(ItemVariant.of(stack), stack.getCount(), ctx);
	}

	@Nullable
	public static <T extends TransferVariant<?>> ResourceAmount<T> extractAny(Storage<T> storage, long maxAmount) {
		return commit(t -> extractAny(storage, maxAmount, t));
	}

	@Nullable
	public static <T extends TransferVariant<?>> ResourceAmount<T> extractAny(Storage<T> storage, long maxAmount, TransactionContext ctx) {
		return StorageUtil.extractAny(storage, maxAmount, ctx);
	}

	@Nullable
	public static <T extends TransferVariant<?>> ResourceAmount<T> extractMatching(Storage<T> storage, Predicate<T> predicate, long maxAmount, TransactionContext ctx) {
		T resourceExtracting = null;
		long extracted = 0;

		for (StorageView<T> view : storage.nonEmptyViews()) {
			T resource = view.getResource();

			// see if a resource has already been chosen
			if (resourceExtracting != null && !resourceExtracting.equals(resource))
				continue;

			// if one hasn't, see if this one matches
			if (resourceExtracting == null && predicate.test(resource)) {
				resourceExtracting = resource;
			} else {
				// nope, skip
				continue;
			}

			extracted += view.extract(resource, maxAmount - extracted, ctx);
			if (extracted >= maxAmount) {
				return new ResourceAmount<>(resource, extracted);
			}
		}

		return resourceExtracting != null ? new ResourceAmount<>(resourceExtracting, extracted) : null;
	}

	@SuppressWarnings("deprecation")
	public static Transaction getTransaction() {
		return Transaction.openNested(Transaction.getCurrentUnsafe());
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorage(BlockEntity be) {
		return ItemStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, null);
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorage(Level level, BlockPos pos) {
		return getItemStorage(level, pos, null);
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorage(Level level, BlockPos pos, @Nullable Direction side) {
		return ItemStorage.SIDED.find(level, pos, side);
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorage(Level level, BlockPos pos) {
		return getFluidStorage(level, pos, null);
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorage(Level level, BlockPos pos, @Nullable Direction side) {
		return FluidStorage.SIDED.find(level, pos, side);
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorage(Level level, BlockPos pos, BlockEntity be, @Nullable Direction side) {
		return FluidStorage.SIDED.find(level, pos, be.getBlockState(), be, side);
	}

	public static Optional<FluidStack> getFluidContained(ItemStack stack) {
		Storage<FluidVariant> storage = net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.withConstant(stack)
			.find(FluidStorage.ITEM);
		if (storage == null)
			return Optional.empty();
		for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
			if (!view.isResourceBlank() && view.getAmount() > 0)
				return Optional.of(new FluidStack(view));
		}
		return Optional.empty();
	}

	public static FluidStack extractAnyFluid(Storage<FluidVariant> storage, long maxAmount, TransactionContext ctx) {
		for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
			long extracted = view.extract(view.getResource(), maxAmount, ctx);
			if (extracted > 0)
				return new FluidStack(view.getResource(), extracted);
		}
		return FluidStack.EMPTY;
	}

	public static FluidStack extractAnyFluid(Storage<FluidVariant> storage, long maxAmount) {
		return commit(t -> extractAnyFluid(storage, maxAmount, t));
	}

	public static ItemStack extractAnyItem(Storage<ItemVariant> storage, long maxAmount) {
		ResourceAmount<ItemVariant> amount = extractAny(storage, maxAmount);
		return amount == null ? ItemStack.EMPTY : amount.resource().toStack((int) amount.amount());
	}

	public static ItemStack extractAnyItem(Storage<ItemVariant> storage, long maxAmount, TransactionContext ctx) {
		ResourceAmount<ItemVariant> amount = extractAny(storage, maxAmount, ctx);
		return amount == null ? ItemStack.EMPTY : amount.resource().toStack((int) amount.amount());
	}

	public static List<ItemStack> getAllItems(Storage<ItemVariant> storage) {
		List<ItemStack> stacks = new ArrayList<>();
		for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
			if (view.isResourceBlank())
				continue;
			stacks.add(view.getResource().toStack((int) view.getAmount()));
		}
		return stacks;
	}

	public static List<ItemStack> extractAllAsStacks(Storage<ItemVariant> storage) {
		List<ItemStack> stacks = new ArrayList<>();
		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
				ItemVariant variant = view.getResource();
				long extracted = view.extract(variant, view.getAmount(), t);
				if (extracted > 0)
					stacks.add(variant.toStack(truncateLong(extracted)));
			}
			t.commit();
		}
		return stacks;
	}

	public static FluidStack firstOrEmpty(Storage<FluidVariant> storage) {
		for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
			if (!view.isResourceBlank() && view.getAmount() > 0)
				return new FluidStack(view.getResource(), view.getAmount());
		}
		return FluidStack.EMPTY;
	}

	public static long totalCapacity(Storage<?> storage) {
		long capacity = 0;
		for (StorageView<?> view : storage) {
			capacity += view.getCapacity();
		}
		return capacity;
	}

	public static long insertToMainInv(Player player, ItemVariant variant, long maxAmount) {
		PlayerInventoryStorage playerInv = PlayerInventoryStorage.of(player);
		try (Transaction t = Transaction.openOuter()) {
			long inserted = 0;
			for (int i = 0; i < 36 && inserted < maxAmount; i++) {
				SingleSlotStorage<ItemVariant> slot = playerInv.getSlot(i);
				inserted += slot.insert(variant, maxAmount - inserted, t);
			}
			t.commit();
			return inserted;
		}
	}

	public static int truncateLong(long l) {
		if (l > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		} else if (l < Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		} else {
			return (int) l;
		}
	}

	public static OptionalLong firstCapacity(Storage<?> storage) {
		for (StorageView<?> view : storage) {
			return OptionalLong.of(view.getCapacity());
		}
		return OptionalLong.empty();
	}

	public static <T> void clear(Storage<T> storage) {
		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<T> view : storage.nonEmptyViews()) {
				view.extract(view.getResource(), view.getAmount(), t);
			}
			t.commit();
		}
	}

	public static <T> T commit(Function<TransactionContext, T> function) {
		try (Transaction t = Transaction.openOuter()) {
			T value = function.apply(t);
			t.commit();
			return value;
		}
	}

	public static <T> T simulate(Function<TransactionContext, T> function) {
		try (Transaction t = Transaction.openOuter()) {
			return function.apply(t);
		}
	}
}
