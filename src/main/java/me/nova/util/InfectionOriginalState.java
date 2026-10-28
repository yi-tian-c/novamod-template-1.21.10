package me.nova.util;

import com.mojang.serialization.Codec;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class InfectionOriginalState extends PersistentState {

    private static final Codec<Map<String, OriginalBlockData>> STATES_CODEC =
            Codec.unboundedMap(
                    Codec.STRING,
                    OriginalBlockData.CODEC
            );

    public static final Codec<InfectionOriginalState> CODEC =
            STATES_CODEC.xmap(
                    InfectionOriginalState::new,
                    state -> state.originalStates
            );

    public static final PersistentStateType<InfectionOriginalState> TYPE =
            new PersistentStateType<>(
                    "novamod_infection_original_states",
                    InfectionOriginalState::new,
                    CODEC,
                    DataFixTypes.LEVEL
            );

    private final Map<String, OriginalBlockData> originalStates;

    public InfectionOriginalState() {
        this.originalStates = new HashMap<>();
    }

    private InfectionOriginalState(Map<String, OriginalBlockData> states) {
        this.originalStates = new HashMap<>(states);
    }

    public static InfectionOriginalState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    private String key(BlockPos pos) {
        return Long.toString(pos.asLong());
    }

    /**
     * 保存指定位置的原始方块状态和方块实体数据。
     * 已有记录时不覆盖，避免覆盖最初的方块。
     */
    public void saveOriginal(
            ServerWorld world,
            BlockPos pos,
            BlockState original
    ) {
        String key = key(pos);

        if (originalStates.containsKey(key)) {
            return;
        }

        NbtCompound blockEntityNbt = new NbtCompound();
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity != null) {
            blockEntityNbt = blockEntity
                    .createNbtWithIdentifyingData(world.getRegistryManager())
                    .copy();
        }

        originalStates.put(
                key,
                new OriginalBlockData(original, blockEntityNbt)
        );

        markDirty();
    }

    public Optional<OriginalBlockData> getOriginal(BlockPos pos) {
        return Optional.ofNullable(originalStates.get(key(pos)));
    }

    public void removeOriginal(BlockPos pos) {
        if (originalStates.remove(key(pos)) != null) {
            markDirty();
        }
    }

    /**
     * 恢复指定位置的方块状态和方块实体数据。
     * 只有状态恢复成功后，才尝试恢复方块实体。
     */
    public boolean restoreOriginal(ServerWorld world, BlockPos pos) {
        OriginalBlockData data = originalStates.get(key(pos));

        if (data == null) {
            return false;
        }

        BlockState originalState = data.blockState();

        boolean stateRestored = world.setBlockState(
                pos,
                originalState,
                net.minecraft.block.Block.NOTIFY_LISTENERS
                        | net.minecraft.block.Block.FORCE_STATE
        );

        if (!stateRestored) {
            return false;
        }

        NbtCompound nbt = data.blockEntityNbt();

        if (!nbt.isEmpty()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity != null) {
                ReadView readView = NbtReadView.create(
                        ErrorReporter.EMPTY,
                        world.getRegistryManager(),
                        nbt.copy()
                );

                blockEntity.read(readView);
                blockEntity.markDirty();
                world.getChunkManager().markForUpdate(pos);
            }
        }

        removeOriginal(pos);
        return true;
    }
}