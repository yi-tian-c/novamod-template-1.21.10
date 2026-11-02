package me.nova;

import me.nova.entity.ShadowEntity;
import me.nova.registry.*;
import me.nova.util.ModNetworking;
import me.nova.util.OrbitBeeKeybind;
import me.nova.util.OrbitBeeManager;
import me.nova.util.OrbitBeePayload;
import me.nova.particle.ModParticles;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NovaMod implements ModInitializer {
	public static final String MOD_ID = "novamod";

	/** 使用模组 ID 标记日志来源。 */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		ModParticles.initialize();
		ModEntities.initialize();
		OrbitBeeKeybind.register();
		ModBlocks.initialize();
		ModItemGroups.initialize();
		ModItems.initialize();
		ModKeyBindings.register();
		ModNetworking.registerC2S();
		PayloadTypeRegistry.playC2S().register(
				OrbitBeePayload.ID,
				OrbitBeePayload.CODEC
		);

		// 每个服务端世界 tick 结束时更新绕圈蜜蜂
		ServerTickEvents.END_WORLD_TICK.register(
				OrbitBeeManager::tick
		);
		LOGGER.info("Hello Fabric world!");
	}
	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
