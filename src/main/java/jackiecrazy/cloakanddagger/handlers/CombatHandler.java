package jackiecrazy.cloakanddagger.handlers;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.config.GeneralConfig;
import jackiecrazy.cloakanddagger.config.SoundConfig;
import jackiecrazy.cloakanddagger.mixin.MixinMobSound;
import jackiecrazy.cloakanddagger.utils.CombatUtils;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.StealthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CloakAndDagger.MODID)
public class CombatHandler {
    @SubscribeEvent
    public static void projectileParry(final ProjectileImpactEvent e) {
        //deflection stealth checks are handled by PWD for sanity reasons.
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)//because compat with BHT...
    public static void parry(final LivingAttackEvent e) {
        //parrying stealth checks are handled by PWD for sanity reasons.
        DamageSource ds = e.getSource();
        if (ds.getEntity() instanceof LivingEntity seme && GeneralConfig.inv > 0) {
            seme.addEffect(new MobEffectInstance(FootworkEffects.EXPOSED.get(), GeneralConfig.inv));
            EntityHandler.lastDecoy.clear();
        }
    }

    @SubscribeEvent
    public static void critHooks(CriticalHitEvent e) {
        if (!e.getEntity().level.isClientSide && e.getTarget() instanceof LivingEntity) {
            LivingEntity uke = (LivingEntity) e.getTarget();
            LivingEntity seme = e.getEntity();
            //stealth attacks automatically crit
            StealthUtils.Awareness awareness = StealthUtils.INSTANCE.getAwareness(seme, uke);
            if (awareness == StealthUtils.Awareness.UNAWARE)
                e.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pain(LivingHurtEvent e) {
        LivingEntity uke = e.getEntity();
        LivingEntity kek = null;
        DamageSource ds = e.getSource();
        if (ds.getDirectEntity() instanceof LivingEntity) {
            kek = (LivingEntity) ds.getDirectEntity();
        }
        StealthOverride.Awareness awareness = StealthUtils.INSTANCE.getAwareness(kek, uke);
        if (ds.getEntity() instanceof LivingEntity) {
            if (CombatUtils.isPhysicalAttack(e.getSource())) {
                if (awareness != StealthOverride.Awareness.ALERT) {
                    e.setAmount((float) (e.getAmount() * CombatUtils.getDamageMultiplier(awareness, CombatUtils.getAttackingItemStack(ds))));
                }
            }
        }
    }

    /**
     * we finally have a sound playing event!
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void pingus(PlayLevelSoundEvent.AtPosition e) {
        if (e.getSound() == null) return;
        Vec3 vec = e.getPosition();
        if (StealthOverride.soundMap.containsKey(e.getSound()) && e.getLevel().hasNearbyAlivePlayer(vec.x, vec.y, vec.z, 40))
            EntityHandler.alertTracker.put(new Tuple<>(e.getLevel(), new BlockPos(e.getPosition())), (float) (StealthOverride.soundMap.get(e.getSound())));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void apingus(PlayLevelSoundEvent.AtEntity e) {
        if (e.getSound() == null) return;
        Entity entityIn = e.getEntity();
        double x = entityIn.getX(), y = entityIn.getY(), z = entityIn.getZ();
        if (StealthOverride.soundMap.containsKey(e.getSound()) && e.getLevel().hasNearbyAlivePlayer(x, y, z, 40))
            EntityHandler.alertTracker.put(new Tuple<>(e.getLevel(), new BlockPos(x, y, z)), (float) (StealthOverride.soundMap.get(e.getSound())));
    }
}
