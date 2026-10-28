package me.nova.util;

import me.nova.entity.ShadowEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public class OrbitBeeKeybind {

    // 在这里定义 ORBIT_BEE_KEY
    public static final KeyBinding ORBIT_BEE_KEY =
            KeyBindingHelper.registerKeyBinding(
                    new KeyBinding(
                            "key.novamod.orbit_bee",
                            InputUtil.Type.KEYSYM,
                            GLFW.GLFW_KEY_B,
                            KeyBinding.Category.create(
                                    Identifier.of("novamod", "entities")
                            )
                    )
            );

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ORBIT_BEE_KEY.wasPressed()) {
                if (client.player == null || client.world == null) {
                    continue;
                }

                if (client.crosshairTarget instanceof EntityHitResult hit) {
                    Entity entity = hit.getEntity();
                        ClientPlayNetworking.send(
                                new OrbitBeePayload(entity.getId())
                        );
                }
            }
        });
    }
}