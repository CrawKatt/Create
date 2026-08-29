package com.simibubi.create.content.trains.entity;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public record CarriageDataUpdatePacket(int entity, CarriageSyncData data) implements ClientboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, CarriageDataUpdatePacket> STREAM_CODEC = StreamCodec.of(
		(buffer, packet) -> {
			buffer.writeVarInt(packet.entity);
			packet.data.write(buffer);
		},
		buffer -> {
			int entityId = buffer.readVarInt();
			CarriageSyncData data = new CarriageSyncData();
			data.read(buffer);
			return new CarriageDataUpdatePacket(entityId, data);
		}
	);

	public CarriageDataUpdatePacket(CarriageContraptionEntity entity) {
		this(entity.getId(), entity.carriageData);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(LocalPlayer player) {
		Entity entity = player.clientLevel.getEntity(this.entity);
		if (entity instanceof CarriageContraptionEntity carriage) {
			carriage.onCarriageDataUpdate(this.data);
		} else {
			Create.LOGGER.error("Invalid CarriageDataUpdatePacket for non-carriage entity: " + entity);
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CARRIAGE_DATA_UPDATE;
	}
}
