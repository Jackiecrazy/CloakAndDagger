package com.flarelabsmc.missinginaction.client;

import com.flarelabsmc.missinginaction.MissingInAction;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = MissingInAction.MODID)
public class Keybind {
    public static final KeyMapping SHOUT = new KeyMapping("mia.shout", KeyConflictContext.IN_GAME, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T, "key.categories.gameplay");

    @SubscribeEvent
    public static void inputs(RegisterKeyMappingsEvent event) {
        event.register(SHOUT);
    }
}
