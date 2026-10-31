package me.nova.block;

import me.nova.registry.ModBlocks;
import me.nova.util.InfectionOriginalState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class InfectionBlock extends Block {

    public static final IntProperty AGE = IntProperty.of("age", 0, 10);
    public static final BooleanProperty ACTIVE =
            BooleanProperty.of("active");
    private static final int MAX_FAILED_ATTEMPTS = 20;
    public InfectionBlock(Settings settings) {
        super(settings);
        setDefaultState(
                getStateManager().getDefaultState()
                        .with(AGE, 0)
                        .with(ACTIVE, true)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE, ACTIVE);
    }

    @Override
    public void onBlockAdded(
            BlockState state, World world, BlockPos pos,
            BlockState oldState, boolean notify
    ) {
        if (!world.isClient()) {
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    protected void scheduledTick(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            Random random
    ) {
        // 已经惰性化，不再尝试侵蚀
        if (!state.get(ACTIVE)) {
            return;
        }

        // 达到侵蚀判定条件
        if (state.get(AGE) >= 10 || random.nextInt(15) <= state.get(AGE)) {

            boolean success = infect(world, pos);

            InfectionFailureState failureState =
                    InfectionFailureState.get(world);

            if (success) {
                // 成功侵蚀，重置失败次数
                failureState.reset(pos);
            } else {
                // 没有找到可以侵蚀的方块
                int failedAttempts = failureState.increment(pos);

                if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                    // 不替换方块，只修改自身状态
                    world.setBlockState(
                            pos,
                            state.with(ACTIVE, false),
                            Block.NOTIFY_LISTENERS
                    );

                    return;
                }
            }

            // 重置年龄，继续下一轮
            world.setBlockState(
                    pos,
                    state.with(AGE, 0),
                    Block.NOTIFY_LISTENERS
            );

        } else {
            // 年龄增长
            world.setBlockState(
                    pos,
                    state.with(AGE, state.get(AGE) + 1),
                    Block.NOTIFY_LISTENERS
            );
        }

        // 继续调度
        world.scheduleBlockTick(pos, this, 1);
    }

    private boolean infect(ServerWorld world, BlockPos pos) {
        boolean infected = false;

        for (Direction direction : Direction.values()) {
            BlockPos target = pos.offset(direction);
            BlockState targetState = world.getBlockState(target);

            if (!isInfectable(targetState)) {
                continue;
            }

            // 浮冰特判：不保存原始状态
            if (!targetState.isOf(Blocks.PACKED_ICE)) {
                InfectionOriginalState.get(world)
                        .saveOriginal(world, target, targetState);
            }

            world.setBlockState(
                    target,
                    ModBlocks.INFECTION_BLOCK.getDefaultState(),
                    Block.NOTIFY_ALL
            );

            infected = true;
        }

        return infected;
    }

    private boolean isInfectable(BlockState state) {
        return !state.isAir()
                && !state.isOf(ModBlocks.INFECTION_BLOCK)
                && !state.isOf(ModBlocks.INFECTION_BARRIER_BLOCK)
                && !state.isOf(ModBlocks.RESTORE_SPREAD_BLOCK)
                && !state.isOf(ModBlocks.RESTORE_TRANSITION_BLOCK);
    }

    private static BlockPos[] neighbors(BlockPos pos) {
        return new BlockPos[] {
                pos.up(), pos.down(),
                pos.north(), pos.south(),
                pos.east(), pos.west()
        };
    }

}
