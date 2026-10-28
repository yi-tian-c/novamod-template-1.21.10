package me.nova;

import me.nova.client.renderer.ArtificialFriendEntityRenderer;
import me.nova.client.renderer.BlackCubeEntityRenderer;
import me.nova.client.renderer.ShadowEntityRenderer;
import me.nova.entity.ShadowEntity;
import me.nova.registry.ModBlocks;
import me.nova.registry.ModEntities;
import me.nova.registry.ModKeyBindings;
import me.nova.util.OrbitBeePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import static me.nova.util.OrbitBeeKeybind.ORBIT_BEE_KEY;

public class NovaModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
                ModEntities.SHADOW,
                context -> new ShadowEntityRenderer<>(
                        context,
                        ModEntities.SHADOW
                )
        );
        EntityRendererRegistry.register(
                ModEntities.BLACK_CUBE,
                context -> new BlackCubeEntityRenderer<>(
                        context,
                        ModEntities.BLACK_CUBE
                )
        );
        EntityRendererRegistry.register(
                ModEntities.ARTIFICIAL_FRIEND,
                context -> new ArtificialFriendEntityRenderer<>(
                        context,
                        ModEntities.ARTIFICIAL_FRIEND
                )
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ORBIT_BEE_KEY.wasPressed()) {
                summonOrbitBee(client);
            }
        });
        // 客户端初始化（ClientModInitializer）
        BlockRenderLayerMap.putBlock(ModBlocks.GHOST_HONEY_BLOCK, BlockRenderLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(ModBlocks.RESTORE_SPREAD_BLOCK, BlockRenderLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(ModBlocks.INFECTION_BARRIER_BLOCK, BlockRenderLayer.TRANSLUCENT);
        BlockRenderLayerMap.putBlock(ModBlocks.RESTORE_TRANSITION_BLOCK, BlockRenderLayer.TRANSLUCENT);
    }
    private static void summonOrbitBee(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        if (client.crosshairTarget instanceof EntityHitResult hit) {
            Entity shadow = hit.getEntity();
                ClientPlayNetworking.send(
                        new OrbitBeePayload(shadow.getId())
                );
        }
    }
}