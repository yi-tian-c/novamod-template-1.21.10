package me.nova.item;

import com.google.common.base.Suppliers;
import me.nova.entity.BlackCubeEntity;
import me.nova.registry.ModEntities;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlackCubeItem extends Item implements GeoItem {

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache geoCache =
            GeckoLibUtil.createInstanceCache(this);

    public BlackCubeItem(Settings settings) {
        super(settings);
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();

        // 只允许放置在去皮苍白橡木上
        if (!world.getBlockState(pos).isOf(Blocks.STRIPPED_PALE_OAK_LOG)||!world.getBlockState(pos).isOf(Blocks.QUARTZ_PILLAR)) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            BlackCubeEntity entity = new BlackCubeEntity(
                    ModEntities.BLACK_CUBE,
                    world
            );

            // 生成在方块顶部中央
            entity.refreshPositionAndAngles(
                    pos.getX() + 0.5,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5,
                    player != null ? player.getYaw() : 0.0F,
                    0.0F
            );

            if (world.spawnEntity(entity)) {
                if (player == null || !player.isCreative()) {
                    context.getStack().decrement(1);
                }
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(new AnimationController<>(
                "idle_controller",
                0,
                state -> state.setAndContinue(IDLE)
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private final Supplier<GeoItemRenderer<BlackCubeItem>> renderer =
                    Suppliers.memoize(
                            () -> new GeoItemRenderer<>(BlackCubeItem.this)
                    );

            @Override
            public GeoItemRenderer<BlackCubeItem> getGeoItemRenderer() {
                return renderer.get();
            }
        });
    }
}