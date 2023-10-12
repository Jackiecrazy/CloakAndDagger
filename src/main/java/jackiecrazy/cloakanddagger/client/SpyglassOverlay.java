package jackiecrazy.cloakanddagger.client;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.action.PermissionData;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class SpyglassOverlay implements IGuiOverlay {
    private static final ResourceLocation stealth = new ResourceLocation(CloakAndDagger.MODID, "textures/hud/stealth.png");
    int descY = 10;

    private void describe(GuiGraphics graphics, String tag, boolean advanced, ChatFormatting... format) {
        Minecraft mc = Minecraft.getInstance();
        graphics.drawString(mc.font, Component.translatable("cloak.tag." + tag).withStyle(format), 4, descY, 0xffffff);
        if (advanced)
            graphics.drawString(mc.font, Component.translatable("cloak.desc." + tag).withStyle(format), 8, descY + 8, 0xffffff);
        descY += 20;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (!PermissionData.getCap(mc.player).canSee()) return;
        if (mc.getCameraEntity() instanceof Player && mc.player != null && !mc.options.hideGui && mc.player.isScoping()) {
            LocalPlayer player = mc.player;
            Entity look = RenderEvents.getEntityLookedAt(player, 32);
            if (look instanceof LivingEntity looked) {
                descY = 10;
                StealthOverride.StealthData sd = StealthOverride.getStealth(looked);
                boolean adv = player.isShiftKeyDown();
                if (sd.allSeeing) describe(guiGraphics, "allsee", adv);
                if (sd.cheliceric) describe(guiGraphics, "cheliceric", adv);
                if (sd.deaf) describe(guiGraphics, "deaf", adv, ChatFormatting.GREEN);
                if (sd.eyeless) describe(guiGraphics, "eyeless", adv);
                if (sd.heatSeeking) describe(guiGraphics, "heat", adv);
                if (sd.lazy) describe(guiGraphics, "lazy", adv, ChatFormatting.GREEN);
                if (sd.mindful) describe(guiGraphics, "mindful", adv);
                if (sd.nightvision) describe(guiGraphics, "night", adv, ChatFormatting.RED);
                if (sd.observant) describe(guiGraphics, "observant", adv);
                if (sd.perceptive) describe(guiGraphics, "perceptive", adv);
                if (sd.quiet) describe(guiGraphics, "quiet", adv, ChatFormatting.GREEN);
                if (sd.skeptical) describe(guiGraphics, "skeptical", adv, ChatFormatting.GREEN);
                if (sd.vigil) describe(guiGraphics, "vigilant", adv);
                if (sd.wary) describe(guiGraphics, "wary", adv);
            }
        }
    }
}
