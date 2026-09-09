package com.simibubi.create.infrastructure.command;

import java.util.Collections;
import java.util.function.Predicate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.createmod.catnip.command.CatnipCommands;
import net.createmod.catnip.platform.CatnipServices;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class AllCommands {

	public static final Predicate<CommandSourceStack> SOURCE_IS_PLAYER = cs -> cs.getEntity() instanceof Player;

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

		LiteralCommandNode<CommandSourceStack> util = buildUtilityCommands();

		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("create")
				.requires(cs -> cs.hasPermission(0))
				// general purpose
				.then(DumpRailwaysCommand.register())
				//.then(FixLightingCommand.register()) fabric: Forge only command
				.then(DebugInfoCommand.register())
				.then(HighlightCommand.register())
				.then(PassengerCommand.register())
				.then(CouplingCommand.register())
				.then(CloneCommand.register())
				.then(TrainCommand.register())
				.then(GlueCommand.register())


				// utility
				.then(util);

		if (CatnipServices.PLATFORM.isDevelopmentEnvironment() && CatnipServices.PLATFORM.getEnv().isClient())
			root.then(CreateTestCommand.register());

		LiteralCommandNode<CommandSourceStack> createRoot = dispatcher.register(root);

		createRoot.addChild(CatnipCommands.buildRedirect("u", util));

		//add all of Create's commands to /c if it already exists, otherwise create the shortcut
		CatnipCommands.createOrAddToShortcut(dispatcher, "c", createRoot);
	}

	private static LiteralCommandNode<CommandSourceStack> buildUtilityCommands() {

		return Commands.literal("util")
				.then(ReplaceInCommandBlocksCommand.register())
				//.then(DebugValueCommand.register())
				//.then(KillTPSCommand.register())
				.build();

	}

	@Environment(EnvType.CLIENT)
	public static void registerClient(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		LiteralCommandNode<FabricClientCommandSource> util = buildClientUtilityCommands();
		LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("create")
			.then(ToggleDebugCommand.register())
			.then(FabulousWarningCommand.register())
			.then(OverlayConfigCommand.register())
			.then(util);
		LiteralCommandNode<FabricClientCommandSource> createRoot = dispatcher.register(root);
		createRoot.addChild(buildClientRedirect("u", util));
		CommandNode<FabricClientCommandSource> shortcut = dispatcher.findNode(Collections.singleton("c"));
		if (shortcut != null)
			for (CommandNode<FabricClientCommandSource> child : createRoot.getChildren())
				shortcut.addChild(child);
		else
			dispatcher.getRoot().addChild(buildClientRedirect("c", createRoot));
	}

	@Environment(EnvType.CLIENT)
	private static LiteralCommandNode<FabricClientCommandSource> buildClientRedirect(String alias, LiteralCommandNode<FabricClientCommandSource> destination) {
		LiteralArgumentBuilder<FabricClientCommandSource> builder = LiteralArgumentBuilder.<FabricClientCommandSource>literal(alias)
			.requires(destination.getRequirement())
			.forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
			.executes(destination.getCommand());
		for (CommandNode<FabricClientCommandSource> child : destination.getChildren())
			builder.then(child);
		return builder.build();
	}

	@Environment(EnvType.CLIENT)
	private static LiteralCommandNode<FabricClientCommandSource> buildClientUtilityCommands() {
		return ClientCommandManager.literal("util")
			.then(ClearBufferCacheCommand.register())
			.then(CameraDistanceCommand.register())
			.then(CameraAngleCommand.register())
			.build();
	}
}
