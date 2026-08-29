package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.content.equipment.bell.BasicParticleData;
import com.simibubi.create.content.fluids.particle.FluidParticleData;
import com.simibubi.create.content.kinetics.base.RotationIndicatorParticleData;
import com.simibubi.create.content.kinetics.fan.AirFlowParticleData;
import com.simibubi.create.content.kinetics.steamEngine.SteamJetParticleData;
import com.simibubi.create.content.trains.CubeParticleData;
import com.simibubi.create.foundation.particle.AirParticleData;
import com.simibubi.create.foundation.particle.ICustomParticleData;

import net.createmod.catnip.lang.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import io.github.fabricators_of_create.porting_lib.util.DeferredRegister;

public enum AllParticleTypes {
	ROTATION_INDICATOR(RotationIndicatorParticleData::new),
	AIR_FLOW(AirFlowParticleData::new),
	AIR(AirParticleData::new),
	STEAM_JET(SteamJetParticleData::new),
	CUBE(CubeParticleData::new),
	FLUID_PARTICLE(FluidParticleData::new),
	BASIN_FLUID(FluidParticleData::new),
	FLUID_DRIP(FluidParticleData::new),
	WIFI(BasicParticleData.WiFiData::new),
	SOUL(BasicParticleData.SoulData::new),
	SOUL_BASE(BasicParticleData.SoulBaseData::new),
	SOUL_PERIMETER(BasicParticleData.SoulPerimeterData::new),
	SOUL_EXPANDING_PERIMETER(BasicParticleData.SoulExpandingPerimeterData::new);

	private final ParticleEntry<?> entry;

	<D extends ParticleOptions> AllParticleTypes(Supplier<? extends ICustomParticleData<D>> typeFactory) {
		String name = Lang.asId(name());
		entry = new ParticleEntry<>(name, typeFactory);
	}

	public static void register() {
		ParticleEntry.REGISTER.register();
	}

	@Environment(EnvType.CLIENT)
	public static void registerFactories() {
		ParticleEngine particles = Minecraft.getInstance().particleEngine;
		for (AllParticleTypes particle : values())
			particle.entry.registerFactory(particles);
	}

	public ParticleType<?> get() {
		return entry.object;
	}

	public String parameter() {
		return entry.name;
	}

	private static class ParticleEntry<D extends ParticleOptions> {
		private static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Create.ID);

		private final String name;
		private final Supplier<? extends ICustomParticleData<D>> typeFactory;
		private final ParticleType<D> object;

		public ParticleEntry(String name, Supplier<? extends ICustomParticleData<D>> typeFactory) {
			this.name = name;
			this.typeFactory = typeFactory;

			object = this.typeFactory.get().createType();
			REGISTER.register(name, () -> object);
		}

		@Environment(EnvType.CLIENT)
		public void registerFactory(ParticleEngine particles) {
			typeFactory.get()
				.register(object, particles);
		}

	}
}
