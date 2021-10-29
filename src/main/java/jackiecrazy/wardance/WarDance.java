package jackiecrazy.wardance;

import jackiecrazy.wardance.api.WarAttributes;
import jackiecrazy.wardance.capability.kits.IKitItemStack;
import jackiecrazy.wardance.capability.kits.KitCapability;
import jackiecrazy.wardance.capability.resources.CombatStorage;
import jackiecrazy.wardance.capability.resources.DummyCombatCap;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.DummySkillCap;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.skill.SkillStorage;
import jackiecrazy.wardance.capability.status.DummyMarkCap;
import jackiecrazy.wardance.capability.status.IMark;
import jackiecrazy.wardance.capability.status.StatusStorage;
import jackiecrazy.wardance.capability.weaponry.DummyCombatItemCap;
import jackiecrazy.wardance.capability.weaponry.ICombatItemCapability;
import jackiecrazy.wardance.client.Keybinds;
import jackiecrazy.wardance.compat.ElenaiCompat;
import jackiecrazy.wardance.compat.WarCompat;
import jackiecrazy.wardance.config.*;
import jackiecrazy.wardance.networking.*;
import jackiecrazy.wardance.potion.WarEffects;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("wardance")
public class WarDance {
    public static final String MODID = "wardance";
    public static final Random rand = new Random();

    public static final Logger LOGGER = LogManager.getLogger();
    public static final GameRules.RuleKey<GameRules.BooleanValue> GATED_SKILLS = GameRules.register("lockWarSkills", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false)); //Blessed be the TF

    public WarDance() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(MODID), MODID);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GeneralConfig.CONFIG_SPEC, MODID + "/general.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, StealthConfig.CONFIG_SPEC, MODID + "/stealth.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CombatConfig.CONFIG_SPEC, MODID + "/combat.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ItemConfig.CONFIG_SPEC, MODID + "/items.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ResourceConfig.CONFIG_SPEC, MODID + "/resources.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC, MODID + "/client.toml");
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        WarAttributes.ATTRIBUTES.register(bus);
        WarSkills.SKILLS.makeRegistry("skills", RegistryBuilder::new);
        WarSkills.SKILLS.register(bus);
        WarEffects.EFFECTS.register(bus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(ICombatCapability.class, new CombatStorage(), DummyCombatCap::new);
        CapabilityManager.INSTANCE.register(ISkillCapability.class, new SkillStorage(), DummySkillCap::new);
        CapabilityManager.INSTANCE.register(ICombatItemCapability.class, new DummyCombatItemCap.Storage(), DummyCombatItemCap::new);
        CapabilityManager.INSTANCE.register(IKitItemStack.class, new KitCapability.Storage(), KitCapability::new);
        CapabilityManager.INSTANCE.register(IMark.class, new StatusStorage(), DummyMarkCap::new);
        // some preinit code
        int index = 0;
        CombatChannel.INSTANCE.registerMessage(index++, UpdateClientPacket.class, new UpdateClientPacket.UpdateClientEncoder(), new UpdateClientPacket.UpdateClientDecoder(), new UpdateClientPacket.UpdateClientHandler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateAfflictionPacket.class, new UpdateAfflictionPacket.UpdateClientEncoder(), new UpdateAfflictionPacket.UpdateClientDecoder(), new UpdateAfflictionPacket.UpdateClientHandler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateAttackPacket.class, new UpdateAttackPacket.UpdateAttackEncoder(), new UpdateAttackPacket.UpdateAttackDecoder(), new UpdateAttackPacket.UpdateAttackHandler());
        CombatChannel.INSTANCE.registerMessage(index++, DodgePacket.class, new DodgePacket.DodgeEncoder(), new DodgePacket.DodgeDecoder(), new DodgePacket.DodgeHandler());
        CombatChannel.INSTANCE.registerMessage(index++, RequestUpdatePacket.class, new RequestUpdatePacket.RequestUpdateEncoder(), new RequestUpdatePacket.RequestUpdateDecoder(), new RequestUpdatePacket.RequestUpdateHandler());
        CombatChannel.INSTANCE.registerMessage(index++, CombatModePacket.class, new CombatModePacket.CombatEncoder(), new CombatModePacket.CombatDecoder(), new CombatModePacket.CombatHandler());
        CombatChannel.INSTANCE.registerMessage(index++, RequestSweepPacket.class, new RequestSweepPacket.RequestSweepEncoder(), new RequestSweepPacket.RequestSweepDecoder(), new RequestSweepPacket.RequestSweepHandler());
        CombatChannel.INSTANCE.registerMessage(index++, RequestAttackPacket.class, new RequestAttackPacket.RequestAttackEncoder(), new RequestAttackPacket.RequestAttackDecoder(), new RequestAttackPacket.RequestAttackHandler());
        CombatChannel.INSTANCE.registerMessage(index++, CastSkillPacket.class, new CastSkillPacket.CombatEncoder(), new CastSkillPacket.CombatDecoder(), new CastSkillPacket.CombatHandler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateSkillSelectionPacket.class, new UpdateSkillSelectionPacket.UpdateSkillEncoder(), new UpdateSkillSelectionPacket.UpdateSkillDecoder(), new UpdateSkillSelectionPacket.UpdateSkillHandler());
        CombatChannel.INSTANCE.registerMessage(index++, SyncSkillPacket.class, new SyncSkillPacket.SyncSkillEncoder(), new SyncSkillPacket.SyncSkillDecoder(), new SyncSkillPacket.SyncSkillHandler());
        CombatChannel.INSTANCE.registerMessage(index++, ManualParryPacket.class, new ManualParryPacket.ParryEncoder(), new ManualParryPacket.ParryDecoder(), new ManualParryPacket.ParryHandler());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        ClientConfig.bake();
        ClientRegistry.registerKeyBinding(Keybinds.COMBAT);
        ClientRegistry.registerKeyBinding(Keybinds.CAST);
        ClientRegistry.registerKeyBinding(Keybinds.SELECT);
        ClientRegistry.registerKeyBinding(Keybinds.QUICKCAST);
        ClientRegistry.registerKeyBinding(Keybinds.PARRY);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        //InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        WarCompat.checkCompatStatus();
        if (WarCompat.elenaiDodge)
            MinecraftForge.EVENT_BUS.register(ElenaiCompat.class);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void skills(final RegistryEvent.Register<Skill> e) {
        }

        @SubscribeEvent
        public static void attributes(EntityAttributeModificationEvent event) {
            for (EntityType<? extends LivingEntity> t : event.getTypes()) {
                if (!event.has(t, Attributes.ATTACK_SPEED)) event.add(t, Attributes.ATTACK_SPEED, 4);
                if (!event.has(t, Attributes.LUCK)) event.add(t, Attributes.LUCK, 4);
                for (RegistryObject<Attribute> a : WarAttributes.ATTRIBUTES.getEntries()) {
                    if (!event.has(t, a.get())) {
                        LOGGER.debug("civilly registering " + a.getId() + " to " + a.getId());
                        event.add(t, a.get());
                    }
                }
            }
        }

        @SubscribeEvent()
        public void skillRegistry(RegistryEvent.NewRegistry event) {
            //WarSkills.SKILLS=new RegistryBuilder<Skill>().setName(new ResourceLocation(MODID, "skills")).setType(Skill.class).create();
        }


    }
}
