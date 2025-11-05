package jackiecrazy.cloakanddagger.api.event;

import jackiecrazy.cloakanddagger.api.Awareness;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;

public class EntityAwarenessEvent extends LivingEvent {
    private final Awareness originalStatus;
    private LivingEntity attacker;
    private Awareness status;

    public EntityAwarenessEvent(LivingEntity entity, LivingEntity attacker, Awareness originally) {
        super(entity);
        this.attacker = attacker;
        originalStatus = status = originally;
    }

    public Awareness getAwareness() {
        return status;
    }

    @Nullable
    public LivingEntity getAttacker() {
        return attacker;
    }

    public void setAwareness(Awareness newAwareness) {
        status = newAwareness;
    }

    public Awareness getOriginalAwareness() {
        return originalStatus;
    }

    public static class Attack extends EntityAwarenessEvent {
        private final DamageSource ds;
        public Attack(LivingEntity entity, LivingEntity attacker, Awareness originally, DamageSource ds) {
            super(entity, attacker, originally);
            this.ds = ds;
        }

        public DamageSource getSource() {
            return ds;
        }
    }

    public static class Hurt extends EntityAwarenessEvent {
        private DamageSource ds;

        public double getDistractedMultiplier() {
            return distractedMult;
        }

        public void setDistractedMultiplier(double distractedMult) {
            this.distractedMult = distractedMult;
        }

        public double getUnawareMultiplier() {
            return unawareMult;
        }

        public void setUnawareMultiplier(double unawareMult) {
            this.unawareMult = unawareMult;
        }

        private double distractedMult;
        private double unawareMult;

        public double getAlertMultiplier() {
            return alertMultiplier;
        }

        public void setAlertMultiplier(double alertMultiplier) {
            this.alertMultiplier = alertMultiplier;
        }

        private double alertMultiplier;

        public Hurt(LivingEntity entity, LivingEntity attacker, Awareness originally, DamageSource ds) {
            super(entity, attacker, originally);
            this.ds = ds;
        }

        public DamageSource getSource() {
            return ds;
        }
    }
}