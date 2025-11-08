package com.flarelabsmc.missinginaction.mixin;

import com.flarelabsmc.missinginaction.config.StealthTags;
import com.flarelabsmc.missinginaction.entity.MiAAttributes;
import com.flarelabsmc.missinginaction.utils.Utils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract boolean hasEffect(MobEffect effect);

    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Redirect(
            method = "getVisibilityPercent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getArmorCoverPercentage()F"
            )
    )
    private float change(LivingEntity e) {
        final double stealth = Utils.getAttributeValueSafe(e, MiAAttributes.STEALTH.get());
        if (stealth >= 0) return 0;
        return (float) Mth.clamp(stealth / -20, 0, 1);
    }

    @Redirect(
            method = "getVisibilityPercent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isInvisible()Z"
            )
    )
    private boolean observant(LivingEntity e) {
        if (e != null && e.getType().is(StealthTags.IGNORE_INVIS))
            return false;
        return isInvisible();
    }

    @Inject(
            method = "updateInvisibilityStatus",
            at = @At(
                    value = "INVOKE",
                    ordinal = 1,
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/entity/LivingEntity;setInvisible(Z)V"
            )
    )
    private void change(CallbackInfo ci) {
        this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY));
    }
}