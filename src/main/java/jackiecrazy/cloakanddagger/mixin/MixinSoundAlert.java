package jackiecrazy.cloakanddagger.mixin;

import jackiecrazy.cloakanddagger.handlers.EntityHandler;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class MixinSoundAlert {

    @Shadow
    public abstract ServerLevel getLevel();

    @Inject(method = "playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", at = @At("TAIL"))
    private void alert(Player player, double x, double y, double z, SoundEvent soundIn, SoundSource category, float volume, float pitch, CallbackInfo ci) {
        if (StealthOverride.soundMap.containsKey(soundIn))
            EntityHandler.alertTracker.put(new Tuple<>(this.getLevel(), new BlockPos(x, y, z)), (float) (StealthOverride.soundMap.get(soundIn)));
    }

    @Inject(method = "playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", at = @At("TAIL"))
    private void alertMoving(Player player, Entity entityIn, SoundEvent eventIn, SoundSource category, float volume, float pitch, CallbackInfo ci) {
        double x = entityIn.getX(), y = entityIn.getY(), z = entityIn.getZ();
        if (StealthOverride.soundMap.containsKey(eventIn))
            EntityHandler.alertTracker.put(new Tuple<>(this.getLevel(), new BlockPos(x, y, z)), (float) (StealthOverride.soundMap.get(eventIn)));
    }

}
