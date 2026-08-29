package com.simibubi.create.compat.jei;

import java.util.Arrays;
import java.util.stream.Stream;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;

import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;

public final class ToolboxColoringRecipeMaker {

	// From JEI's ShulkerBoxColoringRecipeMaker
	public static Stream<RecipeHolder<CraftingRecipe>> createRecipes() {
		String group = "create.toolbox.color";
		ItemStack baseShulkerStack = AllBlocks.TOOLBOXES.get(DyeColor.BROWN)
			.asStack();
		Ingredient baseShulkerIngredient = Ingredient.of(baseShulkerStack);

		return Arrays.stream(DyeColor.values())
			.filter(dc -> dc != DyeColor.BROWN)
			.map(color -> {
				TagKey<Item> colorTag = getColorDyeTag(color);
				Ingredient colorIngredient = Ingredient.of(colorTag);
				NonNullList<Ingredient> inputs =
					NonNullList.of(Ingredient.EMPTY, baseShulkerIngredient, colorIngredient);
				Block coloredShulkerBox = AllBlocks.TOOLBOXES.get(color)
					.get();
				ItemStack output = new ItemStack(coloredShulkerBox);
				ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, output, inputs);
				return new RecipeHolder<>(Create.asResource(group + "/" + color), recipe);
			});
	}

	private static TagKey<Item> getColorDyeTag(DyeColor color) {
		return switch (color) {
			case WHITE -> ConventionalItemTags.WHITE_DYES;
			case ORANGE -> ConventionalItemTags.ORANGE_DYES;
			case MAGENTA -> ConventionalItemTags.MAGENTA_DYES;
			case LIGHT_BLUE -> ConventionalItemTags.LIGHT_BLUE_DYES;
			case YELLOW -> ConventionalItemTags.YELLOW_DYES;
			case LIME -> ConventionalItemTags.LIME_DYES;
			case PINK -> ConventionalItemTags.PINK_DYES;
			case GRAY -> ConventionalItemTags.GRAY_DYES;
			case LIGHT_GRAY -> ConventionalItemTags.LIGHT_GRAY_DYES;
			case CYAN -> ConventionalItemTags.CYAN_DYES;
			case PURPLE -> ConventionalItemTags.PURPLE_DYES;
			case BLUE -> ConventionalItemTags.BLUE_DYES;
			case BROWN -> ConventionalItemTags.BROWN_DYES;
			case GREEN -> ConventionalItemTags.GREEN_DYES;
			case RED -> ConventionalItemTags.RED_DYES;
			case BLACK -> ConventionalItemTags.BLACK_DYES;
		};
	}

	private ToolboxColoringRecipeMaker() {}

}
