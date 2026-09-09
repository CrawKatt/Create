package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.trains.CameraDistanceModifier;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;

@Environment(EnvType.CLIENT)
public class CameraDistanceCommand {
	public static ArgumentBuilder<FabricClientCommandSource, ?> register() {
		return ClientCommandManager.literal("camera")
			.then(ClientCommandManager.literal("reset")
				.executes(ctx -> {
					CameraDistanceModifier.zoomOut(1);

					return Command.SINGLE_SUCCESS;
				})
			).then(ClientCommandManager.argument("multiplier", FloatArgumentType.floatArg(1))
				.executes(ctx -> {
					float multiplier = FloatArgumentType.getFloat(ctx, "multiplier");
					CameraDistanceModifier.zoomOut(multiplier);

					return Command.SINGLE_SUCCESS;
				})
			);
	}
}
