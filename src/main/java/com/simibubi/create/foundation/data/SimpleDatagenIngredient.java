package com.simibubi.create.foundation.data;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.recipe.Mods;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public class SimpleDatagenIngredient implements CustomIngredient {

	/*
	"ingredients": [
		{
			"item": "mod:compat_item"
		}
	]
	 */

	public static final MapCodec<SimpleDatagenIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("item").forGetter(i -> i.mod.asResource(i.id))
	).apply(instance, SimpleDatagenIngredient::fromLocation));

	private static final StreamCodec<RegistryFriendlyByteBuf, SimpleDatagenIngredient> PACKET_CODEC = new StreamCodec<>() {
		@Override
		public SimpleDatagenIngredient decode(RegistryFriendlyByteBuf buf) {
			return fromLocation(ResourceLocation.STREAM_CODEC.decode(buf));
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, SimpleDatagenIngredient ingredient) {
			ResourceLocation.STREAM_CODEC.encode(buf, ingredient.mod.asResource(ingredient.id));
		}
	};

	public static final CustomIngredientSerializer<SimpleDatagenIngredient> SERIALIZER =
			new CustomIngredientSerializer<>() {
				@Override
				public ResourceLocation getIdentifier() {
					return ID;
				}

				@Override
				public MapCodec<SimpleDatagenIngredient> getCodec(boolean allowEmpty) {
					return CODEC;
				}

				@Override
				public StreamCodec<RegistryFriendlyByteBuf, SimpleDatagenIngredient> getPacketCodec() {
					return PACKET_CODEC;
				}
			};

	private static final ResourceLocation ID = Create.asResource("simple_datagen");

	private static SimpleDatagenIngredient fromLocation(ResourceLocation location) {
		for (Mods mod : Mods.values()) {
			if (mod.getId().equals(location.getNamespace())) {
				return new SimpleDatagenIngredient(mod, location.getPath());
			}
		}
		throw new AssertionError("ID " + location.getNamespace() + " doesn't correspond to any compat mod." +
				" SimpleDatagenIngredient is not meant for deserialization anyway");
	}

	private final Mods mod;
	private final String id;

	public SimpleDatagenIngredient(Mods mod, String id) {
		this.mod = mod;
		this.id = id;
	}

	@Override
	public boolean test(@NotNull ItemStack stack) {
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(mod.asResource(id));
	}

	@Override
	public @NotNull List<ItemStack> getMatchingStacks() {
		return List.of();
	}

	@Override
	public boolean requiresTesting() {
		return true;
	}

	@Override
	public @NotNull CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

}
