package com.simibubi.create.infrastructure.command;

import java.util.Collections;
import java.util.function.Predicate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
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
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.player.Player;

public class AllCommands {
	@Environment(EnvType.CLIENT)
	private static LiteralCommandNode<FabricClientCommandSource> clientRoot;

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
		clientRoot = root.build();
		clientRoot.addChild(buildClientRedirect("u", util));
		dispatcher.getRoot().addChild(clientRoot);
		CommandNode<FabricClientCommandSource> shortcut = dispatcher.findNode(Collections.singleton("c"));
		if (shortcut != null)
			for (CommandNode<FabricClientCommandSource> child : clientRoot.getChildren())
				shortcut.addChild(child);
		else
			dispatcher.getRoot().addChild(buildClientRedirect("c", clientRoot));
	}

	@Environment(EnvType.CLIENT)
	public static void mergeClientCommandSuggestions(CommandDispatcher<SharedSuggestionProvider> dispatcher,
		FabricClientCommandSource source) {
		mergeClientCommandSuggestions(clientRoot, dispatcher, source);
	}

	public static <S> void mergeClientCommandSuggestions(CommandNode<S> createRoot,
		CommandDispatcher<SharedSuggestionProvider> dispatcher, S source) {
		for (String alias : new String[] { "create", "c" }) {
			var root = LiteralArgumentBuilder.<SharedSuggestionProvider>literal(alias).build();
			copyClientSuggestions(createRoot, root, source);
			dispatcher.getRoot().addChild(root);
		}
	}

	private static <S> void copyClientSuggestions(CommandNode<S> origin,
		CommandNode<SharedSuggestionProvider> target, S source) {
		// ponytail: Create's local tree uses expanded aliases and default argument suggestions;
		// extend this copier if local commands gain redirects or custom suggestion providers.
		for (CommandNode<S> child : origin.getChildren()) {
			if (!child.canUse(source))
				continue;
			ArgumentBuilder<SharedSuggestionProvider, ?> builder;
			if (child instanceof ArgumentCommandNode<S, ?> argument)
				builder = RequiredArgumentBuilder.argument(child.getName(), argument.getType());
			else
				builder = LiteralArgumentBuilder.literal(child.getName());
			if (child.getCommand() != null)
				builder.executes(context -> 0);
			var copy = builder.build();
			copyClientSuggestions(child, copy, source);
			// Fabric 2.2.28 adds empty nodes before recursion, losing children on shared roots.
			target.addChild(copy);
		}
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
