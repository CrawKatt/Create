package com.simibubi.create.compat.rei.category;

import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;



public class PolishingCategory extends CreateRecipeCategory<SandPaperPolishingRecipe> {

	private final ItemStack renderedSandpaper;

	public PolishingCategory(Info<SandPaperPolishingRecipe> info) {
		super(info);
		renderedSandpaper = AllItems.SAND_PAPER.asStack();
	}

	@Override
	public void addWidgets(CreateDisplay<SandPaperPolishingRecipe> display, List<Widget> ingredients, Point origin) {
		ingredients.add(basicSlot(origin.x + 27, origin.y + 29)
				.markInput()
				.entries(display.getInputEntries().get(0)));
		display.getRecipe().getRollableResults().stream().limit(1).forEach(result -> {
			Slot outputSlot = basicSlot(origin.x + 132, origin.y + 29)
					.markOutput()
					.entries(EntryIngredients.of(result.getStack()));
			ingredients.add(outputSlot);
			addStochasticTooltip(outputSlot, result);
		});
	}

	@Override
	public void draw(SandPaperPolishingRecipe recipe, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SLOT.render(graphics, 26, 28);
		getRenderedSlot(recipe, 0).render(graphics, 131, 28);
		AllGuiTextures.JEI_SHADOW.render(graphics, 61, 21);
		AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 32);

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		ItemStack[] matchingStacks = ingredients.get(0)
			.getItems();
		if (matchingStacks.length == 0)
			return;

		GuiGameElement.of(renderedSandpaper)
				.<GuiGameElement.GuiRenderBuilder>at(getDisplayWidth(null) / 2 - 16, 0, 0)
				.scale(2)
				.render(graphics);
	}

}
