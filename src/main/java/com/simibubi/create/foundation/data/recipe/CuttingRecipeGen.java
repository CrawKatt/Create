package com.simibubi.create.foundation.data.recipe;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.GeneratedRecipe;

import net.minecraft.core.HolderLookup;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

@SuppressWarnings("unused")
public class CuttingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe
		ANDESITE_ALLOY = create(I::andesiteAlloy, b -> b.duration(200)
			.output(AllBlocks.SHAFT.get(), 6)),

		BAMBOO_PLANKS = create(() -> Blocks.BAMBOO_PLANKS, b -> b.duration(20)
			.output(Blocks.BAMBOO_MOSAIC, 1)),

		/*
		 * Mod compat
		 */

		// Ars Nouveau (all logs yield the same plank) (blue is covered by RuntimeDataGenerator to handle the planks into other recipes)
		ARS_N = cuttingCompat(Mods.ARS_N, "purple_archwood", "green_archwood", "red_archwood"),
		ARS_E_1 = stripAndMakePlanksDiffPlanksModId(Mods.ARS_E, null, "stripped_yellow_archwood_log", Mods.ARS_N, "archwood_planks"),
		ARS_E_2 = stripAndMakePlanksDiffPlanksModId(Mods.ARS_E, null, "stripped_yellow_archwood", Mods.ARS_N, "archwood_planks"),

		// Regions Unexplored
		RU_1 = stripAndMakePlanks(Mods.RU, "brimwood_log_magma", "stripped_brimwood_log", null),
		RU_2 = stripAndMakePlanks(Mods.RU, "ashen_log", "stripped_dead_log", null),
		RU_3 = stripAndMakePlanks(Mods.RU, "ashen_wood", "stripped_dead_wood", null),
		RU_4 = stripOnlyDiffModId(Mods.RU, "silver_birch_log", Mods.MC, "stripped_birch_log"),
		RU_5 = stripOnlyDiffModId(Mods.RU, "silver_birch_wood", Mods.MC, "stripped_birch_wood"),

		// Autumnity
		AUTUM_1 = stripAndMakePlanks(Mods.AUTUM, null, "sappy_maple_log", "maple_planks"),
		AUTUM_2 = stripAndMakePlanks(Mods.AUTUM, null, "sappy_maple_wood", "maple_planks"),

		// Endergetic Expansion
		ENDERGETIC_1 = stripAndMakePlanks(Mods.ENDER, "glowing_poise_stem", "stripped_poise_stem", null),
		ENDERGETIC_2 = stripAndMakePlanks(Mods.ENDER, "glowing_poise_wood", "stripped_poise_wood", null),

		// Jaden's Nether Expansion
		JNE_1 = stripAndMakePlanks(Mods.JNE, "cerebrage_claret_stem", "stripped_claret_stem", null),
		JNE_2 = stripAndMakePlanks(Mods.JNE, "cerebrage_claret_hyphae", "stripped_claret_hyphae", null),

		// Atmospheric
		ATM_1 = stripAndMakePlanks(Mods.ATM, "watchful_aspen_log", "aspen_log", null),
		ATM_2 = stripAndMakePlanks(Mods.ATM, "watchful_aspen_wood", "aspen_wood", null),
		ATM_3 = stripAndMakePlanks(Mods.ATM, "crustose_log", "aspen_log", null),
		ATM_4 = stripAndMakePlanks(Mods.ATM, "crustose_wood", "aspen_wood", null),

		// IE
		IE_WIRES = ieWires("copper", "electrum", "aluminum", "steel", "lead"),

		// Oh The Biomes We've Gone
		BYG_1 = stripAndMakePlanksDiffPlanksModId(Mods.BWG, null, "stripped_palo_verde_log", Mods.MC, "birch_planks"),
		BYG_2 = stripAndMakePlanksDiffPlanksModId(Mods.BWG, null, "stripped_palo_verde_wood", Mods.MC, "birch_planks")
		;

	GeneratedRecipe stripAndMakePlanks(Block wood, Block stripped, Block planks) {
		return stripAndMakePlanks(wood, stripped, planks, 6);
	}

	GeneratedRecipe stripAndMakePlanks(Block wood, Block stripped, Block planks, int planksAmount) {
		create(() -> wood, b -> b.duration(50)
			.output(stripped));
		return create(() -> stripped, b -> b.duration(50)
			.output(planks, planksAmount));
	}

	GeneratedRecipe cuttingCompat(Mods mod, String... woodtypes) {
		for (String type : woodtypes) {
			String planks = type + "_planks";

			if (mod == Mods.ARS_N && type.contains("archwood"))
				planks = "archwood_planks";

			String strippedPre = mod.strippedIsSuffix ? "" : "stripped_";
			String strippedPost = mod.strippedIsSuffix ? "_stripped" : "";
			stripAndMakePlanks(mod, type + "_log", strippedPre + type + "_log" + strippedPost, planks);

			String wood = type + (mod.omitWoodSuffix ? "" : "_wood");
			stripAndMakePlanks(mod, wood, strippedPre + wood + strippedPost, planks);
		}
		return null;
	}

	GeneratedRecipe cuttingCompatLogOnly(Mods mod, String... woodtypes) {
		for (String type : woodtypes) {
			String planks = type + "_planks";
			String strippedPre = mod.strippedIsSuffix ? "" : "stripped_";
			String strippedPost = mod.strippedIsSuffix ? "_stripped" : "";
			stripAndMakePlanks(mod, type + "_log", strippedPre + type + "_log" + strippedPost, planks);
		}
		return null;
	}

	GeneratedRecipe stripOnlyDiffModId(Mods mod1, String wood, Mods mod2, String stripped) {
		create("compat/" + mod1.getId() + "/" + wood, b -> b.duration(50)
				.require(mod1, wood)
				.output(1, mod2, stripped, 1)
				.whenModLoaded(mod1.getId()));
		return null;
	}

	GeneratedRecipe stripAndMakePlanksDiffPlanksModId(Mods mod1, String log, String stripped, Mods mod2, String planks) {
		if (log != null)
			create("compat/" + mod1.getId() + "/" + log, b -> b.duration(50)
				.require(mod1, log)
				.output(1, mod1, stripped, 1)
				.whenModLoaded(mod1.getId()));
		if (planks != null) // Shouldn't be needed as stripAndMakePlanks can already do what this method does if planks is null
			create("compat/" + mod1.getId() + "/" + stripped, b -> b.duration(50)
				.require(mod1, stripped)
				.output(1, mod2, planks, 6)
				.whenModLoaded(mod1.getId()));
		return null;
	}

	GeneratedRecipe stripAndMakePlanks(Mods mod, String wood, String stripped, String planks) {
		if (wood != null)
			create("compat/" + mod.getId() + "/" + wood, b -> b.duration(50)
				.require(mod, wood)
				.output(1, mod, stripped, 1)
				.whenModLoaded(mod.getId()));
		if (planks != null)
			if (!Objects.equals(mod.getId(), Mods.VH.getId())) {
				create("compat/" + mod.getId() + "/" + stripped, b -> b.duration(50)
						.require(mod, stripped)
						.output(1, mod, planks, 6)
						.whenModLoaded(mod.getId()));
			} else {
				create("compat/" + mod.getId() + "/" + stripped, b -> b.duration(50)
						.require(mod, stripped)
						.output(1, mod, planks, 4)
						.whenModLoaded(mod.getId()));
			}
		return null;
	}

	public CuttingRecipeGen(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	GeneratedRecipe ieWires(String... metals) {
		for (String metal : metals)
			create(Mods.IE.recipeId("wire_" + metal), b -> b.duration(50)
				.require(AllTags.commonItemTag("plates/" + metal))
				.output(1, Mods.IE, "wire_" + metal, 2)
				.whenModLoaded(Mods.IE.getId()));
		return null;
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.CUTTING;
	}
}
