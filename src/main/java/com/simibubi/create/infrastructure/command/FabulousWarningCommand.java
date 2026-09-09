package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class FabulousWarningCommand {
	public static ArgumentBuilder<FabricClientCommandSource, ?> register() {
		return ClientCommandManager.literal("dismissFabulousWarning")
			.executes(ctx -> {
				AllConfigs.client().ignoreFabulousWarning.set(true);
				ctx.getSource().sendFeedback(Component.literal("Disabled Fabulous graphics warning"));
				return Command.SINGLE_SUCCESS;
			});
	}
}
