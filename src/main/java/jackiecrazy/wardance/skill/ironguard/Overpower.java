package jackiecrazy.wardance.skill.ironguard;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.CombatUtils;
import net.minecraft.entity.LivingEntity;

import java.awt.*;

public class Overpower extends IronGuard {
    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    protected void parry(LivingEntity caster, ParryEvent procPoint, SkillData stats, LivingEntity target) {
        if(stats.getState()==STATE.COOLING)return;
        CombatData.getCap(procPoint.getAttacker()).consumePosture(caster, procPoint.getPostureConsumption());
        CombatData.getCap(procPoint.getAttacker()).consumePosture(caster, CombatUtils.getPostureAtk(caster, target, procPoint.getDefendingHand(), procPoint.getAttackDamage(), procPoint.getDefendingStack()));
        procPoint.setPostureConsumption(0);
        markUsed(caster);
    }


    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (stats.getDuration() > 0.5 || CombatData.getCap(caster).getPosture() == CombatData.getCap(caster).getMaxPosture())
            return cooldownTick(stats);
        return false;
    }
}
