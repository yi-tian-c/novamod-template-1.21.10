package me.nova.block;

import com.mojang.serialization.Codec;
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

public class InfectionFailureState extends PersistentState {

    private static final Codec<Map<String, Integer>> FAILURES_CODEC =
            Codec.unboundedMap(
                    Codec.STRING,
                    Codec.INT
            );

    public static final Codec<InfectionFailureState> CODEC =
            FAILURES_CODEC.xmap(
                    InfectionFailureState::new,
                    state -> state.failures
            );

    public static final PersistentStateType<InfectionFailureState> TYPE =
            new PersistentStateType<>(
                    "novamod_infection_failures",
                    InfectionFailureState::new,
                    CODEC,
                    DataFixTypes.LEVEL
            );

    private final Map<String, Integer> failures;

    public InfectionFailureState() {
        this.failures = new HashMap<>();
    }

    private InfectionFailureState(Map<String, Integer> failures) {
        this.failures = new HashMap<>(failures);
    }

    public static InfectionFailureState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    private String key(BlockPos pos) {
        return Long.toString(pos.asLong());
    }

    /**
     * 获取指定位置连续侵蚀失败的次数。
     */
    public int getFailures(BlockPos pos) {
        return failures.getOrDefault(key(pos), 0);
    }

    /**
     * 失败次数加一。
     */
    public int increment(BlockPos pos) {
        String key = key(pos);

        int count = failures.getOrDefault(key, 0) + 1;

        failures.put(key, count);
        markDirty();

        return count;
    }

    /**
     * 重置指定位置的失败次数。
     */
    public void reset(BlockPos pos) {
        if (failures.remove(key(pos)) != null) {
            markDirty();
        }
    }
}