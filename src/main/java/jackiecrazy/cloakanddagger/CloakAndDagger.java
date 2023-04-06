package jackiecrazy.cloakanddagger;

import jackiecrazy.cloakanddagger.capability.vision.IVision;
import jackiecrazy.cloakanddagger.client.StealthOverlay;
import jackiecrazy.cloakanddagger.config.*;
import jackiecrazy.cloakanddagger.networking.*;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.utils.StealthUtils;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
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

// The value here should match an entry in the META-INF/mods.toml file
@Mod("cloakanddagger")
public class CloakAndDagger {
    public static final String MODID = "cloakanddagger";
    public static final Random rand = new Random();

    public static final Logger LOGGER = LogManager.getLogger();

    public CloakAndDagger() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::caps);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerOverlay);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(MODID), MODID);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GeneralConfig.CONFIG_SPEC, MODID + "/stealth.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SoundConfig.CONFIG_SPEC, MODID + "/sound.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobConfig.CONFIG_SPEC, MODID + "/mob.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC, MODID + "/client.toml");
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

    }

    private void setup(final FMLCommonSetupEvent event) {
        StealthUtils.INSTANCE = new StealthOverride();
        // some preinit code
        int index = 0;
        StealthChannel.INSTANCE.registerMessage(index++, UpdateClientPacket.class, new UpdateClientPacket.UpdateClientEncoder(), new UpdateClientPacket.UpdateClientDecoder(), new UpdateClientPacket.UpdateClientHandler());
        StealthChannel.INSTANCE.registerMessage(index++, RequestUpdatePacket.class, new RequestUpdatePacket.RequestUpdateEncoder(), new RequestUpdatePacket.RequestUpdateDecoder(), new RequestUpdatePacket.RequestUpdateHandler());
        StealthChannel.INSTANCE.registerMessage(index++, UpdateTargetPacket.class, new UpdateTargetPacket.UpdateTargetEncoder(), new UpdateTargetPacket.UpdateTargetDecoder(), new UpdateTargetPacket.UpdateTargetHandler());
        StealthChannel.INSTANCE.registerMessage(index++, ShoutPacket.class, new ShoutPacket.ShoutEncoder(), new ShoutPacket.ShoutDecoder(), new ShoutPacket.ShoutHandler());
    }

    private void caps(final RegisterCapabilitiesEvent event) {
        event.register(IVision.class);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        ClientConfig.bake();
    }

    private void registerOverlay(final RegisterGuiOverlaysEvent event) {
        // do something that can only be done on the client
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "cloakdagger", new StealthOverlay());
    }

    @SubscribeEvent
    public void onJsonListener(AddReloadListenerEvent event) {
        WeaponStats.register(event);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
    }
}