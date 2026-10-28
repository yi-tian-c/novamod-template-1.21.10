package me.nova.block;

import me.nova.registry.ModBlocks;
import me.nova.util.InfectionOriginalState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class InfectionBlock extends Block {

    public static final IntProperty AGE = IntProperty.of("age", 0, 10);

    public InfectionBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(AGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
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
    public void scheduledTick(
            BlockState state, ServerWorld world,
            BlockPos pos, Random random
    ) {
        if (!world.getBlockState(pos).isOf(this)) {
            return;
        }

        int age = state.get(AGE);

        // 随侵蚀程度提高，概率性触发扩散
        if (age >= 10 || random.nextInt(15) <= age) {
            infect(world, pos);

            // 开始下一轮侵蚀
            if (world.getBlockState(pos).isOf(this)) {
                world.setBlockState(
                        pos,
                        world.getBlockState(pos).with(AGE, 0),
                        Block.NOTIFY_LISTENERS | Block.FORCE_STATE
                );
                world.scheduleBlockTick(pos, this, 1);
            }
            return;
        }

        // 逐渐积累侵蚀程度
        world.setBlockState(pos, state.with(AGE, age + 1),Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
        world.scheduleBlockTick(pos, this, 1);
    }

    private void infect(ServerWorld world, BlockPos pos) {
        for (BlockPos target : neighbors(pos)) {
            BlockState targetState = world.getBlockState(target);

            if (!isInfectable(targetState)) {
                continue;
            }

            // 侵蚀前记录原始方块状态和方块实体数据
            InfectionOriginalState.get(world)
                    .saveOriginal(world, target, targetState);

            world.setBlockState(
                    target,
                    ModBlocks.INFECTION_BLOCK.getDefaultState(),
                    Block.NOTIFY_LISTENERS | Block.FORCE_STATE
            );
        }
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
