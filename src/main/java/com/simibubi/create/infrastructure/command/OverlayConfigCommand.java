package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.equipment.goggles.GoggleConfigScreen;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.gui.ScreenOpener;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class OverlayConfigCommand {
	public static ArgumentBuilder<FabricClientCommandSource, ?> register() {
		return ClientCommandManager.literal("overlay")
			.then(ClientCommandManager.literal("reset")
				.executes(ctx -> {
					AllConfigs.client().overlayOffsetX.set(0);
					AllConfigs.client().overlayOffsetY.set(0);

					ctx.getSource().sendFeedback(Component.literal("Create Goggle Overlay has been reset to default position"));
					return Command.SINGLE_SUCCESS;
				})
			)
			.executes(ctx -> {
				ScreenOpener.open(new GoggleConfigScreen());
				return Command.SINGLE_SUCCESS;
			});

	}
}
