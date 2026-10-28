package me.nova.block;

import me.nova.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class RestoreSpreadBlock extends Block {

    public static final IntProperty AGE = IntProperty.of("age", 0, 10);

    public RestoreSpreadBlock(Settings settings) {
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

        // 概率性触发还原扩散
        if (age >= 10 || random.nextInt(15) <= age) {
            spread(world, pos);

            world.setBlockState(
                    pos,
                    ModBlocks.RESTORE_TRANSITION_BLOCK.getDefaultState(),
                    Block.NOTIFY_LISTENERS | Block.FORCE_STATE
            );
            return;
        }

        world.setBlockState(pos, state.with(AGE, age + 1),Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
        world.scheduleBlockTick(pos, this, 1);
    }

    private void spread(ServerWorld world, BlockPos pos) {
        for (BlockPos target : neighbors(pos)) {
            if (world.getBlockState(target)
                    .isOf(ModBlocks.INFECTION_BLOCK)) {
                world.setBlockState(
                        target,
                        ModBlocks.RESTORE_SPREAD_BLOCK.getDefaultState(),
                        Block.NOTIFY_LISTENERS | Block.FORCE_STATE
                );
            }
        }
    }

    private static BlockPos[] neighbors(BlockPos pos) {
        return new BlockPos[] {
                pos.up(), pos.down(),
                pos.north(), pos.south(),
                pos.east(), pos.west()
        };
    }
}