package jackiecrazy.cloakanddagger;

import jackiecrazy.cloakanddagger.api.WarAttributes;
import jackiecrazy.cloakanddagger.capability.goal.GoalCapability;
import jackiecrazy.cloakanddagger.capability.goal.IGoalHelper;
import jackiecrazy.cloakanddagger.capability.kits.IKitItemStack;
import jackiecrazy.cloakanddagger.capability.kits.KitCapability;
import jackiecrazy.cloakanddagger.capability.vision.CombatStorage;
import jackiecrazy.cloakanddagger.capability.vision.DummyVision;
import jackiecrazy.cloakanddagger.capability.vision.IVision;
import jackiecrazy.cloakanddagger.capability.skill.DummySkillCap;
import jackiecrazy.cloakanddagger.capability.skill.ISkillCapability;
import jackiecrazy.cloakanddagger.capability.skill.SkillStorage;
import jackiecrazy.cloakanddagger.capability.status.DummyMarkCap;
import jackiecrazy.cloakanddagger.capability.status.IMark;
import jackiecrazy.cloakanddagger.capability.status.StatusStorage;
import jackiecrazy.cloakanddagger.capability.weaponry.DummyCombatItemCap;
import jackiecrazy.cloakanddagger.capability.weaponry.ICombatItemCapability;
import jackiecrazy.cloakanddagger.client.Keybinds;
import jackiecrazy.cloakanddagger.command.WarDanceCommand;
import jackiecrazy.cloakanddagger.compat.ElenaiCompat;
import jackiecrazy.cloakanddagger.compat.WarCompat;
import jackiecrazy.cloakanddagger.config.*;
import jackiecrazy.cloakanddagger.networking.*;
import jackiecrazy.cloakanddagger.potion.WarEffects;
import jackiecrazy.cloakanddagger.skill.Skill;
import jackiecrazy.cloakanddagger.skill.WarSkills;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegisterCommandsEvent;
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
@Mod("cloakanddagger")
public class CloakAndDagger {
    public static final String MODID = "cloakanddagger";
    public static final Random rand = new Random();

    public static final Logger LOGGER = LogManager.getLogger();
    public static final GameRules.RuleKey<GameRules.BooleanValue> GATED_SKILLS = GameRules.register("lockWarSkills", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false)); //Blessed be the TF

    public CloakAndDagger() {
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
        MinecraftForge.EVENT_BUS.addListener(this::commands);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(IVision.class, new CombatStorage(), DummyVision::new);
        CapabilityManager.INSTANCE.register(ISkillCapability.class, new SkillStorage(), DummySkillCap::new);
        CapabilityManager.INSTANCE.register(ICombatItemCapability.class, new DummyCombatItemCap.Storage(), DummyCombatItemCap::new);
        CapabilityManager.INSTANCE.register(IKitItemStack.class, new KitCapability.Storage(), KitCapability::new);
        CapabilityManager.INSTANCE.register(IMark.class, new StatusStorage(), DummyMarkCap::new);
        CapabilityManager.INSTANCE.register(IGoalHelper.class, new GoalCapability.Storage(), GoalCapability::new);
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
        CombatChannel.INSTANCE.registerMessage(index++, SelectSkillPacket.class, new SelectSkillPacket.CombatEncoder(), new SelectSkillPacket.CombatDecoder(), new SelectSkillPacket.CombatHandler());
        CombatChannel.INSTANCE.registerMessage(index++, EvokeSkillPacket.class, new EvokeSkillPacket.EvokeEncoder(), new EvokeSkillPacket.EvokeDecoder(), new EvokeSkillPacket.EvokeHandler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateSkillSelectionPacket.class, new UpdateSkillSelectionPacket.UpdateSkillEncoder(), new UpdateSkillSelectionPacket.UpdateSkillDecoder(), new UpdateSkillSelectionPacket.UpdateSkillHandler());
        CombatChannel.INSTANCE.registerMessage(index++, SyncSkillPacket.class, new SyncSkillPacket.SyncSkillEncoder(), new SyncSkillPacket.SyncSkillDecoder(), new SyncSkillPacket.SyncSkillHandler());
        CombatChannel.INSTANCE.registerMessage(index++, ManualParryPacket.class, new ManualParryPacket.ParryEncoder(), new ManualParryPacket.ParryDecoder(), new ManualParryPacket.ParryHandler());
        CombatChannel.INSTANCE.registerMessage(index++, UpdateTargetPacket.class, new UpdateTargetPacket.UpdateTargetEncoder(), new UpdateTargetPacket.UpdateTargetDecoder(), new UpdateTargetPacket.UpdateTargetHandler());
        CombatChannel.INSTANCE.registerMessage(index++, ShoutPacket.class, new ShoutPacket.ShoutEncoder(), new ShoutPacket.ShoutDecoder(), new ShoutPacket.ShoutHandler());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        ClientConfig.bake();
        ClientRegistry.registerKeyBinding(Keybinds.COMBAT);
        ClientRegistry.registerKeyBinding(Keybinds.CAST);
        ClientRegistry.registerKeyBinding(Keybinds.SELECT);
        ClientRegistry.registerKeyBinding(Keybinds.BINDCAST);
        ClientRegistry.registerKeyBinding(Keybinds.PARRY);
        ClientRegistry.registerKeyBinding(Keybinds.SHOUT);
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

    private void commands(final RegisterCommandsEvent event) {
        WarDanceCommand.register(event.getDispatcher());
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
                if (!event.has(t, Attributes.LUCK)) event.add(t, Attributes.LUCK, 0);
                for (RegistryObject<Attribute> a : WarAttributes.ATTRIBUTES.getEntries()) {
                    if (!event.has(t, a.get())) {
                        if (GeneralConfig.debug)
                            LOGGER.debug("civilly registering " + a.getId() + " to " + a.getId());
                        event.add(t, a.get());
                    }
                }
            }
        }
    }
}
