package com.simibubi.create.foundation.particle;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

import org.jetbrains.annotations.NotNull;

public interface ICustomParticleDataWithSprite<T extends ParticleOptions> extends ICustomParticleData<T> {

	default ParticleType<T> createType() {
		return new ParticleType<>(false) {

			@Override
			public @NotNull MapCodec<T> codec() {
				return ICustomParticleDataWithSprite.this.getCodec(this);
			}

			@Override
			public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
				return ICustomParticleDataWithSprite.this.getStreamCodec();
			}
		};
	}

	@Override
	@Environment(EnvType.CLIENT)
	default ParticleProvider<T> getFactory() {
		throw new IllegalAccessError("This particle type uses a metaFactory!");
	}

	@Environment(EnvType.CLIENT)
	public MetaFactory<T> getMetaFactory();

	@Override
	@Environment(EnvType.CLIENT)
	public default void register(ParticleType<T> type, ParticleEngine particles) {
		ParticleFactoryRegistry.getInstance().register(type, getMetaFactory()::create);
	}

	@Environment(EnvType.CLIENT)
	interface MetaFactory<T extends ParticleOptions> {
		ParticleProvider<T> create(FabricSpriteProvider spriteProvider);
	}

}
