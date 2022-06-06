package jackiecrazy.cloakanddagger.mixin;

import jackiecrazy.cloakanddagger.capability.skill.CasterData;
import jackiecrazy.cloakanddagger.skill.ProcPoints;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = IForgeItemStack.class, remap = false)
public interface MixinShieldDisabler {
    @Shadow
    ItemStack getStack();

    /**
     * @reason Mixins don't allow injecting interfaces, so here we are
     *
     * @author Jackiecrazy
     */
    @Overwrite
    default boolean canDisableShield(ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        if (CasterData.getCap(attacker).isTagActive(ProcPoints.disable_shield)) return true;
        return getStack().getItem().canDisableShield(getStack(), shield, entity, attacker);
    }
}
