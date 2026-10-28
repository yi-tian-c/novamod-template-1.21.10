package me.nova.util;

import me.nova.NovaMod;
import me.nova.entity.ArtificialFriendEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public final class ModNetworking {

    private ModNetworking() {}

    /**
     * 通用实体操作数据包。
     * entityId: 目标实体 ID
     * action: 操作编号，由服务端根据目标实体解释
     */
    public record EntityActionPayload(int entityId, int action)
            implements CustomPayload {

        public static final Id<EntityActionPayload> ID =
                new Id<>(NovaMod.id("entity_action"));

        public static final PacketCodec<RegistryByteBuf, EntityActionPayload> CODEC =
                PacketCodec.of(
                        (payload, buf) -> {
                            buf.writeVarInt(payload.entityId());
                            buf.writeVarInt(payload.action());
                        },
                        buf -> new EntityActionPayload(
                                buf.readVarInt(),
                                buf.readVarInt()
                        )
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    // AF 操作编号
    public static final int AF_NEXT_TEXTURE = 100;
    public static final int AF_NEXT_POSE = 101;

    public static void registerC2S() {
        PayloadTypeRegistry.playC2S().register(
                EntityActionPayload.ID,
                EntityActionPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                EntityActionPayload.ID,
                (payload, context) -> context.server().execute(() -> {
                    var player = context.player();
                    var entity = player.getEntityWorld().getEntityById(
                            payload.entityId()
                    );

                    if (entity == null
                            || entity.squaredDistanceTo(player) > 32 * 32) {
                        return;
                    }

                    if (entity instanceof ArtificialFriendEntity af) {
                        switch (payload.action()) {
                            case AF_NEXT_TEXTURE -> af.nextTexture();
                            case AF_NEXT_POSE -> af.nextPose();
                            default -> {
                            }
                        }
                    }
                })
        );
    }

    /**
     * 客户端发送通用实体操作。
     */
    public static void sendEntityAction(int entityId, int action) {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new EntityActionPayload(entityId, action)
        );
    }
}