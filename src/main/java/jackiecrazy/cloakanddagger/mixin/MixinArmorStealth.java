package jackiecrazy.cloakanddagger.mixin;

import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.api.WarAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinArmorStealth extends Entity {

    public MixinArmorStealth(EntityType<?> p_i48580_1_, World p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
    }

    @Redirect(method = "getVisibilityPercent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getArmorCoverPercentage()F"))
    private float change(LivingEntity e) {
        final double stealth = e.getAttributeValue(WarAttributes.STEALTH.get());
        if (stealth >= 0) return 0;
        return (float) MathHelper.clamp(stealth / -10, 0, 1);
    }

    @Redirect(method = "getVisibilityPercent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisible()Z"))
    private boolean observant(LivingEntity e) {
        if (e != null)
            if (StealthOverride.stealthMap.getOrDefault(e.getType().getRegistryName(), StealthOverride.STEALTH).isObservant())
                return false;
        return isInvisible();
    }
}