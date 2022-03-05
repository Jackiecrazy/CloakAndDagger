package jackiecrazy.wardance.skill.judgment;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Judgment extends Skill {
    /*
    Redirects all might gain to another bar temporarily. Gaining 10 might in this manner will cause your next attack to deal 20% of the target's current health in damage. 10 second cooldown.
    */
    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return special;
    }

    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return special;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.judgment;
    }

    protected void performEffect(LivingEntity caster, LivingEntity target, int stack, SkillData sd) {
        final float amount = stack == 3 ? target.getHealth() * 0.15f : target.getHealth() * 0.03f;
        if (this == WarSkills.AMPUTATION.get())
            CombatData.getCap(target).addWounding(amount);
        else
            target.attackEntityFrom(new CombatDamageSource("player", caster).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(true).setProcAttackEffects(true).setDamageTyping(CombatDamageSource.TYPE.TRUE).setDamageBypassesArmor().setDamageIsAbsolute(), amount);
    }

    @Override
    public CastStatus castingCheck(LivingEntity caster) {
        if (CombatData.getCap(caster).getComboRank() < 5)
            return CastStatus.OTHER;
        return super.castingCheck(caster);
    }

    @Override
    public float mightConsumption(LivingEntity caster) {
        return 3;
    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getState() == STATE.ACTIVE && stats.getDuration() > 0) {
            boolean stat = stats.getDuration() > 0;
            activeTick(stats);
            if (stats.getDuration() < 0 && stat) {
                onStateChange(caster, stats, STATE.INACTIVE, STATE.HOLSTERED);
                stats.setDuration(0);
            }
            return true;
        }
        return cooldownTick(stats);
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        LivingEntity target = SkillUtils.aimLiving(caster);
        if (from == STATE.HOLSTERED && to == STATE.ACTIVE && target != null && cast(caster, -999)) {
            int stack = 1;
            float arb = Marks.getCap(target).getActiveMark(this).orElse(SkillData.DUMMY).getArbitraryFloat();
            if (arb >= 2) {//detonate
                caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
                removeMark(target);
            } else {
                prev.flagCondition(true);
                mark(caster, target, 6, 1);
            }
            stack += arb;
            performEffect(caster, target, stack, prev);
            target.hurtResistantTime = 0;
            boolean offhand = stack == 2;
            CombatUtils.attack(caster, target, offhand);
            caster.swing(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, true);
            caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.ENTITY_RAVAGER_STEP, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
        }
        if (to == STATE.COOLING) {
            if (prev.isCondition())
                prev.setDuration(2);
            else setCooldown(caster, prev, 20);
            return true;
        }
        if (from != STATE.COOLING && to == STATE.HOLSTERED)
            caster.world.playSound(null, caster.getPosX(), caster.getPosY(), caster.getPosZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 0.25f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat() * 0.5f);
        return boundCast(prev, from, to);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) {
            if (existing.getDuration() < 0) return null;
            sd.setArbitraryFloat(sd.getArbitraryFloat() + existing.getArbitraryFloat());
            sd.setDuration(12);
        }
        return sd;
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration(0.05f);
        return super.markTick(caster, target, sd);
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        if (procPoint instanceof LivingDeathEvent && procPoint.getPhase() == EventPriority.HIGHEST && ((LivingDeathEvent) procPoint).getEntityLiving() == target) {
            Marks.getCap(target).getActiveMark(this).ifPresent((a) -> CombatData.getCap(caster).addMight(a.getArbitraryFloat() * mightConsumption(caster) * 1.4f));
        }
        if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntityLiving() == target) {
            if (state == STATE.ACTIVE)
                ((LivingAttackEvent) procPoint).getSource().setDamageBypassesArmor().setDamageIsAbsolute();
            Marks.getCap(target).getActiveMark(this).ifPresent((a) -> a.setDuration(a.getArbitraryFloat() == 1 ? 6 : 12));
        }
    }

}