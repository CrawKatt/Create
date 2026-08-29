package com.simibubi.create.foundation.data.recipe;

import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

public final class ConditionalRecipeOutput implements RecipeOutput {

	private final RecipeOutput parent;
	private final List<ResourceCondition> conditions;

	private ConditionalRecipeOutput(RecipeOutput parent, List<ResourceCondition> conditions) {
		this.parent = parent;
		this.conditions = conditions;
	}

	public static RecipeOutput withConditions(RecipeOutput parent, List<ResourceCondition> conditions) {
		return conditions.isEmpty() ? parent : new ConditionalRecipeOutput(parent, conditions);
	}

	@Override
	public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
		FabricDataGenHelper.addConditions(recipe, conditions.toArray(new ResourceCondition[0]));
		parent.accept(id, recipe, advancement);
	}

	@Override
	@NotNull
	public Advancement.Builder advancement() {
		return parent.advancement();
	}
}
