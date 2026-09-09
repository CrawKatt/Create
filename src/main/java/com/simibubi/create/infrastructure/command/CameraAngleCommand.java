package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService.Mode;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.commands.arguments.EntityArgument;

import io.github.fabricators_of_create.porting_lib.command.EnumArgument;

@Environment(EnvType.CLIENT)
public class CameraAngleCommand {
	public static ArgumentBuilder<FabricClientCommandSource, ?> register() {
		return ClientCommandManager.literal("angle")
			.requires(cs -> cs.getPlayer().hasPermissions(2))
			.then(ClientCommandManager.argument("players", EntityArgument.players())
				.then(ClientCommandManager.literal("yaw")
					.then(ClientCommandManager.argument("degrees", FloatArgumentType.floatArg())
						.executes(ctx -> {
							float angleTarget = FloatArgumentType.getFloat(ctx, "degrees");
							CameraAngleAnimationService.setYawTarget(angleTarget);

							return Command.SINGLE_SUCCESS;
						})
					)
				).then(ClientCommandManager.literal("pitch")
					.then(ClientCommandManager.argument("degrees", FloatArgumentType.floatArg())
						.executes(ctx -> {
							float angleTarget = FloatArgumentType.getFloat(ctx, "degrees");
							CameraAngleAnimationService.setPitchTarget(angleTarget);

							return Command.SINGLE_SUCCESS;
						})
					)
				).then(ClientCommandManager.literal("mode")
					.then(ClientCommandManager.argument("mode", EnumArgument.enumArgument(Mode.class))
						.executes(ctx -> {
							Mode mode = ctx.getArgument("mode", Mode.class);

							CameraAngleAnimationService.setAnimationMode(mode);

							return Command.SINGLE_SUCCESS;
						})
						.then(ClientCommandManager.argument("speed", FloatArgumentType.floatArg(0))
							.executes(ctx -> {
								Mode mode = ctx.getArgument("mode", Mode.class);
								float speed = FloatArgumentType.getFloat(ctx, "speed");

								CameraAngleAnimationService.setAnimationMode(mode);
								CameraAngleAnimationService.setAnimationSpeed(speed);

								return Command.SINGLE_SUCCESS;
							})
						))
				)
			);
	}
}
