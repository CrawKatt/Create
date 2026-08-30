package com.simibubi.create.foundation.mixin;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.mixin.accessor.LootTableProviderAccessor;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

// Registrate 1.3.77 passes vanilla's loot providers to the superclass and never installs
// the providers returned by getTables(), so none of its per-entry loot callbacks run.
@Mixin(value = RegistrateLootTableProvider.class, remap = false)
public abstract class RegistrateLootTableProviderMixin {

	@Inject(method = "<init>", at = @At("RETURN"))
	private void create$installRegistrateLootProviders(AbstractRegistrate<?> parent, PackOutput output,
		CompletableFuture<HolderLookup.Provider> registries, CallbackInfo ci) {
		RegistrateLootTableProvider provider = (RegistrateLootTableProvider) (Object) this;
		((LootTableProviderAccessor) this).create$setSubProviders(provider.getTables((FabricDataOutput) output));
	}
}
