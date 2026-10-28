package me.nova.block;

import me.nova.util.InfectionOriginalState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class RestoreTransitionBlock extends Block {

    public static final IntProperty AGE = IntProperty.of("age", 0, 10);

    public RestoreTransitionBlock(Settings settings) {
        super(settings);

        setDefaultState(
                getStateManager()
                        .getDefaultState()
                        .with(AGE, 0)
        );
    }

    @Override
    protected void appendProperties(
            StateManager.Builder<Block, BlockState> builder
    ) {
        builder.add(AGE);
    }

    @Override
    public void onBlockAdded(
            BlockState state,
            World world,
            BlockPos pos,
            BlockState oldState,
            boolean notify
    ) {
        if (!world.isClient()) {
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    public void scheduledTick(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            Random random
    ) {
        if (!world.getBlockState(pos).isOf(this)) {
            return;
        }

        int age = state.get(AGE);

        /*
         * 随 AGE 增加，恢复概率逐渐提高：
         * age 0：1/15
         * age 5：6/15
         * age 10：必定恢复
         */
        if (age >= 10 || random.nextInt(15) <= age) {
            InfectionOriginalState records =
                    InfectionOriginalState.get(world);

            boolean restored = records.restoreOriginal(world, pos);

            // 没有记录时，按原有设计恢复为空气
            if (!restored) {
                world.setBlockState(
                        pos,
                        Blocks.AIR.getDefaultState(),
                        Block.NOTIFY_LISTENERS | Block.FORCE_STATE
                );
            }

            return;
        }

        // 增加过渡程度
        world.setBlockState(
                pos,
                state.with(AGE, age + 1),
                Block.NOTIFY_LISTENERS | Block.FORCE_STATE
        );

        world.scheduleBlockTick(pos, this, 1);
    }
}
