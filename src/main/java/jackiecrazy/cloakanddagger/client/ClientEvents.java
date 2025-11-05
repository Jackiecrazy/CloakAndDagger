package jackiecrazy.cloakanddagger.client;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.config.ClientConfig;
import jackiecrazy.cloakanddagger.networking.ShoutPacket;
import jackiecrazy.cloakanddagger.networking.StealthChannel;
import jackiecrazy.cloakanddagger.networking.RequestUpdatePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.math.RoundingMode;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CloakAndDagger.MODID)
public class ClientEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleInputEvent(InputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (Keybind.SHOUT.getKeyConflictContext().isActive() && Keybind.SHOUT.consumeClick() && mc.player.isAlive()) {
            StealthChannel.INSTANCE.sendToServer(new ShoutPacket(ClientConfig.shout));
        }
    }
}
