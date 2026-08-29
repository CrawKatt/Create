package com.simibubi.create.compat.sandwichable;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * No-op port: Sandwichable has no 1.21.1 build yet, so sequenced sandwiching is
 * disabled until the mod updates. Method signatures are kept for upstream parity.
 */
public class SequencedSandwiching {
	/**
	 * Only allow sequenced sandwiching on sandwiches (or bread, becoming sandwiches),
	 * AND if the item-to-add can be put on a sandwich.
	 */
	public static boolean shouldSandwich(ItemStack handling, ItemStack held, Level level) {
		return false;
	}

	public static void activateSandwich(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler,
										DeployerBlockEntity deployer) {}

	/**
	 * Stacks 'toAdd' on top of the given sandwich.
	 * @return the new sandwich, with the item stacked on top, or EMPTY if nothing stacked
	 */
	public static ItemStack stackOnSandwich(ItemStack sandwich, ItemStack toAdd, DeployerBlockEntity deployer) {
		return ItemStack.EMPTY;
	}

	@Nullable
	public static Object sandwichFromStack(ItemStack stack) {
		return null;
	}
}
