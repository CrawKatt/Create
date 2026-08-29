package com.simibubi.create.foundation.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;

import net.minecraft.core.HolderLookup;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;

import io.github.fabricators_of_create.porting_lib.tags.Tags;

public abstract class CreateRecipeProvider extends FabricRecipeProvider {

	protected final List<GeneratedRecipe> all = new ArrayList<>();

	public CreateRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	public void buildRecipes(@NotNull RecipeOutput pRecipeOutput) {
		all.forEach(c -> c.register(pRecipeOutput));
		Create.LOGGER.info("{} registered {} recipe{}", getName(), all.size(), all.size() == 1 ? "" : "s");
	}

	protected GeneratedRecipe register(GeneratedRecipe recipe) {
		all.add(recipe);
		return recipe;
	}

	@FunctionalInterface
	public interface GeneratedRecipe {
		void register(RecipeOutput output);
	}

	protected static class Marker {
	}

	protected static class I {

		static TagKey<Item> redstone() {
			return Tags.Items.DUSTS_REDSTONE;
		}

		static TagKey<Item> planks() {
			return ItemTags.PLANKS;
		}

		static TagKey<Item> woodSlab() {
			return ItemTags.WOODEN_SLABS;
		}

		static TagKey<Item> gold() {
			return Tags.Items.INGOTS_GOLD;
		}

		static TagKey<Item> goldSheet() {
			return AllTags.commonItemTag("plates/gold");
		}

		static TagKey<Item> stone() {
			return Tags.Items.STONES;
		}

		static ItemLike andesiteAlloy() {
			return AllItems.ANDESITE_ALLOY.get();
		}

		static ItemLike shaft() {
			return AllBlocks.SHAFT.get();
		}

		static ItemLike cog() {
			return AllBlocks.COGWHEEL.get();
		}

		static ItemLike largeCog() {
			return AllBlocks.LARGE_COGWHEEL.get();
		}

		static ItemLike andesiteCasing() {
			return AllBlocks.ANDESITE_CASING.get();
		}

		static ItemLike vault() {
			return AllBlocks.ITEM_VAULT.get();
		}

		static ItemLike stockLink() {
			return AllBlocks.STOCK_LINK.get();
		}

		static TagKey<Item> brass() {
			return AllTags.commonItemTag("ingots/brass");
		}

		static TagKey<Item> brassSheet() {
			return AllTags.commonItemTag("plates/brass");
		}

		static TagKey<Item> iron() {
			return Tags.Items.INGOTS_IRON;
		}

		static TagKey<Item> ironNugget() {
			return Tags.Items.NUGGETS_IRON;
		}

		static TagKey<Item> zinc() {
			return AllTags.commonItemTag("ingots/zinc");
		}

		static TagKey<Item> ironSheet() {
			return AllTags.commonItemTag("plates/iron");
		}

		static TagKey<Item> sturdySheet() {
			return AllTags.commonItemTag("plates/obsidian");
		}

		static ItemLike brassCasing() {
			return AllBlocks.BRASS_CASING.get();
		}

		static ItemLike cardboard() {
			return AllItems.CARDBOARD.get();
		}

		static ItemLike railwayCasing() {
			return AllBlocks.RAILWAY_CASING.get();
		}

		static ItemLike electronTube() {
			return AllItems.ELECTRON_TUBE.get();
		}

		static ItemLike precisionMechanism() {
			return AllItems.PRECISION_MECHANISM.get();
		}

		static TagKey<Item> brassBlock() {
			return AllTags.commonItemTag("storage_blocks/brass");
		}

		static TagKey<Item> zincBlock() {
			return AllTags.commonItemTag("storage_blocks/zinc");
		}

		static TagKey<Item> wheatFlour() {
			return AllTags.commonItemTag("flours/wheat");
		}

		static TagKey<Item> copper() {
			return Tags.Items.INGOTS_COPPER;
		}

		static TagKey<Item> copperNugget() {
			return AllTags.commonItemTag("nuggets/copper");
		}

		static TagKey<Item> copperBlock() {
			return Tags.Items.STORAGE_BLOCKS_COPPER;
		}

		static TagKey<Item> copperSheet() {
			return AllTags.commonItemTag("plates/copper");
		}

		static TagKey<Item> brassNugget() {
			return AllTags.commonItemTag("nuggets/brass");
		}

		static TagKey<Item> zincNugget() {
			return AllTags.commonItemTag("nuggets/zinc");
		}

		static ItemLike copperCasing() {
			return AllBlocks.COPPER_CASING.get();
		}

		static ItemLike refinedRadiance() {
			return AllItems.REFINED_RADIANCE.get();
		}

		static ItemLike shadowSteel() {
			return AllItems.SHADOW_STEEL.get();
		}

		static Ingredient netherite() {
			return Ingredient.of(Tags.Items.INGOTS_NETHERITE);
		}

	}
}
