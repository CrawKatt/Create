package com.simibubi.create.content.equipment.blueprint;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute.ItemAttributeEntry;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.InTagAttribute;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;

public class BlueprintItem extends Item {

	public BlueprintItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Direction face = ctx.getClickedFace();
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		BlockPos pos = ctx.getClickedPos()
			.relative(face);

		if (player != null && !player.mayUseItemAt(pos, face, stack))
			return InteractionResult.FAIL;

		Level world = ctx.getLevel();
		HangingEntity hangingentity = new BlueprintEntity(world, pos, face, face.getAxis()
			.isHorizontal() ? Direction.DOWN : ctx.getHorizontalDirection());
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

		if (customData != null)
			EntityType.updateCustomEntityTag(world, player, hangingentity, customData);
		if (!hangingentity.survives())
			return InteractionResult.CONSUME;
		if (!world.isClientSide) {
			hangingentity.playPlacementSound();
			world.addFreshEntity(hangingentity);
		}

		stack.shrink(1);
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	public static void assignCompleteRecipe(Level level, ItemStackHandler inv, Recipe<?> recipe) {
		NonNullList<Ingredient> ingredients = recipe.getIngredients();

		for (int i = 0; i < 9; i++)
			inv.setStackInSlot(i, ItemStack.EMPTY);
		inv.setStackInSlot(9, recipe.getResultItem(level.registryAccess()));

		if (recipe instanceof ShapedRecipe shapedRecipe) {
			for (int row = 0; row < shapedRecipe.getHeight(); row++)
				for (int col = 0; col < shapedRecipe.getWidth(); col++)
					inv.setStackInSlot(row * 3 + col,
						convertIngredientToFilter(ingredients.get(row * shapedRecipe.getWidth() + col)));
		} else {
			for (int i = 0; i < ingredients.size(); i++)
				inv.setStackInSlot(i, convertIngredientToFilter(ingredients.get(i)));
		}
	}

	private static ItemStack convertIngredientToFilter(Ingredient ingredient) {
		if (ingredient instanceof FabricIngredient fabricIngredient) {
			CustomIngredient customIngredient = fabricIngredient.getCustomIngredient();
			if (customIngredient != null)
				return makeListFilter(customIngredient.getMatchingStacks(), customIngredient.requiresTesting());
		}

		ResourceLocation tag = getTag(ingredient);
		if (tag != null)
			return makeTagFilter(tag);

		ItemStack[] acceptedItems = ingredient.getItems();
		if (acceptedItems == null || acceptedItems.length == 0 || acceptedItems.length > 18)
			return ItemStack.EMPTY;
		if (acceptedItems.length == 1)
			return acceptedItems[0].copy();
		return makeListFilter(List.of(acceptedItems), true);
	}

	private static ItemStack makeTagFilter(ResourceLocation tag) {
		ItemStack filter = AllItems.ATTRIBUTE_FILTER.asStack();
		filter.set(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);
		filter.set(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES,
			List.of(new ItemAttributeEntry(new InTagAttribute(TagKey.create(Registries.ITEM, tag)), false)));
		return filter;
	}

	private static ResourceLocation getTag(Ingredient ingredient) {
		JsonElement encoded = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient)
			.result()
			.orElse(null);
		if (!(encoded instanceof JsonObject object))
			return null;
		JsonElement tag = object.get("tag");
		return tag != null && tag.isJsonPrimitive() ? ResourceLocation.tryParse(tag.getAsString()) : null;
	}

	private static ItemStack makeListFilter(List<ItemStack> acceptedItems, boolean respectNbt) {
		if (acceptedItems.isEmpty())
			return ItemStack.EMPTY;
		ItemStack result = AllItems.FILTER.asStack();
		ItemStackHandler filterItems = AllItems.FILTER.get().getFilterItemHandler(result);
		for (int i = 0; i < acceptedItems.size() && i < filterItems.getSlotCount(); i++)
			filterItems.setStackInSlot(i, acceptedItems.get(i).copy());
		result.set(AllDataComponents.FILTER_ITEMS, ItemHelper.containerContentsFromHandler(filterItems));
		if (respectNbt)
			result.set(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, true);
		return result;
	}

}
