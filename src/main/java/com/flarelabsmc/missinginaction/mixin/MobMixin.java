package com.flarelabsmc.missinginaction.mixin;

import com.flarelabsmc.missinginaction.config.SoundConfig;
import com.flarelabsmc.missinginaction.config.StealthTags;
import com.flarelabsmc.missinginaction.handlers.EntityHandler;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "playHurtSound", at = @At("TAIL"))
    private void alert(DamageSource source, CallbackInfo ci) {
        Mob self = ((Mob) (Object) this);
        if (self.isSilent() || self.getType().is(StealthTags.NO_SOUND))
            return;
        float volume = ((LivingEntityInvoker) self).callGetSoundVolume();
        EntityHandler.alertTracker.put(new Tuple<>(self.level(), BlockPos.containing(self.getX(), self.getY(), self.getZ())), (float) (volume * SoundConfig.shout));
    }
}
