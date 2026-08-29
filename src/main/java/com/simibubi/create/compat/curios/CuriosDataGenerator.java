package com.simibubi.create.compat.curios;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.Create;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;

/**
 * fabric: Curios is not available, so no curios slot data is generated.
 */
public class CuriosDataGenerator implements DataProvider {
	public CuriosDataGenerator(PackOutput output, CompletableFuture<Provider> registries, ExistingFileHelper fileHelper) {}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public String getName() {
		return Create.ID + " curios";
	}
}
