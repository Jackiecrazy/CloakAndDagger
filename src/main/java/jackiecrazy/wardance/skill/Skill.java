package jackiecrazy.wardance.skill;

import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.capability.resources.CombatData;
import jackiecrazy.wardance.capability.skill.CasterData;
import jackiecrazy.wardance.capability.skill.ISkillCapability;
import jackiecrazy.wardance.capability.status.Marks;
import jackiecrazy.wardance.config.GeneralConfig;
import jackiecrazy.wardance.event.SkillCastEvent;
import jackiecrazy.wardance.event.SkillCooldownEvent;
import jackiecrazy.wardance.event.SkillResourceEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;

public abstract class Skill extends ForgeRegistryEntry<Skill> {
    public static final HashMap<SkillCategory, List<Skill>> variationMap = new HashMap<>();
    protected static final Tag<String> none = Tag.empty();
    protected static final Tag<String> offensivePhysical = makeTag(SkillTags.offensive, SkillTags.physical);
    protected static final Tag<String> defensivePhysical = makeTag(SkillTags.defensive, SkillTags.physical);
    protected static final Tag<String> offensive = makeTag(SkillTags.offensive);
    protected static final Tag<String> defensive = makeTag(SkillTags.defensive);
    protected static final Tag<String> passive = makeTag(SkillTags.passive);
    protected static final Tag<String> special = makeTag(SkillTags.special);
    protected static final Tag<String> state = makeTag(SkillTags.state);

    public Skill() {
        //SkillCategory
        List<Skill> insert = variationMap.get(getParentCategory());
        if (insert == null) {
            insert = new ArrayList<>();
        }
        insert.add(this);
        variationMap.put(this.getParentCategory(), insert);
    }

    protected static Tag<String> makeTag(String... stuff) {
        return Tag.create(new HashSet<>(Arrays.asList(stuff)));
    }

    @Nullable
    public static Skill getSkill(String name) {
        return getSkill(new ResourceLocation(name));
    }

    @Nullable
    public static Skill getSkill(ResourceLocation name) {
        return GameRegistry.findRegistry(Skill.class).getValue(name);
    }

    public boolean isFamily(Skill s) {
        if (s == null) return false;
        return getParentCategory().equals(s.getParentCategory());
    }

    public boolean isPassive(LivingEntity caster) {
        return getTags(caster).contains("passive");
    }

    @Nonnull
    public SkillCategory getParentCategory() {
        return SkillCategories.none;
    }

    public boolean isSelectable(LivingEntity caster) {
        return true;
    }

    public CastStatus castingCheck(LivingEntity caster) {
        ISkillCapability cap = CasterData.getCap(caster);
        switch (cap.getSkillState(this)) {
            case ACTIVE:
                return CastStatus.ACTIVE;
            case COOLING:
                return CastStatus.COOLDOWN;
            case HOLSTERED:
                return CastStatus.HOLSTERED;
        }
        for (String s : getSoftIncompatibility(caster).getValues())
            if (cap.isTagActive(s))
                return CastStatus.CONFLICT;
        if (caster.isSilent() && getTags(caster).contains("chant")) return CastStatus.SILENCE;
        if (CombatData.getCap(caster).getSpirit() < spiritConsumption(caster))
            return CastStatus.SPIRIT;
        if (CombatData.getCap(caster).getMight() < mightConsumption(caster))
            return CastStatus.MIGHT;
        return CastStatus.ALLOWED;
    }

    public float spiritConsumption(LivingEntity caster) {
        return 0;
    }

    public float mightConsumption(LivingEntity caster) {
        return 0;
    }

    public boolean isCompatibleWith(Skill s, LivingEntity caster) {
        if (s == null) return true;
        for (String tag : this.getTags(caster).getValues())
            if (s.getSoftIncompatibility(caster).contains(tag)) return false;
        for (String tag : s.getTags(caster).getValues())
            if (this.getSoftIncompatibility(caster).contains(tag)) return false;
        return true;
    }

    public void onCooledDown(LivingEntity caster, float overflow) {
        caster.level.playSound(null, caster, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 0.3f + WarDance.rand.nextFloat(), 0.5f + WarDance.rand.nextFloat());
    }

    public ITextComponent description() {
        return new TranslationTextComponent(this.getRegistryName().toString() + ".desc");
    }

    /**
     * @param caster only nonnull if it's in the casting bar!
     */
    public ITextComponent getDisplayName(LivingEntity caster) {
        return new TranslationTextComponent(this.getRegistryName().toString() + ".name");
    }

    public ResourceLocation icon() {
        return getParentCategory().icon();
    }

    public Color getColor() {
        return Color.WHITE;
    }

