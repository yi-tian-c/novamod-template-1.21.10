package me.nova.registry;

import me.nova.NovaMod;
import me.nova.item.SoulItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {
    public static final Item SOUL_ITEM = register(
            "soul",
            SoulItem::new,
            new Item.Settings()
    );
    /**
     * 注册物品
     *
     * @param name 物品 ID
     * @param factory 物品构造方法
     * @param settings 物品设置
     */
    private static Item register(
            String name,
            Function<Item.Settings, Item> factory,
            Item.Settings settings
    ) {
        Identifier id = NovaMod.id(name);

        RegistryKey<Item> itemKey =
                RegistryKey.of(RegistryKeys.ITEM, id);

        Item item = factory.apply(
                settings.registryKey(itemKey)
        );

        return Registry.register(
                Registries.ITEM,
                itemKey,
                item
        );
    }

    public static void initialize() {
        NovaMod.LOGGER.info(
                "Registering items for " + NovaMod.MOD_ID
        );
    }
}