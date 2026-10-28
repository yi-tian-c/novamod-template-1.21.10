package me.nova.datagen;

import me.nova.registry.ModBlocks;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;

public class NovaModelProvider extends FabricModelProvider {

    public NovaModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(
            BlockStateModelGenerator blockStateModelGenerator
    ) {
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.BLACK_PURPLE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.INFECTION_BARRIER_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.INFECTION_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.RESTORE_SPREAD_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.RESTORE_TRANSITION_BLOCK);
        // 在这里生成方块状态和方块模型
    }

    @Override
    public void generateItemModels(
            ItemModelGenerator itemModelGenerator
    ) {
        // 在这里生成物品模型
    }
}