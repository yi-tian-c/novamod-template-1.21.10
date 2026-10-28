package me.nova.registry;

import me.nova.NovaMod;
import me.nova.entity.ArtificialFriendEntity;
import me.nova.entity.BlackCubeEntity;
import me.nova.entity.ShadowEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<ShadowEntity> SHADOW =
            register(
                    "shadow",
                    EntityType.Builder
                            .create(ShadowEntity::new, SpawnGroup.MONSTER)
                            .dimensions(0.6F, 1.8F)
            );
    public static final EntityType<BlackCubeEntity> BLACK_CUBE =
            register(
                    "black_cube",
                    EntityType.Builder
                            .create(BlackCubeEntity::new, SpawnGroup.MONSTER)
                            .dimensions(0.6F, 0.6F)
            );
    public static final EntityType<ArtificialFriendEntity> ARTIFICIAL_FRIEND =
            register(
                    "af",
                    EntityType.Builder
                            .create(ArtificialFriendEntity::new, SpawnGroup.MISC)
                            .dimensions(0.6F, 1.8F)
            );

    private static <T extends Entity> EntityType<T> register(
            String name,
            EntityType.Builder<T> builder
    ) {
        Identifier id = NovaMod.id(name);

        RegistryKey<EntityType<?>> key =
                RegistryKey.of(
                        RegistryKeys.ENTITY_TYPE,
                        id
                );

        return Registry.register(
                Registries.ENTITY_TYPE,
                key,
                builder.build(key)
        );
    }

    public static void initialize() {

        NovaMod.LOGGER.info(
                "Registering entities for " + NovaMod.MOD_ID
        );

        FabricDefaultAttributeRegistry.register(
                SHADOW,
                ShadowEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                BLACK_CUBE,
                BlackCubeEntity.createAttributes()
        );
    }
}
