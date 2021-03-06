package jackiecrazy.cloakanddagger.handlers;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.utils.CombatUtils;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CloakAndDagger.MODID, value = Dist.CLIENT)
public class ItemTooltipHandler {
    @SubscribeEvent()
    public static void tooltip(ItemTooltipEvent e) {
        if (CombatUtils.isWeapon(e.getItemStack())) {
            e.getToolTip().add(new TranslationTextComponent("wardance.tooltip.attack", CombatUtils.getDamageMultiplier(StealthOverride.Awareness.DISTRACTED, e.getItemStack()) + "x", CombatUtils.getDamageMultiplier(StealthOverride.Awareness.UNAWARE, e.getItemStack()) + "x").withStyle(TextFormatting.GRAY));
        }
    }
}
