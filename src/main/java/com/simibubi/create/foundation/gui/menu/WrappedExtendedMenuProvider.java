package com.simibubi.create.foundation.gui.menu;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class WrappedExtendedMenuProvider implements ExtendedScreenHandlerFactory<RegistryFriendlyByteBuf> {

	private final MenuProvider provider;
	private final BiConsumer<RegistryFriendlyByteBuf, ServerPlayer> dataWriter;

	private WrappedExtendedMenuProvider(MenuProvider provider, BiConsumer<RegistryFriendlyByteBuf, ServerPlayer> dataWriter) {
		this.provider = provider;
		this.dataWriter = dataWriter;
	}

	public static WrappedExtendedMenuProvider of(MenuProvider provider, Consumer<RegistryFriendlyByteBuf> dataWriter) {
		return new WrappedExtendedMenuProvider(provider, (buf, $player) -> dataWriter.accept(buf));
	}

	public static WrappedExtendedMenuProvider of(MenuProvider provider,
		BiConsumer<RegistryFriendlyByteBuf, ServerPlayer> dataWriter) {
		return new WrappedExtendedMenuProvider(provider, dataWriter);
	}

	@Override
	public RegistryFriendlyByteBuf getScreenOpeningData(ServerPlayer player) {
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.serverLevel()
			.registryAccess());
		dataWriter.accept(buf, player);
		return buf;
	}

	@Override
	public Component getDisplayName() {
		return provider.getDisplayName();
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return provider.createMenu(id, inventory, player);
	}
}
