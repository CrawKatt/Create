package com.simibubi.create.api.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

/**
 * Shared Fabric setup for addon recipe providers.
 *
 * <p>Use {@link ProcessingRecipeGen} for Create processing recipes; extend this
 * class directly for other recipe types.</p>
 */
public abstract class BaseRecipeProvider extends CreateRecipeProvider {
	protected final String modid;

	protected BaseRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries,
		String defaultNamespace) {
		super(output, registries);
		this.modid = defaultNamespace;
	}

	protected ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(modid, path);
	}
}
