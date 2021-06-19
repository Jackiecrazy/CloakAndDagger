package jackiecrazy.wardance.skill.coupdegrace;

import jackiecrazy.wardance.api.CombatDamageSource;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.entity.SpiritExplosion;
import jackiecrazy.wardance.event.ParryEvent;
import jackiecrazy.wardance.skill.ProcPoint;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import jackiecrazy.wardance.skill.WarSkills;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CoupDeGrace extends Skill {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("physical", ProcPoint.melee, ProcPoint.on_hurt, ProcPoint.recharge_cast, ProcPoint.change_parry_result, "execution")));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Collections.singletonList("execution")));

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return tag;
    }

    @Override
    public Tag<String> getIncompatibleTags(LivingEntity caster) {
        return no;
    }

    @Nullable
    @Override
    public Skill getParentSkill() {
        return this.getClass() == CoupDeGrace.class ? null : WarSkills.COUP_DE_GRACE.get();
    }

    @Override
    public boolean onCast(LivingEntity caster) {
        if (CasterData.getCap(caster).isTagActive("execution"))
            CasterData.getCap(caster).removeActiveTag("execution");
        else activate(caster, 1);
        return true;
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        setCooldown(caster, 2);
    }

    protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
        CombatData.getCap(caster).addMight(1);
    }

    @Override
    public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
        if (procPoint instanceof LivingHurtEvent) {
            LivingHurtEvent e = (LivingHurtEvent) procPoint;
            if (isValid(caster, target)) {
                if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0 && !CombatData.getCap(e.getEntityLiving()).isFirstStaggerStrike()) {
                    execute(e);
//                        CombatData.getCap(target).decrementStaggerTime(CombatData.getCap(target).getStaggerTime());
                    deathCheck(caster, target, e.getAmount());
                    markUsed(caster);
                    //}
                } else {
                    e.setCanceled(true);
                    CombatData.getCap(target).consumePosture(e.getAmount());
                }
            }
        }
    }

    protected void execute(LivingHurtEvent e) {
        e.setAmount(e.getEntityLiving().getHealth());
        e.getSource().setDamageBypassesArmor().setDamageIsAbsolute();
    }

    protected boolean isValid(LivingEntity caster, LivingEntity target) {
        return target.getHealth() < (target.getMaxHealth() + CombatData.getCap(target).getWounding()) * 0.3;
    }

    public static class Rupture extends CoupDeGrace {
        //detonate the entire spirit bar and leech half. Size of explosion is determined by size of mob, damage is determined by spirit.
        //danse macabre: proc percentage scales with combo
        //reaping: deal weapon damage*2+5% max health in a wide area. Enemies below that line take true damage, and skill refreshes on any death
        @Override
        public Color getColor() {
            return Color.CYAN;
        }

        @Override
        protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
            CombatData.getCap(caster).addSpirit(CombatData.getCap(target).getSpirit() / 2);
            SpiritExplosion.spirituallyExplode(caster.world, caster, target.getPosX(), target.getPosY(), target.getPosZ(), (float) Math.sqrt(CombatData.getCap(target).getTrueMaxPosture()), new CombatDamageSource("player", caster).setProxy(target).setExplosion().setMagicDamage(), CombatData.getCap(target).getSpirit());
        }
    }

    public static class DanseMacabre extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.BLACK;
        }

        @Override
        protected boolean isValid(LivingEntity caster, LivingEntity target) {
            return target.getHealth() < (target.getMaxHealth() + CombatData.getCap(target).getWounding()) * 0.3 * (1 + CombatData.getCap(caster).getCombo() / 10);
        }
    }

    public static class Reaping extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.RED;
        }

        @Override
        public boolean onCast(LivingEntity caster) {
            CombatData.getCap(caster).setForcedSweep(360);
            return super.onCast(caster);
        }

        @Override
        public void onEffectEnd(LivingEntity caster, SkillData stats) {
            if (!stats.isCondition())
                setCooldown(caster, 5);
            CombatData.getCap(caster).setForcedSweep(-1);
        }

        @Override
        public void onSuccessfulProc(LivingEntity caster, SkillData stats, LivingEntity target, Event procPoint) {
            if (procPoint instanceof LivingHurtEvent) {
                LivingHurtEvent e = (LivingHurtEvent) procPoint;
                if (CombatData.getCap(e.getEntityLiving()).getStaggerTime() > 0 && !CombatData.getCap(e.getEntityLiving()).isFirstStaggerStrike()) {
                    e.setAmount(e.getAmount() * 2 + e.getEntityLiving().getMaxHealth() * 0.05f);
                    deathCheck(caster, target, e.getAmount());
                } else {
                    e.setCanceled(true);
                    CombatData.getCap(target).consumePosture(e.getAmount());
                }
            }
            if (procPoint instanceof ParryEvent) {
                procPoint.setResult(Event.Result.DENY);
                markUsed(caster);
            }
        }
    }

    public static class Reinvigorate extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
            final ICombatCapability cap = CombatData.getCap(caster);
            cap.addFatigue(-amount / 10);
            cap.addWounding(-amount / 10);
            cap.addBurnout(-amount / 10);
            cap.addPosture(amount / 10);
            cap.addSpirit(amount / 10);
            caster.heal(amount / 10);
        }
    }

    public static class Frenzy extends CoupDeGrace {
        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        protected void deathCheck(LivingEntity caster, LivingEntity target, float amount) {
            ISkillCapability isc = CasterData.getCap(caster);
            final Set<Skill> skills = new HashSet<>(isc.getSkillCooldowns().keySet());
            for (Skill s : skills) {
                if (s.getTags(caster).contains("physical"))
                    isc.coolSkill(s);
            }
        }
    }

}