    public abstract Tag<String> getTags(LivingEntity caster);//requires breath, bound, debuffing, healing, aoe, etc.

    @Nonnull
    public abstract Tag<String> getSoftIncompatibility(LivingEntity caster);

    public Tag<String> getHardIncompatibility(LivingEntity caster) {//TODO implement
        return none;
    }

    public boolean equippedTick(LivingEntity caster, SkillData stats) {
        return false;
    }

    public boolean markTick(LivingEntity caster, LivingEntity target, SkillData sd) {
        if (sd.getDuration() <= 0) {
            removeMark(target);
            return true;
        }
        return false;
    }

    public boolean showsMark(SkillData mark, LivingEntity target){
        return true;
    }

    protected void removeMark(LivingEntity target) {
        Marks.getCap(target).removeMark(this);
    }

    public void onEquip(LivingEntity caster) {
        SkillData put = new SkillData(this, 0).setState(STATE.INACTIVE).setCaster(caster);
        onStateChange(caster, put, STATE.INACTIVE, STATE.COOLING);
        put.markDirty();
        CasterData.getCap(caster).getAllSkillData().put(this, put);
    }

    public void onUnequip(LivingEntity caster, SkillData stats) {
    }

    public void onProc(LivingEntity caster, Event procPoint, STATE state, SkillData stats, @Nullable LivingEntity target) {

    }

    public abstract boolean onStateChange(LivingEntity caster, SkillData prev, STATE from, STATE to);

    @Nullable
    public SkillData onMarked(LivingEntity caster, LivingEntity target, SkillData sd, @Nullable SkillData existing) {
        if (existing != null) {
            sd.setDuration(sd.getDuration() + existing.getDuration());
            sd.setMaxDuration(sd.getDuration());
        }
        return sd;
    }

    public void onMarkEnd(LivingEntity caster, LivingEntity target, SkillData sd) {

    }

    //***************************************//
    //   convenience functions start here    //
    //***************************************//

    /**
     * returns true if it does something expected, in which case this will just toggle the state.
     */
    protected boolean instantCast(SkillData mod, STATE from, STATE to) {
        //what
        if (from == to) {
            return true;
        }
        //switching between inactive and active
        if (to == STATE.HOLSTERED) {
            mod.setState(STATE.ACTIVE);
            return true;
        }
        //switching between cooling and inactive, duration test to prevent modification by holstering
        if (to == STATE.INACTIVE && from == STATE.COOLING && mod.getDuration() <= 0) {
            mod.setState(STATE.INACTIVE);
            return true;
        }
        return false;
    }

    /**
     * returns true if it does something expected, in which case this will just toggle the state.
     */
    protected boolean passive(SkillData mod, STATE from, STATE to) {
        //what
        if (from == to) {
            return true;
        }
        //switching between cooling and inactive, duration test to prevent modification by holstering
        if (to == STATE.INACTIVE && from == STATE.COOLING && mod.getDuration() <= 0) {
            mod.setState(STATE.INACTIVE);
            return true;
        }
        return false;
    }

    /**
     * returns true if it does something expected, in which case this will just toggle the state.
     */
    protected boolean boundCast(SkillData mod, STATE from, STATE to) {
        //what
        if (from == to) {
            return true;
        }
        //switching between inactive and holstered
        if ((from == STATE.INACTIVE && to == STATE.HOLSTERED) || (from == STATE.HOLSTERED && to == STATE.INACTIVE)) {
            mod.setState(to);
            return true;
        }
        //switching between cooling and inactive, duration test to prevent modification by holstering
        if (to == STATE.INACTIVE && from == STATE.COOLING && mod.getDuration() <= 0) {
            //caster.world.playMovingSound(null, caster, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 0.3f + WarDance.rand.nextFloat(), 0.5f + WarDance.rand.nextFloat());
            mod.setState(STATE.INACTIVE);
            return true;
        }
        return false;
    }

    protected void attackCooldown(Event e, LivingEntity caster, SkillData stats) {
        if (e instanceof LivingAttackEvent && ((LivingAttackEvent) e).getEntityLiving() != caster && stats.getState() == STATE.COOLING && e.getPhase() == EventPriority.HIGHEST) {
            stats.decrementDuration();
        }
    }

    protected boolean cooldownTick(SkillData stats) {
        if (stats.getState() == STATE.COOLING && stats.getDuration() > 0) {
            stats.decrementDuration(0.05f);
            int round = (int) (stats.getDuration() * 20);
            return stats.getDuration() < 3 || round % 20 == 0;
        }
        return false;
    }

