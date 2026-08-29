package com.simibubi.create.compat.jei;

import java.util.List;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;

import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;

/* From JEI's Potion item subtype interpreter */
public class PotionFluidSubtypeInterpreter implements ISubtypeInterpreter<IJeiFluidIngredient> {
	@Override
	public @Nullable Object getSubtypeData(IJeiFluidIngredient jeiIngredient, UidContext context) {
		FluidStack ingredient = CreateRecipeCategory.fromJei(jeiIngredient);
		if (ingredient.getComponentsPatch().isEmpty())
			return null;

		PotionContents contents = ingredient.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		BottleType bottleTypeEnum = ingredient.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
		ItemLike itemFromBottleType = PotionFluidHandler.itemFromBottleType(bottleTypeEnum);
		String potionTypeString = Potion.getName(contents.potion(), itemFromBottleType.asItem()
			.getDescriptionId() + ".effect.");
		String bottleType = bottleTypeEnum.name();

		StringBuilder stringBuilder = new StringBuilder(potionTypeString);
		List<MobEffectInstance> effects = contents.customEffects();

		stringBuilder.append(";")
				.append(bottleType);
		contents.potion().ifPresent(p -> {
			for (MobEffectInstance effect : p.value().getEffects())
				stringBuilder.append(";")
					.append(effect);
		});
		for (MobEffectInstance effect : effects)
			stringBuilder.append(";")
					.append(effect);
		return stringBuilder.toString();
	}

	@Override
	public String getLegacyStringSubtypeInfo(IJeiFluidIngredient ingredient, UidContext context) {
		return "";
	}
}
