package jackiecrazy.cloakanddagger.mixin;

import jackiecrazy.cloakanddagger.config.SoundConfig;
import jackiecrazy.cloakanddagger.handlers.EntityHandler;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//temporarily sealed away
@Mixin(Mob.class)
public abstract class MixinMobAlert {

    @Inject(method = "playHurtSound", at = @At("TAIL"))
    private void alert(DamageSource source, CallbackInfo ci) {
        Mob me = ((Mob) (Object) this);
        StealthOverride.StealthData stealthData = StealthOverride.stealthMap.getOrDefault(EntityType.getKey(me.getType()), StealthOverride.STEALTH);
        if (me.isSilent() || stealthData.quiet)
            return;
        float volume = ((MixinMobSound) me).callGetSoundVolume();

        EntityHandler.alertTracker.put(new Tuple<>(me.level(), BlockPos.containing(me.getX(), me.getY(), me.getZ())), (float) (volume * SoundConfig.shout));
    }

}
