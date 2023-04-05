package jackiecrazy.cloakanddagger.mixin;

import jackiecrazy.footwork.potion.FootworkEffects;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinNotInvisible extends Entity {

    public MixinNotInvisible(EntityType<?> p_i48580_1_, Level p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
    }

    @Shadow
    public abstract boolean hasEffect(MobEffect p_21024_);

    @Inject(method = "updateInvisibilityStatus",
            at = @At(value = "INVOKE", ordinal = 1, shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/LivingEntity;setInvisible(Z)V"))
    private void change(CallbackInfo ci) {
        this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY) && !this.hasEffect(FootworkEffects.EXPOSED.get()));
    }
}
