package com.simibubi.create.content.equipment.bell;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BasicParticleData implements ParticleOptions, ICustomParticleDataWithSprite<BasicParticleData> {

	public BasicParticleData() {
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, BasicParticleData> getStreamCodec() {
		return StreamCodec.unit(this);
	}

	@Override
	public MapCodec<BasicParticleData> getCodec(ParticleType<BasicParticleData> type) {
		return MapCodec.unit(this);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MetaFactory<BasicParticleData> getMetaFactory() {
		throw new UnsupportedOperationException("Basic particle factories are registered by CreateClient");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void register(ParticleType<BasicParticleData> type, ParticleEngine particles) {
		CreateClient.registerBasicParticleFactory(type, this);
	}

	public static class WiFiData extends BasicParticleData {
		@Override
		public ParticleType<?> getType() {
			return AllParticleTypes.WIFI.get();
		}
	}

	public static class SoulData extends BasicParticleData {
		@Override
		public ParticleType<?> getType() {
			return AllParticleTypes.SOUL.get();
		}
	}

	public static class SoulBaseData extends BasicParticleData {
		@Override
		public ParticleType<?> getType() {
			return AllParticleTypes.SOUL_BASE.get();
		}
	}

	public static class SoulPerimeterData extends BasicParticleData {
		@Override
		public ParticleType<?> getType() {
			return AllParticleTypes.SOUL_PERIMETER.get();
		}
	}

	public static class SoulExpandingPerimeterData extends SoulPerimeterData {
		@Override
		public ParticleType<?> getType() {
			return AllParticleTypes.SOUL_EXPANDING_PERIMETER.get();
		}
	}
}
