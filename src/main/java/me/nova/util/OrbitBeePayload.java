package me.nova.util;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OrbitBeePayload(int entityId) implements CustomPayload {

    public static final Id<OrbitBeePayload> ID =
            new Id<>(Identifier.of("novamod", "orbit_bee"));

    public static final PacketCodec<RegistryByteBuf, OrbitBeePayload> CODEC =
            PacketCodec.of(
                    (payload, buf) -> buf.writeVarInt(payload.entityId()),
                    buf -> new OrbitBeePayload(buf.readVarInt())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}