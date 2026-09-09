package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.kinetics.KineticDebugger;

import net.minecraft.ChatFormatting;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public class ToggleDebugCommand {
	public static ArgumentBuilder<FabricClientCommandSource, ?> register() {
		return ClientCommandManager.literal("rainbowDebug")
			.then(ClientCommandManager.argument("status", BoolArgumentType.bool())
				.executes(ctx -> {
					KineticDebugger.rainbowDebug = BoolArgumentType.getBool(ctx, "status");
					Component text = boolToText(KineticDebugger.rainbowDebug)
						.append(Component.literal(" Rainbow Debug Utility").withStyle(ChatFormatting.WHITE));

					ctx.getSource().sendFeedback(text);
					return Command.SINGLE_SUCCESS;
				})
			)
			.executes(ctx -> {
				Component text = Component.literal("Rainbow Debug Utility is currently: ")
					.append(boolToText(KineticDebugger.rainbowDebug));

				ctx.getSource().sendFeedback(text);
				return Command.SINGLE_SUCCESS;
			});
	}

	private static MutableComponent boolToText(boolean b) {
		if (b) {
			return Component.literal("enabled").withStyle(ChatFormatting.GREEN);
		} else {
			return Component.literal("disabled").withStyle(ChatFormatting.RED);
		}
	}
}
