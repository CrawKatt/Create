package com.simibubi.create.foundation.recipe;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerContainer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class ItemStackHandlerRecipeInput implements RecipeInput {

	private final ItemStackHandlerContainer handler;

	public ItemStackHandlerRecipeInput(ItemStackHandlerContainer handler) {
		this.handler = handler;
	}

	@Override
	public ItemStack getItem(int slot) {
		return handler.getStackInSlot(slot);
	}

	@Override
	public int size() {
		return handler.getSlotCount();
	}
}