    protected boolean activeTick(SkillData stats) {
        if (stats.getState() == STATE.ACTIVE) {
            stats.decrementDuration(0.05f);
            int round = (int) (stats.getDuration() * 20);
            return stats.getDuration() < 3 || round % 20 == 0;
        }
        return false;
    }

    protected boolean cast(LivingEntity caster, float duration) {
        return cast(caster, null, duration);
    }

    protected boolean cast(LivingEntity caster, LivingEntity target, float duration) {
        return cast(caster, target, duration, false, 0);
    }

    protected boolean cast(LivingEntity caster, @Nullable LivingEntity target, float duration, boolean flag, float arbitrary) {
        SkillResourceEvent sre = new SkillResourceEvent(caster, target, this);
        MinecraftForge.EVENT_BUS.post(sre);
        if (!sre.isCanceled() && CombatData.getCap(caster).getMight() >= sre.getMight() && CombatData.getCap(caster).getSpirit() >= sre.getSpirit()) {
            SkillCastEvent sce = new SkillCastEvent(caster, target, this, sre.getMight(), sre.getSpirit(), duration, flag, arbitrary);
            MinecraftForge.EVENT_BUS.post(sce);
            CombatData.getCap(caster).consumeMight(sce.getMight());
            CombatData.getCap(caster).consumeSpirit(sce.getSpirit());
            activate(caster, sce.getDuration(), sce.isFlag(), sce.getArbitrary());
            return true;
        }
        return false;
    }

    protected void setCooldown(LivingEntity caster, SkillData a, float duration) {
        SkillCooldownEvent sce = new SkillCooldownEvent(caster, this, duration);
        MinecraftForge.EVENT_BUS.post(sce);
//        if (getParentSkill() != null)
//            CasterData.getCap(caster).setSkillCooldown(getParentSkill(), sce.getCooldown());
        a.flagCondition(false);
        a.setArbitraryFloat(0);
        a.setState(STATE.COOLING);
        final float dur = CasterData.getCap(caster).getSkillData(this).isPresent() ? sce.getCooldown() : duration;
        a.setMaxDuration(dur);
        a.setDuration(dur);
    }

    protected boolean activate(LivingEntity caster, float duration) {
        return activate(caster, duration, false, 0);
    }

    protected boolean activate(LivingEntity caster, float duration, boolean flag) {
        return activate(caster, duration, flag, 0);
    }

    protected boolean activate(LivingEntity caster, float duration, float something) {
        return activate(caster, duration, false, something);
    }

    /**
     * @return whether the skill was successfully cast
     */
    protected boolean activate(LivingEntity caster, float duration, boolean flag, float something) {
        //System.out.println("enabling for " + duration);
        caster.level.playSound(null, caster, SoundEvents.FIRECHARGE_USE, SoundCategory.AMBIENT, 0.3f + WarDance.rand.nextFloat(), 0.5f + WarDance.rand.nextFloat());
        CasterData.getCap(caster).getSkillData(this).ifPresent(a -> {
            a.setDuration(duration);
            a.setMaxDuration(duration);
            a.flagCondition(flag);
            a.setArbitraryFloat(something);
            a.setState(STATE.ACTIVE);
        });
        return true;
    }

    protected void markUsed(LivingEntity caster) {
        if (GeneralConfig.debug)
            WarDance.LOGGER.debug(this.getRegistryName() + " has ended");
        caster.level.playSound(null, caster, SoundEvents.FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.3f + WarDance.rand.nextFloat() * 0.5f, 0.5f + WarDance.rand.nextFloat());
        CasterData.getCap(caster).getSkillData(this).ifPresent(a -> {
            a.setDuration(-Float.MAX_VALUE / 2);
            a.setState(STATE.ACTIVE);
        });
    }

    protected void mark(LivingEntity caster, LivingEntity target, float duration) {
        mark(caster, target, duration, 0);
    }

    protected void mark(LivingEntity caster, LivingEntity target, float duration, float arbitrary) {
        Marks.getCap(target).mark(new SkillData(this, duration).setCaster(caster).setArbitraryFloat(arbitrary));
    }

    protected SkillData getExistingMark(LivingEntity target) {
        return Marks.getCap(target).getActiveMark(this).orElse(SkillData.DUMMY);
    }

    protected boolean hasMark(LivingEntity target){
        return Marks.getCap(target).getActiveMark(this).isPresent();
    }

    public enum CastStatus {
        ALLOWED,
        COOLDOWN,
        CONFLICT,
        HOLSTERED,
        SPIRIT,
        MIGHT,
        SILENCE,
        ACTIVE,
        OTHER
    }

    public enum STATE {
        INACTIVE,
        HOLSTERED,
        ACTIVE,
        COOLING
    }


}