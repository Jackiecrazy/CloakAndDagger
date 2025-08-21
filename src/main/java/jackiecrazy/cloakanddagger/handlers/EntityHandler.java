package jackiecrazy.cloakanddagger.handlers;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.action.PermissionData;
import jackiecrazy.cloakanddagger.capability.vision.ISense;
import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import jackiecrazy.cloakanddagger.config.GeneralConfig;
import jackiecrazy.cloakanddagger.config.SoundConfig;
import jackiecrazy.cloakanddagger.config.StealthTags;
import jackiecrazy.cloakanddagger.entity.DecoyEntity;
import jackiecrazy.cloakanddagger.entity.ai.InvestigateSoundGoal;
import jackiecrazy.cloakanddagger.entity.ai.SearchLookGoal;
import jackiecrazy.cloakanddagger.mixin.RevengeAccessor;
import jackiecrazy.cloakanddagger.networking.StealthChannel;
import jackiecrazy.cloakanddagger.networking.UpdateTargetPacket;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.api.FootworkAttributes;
import jackiecrazy.footwork.capability.goal.GoalCapabilityProvider;
import jackiecrazy.footwork.entity.DummyEntity;
import jackiecrazy.footwork.entity.FootworkEntities;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.LuckUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = CloakAndDagger.MODID)
public class EntityHandler {
    public static final HashMap<Player, Entity> mustUpdate = new HashMap<>();
    public static final HashMap<LivingEntity, DummyEntity> lastDecoy = new HashMap<>();
    public static final ConcurrentHashMap<Tuple<Level, BlockPos>, Float> alertTracker = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void caps(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Mob m) {
            e.addCapability(new ResourceLocation("cloakanddagger:targeting"), new GoalCapabilityProvider());
            e.addCapability(new ResourceLocation("cloakanddagger:vision"), new SenseData(m));

        }
        if (e.getObject() instanceof Player p)
            e.addCapability(new ResourceLocation("cloakanddagger:permissions"), new PermissionData(p));
    }

    private static double compensation(double original, double max, double compensate) {
        return original + (max - original) * (1 - compensate);
    }

    @SubscribeEvent
    public static void lure(TickEvent.ServerTickEvent e) {
        int checked = 0;
        for (Map.Entry<Tuple<Level, BlockPos>, Float> n : alertTracker.entrySet()) {
            if (checked > SoundConfig.cap) break;
            if (n.getKey().getA().isAreaLoaded(n.getKey().getB(), n.getValue().intValue())) {
                for (PathfinderMob c : (n.getKey().getA().getEntitiesOfClass(PathfinderMob.class, new AABB(n.getKey().getB()).inflate(n.getValue())))) {
                    if (StealthUtils.INSTANCE.getAwareness(null, c) == StealthOverride.Awareness.UNAWARE) {
                        if (!c.getType().is(StealthTags.IGNORE_SOUND)) {
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
            checked++;
        }
        alertTracker.clear();
    }

    @SubscribeEvent
    public static void nigerundayo(final MobEffectEvent e) {
        if (e.getEffectInstance() != null && !e.getEntity().getType().is(StealthTags.IGNORE_BLINDNESS) && e.getEffectInstance().getEffect() == MobEffects.BLINDNESS) {
            if (e.getEntity() instanceof Mob)
                ((Mob) e.getEntity()).setTarget(null);
            e.getEntity().setLastHurtByMob(null);
        }
    }

    @SubscribeEvent
    public static void pray(LivingChangeTargetEvent e) {
        final LivingEntity newTarget = e.getNewTarget();
        if (newTarget == null) return;
        final LivingEntity originalTarget = e.getOriginalTarget();
        if (originalTarget instanceof DecoyEntity && CloakAndDagger.rand.nextFloat() < 0.3) return;
        if (!(e.getEntity() instanceof final Mob mob)) return;
        final ISense cap = SenseData.getCap(mob);
        if (mob.hasEffect(FootworkEffects.FEAR.get()) || mob.hasEffect(FootworkEffects.CONFUSION.get()) || mob.hasEffect(FootworkEffects.SLEEP.get()))
            e.setCanceled(true);
        if (lastDecoy.containsKey(originalTarget) && originalTarget.isInvisible() && !mob.getType().is(StealthTags.IGNORE_INVIS) && originalTarget == newTarget) {
            //shift aggro to decoy
            e.setNewTarget(lastDecoy.get(originalTarget));
        }
        //not (owner) or self revenge target
        if (mob instanceof OwnableEntity pet) {
            if (pet.getOwner() != null) {
                LivingEntity owner = pet.getOwner();
                if (owner.getLastHurtByMob() == newTarget || owner.getLastHurtMob() == newTarget)
                    return;
            }
        }
        if (mob.getLastHurtByMob() != newTarget) {
            if (cap.getDetection(newTarget) < 1 && !mob.getType().is(StealthTags.SKIP_SEARCH)) {
                //cancel unless "alert"
                cap.modifyDetection(e.getEntity(), 0);
                //Pray to RNGesus!
                double luckDiff = GeneralUtils.getAttributeValueSafe(newTarget, Attributes.LUCK) - GeneralUtils.getAttributeValueSafe(mob, Attributes.LUCK);
                if (!mob.getType().is(StealthTags.NEVER_LOOK) && (mob.getType().is(StealthTags.IGNORE_FOV) || mob.getType().is(StealthTags.IGNORE_LUCK) || luckDiff < 0 || !LuckUtils.luckRoll(newTarget, (float) (luckDiff / (1 + luckDiff))))) {
                    //you failed! mob begins searching
                    SenseData.getCap(mob).setLookingFor(newTarget);
                }
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void sneak(final LivingEvent.LivingVisibilityEvent e) {
        /*
        out of LoS, reduce by 80%
        light, reduce by 70%
        speed, reduce by 50%
        can't see, reduce by 90%
        each stat scaled by stealth logarithmically. 10 stealth=halved bonus
        max is 0.045 in absolute darkness standing still behind
        min is 0.595 in above conditions but full armor

        NEW:
        up to 0.4x from flat light
        up to 0.4x from light difference
        up to 0.5x from speed
        up to 0.2x from fov
        up to 0.5x from los
        up to 0.11x from blind
         */
        if (e.getLookingEntity() != e.getEntity() && e.getLookingEntity() instanceof LivingEntity watcher) {
            double hardMult = e.getVisibilityModifier();
            double mult = e.getVisibilityModifier();
            LivingEntity sneaker = e.getEntity();
            //bumbling fools don't get visibility changes
            if (sneaker.getType().is(StealthTags.NO_STEALTH)) return;
            double stealth = GeneralUtils.getAttributeValueSafe(sneaker, FootworkAttributes.STEALTH.get());
            //it's time for RNGesus!
            double luckDiff = GeneralUtils.getAttributeValueSafe(sneaker, Attributes.LUCK) - GeneralUtils.getAttributeValueSafe(watcher, Attributes.LUCK);
            stealth += CloakAndDagger.rand.nextDouble() * luckDiff * GeneralConfig.luck;
            double negMult = 1;
            double posMult = 1;
            //each level of negative stealth reduces effectiveness by 5%
            if (stealth < 0) {
                negMult += stealth / 20;//magic number for full diamond
            }
            //each level of positive stealth increases effectiveness by 0.95
            while (stealth >= 1) {
                posMult *= 0.95;
                stealth--;
            }
            negMult = Math.max(0, negMult);
            //initial detection, won't save you from counterattacking
            if (watcher.getKillCredit() != sneaker && watcher.getLastHurtByMob() != sneaker && (!(watcher instanceof Mob) || ((Mob) watcher).getTarget() != sneaker)) {
                //stay dark, stay dank
                //light is treated as "neutral" on light level 9
                //up to 60% from light difference, and up to 60% from flat light
                //if light 0, 60%. if light 9, 0%
                if (!watcher.getType().is(StealthTags.IGNORE_LIGHT) && !watcher.hasEffect(MobEffects.NIGHT_VISION) && !sneaker.hasEffect(MobEffects.GLOWING) && sneaker.getRemainingFireTicks() <= 0) {
                    Level world = sneaker.level();
                    if (world.isAreaLoaded(sneaker.blockPosition(), 5) && world.isAreaLoaded(watcher.blockPosition(), 5)) {
                        final int slight = StealthOverride.getActualLightLevel(world, sneaker.blockPosition()) + 1;
                        final int wlight = StealthOverride.getActualLightLevel(world, watcher.blockPosition()) + 1;
                        float magicLightCutoff = 10;
                        //light level adds a flat 6% per level around the 10 mark
                        if (slight < 10) {
                            mult *= (1 - compensation((magicLightCutoff - slight) * 0.07, 0.7, posMult));
                            hardMult *= (1 - compensation((magicLightCutoff - slight) * 0.02, 0.2, posMult));
                        }
                        //light level also adds 6% either way per level of difference between sneaker and watcher
                        double lightDiff = 1 - compensation((wlight - slight) * 0.07, 0.7, posMult);//higher is better
                        double hardLightDiff = 1 - compensation((wlight - slight) * 0.02, 0.2, posMult);//lower is better, 10 is baseline
                        mult *= (lightDiff);
                        hardMult *= hardLightDiff;
                    }
                }
                //slow is smooth, smooth is fast, not modified by light anymore because people keep saying it's too good
                if (!watcher.getType().is(StealthTags.IGNORE_MOTION)) {
                    final double speedSq = GeneralUtils.getSpeedSq(sneaker);
                    final float speed = Mth.sqrt((float) speedSq);
                    mult *= (1 - compensation(0.6 - speed * 2, 0.7, posMult));// * (1 - lightMalus)
                }
                //internally enforced 3 blocks of vision, the other two can bypass this
                //mult = Math.max(mult, 3 / (watcher.getAttributeValue(Attributes.FOLLOW_RANGE) + 1));
                //mobs that can't see behind their backs get a hefty debuff
                if (!watcher.getType().is(StealthTags.IGNORE_FOV) && !GeneralUtils.isFacingEntity(watcher, sneaker, GeneralConfig.baseHorizontalDetection, GeneralConfig.baseVerticalDetection))
                    mult *= (1 - (compensation(0.4, 0.8, posMult)));
            }
            //normalize values
            //mult = Math.min(1, mult);
            //blinded mobs cannot see
            if (watcher.hasEffect(MobEffects.BLINDNESS) && !watcher.getType().is(StealthTags.IGNORE_BLINDNESS)) {
                hardMult /= 11;
                mult /= 11;
            }
            //is this LoS?
            if (!watcher.getType().is(StealthTags.IGNORE_LOS) && GeneralUtils.viewBlocked(watcher, sneaker, true)) {
                hardMult *= 1 - compensation(0.5, 0.7, posMult);
                mult *= 1 - compensation(0.3, 0.5, posMult);
            }
            //dude you literally just bumped into me
            //mult = Math.max(mult, 2f / (watcher.getAttributeValue(Attributes.FOLLOW_RANGE) + 1));

            //normalizing for negative stealth
            hardMult = 1 - (1 - hardMult) * negMult;
            mult = 1 - (1 - mult) * negMult;

            e.modifyVisibility(Math.max(hardMult, 0));


            //if you are in detection range, add the range-modified multiplier to detection, otherwise subtract fixed amount
            if (e.getEntity() instanceof Player p && p.getAbilities().invulnerable) return;
            if (!watcher.level().isClientSide) {
                if (watcher.getType().is(StealthTags.SKIP_SEARCH)) return;//unnecessary
                final double maxDist = watcher.getAttributeValue(Attributes.FOLLOW_RANGE);
                double follow = maxDist * e.getVisibilityModifier();
                final double sqdist = e.getEntity().distanceToSqr(watcher);
                boolean out = sqdist > follow * follow;
                if (!out) {
                    //bout 3 or 4 seconds when naked, 2 when in chain, about 0.6 in diamond
                    //3.5+3*stealth/20
                    final double add = mult / 20;
                    //final double add = Mth.clamp(0.5 + ((follow - Math.sqrt(sqdist)) / (follow)) / 2, 0, 1) * modifiedVisibility / 20;
                    SenseData.getCap(watcher).modifyDetection(e.getEntity(), (float) add);
                }
            }
        }
    }

    @SubscribeEvent
    public static void start(ServerStartingEvent e) {
        mustUpdate.clear();
        lastDecoy.clear();
        alertTracker.clear();
    }

    @SubscribeEvent
    public static void stop(ServerStoppingEvent e) {
        mustUpdate.clear();
        lastDecoy.clear();
        alertTracker.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void sync(LivingChangeTargetEvent e) {
        if (!e.getEntity().level().isClientSide())
            StealthChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(e::getEntity), new UpdateTargetPacket(e.getEntity().getId(), e.getNewTarget() == null ? -1 : e.getNewTarget().getId()));
    }

    @SubscribeEvent
    public static void takeThis(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof Mob mob) {
            if (e.getEntity() instanceof PathfinderMob creature) {
                if (!creature.getType().is(StealthTags.IGNORE_SOUND))
                    mob.goalSelector.addGoal(0, new InvestigateSoundGoal(creature));
                mob.goalSelector.addGoal(0, new SearchLookGoal(creature));
                //nuke target alert
                for (WrappedGoal wg : new HashSet<>(mob.goalSelector.getAvailableGoals())) {
                    if (wg.getGoal() instanceof HurtByTargetGoal)
                        ((RevengeAccessor) wg.getGoal()).setAlertSameType(false);
                }
                for (WrappedGoal wg : new HashSet<>(mob.targetSelector.getAvailableGoals())) {
                    if (wg.getGoal() instanceof HurtByTargetGoal)
                        ((RevengeAccessor) wg.getGoal()).setAlertSameType(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void tickMobs(LivingEvent.LivingTickEvent e) {
        LivingEntity elb = e.getEntity();
        if (!elb.level().isClientSide && !(elb instanceof Player)) {
            ISense cap = SenseData.getCap(elb);
            if (elb.tickCount % 100 == 0 || mustUpdate.containsValue(elb))
                cap.serverTick();
        }
        if (!elb.level().isClientSide && elb.tickCount % 108 == 0 && elb.isInvisible() && elb instanceof Player p && !p.getAbilities().invulnerable) {
            DummyEntity d = new DecoyEntity(FootworkEntities.DUMMY.get(), elb.level()).setBoundTo(elb).setTicksToLive(100);
            d.setPos(elb.getEyePosition().add(CloakAndDagger.rand.nextInt(6) - 3, CloakAndDagger.rand.nextInt(6)-3, CloakAndDagger.rand.nextInt(6) - 3));
            d.setNoGravity(true);
            elb.level().addFreshEntity(d);
            lastDecoy.put(elb, d);
        }
    }
}
