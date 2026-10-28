package me.nova.registry;

import me.nova.NovaMod;
import me.nova.entity.ArtificialFriendEntity;
import me.nova.util.ModNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;

public class ModKeyBindings {

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(
                    Identifier.of(NovaMod.MOD_ID, "artificial_friend")
            );

    private static KeyBinding TEXTURE_KEY;
    private static KeyBinding POSE_KEY;

    public static void register() {
        TEXTURE_KEY = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.novamod.af_texture",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_K,
                        CATEGORY
                )
        );

        POSE_KEY = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.novamod.af_pose",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_L,
                        CATEGORY
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(
                ModKeyBindings::onClientTick
        );
    }

    private static void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        // 查找玩家附近最近的 AF 实体
        ArtificialFriendEntity af = client.world
                .getEntitiesByClass(
                        ArtificialFriendEntity.class,
                        client.player.getBoundingBox().expand(32),
                        entity -> true
                )
                .stream()
                .min(Comparator.comparingDouble(
                        entity -> entity.squaredDistanceTo(client.player)
                ))
                .orElse(null);

        if (af == null) {
            return;
        }
        while (TEXTURE_KEY.wasPressed()) {
            ModNetworking.sendEntityAction(
                    af.getId(),
                    ModNetworking.AF_NEXT_TEXTURE
            );
        }

        while (POSE_KEY.wasPressed()) {
            ModNetworking.sendEntityAction(
                    af.getId(),
                    ModNetworking.AF_NEXT_POSE
            );
        }
    }
}