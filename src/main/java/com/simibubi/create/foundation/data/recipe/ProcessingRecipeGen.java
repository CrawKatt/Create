package com.simibubi.create.foundation.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.Create;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.createmod.catnip.registry.RegisteredObjectsHelper;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public abstract class ProcessingRecipeGen extends CreateRecipeProvider {

	protected static final List<ProcessingRecipeGen> GENERATORS = new ArrayList<>();
	protected static final long BUCKET = FluidConstants.BUCKET;
	protected static final long BOTTLE = FluidConstants.BOTTLE;

	public static DataProvider registerAll(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		GENERATORS.add(new CrushingRecipeGen(output, registries));
		GENERATORS.add(new MillingRecipeGen(output, registries));
		GENERATORS.add(new CuttingRecipeGen(output, registries));
		GENERATORS.add(new WashingRecipeGen(output, registries));
		GENERATORS.add(new PolishingRecipeGen(output, registries));
		GENERATORS.add(new DeployingRecipeGen(output, registries));
		GENERATORS.add(new MixingRecipeGen(output, registries));
		GENERATORS.add(new CompactingRecipeGen(output, registries));
		GENERATORS.add(new PressingRecipeGen(output, registries));
		GENERATORS.add(new FillingRecipeGen(output, registries));
		GENERATORS.add(new EmptyingRecipeGen(output, registries));
		GENERATORS.add(new HauntingRecipeGen(output, registries));
		GENERATORS.add(new ItemApplicationRecipeGen(output, registries));

		return new DataProvider() {

			@Override
			public String getName() {
				return "Create's Processing Recipes";
			}

			@Override
			public CompletableFuture<?> run(CachedOutput dc) {
				return CompletableFuture.allOf(GENERATORS.stream()
					.map(gen -> gen.run(dc))
					.toArray(CompletableFuture[]::new));
			}
		};
	}

	public ProcessingRecipeGen(FabricDataOutput generator, CompletableFuture<HolderLookup.Provider> registries) {
		super(generator, registries);
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	protected GeneratedRecipe create(String namespace,
		Supplier<ItemLike> singleIngredient, UnaryOperator<StandardProcessingRecipe.Builder<?>> transform) {
		StandardProcessingRecipe.Serializer<?> serializer = getSerializer();
		GeneratedRecipe generatedRecipe = c -> {
			ItemLike itemLike = singleIngredient.get();
			transform
				.apply(new StandardProcessingRecipe.Builder<>(serializer.factory(),
					ResourceLocation.fromNamespaceAndPath(namespace, RegisteredObjectsHelper.getKeyOrThrow(itemLike.asItem())
						.getPath())).withItemIngredients(Ingredient.of(itemLike)))
				.build(c);
		};
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	protected GeneratedRecipe create(Supplier<ItemLike> singleIngredient,
		UnaryOperator<StandardProcessingRecipe.Builder<?>> transform) {
		return create(Create.ID, singleIngredient, transform);
	}

	protected GeneratedRecipe createWithDeferredId(Supplier<ResourceLocation> name,
		UnaryOperator<StandardProcessingRecipe.Builder<?>> transform) {
		StandardProcessingRecipe.Serializer<?> serializer = getSerializer();
		GeneratedRecipe generatedRecipe =
			c -> transform.apply(new StandardProcessingRecipe.Builder<>(serializer.factory(), name.get()))
				.build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a new processing recipe, with recipe definitions provided by the
	 * function
	 */
	protected GeneratedRecipe create(ResourceLocation name,
		UnaryOperator<StandardProcessingRecipe.Builder<?>> transform) {
		return createWithDeferredId(() -> name, transform);
	}

	/**
	 * Create a new processing recipe, with recipe definitions provided by the
	 * function
	 */
	protected GeneratedRecipe create(String name,
		UnaryOperator<StandardProcessingRecipe.Builder<?>> transform) {
		return create(Create.asResource(name), transform);
	}

	protected abstract IRecipeTypeInfo getRecipeType();

	@SuppressWarnings("unchecked")
	protected StandardProcessingRecipe.Serializer<?> getSerializer() {
		return (StandardProcessingRecipe.Serializer<?>) getRecipeType().getSerializer();
	}

	protected GeneratedRecipe createItemApplication(String name,
		UnaryOperator<ItemApplicationRecipe.Builder<?>> transform) {
		return createItemApplication(Create.asResource(name), transform);
	}

	protected GeneratedRecipe createItemApplication(ResourceLocation name,
		UnaryOperator<ItemApplicationRecipe.Builder<?>> transform) {
		GeneratedRecipe generatedRecipe = c -> transform.apply(new ItemApplicationRecipe.Builder<ItemApplicationRecipe>(getItemApplicationFactory(), name)).build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	protected GeneratedRecipe createItemApplicationWithDeferredId(Supplier<ResourceLocation> name,
		UnaryOperator<ItemApplicationRecipe.Builder<?>> transform) {
		GeneratedRecipe generatedRecipe = c -> transform.apply(new ItemApplicationRecipe.Builder<ItemApplicationRecipe>(getItemApplicationFactory(), name.get())).build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	private ItemApplicationRecipe.Factory<ItemApplicationRecipe> getItemApplicationFactory() {
		if (getRecipeType() == AllRecipeTypes.DEPLOYING)
			return params -> new DeployerApplicationRecipe(params);
		return params -> new ManualApplicationRecipe(params);
	}

	protected Supplier<ResourceLocation> idWithSuffix(Supplier<ItemLike> item, String suffix) {
		return () -> {
			ResourceLocation registryName = RegisteredObjectsHelper.getKeyOrThrow(item.get()
					.asItem());
			return Create.asResource(registryName.getPath() + suffix);
		};
	}

}
