package jackiecrazy.cloakanddagger.mixin;

import jackiecrazy.cloakanddagger.config.GeneralConfig;
import jackiecrazy.cloakanddagger.config.StealthTags;
import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.GeneralUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> p_i48580_1_, Level p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
    }

    @Redirect(method = "getVisibilityPercent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getArmorCoverPercentage()F"))
    private float change(LivingEntity e) {
        final double stealth = GeneralUtils.getAttributeValueSafe(e, FootworkAttributes.STEALTH.get());
        if (stealth >= 0) return 0;
        return (float) Mth.clamp(stealth / -20, 0, 1);
    }

    @Redirect(method = "getVisibilityPercent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInvisible()Z"))
    private boolean observant(LivingEntity e) {
        if (e != null && e.getType().is(StealthTags.IGNORE_INVIS))
            return false;
        return isInvisible();
    }


    @Inject(method = "hasLineOfSight",
            at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void help(Entity e, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()&&!GeneralUtils.isFacingEntity(this, e, GeneralConfig.baseHorizontalDetection, GeneralConfig.baseVerticalDetection)){
            cir.setReturnValue(false);
        }
    }

    @Shadow
    public abstract boolean hasEffect(MobEffect p_21024_);

    @Inject(method = "updateInvisibilityStatus",
            at = @At(value = "INVOKE", ordinal = 1, shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/LivingEntity;setInvisible(Z)V"))
    private void change(CallbackInfo ci) {
        this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY) && !this.hasEffect(FootworkEffects.EXPOSED.get()));
    }
}
