package com.simibubi.create.compat.emi.recipes.basin;

import com.simibubi.create.content.processing.basin.BasinRecipe;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.resources.ResourceLocation;

public class ShapelessEmiRecipe extends MixingEmiRecipe {

	public ShapelessEmiRecipe(EmiRecipeCategory category, BasinRecipe recipe, ResourceLocation recipeId) {
		super(category, recipe);
		this.id = ResourceLocation.fromNamespaceAndPath("emi", "create/shapeless/" + recipeId.getNamespace() + "/" + recipeId.getPath());
	}
}
