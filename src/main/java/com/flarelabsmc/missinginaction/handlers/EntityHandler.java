package com.flarelabsmc.missinginaction.handlers;

import com.flarelabsmc.missinginaction.MissingInAction;
import com.flarelabsmc.missinginaction.api.Awareness;
import com.flarelabsmc.missinginaction.api.StealthUtils;
import com.flarelabsmc.missinginaction.capability.vision.ISense;
import com.flarelabsmc.missinginaction.capability.vision.SenseData;
import com.flarelabsmc.missinginaction.config.GeneralConfig;
import com.flarelabsmc.missinginaction.config.SoundConfig;
import com.flarelabsmc.missinginaction.config.StealthTags;
import com.flarelabsmc.missinginaction.entity.MiAAttributes;
import com.flarelabsmc.missinginaction.entity.ai.SearchLookGoal;
import com.flarelabsmc.missinginaction.mixin.HurtByTargetGoalAccessor;
import com.flarelabsmc.missinginaction.networking.StealthChannel;
import com.flarelabsmc.missinginaction.networking.UpdateTargetPacket;
import com.flarelabsmc.missinginaction.utils.StealthOverride;
import com.flarelabsmc.missinginaction.utils.Utils;
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
@Mod.EventBusSubscriber(modid = MissingInAction.MODID)
public class EntityHandler {
    public static final HashMap<Player, Entity> mustUpdate = new HashMap<>();
    public static final ConcurrentHashMap<Tuple<Level, BlockPos>, Float> alertTracker = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void caps(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Mob m) {
            e.addCapability(new ResourceLocation("missinginaction:vision"), new SenseData(m));
        }
    }

    public static void addAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, MiAAttributes.STEALTH.get());
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
                            c.getLookControl().setLookAt(new Vec3(vec.getX(), vec.getY(), vec.getZ()));
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

    @SubscribeEvent
    public static void sneak(final LivingEvent.LivingVisibilityEvent e) {
        if (e.getLookingEntity() != e.getEntity() && e.getLookingEntity() instanceof LivingEntity watcher) {
            double mult = 1;
            LivingEntity sneaker = e.getEntity();
            if (sneaker.getType().is(StealthTags.NO_STEALTH)) return;
            if (watcher.getKillCredit() != sneaker && watcher.getLastHurtByMob() != sneaker && (!(watcher instanceof Mob) || ((Mob) watcher).getTarget() != sneaker)) {
                double stealth = Utils.getAttributeValueSafe(sneaker, MiAAttributes.STEALTH.get());
                double luckDiff = Utils.getAttributeValueSafe(sneaker, Attributes.LUCK) - Utils.getAttributeValueSafe(watcher, Attributes.LUCK);
                stealth += (MissingInAction.rand.nextDouble() * luckDiff) * GeneralConfig.luck;
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
                        int lightDiff = wlight - slight;
                        float magicLightCutoff = 10;
                        lightDiff -= (int) Math.min(0, slight - magicLightCutoff);
                        lightDiff += (int) Math.max(0, wlight - magicLightCutoff);
                        float modifiedLight = 1 - lightDiff / magicLightCutoff;
                        lightMalus = (float) Mth.clamp((1 - modifiedLight) / posMult, -10f, 0.7f);
                        mult *= (1 - (lightMalus * negMult));
                    }
                }
                if (!watcher.getType().is(StealthTags.IGNORE_MOTION)) {
                    final double speedSq = Utils.getSpeedSq(sneaker);
                    final float speed = Mth.sqrt((float) speedSq);
                    mult *= (1 - (0.5 - speed * 2 * posMult) * negMult);
                }
                mult = Math.max(mult, 3 / (watcher.getAttributeValue(Attributes.FOLLOW_RANGE) + 1));
                boolean facing = Utils.isFacingEntity(watcher, sneaker, GeneralConfig.baseHorizontalDetection, GeneralConfig.baseVerticalDetection);
                if (!watcher.getType().is(StealthTags.IGNORE_FOV)) {
                    mult *= (1 - ((facing ? -0.7 : 0.7) * negMult));
                }
                if (watcher.hasEffect(MobEffects.BLINDNESS) && !watcher.getType().is(StealthTags.IGNORE_BLINDNESS))
                    mult /= 11;
                if (!watcher.getType().is(StealthTags.IGNORE_LOS) && Utils.viewBlocked(watcher, sneaker, true))
                    mult *= 0.6;
                e.modifyVisibility(Math.max(mult, 0));
            }
        }
    }

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
                        ((HurtByTargetGoalAccessor) wg.getGoal()).setAlertSameType(false);
                }
                for (WrappedGoal wg : new HashSet<>(mob.targetSelector.getAvailableGoals())) {
                    if (wg.getGoal() instanceof HurtByTargetGoal)
                        ((HurtByTargetGoalAccessor) wg.getGoal()).setAlertSameType(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void tickMobs(LivingEvent.LivingTickEvent e) {
        LivingEntity elb = e.getEntity();
        if (!elb.level().isClientSide && !(elb instanceof Player)) {
            ISense cap = SenseData.getCap(elb);
            if (elb.tickCount % 10 == 0 || mustUpdate.containsValue(elb))
                cap.serverTick();
        }
    }
}
