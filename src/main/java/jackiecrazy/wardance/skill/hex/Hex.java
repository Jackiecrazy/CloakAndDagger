package jackiecrazy.wardance.skill.hex;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.resources.ICombatCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.entity.FakeExplosion;
import jackiecrazy.wardance.skill.*;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.EffectUtils;
import jackiecrazy.wardance.utils.SkillUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.Tag;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;

@Mod.EventBusSubscriber(modid = WarDance.MODID)
public class Hex extends Skill {
    static final AttributeModifier HEX = new AttributeModifier(UUID.fromString("67fe7ef6-a398-4c62-9bb1-42edaa80e7b1"), "hex", -2, AttributeModifier.Operation.ADDITION);
    private final Tag<String> tag = makeTag("melee", "noDamage", "boundCast", ProcPoints.afflict_tick, ProcPoints.change_parry_result, ProcPoints.recharge_time, "normalAttack", "chant", "countdown");
    private final Tag<String> thing = makeTag(SkillTags.offensive, SkillTags.magical);

    @SubscribeEvent
    public static void snakebite(LivingHealEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity entity = e.getEntityLiving();
        //snakebite nullifies healing
        if (Marks.getCap(entity).isMarked(WarSkills.SNAKEBITE.get()))
            e.setCanceled(true);
    }

    @SubscribeEvent
    public static void blackmark(LivingDamageEvent e) {
        if (!e.getEntityLiving().isServerWorld()) return;
        LivingEntity target = e.getEntityLiving();
        //black mark stealing health/posture/spirit
        if (e.getAmount() > 0 && CombatUtils.isMeleeAttack(e.getSource()) && Marks.getCap(target).isMarked(WarSkills.BLACK_MARK.get())) {
            Entity source = e.getSource().getTrueSource();
            if (source instanceof LivingEntity) {
                LivingEntity le = (LivingEntity) source;
                final ICombatCapability cap = CombatData.getCap(le);
                float posture = cap.getPosture();
                float spirit = cap.getSpirit();
                if (posture < spirit) {
                    CombatData.getCap(target).consumePosture(2);
                    cap.addPosture(1);
                } else {
                    CombatData.getCap(target).consumeSpirit(3);
                    cap.addSpirit(1.5f);
                }
                le.heal(e.getAmount() / 3);
            }

        }
    }

//    @SubscribeEvent
//    public static void petrify(CriticalHitEvent e) {
//        if (!e.getEntityLiving().isServerWorld()) return;
//        if (!(e.getTarget() instanceof LivingEntity)) return;
//        LivingEntity target = (LivingEntity) e.getTarget();
//        //crit explosion on petrified target
//        StatusEffects.getCap(target).getActiveStatus(WarSkills.PETRIFY.get()).ifPresent((sd) -> {
//            if (sd.isCondition() && (e.getResult() == Event.Result.ALLOW || (e.getResult() == Event.Result.DEFAULT && e.isVanillaCritical()))) {
//                FakeExplosion.explode(e.getEntityLiving().world, e.getPlayer(), target.getPosX(), target.getPosY(), target.getPosZ(), target.getWidth() * target.getHeight() * 3, new CombatDamageSource("explosion.player", e.getPlayer()).setArmorReductionPercentage(2).setDamageTyping(CombatDamageSource.TYPE.PHYSICAL).setProcSkillEffects(false).setProcNormalEffects(false).setProcAttackEffects(false).setExplosion(), target.getTotalArmorValue());
//                StatusEffects.getCap(target).removeStatus(WarSkills.PETRIFY.get());
//            }
//        });
//
//    }

    @Override
    public Tag<String> getTags(LivingEntity caster) {
        return thing;
    }

    @Override
    public Tag<String> getSoftIncompatibility(LivingEntity caster) {
        return offensive;
    }

    @Override
    public float spiritConsumption(LivingEntity caster) {
        return 4;
    }

    @Nonnull
    @Override
    public SkillCategory getParentCategory() {
        return SkillCategories.hex;
    }


    @Override
    public boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to) {
        if (to == STATE.ACTIVE) {
            LivingEntity e = SkillUtils.aimLiving(caster);
            if (e != null) {
                mark(caster, e, 200);
                markUsed(caster);
            }
        }
        if(to==STATE.COOLING)
            setCooldown(caster,15);
        return boundCast(prev, from, to);
    }

    @Override
    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        sd.decrementDuration();
        return true;
    }

    @Override
    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, LivingEntity target) {

    }

    @Override
    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        if (cooldownTick(stats)) {
            return true;
        }
        return super.equippedTick(caster, stats);
    }

    @Override
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        final ModifiableAttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null && this == WarSkills.CURSE_OF_MISFORTUNE.get()) {
            luck.removeModifier(HEX);
            luck.applyNonPersistentModifier(HEX);
        }
        return super.onMarked(caster, target, sd, existing);
    }

    @Override
    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {
        final ModifiableAttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null) {
            luck.removeModifier(HEX);
        }
        super.onMarkEnd(caster, target, sd);
    }

    public static class Snakebite extends Hex {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
            EffectInstance poison = new EffectInstance(Effects.POISON, 200, 1);
            if (target.isPotionActive(Effects.POISON)) {
                poison = new EffectInstance(Effects.POISON, Math.max(target.getActivePotionEffect(Effects.POISON).getDuration(), 200), Math.max(target.getActivePotionEffect(Effects.POISON).getAmplifier(), 1));
            }
            poison.setCurativeItems(Collections.emptyList());
            target.removePotionEffect(Effects.POISON);
            target.addPotionEffect(poison);
            return super.onMarked(caster, target, sd, existing);
        }

        @Override
        public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
            //heal block, removed with poison
            if (!target.isPotionActive(Effects.POISON)) {
                removeMark(target);
                return true;
            }
            return false;
        }
    }

    public static class BlackMark extends Hex {
        @Override
        public Color getColor() {
            return Color.LIGHT_GRAY;
        }
    }

    public static class Unravel extends Hex {
        private final Tag<String> tag = Tag.getTagFromContents(new HashSet<>(Arrays.asList("melee", "noDamage", "boundCast", ProcPoints.change_parry_result, ProcPoints.recharge_time, "chant", ProcPoints.unblockable, "normalAttack", "countdown")));

        @Override
        public Color getColor() {
            return Color.ORANGE;
        }

        @Override
        protected void mark(LivingEntity caster, LivingEntity target, float duration) {
            ItemStack milk = new ItemStack(Items.MILK_BUCKET);
            final Collection<EffectInstance> potions = new ArrayList<>(target.getActivePotionEffects());
            target.curePotionEffects(milk);
            float size = 8, damage = 6;
            boolean proc = false;
            for (EffectInstance ei : potions) {
                proc = true;
                EffectInstance drop = new EffectInstance(ei.getPotion(), 0, -2);
                drop = EffectUtils.stackPot(caster, drop, EffectUtils.StackingMethod.MAXDURATION);
                if (drop.getAmplifier() >= 0) {
                    target.addPotionEffect(drop);
                }
            }
            if (proc)
                FakeExplosion.explode(caster.world, caster, target.getPosX(), target.getPosY() + target.getHeight() * 1.1f, target.getPosZ(), size, DamageSource.causeExplosionDamage(caster).setMagicDamage(), damage);
        }
    }


}