package com.flarelabsmc.missinginaction.mixin;

import com.flarelabsmc.missinginaction.config.StealthTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HurtByTargetGoal.class)
public abstract class HurtByTargetGoalMixin extends TargetGoal {
    @Unique private static final TargetingConditions FOR_COMBAT = TargetingConditions.forCombat();
    @Unique private static final TargetingConditions IGNORE_INVISIBILITY_TESTING = TargetingConditions.forCombat().ignoreInvisibilityTesting();
    @Unique private static final TargetingConditions IGNORE_LINE_OF_SIGHT = TargetingConditions.forCombat().ignoreLineOfSight();

    public HurtByTargetGoalMixin(Mob mob, boolean mustReach) {
        super(mob, mustReach);
    }

    @Redirect(
            method = "canUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/target/HurtByTargetGoal;canAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;)Z"
            )
    )
    private boolean change(HurtByTargetGoal instance, LivingEntity livingEntity, TargetingConditions targetingConditions) {
        TargetingConditions tc = targetingConditions;
        if (!livingEntity.getType().is(StealthTags.IGNORE_INVIS)) {
            if (livingEntity.getType().is(StealthTags.IGNORE_LOS)) tc = IGNORE_LINE_OF_SIGHT;
            else tc = FOR_COMBAT;
        } else if (!livingEntity.getType().is(StealthTags.IGNORE_LOS)) tc = IGNORE_INVISIBILITY_TESTING;
        return canAttack(livingEntity, tc);
    }


}
