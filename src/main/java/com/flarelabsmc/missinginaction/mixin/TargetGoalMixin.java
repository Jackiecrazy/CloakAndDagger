package com.flarelabsmc.missinginaction.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(TargetGoal.class)
public abstract class TargetGoalMixin extends Goal {
    @Shadow
    @Final
    protected Mob mob;
    @Shadow
    @Nullable
    protected LivingEntity targetMob;
    @Unique boolean missinginaction$continueIntercept;

    @Inject(method = "canContinueToUse", at = @At("HEAD"))
    private void start(CallbackInfoReturnable<Boolean> cir) {
        missinginaction$continueIntercept = true;
    }

    @Inject(method = "canContinueToUse", at = @At("TAIL"))
    private void stop(CallbackInfoReturnable<Boolean> cir) {
        missinginaction$continueIntercept = false;
    }

    @Inject(method = "getFollowDistance", at = @At(value = "RETURN"), cancellable = true)
    protected void getFollowDistance(CallbackInfoReturnable<Double> cir) {
        double range = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (missinginaction$continueIntercept && targetMob != null)
            range *= targetMob.getVisibilityPercent(mob);
        cir.setReturnValue(range);
    }
}
