package jackiecrazy.cloakanddagger.client;

import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.action.PermissionData;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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

    private void describe(PoseStack stack, String tag, boolean advanced, ChatFormatting... format) {
        Minecraft mc = Minecraft.getInstance();
        mc.font.drawShadow(stack, Component.translatable("cloak.tag." + tag).withStyle(format), 4, descY, 0xffffff);
        if (advanced)
            mc.font.drawShadow(stack, Component.translatable("cloak.desc." + tag).withStyle(format), 8, descY + 8, 0xffffff);
        descY += 20;
    }

    @Override
    public void render(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (!PermissionData.getCap(mc.player).canSee()) return;
        if (mc.getCameraEntity() instanceof Player && mc.player != null && !mc.options.hideGui && mc.player.isScoping()) {
            LocalPlayer player = mc.player;
            Entity look = RenderEvents.getEntityLookedAt(player, 32);
            if (look instanceof LivingEntity looked) {
                descY = 10;
                StealthOverride.StealthData sd = StealthOverride.getStealth(looked);
                boolean adv = player.isShiftKeyDown();
                if (sd.isAllSeeing()) describe(stack, "allsee", adv);
                if (sd.isCheliceric()) describe(stack, "cheliceric", adv);
                if (sd.isDeaf()) describe(stack, "deaf", adv, ChatFormatting.GREEN);
                if (sd.isEyeless()) describe(stack, "eyeless", adv);
                if (sd.isHeatSeeking()) describe(stack, "heat", adv);
                if (sd.isLazy()) describe(stack, "lazy", adv, ChatFormatting.GREEN);
                if (sd.isMindful()) describe(stack, "mindful", adv);
                if (sd.isNightVision()) describe(stack, "night", adv, ChatFormatting.RED);
                if (sd.isObservant()) describe(stack, "observant", adv);
                if (sd.isPerceptive()) describe(stack, "perceptive", adv);
                if (sd.isQuiet()) describe(stack, "quiet", adv, ChatFormatting.GREEN);
                if (sd.isSkeptical()) describe(stack, "skeptical", adv, ChatFormatting.GREEN);
                if (sd.isVigilant()) describe(stack, "vigilant", adv);
                if (sd.isWary()) describe(stack, "wary", adv);
            }
        }
    }
}
