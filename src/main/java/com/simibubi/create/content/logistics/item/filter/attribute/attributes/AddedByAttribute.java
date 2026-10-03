package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import io.github.fabricators_of_create.porting_lib.item.ItemHooks;
import io.github.fabricators_of_create.porting_lib.item.extensions.CreatorModIdItem;

public record AddedByAttribute(String modId) implements ItemAttribute {
	public static final MapCodec<AddedByAttribute> CODEC = Codec.STRING
			.xmap(AddedByAttribute::new, AddedByAttribute::modId)
			.fieldOf("value");

	public static final StreamCodec<ByteBuf, AddedByAttribute> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
		.map(AddedByAttribute::new, AddedByAttribute::modId);

	@Override
	public boolean appliesTo(ItemStack stack, Level world) {
		return modId.equals(getCreatorModId(stack));
	}

	private static String getCreatorModId(ItemStack stack) {
		return stack.getItem() instanceof CreatorModIdItem item
			? item.getCreatorModId(stack) : ItemHooks.getDefaultCreatorModId(stack);
	}

	@Override
	public String getTranslationKey() {
		return "added_by";
	}

	@Override
	public Object[] getTranslationParameters() {
		ModContainer container = FabricLoader.getInstance().getModContainer(modId).orElse(null);
		String name = container == null ? name = StringUtils.capitalize(modId) : container.getMetadata().getName();
		return new Object[]{name};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.ADDED_BY;
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new AddedByAttribute("dummy");
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			String id = getCreatorModId(stack);
			return id == null ? Collections.emptyList() : List.of(new AddedByAttribute(id));
		}

		@Override
		public MapCodec<? extends ItemAttribute> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
