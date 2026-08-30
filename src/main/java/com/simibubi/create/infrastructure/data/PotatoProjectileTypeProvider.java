package com.simibubi.create.infrastructure.data;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.api.registry.CreateRegistries;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;

public class PotatoProjectileTypeProvider extends FabricDynamicRegistryProvider {
	public PotatoProjectileTypeProvider(FabricDataOutput output,
		CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		entries.addAll(registries.lookupOrThrow(CreateRegistries.POTATO_PROJECTILE_TYPE));
	}

	@Override
	public String getName() {
		return "Create's Potato Projectile Types";
	}
}
