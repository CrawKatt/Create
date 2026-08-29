package com.simibubi.create.infrastructure.fabric;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Passes the raw opening buffer through to the menu, so menus can read their
 * custom data the same way they did with the pre-1.20.5 extended screen handler API.
 */
public final class IdentityBufCodec {
	public static final StreamCodec<RegistryFriendlyByteBuf, RegistryFriendlyByteBuf> INSTANCE =
			StreamCodec.of(
				(out, data) -> {
					try {
						out.writeBytes(data, data.readerIndex(), data.readableBytes());
					} finally {
						data.release();
					}
				},
				in -> new RegistryFriendlyByteBuf(in.readBytes(in.readableBytes()), in.registryAccess())
			);

	private IdentityBufCodec() {}
}
