package me.nova.datagen;

import me.nova.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class NovaLanguageProvider extends FabricLanguageProvider {

    public NovaLanguageProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup
    ) {
        super(output, "zh_cn", registryLookup);
    }

    @Override
    public void generateTranslations(
            RegistryWrapper.WrapperLookup registryLookup,
            TranslationBuilder translationBuilder
    ) {
        translationBuilder.add(
                "key.novamod.open_text_animation",
                "打开文字动画管理器"
        );
        // 创造模式物品栏翻译
        translationBuilder.add("itemGroup.novamod", "NovaMod");
        translationBuilder.add(ModBlocks.INFECTION_BLOCK, "侵蚀方块 危险！！！");

        // 实体翻译
        translationBuilder.add("entity.novamod.shadow", "黑影");
    }
}