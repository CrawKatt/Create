package com.simibubi.create.api.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Builder;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;

/**
 * Convenience base for processing recipes using Create's standard parameters.
 */
public abstract class StandardProcessingRecipeGen<R extends StandardProcessingRecipe<?>>
		extends ProcessingRecipeGen<ProcessingRecipeParams, R, StandardProcessingRecipe.Builder<R>> {

	public StandardProcessingRecipeGen(FabricDataOutput output, CompletableFuture<Provider> registries,
		String defaultNamespace) {
		super(output, registries, defaultNamespace);
	}

	protected StandardProcessingRecipe.Serializer<R> getSerializer() {
		return getRecipeType().getSerializer();
	}

	@Override
	protected Builder<R> getBuilder(ResourceLocation id) {
		return new StandardProcessingRecipe.Builder<>(getSerializer().factory(), id);
	}
}
