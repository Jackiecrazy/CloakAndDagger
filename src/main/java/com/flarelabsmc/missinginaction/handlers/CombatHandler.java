package com.flarelabsmc.missinginaction.handlers;

import com.flarelabsmc.missinginaction.MissingInAction;
import com.flarelabsmc.missinginaction.api.Awareness;
import com.flarelabsmc.missinginaction.api.StealthUtils;
import com.flarelabsmc.missinginaction.api.event.EntityAwarenessEvent;
import com.flarelabsmc.missinginaction.utils.CombatUtils;
import com.flarelabsmc.missinginaction.utils.StealthOverride;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MissingInAction.MODID)
public class CombatHandler {
    @SubscribeEvent
    public static void stealthCriticalHit(CriticalHitEvent e) {
        if (!e.getEntity().level().isClientSide && e.getTarget() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) e.getTarget();
            LivingEntity entity = e.getEntity();
            Awareness awareness = StealthOverride.INSTANCE.getAwareness(entity, target);
            if (awareness == Awareness.UNAWARE)
                e.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void stealthDamage(LivingHurtEvent e) {
        LivingEntity target = e.getEntity();
        LivingEntity entity = null;
        DamageSource source = e.getSource();
        if (source.getEntity() instanceof LivingEntity eee) {
            entity = eee;
        }
        EntityAwarenessEvent.Hurt subevent = new EntityAwarenessEvent.Hurt(target, entity, StealthUtils.INSTANCE.getAwareness(entity, target), e.getSource());
        subevent.setAlertMultiplier(1);
        subevent.setDistractedMultiplier(CombatUtils.getDamageMultiplier(Awareness.DISTRACTED, CombatUtils.getAttackingItemStack(source)));
        subevent.setUnawareMultiplier(CombatUtils.getDamageMultiplier(Awareness.UNAWARE, CombatUtils.getAttackingItemStack(source)));
        MinecraftForge.EVENT_BUS.post(subevent);
        if (source.getEntity() instanceof LivingEntity) {
            if (CombatUtils.isPhysicalAttack(e.getSource())) {
                double amount = 1;
                switch (subevent.getAwareness()) {
                    case ALERT -> amount = subevent.getAlertMultiplier();
                    case DISTRACTED -> amount = subevent.getDistractedMultiplier();
                    case UNAWARE -> amount = subevent.getUnawareMultiplier();
                }
                e.setAmount((float) (e.getAmount() * amount));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void soundPlayedA(PlayLevelSoundEvent.AtPosition e) {
        if (e.getSound() == null) return;
        Vec3 vec = e.getPosition();
        if (StealthOverride.soundMap.containsKey(e.getSound()) && e.getLevel().hasNearbyAlivePlayer(vec.x, vec.y, vec.z, 40))
            EntityHandler.alertTracker.put(new Tuple<>(e.getLevel(), BlockPos.containing(vec.x, vec.y, vec.z)), (float) (StealthOverride.soundMap.get(e.getSound())));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void soundPlayedB(PlayLevelSoundEvent.AtEntity e) {
        if (e.getSound() == null) return;
        Entity entityIn = e.getEntity();
        double x = entityIn.getX(), y = entityIn.getY() + entityIn.getEyeHeight(), z = entityIn.getZ();
        if (StealthOverride.soundMap.containsKey(e.getSound()) && e.getLevel().hasNearbyAlivePlayer(x, y, z, 40))
            EntityHandler.alertTracker.put(new Tuple<>(e.getLevel(), BlockPos.containing(x, y, z)), (float) (StealthOverride.soundMap.get(e.getSound())));
    }
}
