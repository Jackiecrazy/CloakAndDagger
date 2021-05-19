package jackiecrazy.wardance.capability.skill;

import com.google.common.collect.Maps;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.SyncSkillPacket;
import jackiecrazy.wardance.skill.ProcPoint;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillCooldownData;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

public class SkillCapability implements ISkillCapability {
    private final Map<Skill, SkillData> activeSkills = Maps.newHashMap();
    private final Map<Skill, SkillCooldownData> coolingSkills = Maps.newHashMap();
    private final List<Skill> equippedSkill = new ArrayList<>(12);
    //weapon bound skills are added to their NBT instead of being handled here.
    //^not anymore. All skills are now self bound.
    private final WeakReference<LivingEntity> dude;
    private Queue<Skill> lastCast = new LinkedList<>();

    public SkillCapability(LivingEntity attachTo) {
        dude = new WeakReference<>(attachTo);
    }

    @Nullable
    @Override
    public Optional<SkillData> getActiveSkill(Skill s) {
        return Optional.of(activeSkills.get(s));
    }

    @Override
    public void activateSkill(SkillData d) {
        if (activeSkills.containsKey(d.getSkill())) {
            WarDance.LOGGER.warn("skill " + d + " is already active, overwriting.");
        }
        activeSkills.put(d.getSkill(), d);
    }

    @Override
    public void removeActiveSkill(Skill s) {
        SkillData sd = activeSkills.get(s);
        LivingEntity caster = dude.get();
        if (sd != null && caster != null) {
            sd.getSkill().onEffectEnd(caster, sd);
        }
        lastCast.add(s);
        while (lastCast.size() > 5)
            lastCast.remove();
        activeSkills.remove(s);
    }

    @Override
    public Map<Skill, SkillData> getActiveSkills() {
        return activeSkills;
    }

    @Override
    public void clearActiveSkills() {
        for (Skill s : new HashSet<>(activeSkills.keySet())) {
            removeActiveSkill(s);
        }
    }

    @Override
    public boolean isSkillActive(Skill skill) {
        if (skill.getParentSkill() == null)
            for (Skill s : activeSkills.keySet()) {
                if (s.isFamily(skill)) return true;
            }
        return activeSkills.containsKey(skill);
    }

    @Override
    public boolean isTagActive(String tag) {
        for (Map.Entry<Skill, SkillData> e : activeSkills.entrySet()) {
            if (e.getKey().getTags(dude.get()).contains(tag))
                return true;
        }
        return false;
    }

    @Override
    public void removeActiveTag(String tag) {
        for (Map.Entry<Skill, SkillData> e : activeSkills.entrySet()) {
            if (e.getKey().getTags(dude.get()).contains(tag))
                markSkillUsed(e.getKey());
        }
    }

    @Override
    public void markSkillUsed(Skill s) {
        if (activeSkills.containsKey(s))
            activeSkills.get(s).setDuration(-1);
    }

