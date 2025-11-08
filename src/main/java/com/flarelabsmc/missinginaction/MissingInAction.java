package com.flarelabsmc.missinginaction;

import com.flarelabsmc.missinginaction.api.StealthUtils;
import com.flarelabsmc.missinginaction.capability.vision.ISense;
import com.flarelabsmc.missinginaction.config.ClientConfig;
import com.flarelabsmc.missinginaction.config.GeneralConfig;
import com.flarelabsmc.missinginaction.config.SoundConfig;
import com.flarelabsmc.missinginaction.config.WeaponStats;
import com.flarelabsmc.missinginaction.networking.*;
import com.flarelabsmc.missinginaction.entity.MiAAttributes;
import com.flarelabsmc.missinginaction.entity.MiAEntities;
import com.flarelabsmc.missinginaction.handlers.EntityHandler;
import com.flarelabsmc.missinginaction.utils.CombatUtils;
import com.flarelabsmc.missinginaction.utils.StealthOverride;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod("missinginaction")
public class MissingInAction {
    public static final String MODID = "missinginaction";
    public static final Random rand = new Random();

    public static final Logger LOGGER = LogManager.getLogger();

    public MissingInAction() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::caps);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EntityHandler::addAttributes);

        MinecraftForge.EVENT_BUS.register(this);
        FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(MODID));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GeneralConfig.CONFIG_SPEC, MODID + "/stealth.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SoundConfig.CONFIG_SPEC, MODID + "/sound.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC, MODID + "/client.toml");
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MiAAttributes.registerAttributes(bus);
        MiAEntities.register(bus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        StealthUtils.INSTANCE = new StealthOverride();
        int index = 0;
        StealthChannel.INSTANCE.registerMessage(index++, UpdateClientPacket.class, new UpdateClientPacket.UpdateClientEncoder(), new UpdateClientPacket.UpdateClientDecoder(), new UpdateClientPacket.UpdateClientHandler());
        StealthChannel.INSTANCE.registerMessage(index++, RequestUpdatePacket.class, new RequestUpdatePacket.RequestUpdateEncoder(), new RequestUpdatePacket.RequestUpdateDecoder(), new RequestUpdatePacket.RequestUpdateHandler());
        StealthChannel.INSTANCE.registerMessage(index++, UpdateTargetPacket.class, new UpdateTargetPacket.UpdateTargetEncoder(), new UpdateTargetPacket.UpdateTargetDecoder(), new UpdateTargetPacket.UpdateTargetHandler());
        StealthChannel.INSTANCE.registerMessage(index++, ShoutPacket.class, new ShoutPacket.ShoutEncoder(), new ShoutPacket.ShoutDecoder(), new ShoutPacket.ShoutHandler());
        StealthChannel.INSTANCE.registerMessage(index++, SyncItemDataPacket.class, new SyncItemDataPacket.Encoder(), new SyncItemDataPacket.Decoder(), new SyncItemDataPacket.Handler());
        StealthChannel.INSTANCE.registerMessage(index++, SyncTagDataPacket.class, new SyncTagDataPacket.Encoder(), new SyncTagDataPacket.Decoder(), new SyncTagDataPacket.Handler());
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            CombatUtils.sendItemData(sp);
        }
    }

    @SubscribeEvent
    public static void reload(OnDatapackSyncEvent e){
        for(ServerPlayer p: e.getPlayerList().getPlayers()){
            CombatUtils.sendItemData(p);
        }
    }

    private void caps(final RegisterCapabilitiesEvent event) {
        event.register(ISense.class);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        ClientConfig.bake();
    }

    @SubscribeEvent
    public void onJsonListener(AddReloadListenerEvent event) {
        WeaponStats.register(event);
    }
}