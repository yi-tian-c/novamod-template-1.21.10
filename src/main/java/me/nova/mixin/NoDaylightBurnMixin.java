package me.nova.mixin;

import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
/**
 * NoDaylightBurnSkeletonMixin 类
 * 这个类是一个Mixin类，用于修改或扩展现有类中与骷髅在日光下燃烧相关的功能
 * 通过使用Mixin框架，可以在不直接修改原始类的情况下改变其行为
 */
public class NoDaylightBurnMixin {
    @Inject(
            method = "isAffectedByDaylight",
            at = @At("HEAD"),
            cancellable = true
    )
    private void novamod$disableSkeletonDaylightBurn(
            CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(false);
    }
}
