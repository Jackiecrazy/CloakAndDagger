package jackiecrazy.cloakanddagger.handlers;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.api.Awareness;
import jackiecrazy.cloakanddagger.api.StealthUtils;
import jackiecrazy.cloakanddagger.capability.action.PermissionData;
import jackiecrazy.cloakanddagger.capability.vision.ISense;
import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import jackiecrazy.cloakanddagger.config.GeneralConfig;
import jackiecrazy.cloakanddagger.config.SoundConfig;
import jackiecrazy.cloakanddagger.config.StealthTags;
import jackiecrazy.cloakanddagger.entity.CnDAttributes;
import jackiecrazy.cloakanddagger.entity.CnDEntities;
import jackiecrazy.cloakanddagger.entity.DecoyEntity;
import jackiecrazy.cloakanddagger.entity.ai.SearchLookGoal;
import jackiecrazy.cloakanddagger.mixin.RevengeAccessor;
import jackiecrazy.cloakanddagger.networking.StealthChannel;
import jackiecrazy.cloakanddagger.networking.UpdateTargetPacket;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.cloakanddagger.utils.Utils;
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
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
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

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(modid = CloakAndDagger.MODID)
public class EntityHandler {
    public static final HashMap<Player, Entity> mustUpdate = new HashMap<>();
    public static final HashMap<LivingEntity, DecoyEntity> lastDecoy = new HashMap<>();
    public static final ConcurrentHashMap<Tuple<Level, BlockPos>, Float> alertTracker = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void caps(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Mob m) {
            e.addCapability(new ResourceLocation("cloakanddagger:vision"), new SenseData(m));

        }
        if (e.getObject() instanceof Player p)
            e.addCapability(new ResourceLocation("cloakanddagger:permissions"), new PermissionData(p));
    }

    @SubscribeEvent
    public static void createAttributesForEntities(EntityAttributeCreationEvent event) {
        event.put(CnDEntities.DECOY.get(), DecoyEntity.createAttributes().build());
    }

    public static void addAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, CnDAttributes.STEALTH.get());
    }

    @SubscribeEvent
    public static void lure(TickEvent.ServerTickEvent e) {
        int checked = 0;
        for (Map.Entry<Tuple<Level, BlockPos>, Float> n : alertTracker.entrySet()) {
            if (checked > SoundConfig.cap) break;
            if (n.getKey().getA().isAreaLoaded(n.getKey().getB(), n.getValue().intValue())) {
                for (PathfinderMob c : (n.getKey().getA().getEntitiesOfClass(PathfinderMob.class, new AABB(n.getKey().getB()).inflate(n.getValue())))) {
                    if (StealthUtils.INSTANCE.getAwareness(null, c) == Awareness.UNAWARE) {
                        if (!c.getType().is(StealthTags.IGNORE_SOUND)) {
                            c.goalSelector.disableControlFlag(Goal.Flag.MOVE);
                            c.getNavigation().stop();
                            c.getNavigation().moveTo(c.getNavigation().createPath(n.getKey().getB(), (int) (n.getValue() + 3)), 1);
                            BlockPos vec = n.getKey().getB();
                            c.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(vec.getX(), vec.getY(), vec.getZ()));
                        }
                    }
                }
            }
            checked++;
        }
        alertTracker.clear();
    }

    @SubscribeEvent
    public static void blindness(final MobEffectEvent e) {
        if (e.getEffectInstance() != null && !e.getEntity().getType().is(StealthTags.IGNORE_BLINDNESS) && e.getEffectInstance().getEffect() == MobEffects.BLINDNESS) {
            if (e.getEntity() instanceof Mob)
                ((Mob) e.getEntity()).setTarget(null);
            e.getEntity().setLastHurtByMob(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void sneak(final LivingEvent.LivingVisibilityEvent e) {
        if (e.getLookingEntity() != e.getEntity() && e.getLookingEntity() instanceof LivingEntity watcher) {
            double mult = 1;
            LivingEntity sneaker = e.getEntity();
            if (sneaker.getType().is(StealthTags.NO_STEALTH)) return;
            if (watcher.getKillCredit() != sneaker && watcher.getLastHurtByMob() != sneaker && (!(watcher instanceof Mob) || ((Mob) watcher).getTarget() != sneaker)) {
                double stealth = Utils.getAttributeValueSafe(sneaker, CnDAttributes.STEALTH.get());
                double luckDiff = Utils.getAttributeValueSafe(sneaker, Attributes.LUCK) - Utils.getAttributeValueSafe(watcher, Attributes.LUCK);
                stealth += CloakAndDagger.rand.nextDouble() * luckDiff * GeneralConfig.luck;
                double negMult = 1;
                double posMult = 1;
                if (stealth < 0) {
                    negMult -= (stealth * stealth) / 225;
                }
                while (stealth >= 1) {
                    posMult *= 0.933;
                    stealth--;
                }
                negMult = Math.max(0, negMult);
                float lightMalus = 0;
                if (!watcher.getType().is(StealthTags.IGNORE_LIGHT) && !watcher.hasEffect(MobEffects.NIGHT_VISION) && !sneaker.hasEffect(MobEffects.GLOWING) && sneaker.getRemainingFireTicks() <= 0) {
                    Level world = sneaker.level();
                    if (world.isAreaLoaded(sneaker.blockPosition(), 5) && world.isAreaLoaded(watcher.blockPosition(), 5)) {
                        final int slight = StealthOverride.getActualLightLevel(world, sneaker.blockPosition());
                        final int wlight = StealthOverride.getActualLightLevel(world, watcher.blockPosition());
                        int lightDiff = wlight - slight;//higher is better
                        float magicLightCutoff = 10;
                        lightDiff -= (int) Math.min(0, slight - magicLightCutoff);//less than 10? bonus
                        lightDiff += (int) Math.max(0, wlight - magicLightCutoff);//more than 10? bonus
                        float modifiedLight = 1 - lightDiff / magicLightCutoff;//lower is better, 10 is baseline
                        lightMalus = (float) Mth.clamp((1 - modifiedLight) / posMult, -10f, 0.7f); //higher is better
                        mult *= (1 - (lightMalus * negMult));
                    }
                }
                if (!watcher.getType().is(StealthTags.IGNORE_MOTION)) {
                    final double speedSq = Utils.getSpeedSq(sneaker);
                    final float speed = Mth.sqrt((float) speedSq);
                    mult *= (1 - (0.5 - speed * 2 * posMult) * negMult);
                }
                mult = Math.max(mult, 3 / (watcher.getAttributeValue(Attributes.FOLLOW_RANGE) + 1));
                if (!watcher.getType().is(StealthTags.IGNORE_FOV) && !Utils.isFacingEntity(watcher, sneaker, GeneralConfig.baseHorizontalDetection, GeneralConfig.baseVerticalDetection))
                    mult *= (1 - (0.7 * negMult));
            }
            if (watcher.hasEffect(MobEffects.BLINDNESS) && !watcher.getType().is(StealthTags.IGNORE_BLINDNESS)) {
                mult /= 11;
            }
            if (!watcher.getType().is(StealthTags.IGNORE_LOS) && Utils.viewBlocked(watcher, sneaker, true))
                mult *= 0.6;
            mult = Math.max(mult, 2f / (watcher.getAttributeValue(Attributes.FOLLOW_RANGE) + 1));
            e.modifyVisibility(Math.max(mult, 0));
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void detect(final LivingEvent.LivingVisibilityEvent e) {
        if (e.getEntity() instanceof Player p && p.getAbilities().invulnerable) return;
        if (e.getLookingEntity() instanceof LivingEntity watcher && !watcher.level().isClientSide) {
            if (watcher.getType().is(StealthTags.SKIP_SEARCH)) return;//unnecessary
            final double maxDist = watcher.getAttributeValue(Attributes.FOLLOW_RANGE);
            double follow = maxDist * e.getVisibilityModifier();
            final double sqdist = e.getEntity().distanceToSqr(watcher);
            boolean out = sqdist > follow * follow;
            if (!out) {
                double modifiedVisibility = e.getVisibilityModifier() * 1.2;
                final double add = Mth.clamp(0.5 + ((follow - Math.sqrt(sqdist)) / (follow)) / 2, 0, 1) * modifiedVisibility / 20;
                SenseData.getCap(watcher).modifyDetection(e.getEntity(), (float) add);
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
    public static void addGoals(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof Mob mob) {
            if (e.getEntity() instanceof PathfinderMob creature) {
                mob.goalSelector.addGoal(0, new SearchLookGoal(creature));
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
            DecoyEntity d = new DecoyEntity(CnDEntities.DECOY.get(), elb.level()).setBoundTo(elb).setTicksToLive(100);
            d.setPos(elb.getEyePosition().add(CloakAndDagger.rand.nextInt(6) - 3, 0, CloakAndDagger.rand.nextInt(6) - 3));
            elb.level().addFreshEntity(d);
            lastDecoy.put(elb, d);
        }
    }
}
