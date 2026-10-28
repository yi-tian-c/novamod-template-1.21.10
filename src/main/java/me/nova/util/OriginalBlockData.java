package me.nova.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;

public record OriginalBlockData(
        BlockState blockState,
        NbtCompound blockEntityNbt
) {
    public static final Codec<OriginalBlockData> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    BlockState.CODEC
                            .fieldOf("block_state")
                            .forGetter(OriginalBlockData::blockState),

                    NbtCompound.CODEC
                            .optionalFieldOf("block_entity_nbt", new NbtCompound())
                            .forGetter(OriginalBlockData::blockEntityNbt)
            ).apply(instance, OriginalBlockData::new));
}