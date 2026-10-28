
package me.nova.registry;

import me.nova.NovaMod;
import me.nova.block.*;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModBlocks {

    // 测试方块
    public static final Block TEST_BLOCK = register(
            "test_block",
            Block::new,
            AbstractBlock.Settings.create()
                    .strength(2.0f)
                    .requiresTool()
    );

    // 幽灵蜂蜜块
    public static final Block GHOST_HONEY_BLOCK = register(
            "ghost_honey_block",
            Block::new,
            AbstractBlock.Settings.copy(Blocks.HONEY_BLOCK)
                    .noCollision()
    );

    // 方块1：实心侵蚀方块
    public static final Block INFECTION_BLOCK = register(
            "infection_block",
            InfectionBlock::new,
            AbstractBlock.Settings.copy(Blocks.STONE)
    );

    // 方块2：透明隔离屏障
    public static final Block INFECTION_BARRIER_BLOCK = register(
            "infection_barrier_block",
            InfectionBarrierBlock::new,
            AbstractBlock.Settings.copy(Blocks.GLASS)
                    .nonOpaque()
    );

    // 方块3：透明还原扩散方块
    public static final Block RESTORE_SPREAD_BLOCK = register(
            "restore_spread_block",
            RestoreSpreadBlock::new,
            AbstractBlock.Settings.copy(Blocks.GLASS)
                    .nonOpaque()
    );

    // 方块4：透明还原过渡方块
    public static final Block RESTORE_TRANSITION_BLOCK = register(
            "restore_transition_block",
            RestoreTransitionBlock::new,
            AbstractBlock.Settings.copy(Blocks.GLASS)
                    .nonOpaque()
    );

    // 黑紫方块
    public static final Block BLACK_PURPLE_BLOCK = register(
            "black_purple",
            BlackPurpleBlock::new,
            AbstractBlock.Settings.create()
                    .strength(1.0f)
    );
    public static final Block NOVA_BLOCK = register(
            "nova_block",
            NovaBlock::new,   // 如果上面没写单独类，就改成 Block::new
            AbstractBlock.Settings.copy(Blocks.GLASS)
                    .nonOpaque()
                    .strength(0.3f)
    );

    /**
     * 注册普通方块。
     * 同时注册对应的方块物品。
     *
     * 注意：必须在创建 Block 实例之前设置 registryKey。
     */
    private static Block register(
            String name,
            java.util.function.Function<AbstractBlock.Settings, Block> factory,
            AbstractBlock.Settings settings
    ) {
        Identifier id = NovaMod.id(name);

        RegistryKey<Block> blockKey =
                RegistryKey.of(RegistryKeys.BLOCK, id);

        RegistryKey<Item> itemKey =
                RegistryKey.of(RegistryKeys.ITEM, id);

        // 先设置注册键，再创建方块实例
        Block block = factory.apply(
                settings.registryKey(blockKey)
        );

        Registry.register(
                Registries.BLOCK,
                blockKey,
                block
        );

        // 注册对应的方块物品
        BlockItem blockItem = new BlockItem(
                block,
                new Item.Settings().registryKey(itemKey)
        );

        Registry.register(
                Registries.ITEM,
                itemKey,
                blockItem
        );

        return block;
    }

    public static void initialize() {
        NovaMod.LOGGER.info(
                "Registering blocks for " + NovaMod.MOD_ID
        );
    }
}