    @Override
    public void setSkillCooldown(Skill s, float amount) {
        float highest = amount;
        if (coolingSkills.containsKey(s))
            highest = Math.max(coolingSkills.get(s).getMaxDuration(), amount);
        coolingSkills.put(s, new SkillCooldownData(s, highest, amount));
        CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) dude.get()), new SyncSkillPacket(this.write()));
    }

    @Override
    public boolean isSkillCoolingDown(Skill s) {
        return coolingSkills.containsKey(s.tryGetParentSkill())||coolingSkills.containsKey(s);
    }

    @Override
    public void decrementSkillCooldown(Skill s, float amount) {
        if (!coolingSkills.containsKey(s)) return;
        coolingSkills.get(s).decrementDuration(amount);
        LivingEntity caster = dude.get();
        if (caster instanceof ServerPlayerEntity)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) caster), new SyncSkillPacket(this.write()));
    }

    @Override
    public void coolSkill(Skill s) {
        s.onCooledDown(dude.get(), coolingSkills.remove(s).getDuration());
        LivingEntity caster = dude.get();
        if (caster instanceof ServerPlayerEntity)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) caster), new SyncSkillPacket(this.write()));
        //todo skill cooldown hud
    }

    @Override
    public float getSkillCooldown(Skill s) {
        if (!coolingSkills.containsKey(s.tryGetParentSkill()))
            return 0;
        return coolingSkills.get(s.tryGetParentSkill()).getDuration();
    }

    @Override
    public float getMaxSkillCooldown(Skill s) {
        if (!coolingSkills.containsKey(s.tryGetParentSkill()))
            return 1;
        return coolingSkills.get(s.tryGetParentSkill()).getMaxDuration();
    }

    @Override
    public Map<Skill, SkillCooldownData> getSkillCooldowns() {
        return coolingSkills;
    }

    @Override
    public void clearSkillCooldowns() {
        coolingSkills.clear();
    }

    @Override
    public List<Skill> getEquippedSkills() {
        return equippedSkill;
    }

    @Override
    public void setEquippedSkills(List<Skill> skills) {
        equippedSkill.clear();
        equippedSkill.addAll(skills);
        for (Skill s : skills) {
            if (s != null && s.isPassive(dude.get())) {
                s.checkAndCast(dude.get());
            }
        }
    }

    @Override
    public boolean isSkillUsable(Skill skill) {
        if(!equippedSkill.contains(skill))return false;
        return skill.canCast(dude.get());
    }

    @Override
    public CompoundNBT write() {
        CompoundNBT to=new CompoundNBT();
        if (!this.activeSkills.isEmpty()) {
            ListNBT listnbt = new ListNBT();

            for (SkillData effectinstance : this.activeSkills.values()) {
                listnbt.add(effectinstance.write(new CompoundNBT()));
            }

            to.put("ActiveSkills", listnbt);
        }
        if (!this.coolingSkills.isEmpty()) {
            ListNBT listnbt = new ListNBT();

            for (SkillCooldownData effectinstance : this.coolingSkills.values()) {
                listnbt.add(effectinstance.write(new CompoundNBT()));
            }

            to.put("CoolingSkills", listnbt);
        }
        for (int a = 0; a < equippedSkill.size(); a++)
            if (equippedSkill.get(a) != null)
                to.putString("equippedSkill" + a, equippedSkill.get(a).getRegistryName().toString());
        return to;
    }

    @Override
    public void read(CompoundNBT from) {
        activeSkills.clear();
        if (from.contains("ActiveSkills", 9)) {
            ListNBT listnbt = from.getList("ActiveSkills", 10);

            for (int i = 0; i < listnbt.size(); ++i) {
                CompoundNBT compoundnbt = listnbt.getCompound(i);
                SkillData effectinstance = SkillData.read(compoundnbt);
                if (effectinstance != null) {
                    this.activeSkills.put(effectinstance.getSkill(), effectinstance);
                }
            }
        }
        coolingSkills.clear();
        if (from.contains("CoolingSkills", 9)) {
            ListNBT listnbt = from.getList("CoolingSkills", 10);

            for (int i = 0; i < listnbt.size(); ++i) {
                CompoundNBT compoundnbt = listnbt.getCompound(i);
                SkillCooldownData effectinstance = SkillCooldownData.read(compoundnbt);
                if (effectinstance != null) {
                    this.coolingSkills.put(effectinstance.getSkill(), effectinstance);
                }
            }
        }
        Skill[] als = new Skill[12];
        for (int a = 0; a < als.length; a++)
            if (from.contains("equippedSkill" + a))
                als[a] = (Skill.getSkill(from.getString("equippedSkill" + a)));
        setEquippedSkills(Arrays.asList(als));
    }

    @Override
    public void update() {
        boolean sync = false;
        final LivingEntity caster = dude.get();
        for (SkillData d : activeSkills.values()) {
            if (d.getSkill().getTags(caster).contains(ProcPoint.countdown)) {
                d.decrementDuration();
                sync = true;
            }
        }
        for (SkillCooldownData d : coolingSkills.values()) {
            if (d.getSkill().getTags(caster).contains(ProcPoint.recharge_time)) {
                d.decrementDuration(-1);
                sync = true;
            }
        }
        HashSet<Skill> finish = new HashSet<>();
        for (Map.Entry<Skill, SkillData> cd : getActiveSkills().entrySet()) {
            if (cd.getValue().getDuration() < 0) {
                finish.add(cd.getKey());
                sync = true;
            }
        }
        for (Skill s : finish)
            removeActiveSkill(s);
        finish.clear();
        for (Map.Entry<Skill, SkillCooldownData> cd : getSkillCooldowns().entrySet()) {
            if (cd.getValue().getDuration() <= 0) {
                finish.add(cd.getKey());
                sync = true;
            }
        }
        for (Skill s : finish)
            coolSkill(s);
        if (sync && caster instanceof ServerPlayerEntity)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) caster), new SyncSkillPacket(this.write()));
    }

    @Override
    public Skill[] getPastCasts() {
        return lastCast.toArray(new Skill[5]);
    }
}
