package com.flarelabsmc.missinginaction.handlers;

import com.flarelabsmc.missinginaction.MissingInAction;
import com.flarelabsmc.missinginaction.api.Awareness;
import com.flarelabsmc.missinginaction.utils.CombatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MissingInAction.MODID, value = Dist.CLIENT)
public class ItemTooltipHandler {
    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent e) {
        if (CombatUtils.isWeapon(e.getItemStack())) {
            e.getToolTip().add(Component.translatable("missinginaction.tooltip.distract", CombatUtils.getDamageMultiplier(Awareness.DISTRACTED, e.getItemStack()) + "x").withStyle(ChatFormatting.GRAY));
            e.getToolTip().add(Component.translatable("missinginaction.tooltip.unaware", CombatUtils.getDamageMultiplier(Awareness.UNAWARE, e.getItemStack()) + "x").withStyle(ChatFormatting.GRAY));
        }
    }
}
