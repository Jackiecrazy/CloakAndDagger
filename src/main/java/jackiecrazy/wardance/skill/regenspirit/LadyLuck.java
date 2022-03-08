package jackiecrazy.wardance.skill.regenspirit;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.event.SkillResourceEvent;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCategories;
import jackiecrazy.wardance.skill.SkillCategory;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.tags.Tag;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import javax.annotation.Nonnull;
import java.awt.*;

import jackiecrazy.wardance.skill.Skill.STATE;

public class LadyLuck extends Skill {
    /*
    back and forth: recover (1/attack speed) spirit when parrying or landing a critical hit.
natural sprinter: max spirit doubled, but regeneration speed reduced to a third; recover 3 spirit on a kill.
ranged support: gain 1 spirit when you perform a distracted attack or when your projectile hits; 1s cooldown.
speed demon: halve spirit cooldown on dodge, recover spirit on attack depending on relative speed.
lady luck: after casting a skill, have a 1+luck/5+luck chance to recover the spirit cost, stacking chance until it triggers.
apathy: your max spirit is 4, your spirit instantly refills after cooldown, you are immune to burnout.
     */

    @Override
    public Color getColor() {
        return Color.ORANGE;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.morale;
    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return passive;
    }

    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return none;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {
        if (procPoint instanceof SkillResourceEvent && procPoint.getPhase() == EventPriority.HIGHEST) {
            float luck = (float) Math.max(0, GeneralUtils.getAttributeValueSafe(caster, Attributes.LUCK));
            stats.setArbitraryFloat(stats.getArbitraryFloat() + ((1 + luck) / (5 + luck)));
            if (WarDance.rand.nextFloat() < stats.getArbitraryFloat()) {
                ((SkillResourceEvent) procPoint).setSpirit(0);
                stats.setArbitraryFloat(0);
            }
        }
    }

    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        return false;
    }
}
