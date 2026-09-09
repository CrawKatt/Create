package com.simibubi.create.api.data.recipe;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * Base class for addon processing recipe generators.
 */
public abstract class ProcessingRecipeGen<P extends ProcessingRecipeParams, R extends ProcessingRecipe<?, P>, B extends ProcessingRecipeBuilder<P, R, B>>
		 extends BaseRecipeProvider {

	public ProcessingRecipeGen(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries,
		String defaultNamespace) {
		super(output, registries, defaultNamespace);
	}

	protected GeneratedRecipe create(String namespace, Supplier<ItemLike> singleIngredient, UnaryOperator<B> transform) {
		return register(output -> {
			ItemLike itemLike = singleIngredient.get();
			transform.apply(getBuilder(ResourceLocation.fromNamespaceAndPath(namespace,
				RegisteredObjectsHelper.getKeyOrThrow(itemLike.asItem()).getPath()))
				.withItemIngredients(Ingredient.of(itemLike))).build(output);
		});
	}

	protected GeneratedRecipe create(Supplier<ItemLike> singleIngredient, UnaryOperator<B> transform) {
		return create(Create.ID, singleIngredient, transform);
	}

	protected GeneratedRecipe createWithDeferredId(Supplier<ResourceLocation> name, UnaryOperator<B> transform) {
		return register(output -> transform.apply(getBuilder(name.get())).build(output));
	}

	protected GeneratedRecipe create(ResourceLocation name, UnaryOperator<B> transform) {
		return createWithDeferredId(() -> name, transform);
	}

	protected GeneratedRecipe create(String name, UnaryOperator<B> transform) {
		return create(asResource(name), transform);
	}

	protected abstract IRecipeTypeInfo getRecipeType();

	protected abstract B getBuilder(ResourceLocation id);

	protected Supplier<ResourceLocation> idWithSuffix(Supplier<ItemLike> item, String suffix) {
		return () -> {
			ResourceLocation registryName = RegisteredObjectsHelper.getKeyOrThrow(item.get().asItem());
			return asResource(registryName.getPath() + suffix);
		};
	}

	@NotNull
	@Override
	public String getName() {
		return modid + "'s processing recipes: " + getRecipeType().getId().getPath();
	}
}
