package jackiecrazy.cloakanddagger.client;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.networking.StealthChannel;
import jackiecrazy.cloakanddagger.networking.RequestUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.math.RoundingMode;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CloakAndDagger.MODID)
public class ClientEvents {
    private static Entity lastTickLookAt;

    static {
        RenderEvents.formatter.setRoundingMode(RoundingMode.DOWN);
        RenderEvents.formatter.setMinimumFractionDigits(1);
        RenderEvents.formatter.setMaximumFractionDigits(1);
    }

    @SubscribeEvent
    public static void tickPlayer(TickEvent.ClientTickEvent e) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p != null && !mc.isPaused()) {
            if (e.phase == TickEvent.Phase.START) {
                Entity look = RenderEvents.getEntityLookedAt(p, 32);
                if (look != lastTickLookAt) {
                    lastTickLookAt = look;
                    if (look instanceof LivingEntity && look.isAlive())
                        StealthChannel.INSTANCE.sendToServer(new RequestUpdatePacket(look.getId()));
                    else
                        StealthChannel.INSTANCE.sendToServer(new RequestUpdatePacket(-1));
                }
            }
        }
    }

}
