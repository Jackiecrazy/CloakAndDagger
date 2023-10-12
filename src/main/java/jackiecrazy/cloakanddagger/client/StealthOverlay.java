package jackiecrazy.cloakanddagger.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.action.PermissionData;
import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import jackiecrazy.cloakanddagger.config.ClientConfig;
import jackiecrazy.footwork.config.DisplayConfigUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class StealthOverlay implements IGuiOverlay {
    private static final ResourceLocation stealth = new ResourceLocation(CloakAndDagger.MODID, "textures/hud/stealth.png");
    private double prevTick = 0;
    private int lastTick = 0;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (!PermissionData.getCap(mc.player).canSee()) return;
        if (mc.getCameraEntity() instanceof Player && mc.player != null && !mc.options.hideGui) {
            LocalPlayer player = mc.player;
            Entity look = RenderEvents.getEntityLookedAt(player, 32);
            if (look instanceof LivingEntity) {
                LivingEntity looked = (LivingEntity) look;
                stealth:
                {
                    if (ClientConfig.CONFIG.stealth.enabled) {
                        Pair<Integer, Integer> pair = DisplayConfigUtils.translateCoords(ClientConfig.CONFIG.stealth, width, height);
                        final StealthInfo info = StealthInfo.stealthInfo(looked);
                        double dist = info.getRange();
                        int shift = 0;
                        switch (info.getAwareness()) {
                            case ALERT:
                                break stealth;
                            case DISTRACTED:
                                shift = 1;
                                break;
                            case UNAWARE:
                                if (Minecraft.getInstance().player != null)
                                    shift = looked.distanceToSqr(Minecraft.getInstance().player) < dist * dist ? 2 : 3;
                                break;
                        }
                        if (info.getRange() < 0)
                            shift = 0;
                        guiGraphics.blit(stealth, pair.getFirst() - 16, pair.getSecond() - 8, 0, shift * 16, 32, 16, 64, 64);
                        if (info.getAwareness() == StealthUtils.Awareness.UNAWARE) {
                            RenderSystem.setShaderColor(1, 0, 0, 1);
                            guiGraphics.blit(stealth, pair.getFirst() - 16, pair.getSecond() - 8, 0, shift * 16, (int) (32 * (prevTick + (SenseData.getCap(looked).getDetection(player) - prevTick) * (partialTick))), 16, 64, 64);
                            RenderSystem.setShaderColor(1, 1, 1, 1);
                        }
                        if (player.tickCount != lastTick) {
                            lastTick = player.tickCount;
                            prevTick = SenseData.getCap(looked).getDetection(player);
                        }
                    }
                }
            }
        }
    }
}
