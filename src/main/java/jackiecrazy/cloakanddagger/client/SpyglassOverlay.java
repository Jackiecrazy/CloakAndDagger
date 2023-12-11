package jackiecrazy.cloakanddagger.client;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.action.PermissionData;
import jackiecrazy.cloakanddagger.config.StealthTags;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class SpyglassOverlay implements IGuiOverlay {
    private static final ResourceLocation stealth = new ResourceLocation(CloakAndDagger.MODID, "textures/hud/stealth.png");
    int descY = 10;

    private void describe(GuiGraphics graphics, boolean advanced, MutableComponent basic, MutableComponent advance) {
        Minecraft mc = Minecraft.getInstance();
        graphics.drawString(mc.font, basic, 4, descY, 0xffffff);
        if (advanced)
            graphics.drawString(mc.font, advance, 8, descY + 8, 0xffffff);
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
                boolean adv = player.isShiftKeyDown();
                EntityType<?> type = looked.getType();
                for (var o : StealthTags.MAP.entrySet()) {
                    if (type.is(o.getKey())) {
                        describe(guiGraphics, adv, o.getValue().getA(), o.getValue().getB());
                    }
                }
            }
        }
    }
}
