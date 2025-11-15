package com.flarelabsmc.missinginaction.mixin;

import com.flarelabsmc.missinginaction.MissingInAction;
import com.flarelabsmc.missinginaction.config.StealthTags;
import com.flarelabsmc.missinginaction.entity.MiAAttributes;
import com.flarelabsmc.missinginaction.handlers.EntityHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "playStepSound", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void mia$alertViaStepSound(BlockPos pos, BlockState state, CallbackInfo ci, SoundType soundtype) {
        Entity self = ((Entity) (Object) this);
        if (self.isSilent() || self.getType().is(StealthTags.NO_SOUND)) return;
        float volume = soundtype.getVolume();
        if (self instanceof LivingEntity living) {
            volume *= living.isSprinting() ? 15f : 4.0f;
            if (living instanceof Player p) {
                if (p.getAbilities().invulnerable) return;
                float luck = (float) living.getAttributeValue(Attributes.LUCK);
                float luckModifier = Math.max(0, MissingInAction.rand.nextFloat() - (luck / 10f));
                volume *= luckModifier;
                float stealth = (float) living.getAttributeValue(MiAAttributes.STEALTH.get());
                if (stealth > 0) {
                    volume /= (stealth / 4);
                }
            };
        }
        if (volume <= 0.0f) return;
        EntityHandler.alertTracker.put(new Tuple<>(self.level(), BlockPos.containing(self.getX(), self.getY() + self.getEyeHeight(), self.getZ())), volume);
    }
}
