package jackiecrazy.wardance.capability.status;

import com.google.common.collect.Maps;
import jackiecrazy.wardance.WarDance;
import jackiecrazy.wardance.handlers.EntityHandler;
import jackiecrazy.wardance.networking.CombatChannel;
import jackiecrazy.wardance.networking.UpdateAfflictionPacket;
import jackiecrazy.wardance.skill.Skill;
import jackiecrazy.wardance.skill.SkillData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.fml.network.PacketDistributor;

import java.lang.ref.WeakReference;
import java.util.*;

public class Mark implements IMark {
    private final Map<Skill, SkillData> statuus = Maps.newHashMap();
    private final WeakReference<LivingEntity> dude;
    boolean sync = false;

    public Mark(LivingEntity attachTo) {
        dude = new WeakReference<>(attachTo);
    }

    @Override
    public Optional<SkillData> getActiveMark(Skill s) {
        return Optional.ofNullable(statuus.get(s));
    }

    @Override
    public void mark(SkillData d) {
        if (dude.get() == null) return;
        if (statuus.containsKey(d.getSkill())) {
            WarDance.LOGGER.warn("status " + d + " is already active, merging according to rules.");
        }
        SkillData sd = d.getSkill().onMarked(d.getCaster(dude.get().world), dude.get(), d, statuus.get(d.getSkill()));
        statuus.put(d.getSkill(), sd);
        sync = true;
    }

    @Override
    public void removeMark(Skill s) {
        SkillData sd = statuus.get(s);
        LivingEntity victim = dude.get();
        if (sd != null && victim != null) {
            sd.getSkill().onMarkEnd(sd.getCaster(victim.world), victim, sd);
        }
        sync = true;
        statuus.remove(s);
    }

    @Override
    public Map<Skill, SkillData> getActiveMarks() {
        return statuus;
    }

    @Override
    public void clearMarks() {
        for (Skill s : new HashSet<>(statuus.keySet())) {
            removeMark(s);
        }
    }

    @Override
    public boolean isMarked(Skill skill) {
        if (statuus.containsKey(skill)) return true;
        if (skill.getParentSkill() == null)
            for (Skill s : statuus.keySet()) {
                if (s.isFamily(skill)) return true;
            }
        return false;
    }

    @Override
    public CompoundNBT write() {
        CompoundNBT to = new CompoundNBT();
        if (!this.statuus.isEmpty()) {
            ListNBT listnbt = new ListNBT();

            for (SkillData effectinstance : this.statuus.values()) {
                listnbt.add(effectinstance.write(new CompoundNBT()));
            }

            to.put("ActiveAfflictions", listnbt);
        }
        return to;
    }

    @Override
    public void read(CompoundNBT from) {
        statuus.clear();
        if (from.contains("ActiveAfflictions", 9)) {
            ListNBT listnbt = from.getList("ActiveAfflictions", 10);

            for (int i = 0; i < listnbt.size(); ++i) {
                CompoundNBT compoundnbt = listnbt.getCompound(i);
                SkillData effectinstance = SkillData.read(compoundnbt);
                if (effectinstance != null) {
                    this.statuus.put(effectinstance.getSkill(), effectinstance);
                }
            }
        }
    }

    @Override
    public void update() {
        final LivingEntity ticker = dude.get();
        if (ticker == null) return;
        final Collection<SkillData> active = new ArrayList<>(getActiveMarks().values());
        for (SkillData cd : active) {
            if (cd.getSkill().markTick(cd.getCaster(ticker.world), ticker, cd)) sync = true;
        }
        if (sync && ticker instanceof ServerPlayerEntity) {
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) ticker), new UpdateAfflictionPacket(ticker.getEntityId(), this.write()));
            sync=false;
        }
        if (sync && EntityHandler.mustUpdate.containsValue(ticker)) {
            CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> ticker), new UpdateAfflictionPacket(ticker.getEntityId(), this.write()));
            sync = false;
        }
    }
}