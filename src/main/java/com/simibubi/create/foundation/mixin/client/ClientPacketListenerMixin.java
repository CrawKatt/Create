package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomDataPacketHandlingBlockEntity;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@WrapWithCondition(
		method = "method_38542(Lnet/minecraft/network/protocol/game/ClientboundBlockEntityDataPacket;Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"
		)
	)
	private boolean create$skipAlreadyHandledUpdate(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider registries) {
		// Porting Lib already dispatched the custom packet handler at the method's HEAD.
		return !(blockEntity instanceof CustomDataPacketHandlingBlockEntity);
	}
}
