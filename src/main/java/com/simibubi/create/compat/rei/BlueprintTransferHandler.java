package com.simibubi.create.compat.rei;

import com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket;
import com.simibubi.create.content.equipment.blueprint.BlueprintScreen;

import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.display.Display;

import net.createmod.catnip.platform.CatnipServices;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BlueprintTransferHandler implements TransferHandler {
	@Override
	public Result handle(Context context) {
		if (context.getContainerScreen() instanceof BlueprintScreen blueprint) {
			Display d = context.getDisplay();
			if (d.getDisplayLocation().isPresent()) {
				if (d.getCategoryIdentifier().toString().equals("minecraft:plugins/crafting")) {
					if (context.isActuallyCrafting()) {
						CatnipServices.NETWORK.sendToServer(new BlueprintAssignCompleteRecipePacket(d.getDisplayLocation().get()));
						context.getMinecraft().setScreen(blueprint);
					}
					return Result.createSuccessful();
				}
			}

		}
		return Result.createNotApplicable();
	}
}
