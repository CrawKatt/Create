package com.simibubi.create.content.logistics.packagePort;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget.ChainConveyorFrogportTarget;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget.TrainStationFrogportTarget;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class AllPackagePortTargetTypes {
	public static final Holder<PackagePortTargetType> CHAIN_CONVEYOR = register("chain_conveyor", ChainConveyorFrogportTarget.Type::new);
	public static final Holder<PackagePortTargetType> TRAIN_STATION = register("train_station", TrainStationFrogportTarget.Type::new);

	private static Holder<PackagePortTargetType> register(String name, Supplier<? extends PackagePortTargetType> factory) {
		return Registry.registerForHolder(CreateBuiltInRegistries.PACKAGE_PORT_TARGET_TYPE, Create.asResource(name), factory.get());
	}

	@Internal
	public static void register() {
	}
}
