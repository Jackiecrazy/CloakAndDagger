package jackiecrazy.cloakanddagger.mixin;

import jackiecrazy.cloakanddagger.config.StealthTags;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.utils.GeneralUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinArmorStealth extends Entity {

    public MixinArmorStealth(EntityType<?> p_i48580_1_, Level p_i48580_2_) {
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
}