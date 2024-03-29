package jackiecrazy.cloakanddagger.mixin;

import jackiecrazy.cloakanddagger.config.StealthTags;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HurtByTargetGoal.class)
public abstract class MixinInvisibleRevenge extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING_1 = TargetingConditions.forCombat();
    private static final TargetingConditions HURT_BY_TARGETING_2 = TargetingConditions.forCombat().ignoreInvisibilityTesting();
    private static final TargetingConditions HURT_BY_TARGETING_3 = TargetingConditions.forCombat().ignoreLineOfSight();
    @Shadow
    @Final
    private static TargetingConditions HURT_BY_TARGETING;

    public MixinInvisibleRevenge(Mob p_26140_, boolean p_26141_) {
        super(p_26140_, p_26141_);
    }

    @Redirect(method = "canUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/target/HurtByTargetGoal;canAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;)Z"))
    private boolean change(HurtByTargetGoal instance, LivingEntity livingEntity, TargetingConditions targetingConditions) {
        TargetingConditions tc = targetingConditions;
        if (!livingEntity.getType().is(StealthTags.IGNORE_INVIS)) {
            if (livingEntity.getType().is(StealthTags.IGNORE_LOS)) tc = HURT_BY_TARGETING_3;
            else tc = HURT_BY_TARGETING_1;
        } else if (!livingEntity.getType().is(StealthTags.IGNORE_LOS)) tc = HURT_BY_TARGETING_2;
        return canAttack(livingEntity, tc);
    }


}
