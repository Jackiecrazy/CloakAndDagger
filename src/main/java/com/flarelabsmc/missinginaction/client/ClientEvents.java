package com.flarelabsmc.missinginaction.client;

import com.flarelabsmc.missinginaction.MissingInAction;
import com.flarelabsmc.missinginaction.config.ClientConfig;
import com.flarelabsmc.missinginaction.networking.ShoutPacket;
import com.flarelabsmc.missinginaction.networking.StealthChannel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MissingInAction.MODID)
public class ClientEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleInput(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.phase != TickEvent.Phase.END) return;
        if (Keybind.SHOUT.getKeyConflictContext().isActive() && Keybind.SHOUT.consumeClick() && mc.player.isAlive()) {
            StealthChannel.INSTANCE.sendToServer(new ShoutPacket(ClientConfig.shout));
        }
    }
}