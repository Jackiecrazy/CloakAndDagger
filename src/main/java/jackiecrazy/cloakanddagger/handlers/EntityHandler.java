package jackiecrazy.cloakanddagger.handlers;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.vision.IVision;
import jackiecrazy.cloakanddagger.capability.vision.VisionData;
import jackiecrazy.cloakanddagger.config.GeneralConfig;
import jackiecrazy.cloakanddagger.entity.ai.InvestigateSoundGoal;
import jackiecrazy.cloakanddagger.networking.StealthChannel;
import jackiecrazy.cloakanddagger.networking.UpdateTargetPacket;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.api.WarAttributes;
import jackiecrazy.footwork.capability.goal.GoalCapabilityProvider;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.LuckUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = CloakAndDagger.MODID)
public class EntityHandler {
    public static final HashMap<Player, Entity> mustUpdate = new HashMap<>();
    public static final ConcurrentHashMap<Tuple<Level, BlockPos>, Float> alertTracker = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void start(ServerStartingEvent e) {
        mustUpdate.clear();
        alertTracker.clear();
    }

    @SubscribeEvent
    public static void stop(ServerStoppingEvent e) {
        mustUpdate.clear();
        alertTracker.clear();
    }

    @SubscribeEvent
    public static void caps(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Mob)
            e.addCapability(new ResourceLocation("wardance:targeting"), new GoalCapabilityProvider());
    }

    @SubscribeEvent
    public static void takeThis(EntityJoinWorldEvent e) {
        if (e.getEntity() instanceof Mob) {
            Mob mob = (Mob) e.getEntity();
            if (e.getEntity() instanceof PathfinderMob) {
                PathfinderMob creature = (PathfinderMob) e.getEntity();
                if (!StealthOverride.stealthMap.getOrDefault(creature.getType().getRegistryName(), StealthOverride.STEALTH).isDeaf())
                    mob.goalSelector.addGoal(0, new InvestigateSoundGoal(creature));
            }
        }
    }

    @SubscribeEvent
    public static void sneak(final LivingEvent.LivingVisibilityEvent e) {
        /*
        out of LoS, reduce by 80%
        light, reduce by 80%
        speed, reduce by 50%
        can't see, reduce by 90%
        each stat scaled by stealth logarithmically. 10 stealth=halved bonus
        max is 0.045 in absolute darkness standing still behind
        min is 0.595 in above conditions but full armor
         */
        if (e.getLookingEntity() != e.getEntityLiving() && e.getLookingEntity() instanceof LivingEntity) {
            double mult = 1;
            LivingEntity sneaker = e.getEntityLiving(), watcher = (LivingEntity) e.getLookingEntity();
            StealthOverride.StealthData sd = StealthOverride.stealthMap.getOrDefault(watcher.getType().getRegistryName(), StealthOverride.STEALTH);
            if (StealthOverride.stealthMap.getOrDefault(sneaker.getType().getRegistryName(), StealthOverride.STEALTH).isCheliceric())
                return;
            if (watcher.getKillCredit() != sneaker && watcher.getLastHurtByMob() != sneaker && watcher.getLastHurtMob() != sneaker && (!(watcher instanceof Mob) || ((Mob) watcher).getTarget() != sneaker)) {
                double stealth = GeneralUtils.getAttributeValueSafe(sneaker, WarAttributes.STEALTH.get());
                double negMult = 1;
                double posMult = 1;
                //each level of negative stealth reduces effectiveness by 5%
                while (stealth < 0) {
                    negMult -= 0.05;
                    stealth++;
                }
                //each level of positive stealth multiplies ineffectiveness by 0.93
                while (stealth > 1) {
                    posMult *= 0.933;
                    stealth--;
                }
                //blinded mobs cannot see
                if (watcher.hasEffect(MobEffects.BLINDNESS) && !sd.isEyeless())
                    mult /= 8;
                //mobs that can't see behind their backs get a hefty debuff
                if (!sd.isAllSeeing() && !GeneralUtils.isFacingEntity(watcher, sneaker, GeneralConfig.baseHorizontalDetection, GeneralConfig.baseVerticalDetection))
                    mult *= (1 - (0.7 * negMult)) * posMult;
                float lightMalus=0;
                //stay dark, stay dank
                if (!sd.isNightVision() && !watcher.hasEffect(MobEffects.NIGHT_VISION) && !sneaker.hasEffect(MobEffects.GLOWING) && sneaker.getRemainingFireTicks() <= 0) {
                    Level world = sneaker.level;
                    if (world.isAreaLoaded(sneaker.blockPosition(), 5) && world.isAreaLoaded(watcher.blockPosition(), 5)) {
                        final int slight = StealthOverride.getActualLightLevel(world, sneaker.blockPosition());
                        final int wlight = VisionData.getCap(watcher).getRetina();
                        float m = (1 + (slight - wlight) / 15f) * (slight + 3) / 15f;//ugly, but welp.
                        lightMalus = Mth.clamp(1 - m, 0f, 0.7f);
                        mult *= (1 - (lightMalus * negMult)) * posMult;
                    }
                }
                //slow is smooth, smooth is fast, modified by light
                if (!sd.isPerceptive()) {
                    final double speedSq = GeneralUtils.getSpeedSq(sneaker);
                    mult *= (1 - (0.5 - Mth.sqrt((float) speedSq) * 2 * posMult) * negMult * lightMalus);
                }

            }
            //is this LoS?
            if (!sd.isHeatSeeking() && GeneralUtils.viewBlocked(watcher, sneaker, true))
                mult *= (0.4);
            e.modifyVisibility(Mth.clamp(mult, 0.001, 1));
        }
    }

    @SubscribeEvent
    public static void pray(LivingSetAttackTargetEvent e) {
        if (e.getTarget() == null) return;
        if (!(e.getEntityLiving() instanceof Mob)) return;
        final Mob mob = (Mob) e.getEntityLiving();
        if (mob.hasEffect(FootworkEffects.FEAR.get()) || mob.hasEffect(FootworkEffects.CONFUSION.get()) || mob.hasEffect(FootworkEffects.SLEEP.get()))
            mob.setTarget(null);
        if (mob.getLastHurtByMob() != e.getTarget() && !GeneralUtils.isFacingEntity(mob, e.getTarget(), GeneralConfig.baseHorizontalDetection, GeneralConfig.baseVerticalDetection)) {
            StealthOverride.StealthData sd = StealthOverride.stealthMap.getOrDefault(mob.getType().getRegistryName(), StealthOverride.STEALTH);
            if (sd.isAllSeeing() || sd.isWary()) return;
            //outside of LoS, perform luck check. Pray to RNGesus!
            double luckDiff = GeneralUtils.getAttributeValueSafe(e.getTarget(), Attributes.LUCK) - GeneralUtils.getAttributeValueSafe(mob, Attributes.LUCK);
            if (luckDiff <= 0 || !LuckUtils.luckRoll(e.getTarget(), (float) (luckDiff / (2 + luckDiff)))) {
                //you failed!
                if (sd.isSkeptical()) {
                    mob.setTarget(null);
                    mob.lookAt(EntityAnchorArgument.Anchor.FEET, e.getTarget().position());//.getLookController().setLookPositionWithEntity(e.getTarget(), 0, 0);
                }
            } else {
                //success!
                mob.setTarget(null);
                if (!sd.isLazy())
                    mob.lookAt(EntityAnchorArgument.Anchor.FEET, e.getTarget().position());//.getLookController().setLookPositionWithEntity(e.getTarget(), 0, 0);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void sync(LivingSetAttackTargetEvent e) {
        if (!e.getEntityLiving().level.isClientSide())
            StealthChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(e::getEntityLiving), new UpdateTargetPacket(e.getEntityLiving().getId(), e.getTarget() == null ? -1 : e.getTarget().getId()));
    }

    @SubscribeEvent
    public static void nigerundayo(final PotionEvent.PotionAddedEvent e) {
        if (e.getPotionEffect().getEffect() == MobEffects.BLINDNESS) {
            if (e.getEntityLiving() instanceof Mob)
                ((Mob) e.getEntityLiving()).setTarget(null);
            e.getEntityLiving().setLastHurtByMob(null);
        }
    }

    @SubscribeEvent
    public static void lure(TickEvent.ServerTickEvent e) {
        Iterator<Map.Entry<Tuple<Level, BlockPos>, Float>> it = alertTracker.entrySet().iterator();
        {
            while (it.hasNext()) {
                Map.Entry<Tuple<Level, BlockPos>, Float> n = it.next();
                if (n.getKey().getA().isAreaLoaded(n.getKey().getB(), n.getValue().intValue())) {
                    for (PathfinderMob c : (n.getKey().getA().getEntitiesOfClass(PathfinderMob.class, new AABB(n.getKey().getB()).inflate(n.getValue())))) {
                        if (StealthUtils.INSTANCE.getAwareness(null, c) == StealthOverride.Awareness.UNAWARE && !StealthOverride.stealthMap.getOrDefault(c.getType().getRegistryName(), StealthOverride.STEALTH).isDeaf()) {
                            c.goalSelector.disableControlFlag(Goal.Flag.MOVE);
                            c.getNavigation().stop();
                            c.getNavigation().moveTo(c.getNavigation().createPath(n.getKey().getB(), (int) (n.getValue() + 3)), 1);
                            BlockPos vec = n.getKey().getB();
                            c.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(vec.getX(), vec.getY(), vec.getZ()));
                            c.getCapability(GoalCapabilityProvider.CAP).ifPresent(a -> a.setSoundLocation(n.getKey().getB()));
                        }
                    }
                }
            }
        }
        alertTracker.clear();
    }

    @SubscribeEvent
    public static void tickMobs(LivingEvent.LivingUpdateEvent e) {
        LivingEntity elb = e.getEntityLiving();
        if (!elb.level.isClientSide && !(elb instanceof Player)) {
            IVision cap = VisionData.getCap(elb);
            if (elb.tickCount % 100 == 0 || mustUpdate.containsValue(elb))
                cap.serverTick();
        }
    }
}
