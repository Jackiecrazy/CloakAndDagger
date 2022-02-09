package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nullable;
import java.awt.*;

public class Mikiri extends IronGuard {
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {
        super.onProc(caster, procPoint, state, stats, target);
        if (procPoint instanceof LivingAttackEvent && caster.getLastAttackedEntityTime() != caster.ticksExisted && ((LivingAttackEvent) procPoint).getEntityLiving() == target && procPoint.getPhase() == EventPriority.HIGHEST && state == STATE.COOLING) {
            stats.decrementDuration();
        }
    }

    @Override
    protected void parry(LivingEntity caster, ParryEvent procPoint, SkillData stats, LivingEntity target) {
        if (stats.getState() == STATE.COOLING) {
            stats.decrementDuration();
        } else {
            procPoint.setPostureConsumption(0);
            markUsed(caster);
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.COOLING)
            setCooldown(caster, 7);
        return passive(prev, from, to);
    }
}
