package jackiecrazy.wardance.skill.grapple;

import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.awt.*;

public class Submission extends Grapple {
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof LivingAttackEvent && ((LivingAttackEvent) procPoint).getEntityLiving() == target && procPoint.getPhase() == EventPriority.HIGHEST) {
            if (state == STATE.HOLSTERED && (caster.ticksExisted - caster.getLastAttackedEntityTime() < 40 || caster.getTotalArmorValue() > target.getTotalArmorValue()) && CombatUtils.isUnarmed(caster.getHeldItemMainhand(), caster) && caster.getLastAttackedEntity() == target && cast(caster, -999)) {
                performEffect(caster, target);
                stats.flagCondition(caster.getTotalArmorValue() > target.getTotalArmorValue());
            } else if (state == STATE.COOLING) stats.decrementDuration();
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING) {
            setCooldown(caster, prev, prev.isCondition() ? 4 : 7);
        }
        prev.flagCondition(false);
        return boundCast(prev, from, to);
    }
}
