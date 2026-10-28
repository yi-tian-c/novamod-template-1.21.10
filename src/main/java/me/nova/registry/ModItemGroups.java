package me.nova.registry;

import me.nova.NovaMod;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class ModItemGroups {

    public static final RegistryKey<ItemGroup> NOVA_GROUP_KEY =
            RegistryKey.of(
                    RegistryKeys.ITEM_GROUP,
                    NovaMod.id("main")
            );

    public static final ItemGroup NOVA_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            NOVA_GROUP_KEY,
            ItemGroup.create(ItemGroup.Row.TOP, 7)
                    .displayName(Text.translatable("itemGroup.novamod.main"))
                    .icon(() -> new ItemStack(ModBlocks.TEST_BLOCK))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.GHOST_HONEY_BLOCK);
                        entries.add(ModBlocks.BLACK_PURPLE_BLOCK);
                        entries.add(ModBlocks.INFECTION_BLOCK);
                        entries.add(ModBlocks.RESTORE_SPREAD_BLOCK);
                        entries.add(ModBlocks.INFECTION_BARRIER_BLOCK);
                        entries.add(ModBlocks.RESTORE_TRANSITION_BLOCK);
                        entries.add(ModItems.SOUL_ITEM);
                        entries.add(ModBlocks.NOVA_BLOCK);
                        // 添加自己的物品，例如：
                        // entries.add(ModItems.TEST_ITEM);
                    })
                    .build()
    );

    public static void initialize() {
        NovaMod.LOGGER.info(
                "Registering item groups for " + NovaMod.MOD_ID
        );
    }
}