package jackiecrazy.cloakanddagger.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.action.PermissionData;
import jackiecrazy.cloakanddagger.config.ClientConfig;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.config.DisplayConfigUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class StealthOverlay implements IGuiOverlay {
    private static final ResourceLocation stealth = new ResourceLocation(CloakAndDagger.MODID, "textures/hud/stealth.png");

    @Override
    public void render(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {
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
                        final Tuple<StealthOverride.Awareness, Double> info = RenderEvents.stealthInfo(looked);
                        double dist = info.getB();
                        int shift = 0;
                        switch (info.getA()) {
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
                        if (info.getB() < 0)
                            shift = 0;
                        RenderSystem.setShaderTexture(0, stealth);
                        GuiComponent.blit(stack, pair.getFirst() - 16, pair.getSecond() - 8, 0, shift * 16, 32, 16, 64, 64);
                    }
                }
            }
        }
    }
}
