package jackiecrazy.cloakanddagger.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface MixinMobSound {
    @Invoker
    float callGetSoundVolume();
    @Invoker
    float callGetVoicePitch();
}
