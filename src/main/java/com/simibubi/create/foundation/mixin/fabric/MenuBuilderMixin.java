package com.simibubi.create.foundation.mixin.fabric;

import com.simibubi.create.infrastructure.fabric.IdentityBufCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


// registrate 1.3.77 constructs ExtendedScreenHandlerType with a null packet codec,
// which modern fabric-screen-handler-api-v1 rejects; supply an identity codec so the
// raw opening data written by WrappedExtendedMenuProvider reaches the menu untouched.
@Mixin(targets = "com.tterrag.registrate.builders.MenuBuilder")
public abstract class MenuBuilderMixin {

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Redirect(
		method = "createEntry",
		at = @At(
			value = "NEW",
			target = "(Lnet/fabricmc/fabric/api/screenhandler/v1/ExtendedScreenHandlerType$ExtendedFactory;Lnet/minecraft/network/codec/StreamCodec;)Lnet/fabricmc/fabric/api/screenhandler/v1/ExtendedScreenHandlerType;"
		)
	)
	private ExtendedScreenHandlerType create$withCodec(
			ExtendedScreenHandlerType.ExtendedFactory factory, StreamCodec codec) {
		return new ExtendedScreenHandlerType<>((id, inventory, data) -> {
			try {
				return factory.create(id, inventory, data);
			} finally {
				((RegistryFriendlyByteBuf) data).release();
			}
		}, IdentityBufCodec.INSTANCE);
	}
}
