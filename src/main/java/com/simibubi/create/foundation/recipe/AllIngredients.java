package com.simibubi.create.foundation.recipe;

import com.simibubi.create.foundation.data.SimpleDatagenIngredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public class AllIngredients {
	public static void register() {
		CustomIngredientSerializer.register(SimpleDatagenIngredient.SERIALIZER);
	}
}
