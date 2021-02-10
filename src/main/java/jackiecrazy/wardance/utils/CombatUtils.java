package jackiecrazy.wardance.utils;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.CombatData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class CombatUtils {

    public static float getCooledAttackStrength(LivingEntity e, Hand h, float adjustTicks) {
        return MathHelper.clamp(((float) (h == Hand.MAIN_HAND ? e.ticksSinceLastSwing : CombatData.getCap(e).getOffhandCooldown()) + adjustTicks) / getCooldownPeriod(e, h), 0.0F, 1.0F);
    }

    public static float getCooldownPeriod(LivingEntity e, Hand h) {
        return (float) (1.0D / GeneralUtils.getAttributeValueHandSensitive(e, Attributes.ATTACK_SPEED, h) * 20.0D);
    }

    public static boolean isShield(LivingEntity e, ItemStack stack) {
        return stack.isShield(e);
    }

    public static boolean isWeapon(LivingEntity e, ItemStack stack) {
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
    }

    @Nullable
    public static ItemStack getDefendingItemStack(LivingEntity e) {
        ItemStack ret = null;
        float posMod = 1337;
        boolean mainRec = getCooledAttackStrength(e, Hand.MAIN_HAND, 0.5f) > 0.9f;
        boolean offRec = getCooledAttackStrength(e, Hand.OFF_HAND, 0.5f) > 0.9f;
        if (offRec && isShield(e, e.getHeldItemOffhand())) {
            ret = e.getHeldItemOffhand();
            posMod = getPostureDef(e, e.getHeldItemOffhand());
        }
        if (mainRec && isShield(e, e.getHeldItemMainhand())) {
            if (ret == null || getPostureDef(e, e.getHeldItemMainhand()) < posMod) {
                posMod = getPostureDef(e, e.getHeldItemMainhand());
                ret = e.getHeldItemMainhand();
            }
        }
        if (offRec && isWeapon(e, e.getHeldItemOffhand())) {
            if (ret == null || getPostureDef(e, e.getHeldItemMainhand()) < posMod) {
                posMod = getPostureDef(e, e.getHeldItemOffhand());
                ret = e.getHeldItemMainhand();
            }
        }
        if (mainRec && isWeapon(e, e.getHeldItemMainhand())) {
            if (ret == null || getPostureDef(e, e.getHeldItemMainhand()) < posMod) {
                ret = e.getHeldItemMainhand();
            }
        }
        return ret;
    }

    @Nullable
    public static ItemStack getAttackingItemStack(DamageSource ds) {
        if (ds instanceof CombatDamageSource)
            return ((CombatDamageSource) ds).getDamageDealer();
        else if (ds.getTrueSource() instanceof LivingEntity) {
            LivingEntity e = (LivingEntity) ds.getTrueSource();
            return CombatData.getCap(e).isOffhandAttack() ? e.getHeldItemOffhand() : e.getHeldItemMainhand();
        }
        return null;
    }

    public static float getPostureAtk(LivingEntity e, float amount, ItemStack stack) {
        return amount * 0.35f;//TODO read config
    }

    public static float getPostureDef(LivingEntity e, ItemStack stack) {
        return 1f;//TODO read config
    }

    public static boolean isMeleeAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            CombatDamageSource cds = (CombatDamageSource) s;
            return cds.canProcAutoEffects();
        }
        return s.getTrueSource() == s.getImmediateSource() && !s.isExplosion() && !s.isFireDamage() && !s.isMagicDamage() && !s.isUnblockable() && !s.isProjectile();
    }

    /**
     * knocks the target back, with regards to the attacker's relative angle to the target, and adding y knockback
     */
    public static void knockBack(Entity to, Entity from, float strength, boolean considerRelativeAngle, boolean bypassAllChecks) {
        Vector3d distVec = to.getPositionVec().subtractReverse(from.getPositionVec()).normalize();
        if (to instanceof LivingEntity && !bypassAllChecks) {
            if (considerRelativeAngle)
                knockBack((LivingEntity) to, from, strength, distVec.x, distVec.y, distVec.z, false);
            else
                knockBack(((LivingEntity) to), from, (float) strength * 0.5F, (double) MathHelper.sin(from.rotationYaw * 0.017453292F), 0, (double) (-MathHelper.cos(from.rotationYaw * 0.017453292F)), false);
        } else {
            //eh
            if (considerRelativeAngle) {
                to.setVelocity(distVec.x * -strength, to.collidedVertically ? 0.1 : distVec.y * -strength, distVec.z * -strength);
            } else {
                to.addVelocity(-MathHelper.sin(-from.rotationYaw * 0.017453292F - (float) Math.PI) * 0.5, 0.1, -MathHelper.cos(-from.rotationYaw * 0.017453292F - (float) Math.PI) * 0.5);
            }
            to.velocityChanged = true;
        }
    }

    /**
     * knockback in LivingEntity except it makes sense and the resist is factored into the event
     */
    public static void knockBack(LivingEntity to, Entity from, float strength, double xRatio, double yRatio, double zRatio, boolean bypassEventCheck) {
        if (!bypassEventCheck) {
            net.minecraftforge.event.entity.living.LivingKnockBackEvent event = net.minecraftforge.common.ForgeHooks.onLivingKnockBack(to, strength, xRatio, zRatio);
            if (event.isCanceled()) return;
            strength = event.getStrength();
            xRatio = event.getRatioX();
            zRatio = event.getRatioZ();
        } else {
            strength *= (float) (1 - to.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        }
        if (strength != 0f) {
            Vector3d vec = to.getMotion();
            double motionX = vec.x, motionY = vec.y, motionZ = vec.z;
            to.isAirBorne = true;
            float pythagora = MathHelper.sqrt(xRatio * xRatio + zRatio * zRatio);
            if (to.isOnGround()) {
                motionY /= 2.0D;
                motionY += strength;

                if (motionY > 0.4000000059604645D) {
                    motionY = 0.4000000059604645D;
                }
            } else if (yRatio != 0) {
                pythagora = MathHelper.sqrt(xRatio * xRatio + zRatio * zRatio + yRatio * yRatio);
                motionY /= 2.0D;
                motionY -= yRatio / (double) pythagora * (double) strength;
            }
            motionX /= 2.0D;
            motionZ /= 2.0D;
            motionX -= xRatio / (double) pythagora * (double) strength;
            motionZ -= zRatio / (double) pythagora * (double) strength;
            to.setMotion(motionX, motionY, motionZ);
        }
    }

    public static void setHandCooldown(LivingEntity e, Hand h, float percent) {
        //special case for quick maths
        int real = percent == 0 ? 0 : (int) (percent * getCooldownPeriod(e, h));
        switch (h) {
            case MAIN_HAND:
                e.ticksSinceLastSwing = real;
                break;
            case OFF_HAND:
                CombatData.getCap(e).setOffhandCooldown(real);
                break;
        }
    }
}
