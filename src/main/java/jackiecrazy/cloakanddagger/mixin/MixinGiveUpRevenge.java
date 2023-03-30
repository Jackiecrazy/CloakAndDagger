package jackiecrazy.cloakanddagger.mixin;

import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(TargetGoal.class)
public abstract class MixinGiveUpRevenge extends Goal {
    @Shadow
    @Final
    protected Mob mob;
    @Shadow
    @Nullable
    protected LivingEntity targetMob;
    boolean continueIntercept;

    @Inject(method = "canContinueToUse",
            at = @At("HEAD"))
    private void start(CallbackInfoReturnable<Boolean> cir) {
        continueIntercept = true;
    }

    @Inject(method = "canContinueToUse",
            at = @At("TAIL"))
    private void stop(CallbackInfoReturnable<Boolean> cir) {
        continueIntercept = false;
    }

    /**
     * @author Jackiecrazy
     * @reason dude it's one line
     */
    @Overwrite
    protected double getFollowDistance() {
        double range = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (continueIntercept && targetMob != null)
            range *= targetMob.getVisibilityPercent(mob);
        return range;
    }
}
