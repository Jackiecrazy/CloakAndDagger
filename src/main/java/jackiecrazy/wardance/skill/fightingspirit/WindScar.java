package jackiecrazy.wardance.skill.fightingspirit;

import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.skill.ProcPoints;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.ForgeMod;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class WindScar extends WarCry {
    private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("chant", "sweep", ProcPoints.on_being_hurt, ProcPoints.countdown, ProcPoints.recharge_time, ProcPoints.recharge_sleep)));
    private final Tag<String> no = Tag.getTagFromContents(new HashSet<>(Arrays.asList("sweep")));
    private static final AttributeModifier reach=new AttributeModifier(UUID.fromString("abe24c38-73e3-4551-9df4-e06e117699c1"), "wind scar bonus", 1, AttributeModifier.Operation.ADDITION);

    @Override
    public Tag<String> getProcPoints(LivingEntity caster) {
        return tag;
    }

    @Override
    protected void evoke(LivingEntity caster) {
        CombatData.getCap(caster).setForcedSweep(360);
    }

    @Override
    public boolean activeTick(LivingEntity caster, SkillData d) {
        CombatData.getCap(caster).setForcedSweep(360);
        return super.activeTick(caster, d);
    }

    @Override
    public void onEffectEnd(LivingEntity caster, SkillData stats) {
        caster.getAttribute(ForgeMod.REACH_DISTANCE.get()).removeModifier(reach);
        CombatData.getCap(caster).setForcedSweep(-1);
        super.onEffectEnd(caster, stats);
    }

    @Override
    public void onCooledDown(LivingEntity caster, float overflow) {
        final ModifiableAttributeInstance instance = caster.getAttribute(ForgeMod.REACH_DISTANCE.get());
        instance.removeModifier(reach);
        instance.applyPersistentModifier(reach);
        super.onCooledDown(caster, overflow);
    }

    @Override
    public Color getColor() {
        return Color.GREEN;
    }
}
