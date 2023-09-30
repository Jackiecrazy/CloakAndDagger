package jackiecrazy.cloakanddagger.capability.vision;

import jackiecrazy.cloakanddagger.networking.StealthChannel;
import jackiecrazy.cloakanddagger.networking.UpdateClientPacket;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.potion.FootworkEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;
import org.checkerframework.checker.units.qual.C;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Sense implements ISense {

    public static final DetectionData DUMMY = new DetectionData(0);
    private static final int SEE_COOLDOWN = 20;
    private final WeakReference<LivingEntity> dude;
    private final HashMap<LivingEntity, DetectionData> detectionTracker = new HashMap<>();
    private float vision;
    private int retina;
    private long lastUpdate;
    private Vec3 motion;

    private LivingEntity target;

    public Sense(LivingEntity e) {
        dude = new WeakReference<>(e);
    }

    @Override
    public void clientTick() {
    }

    @Override
    public void serverTick() {
        LivingEntity elb = dude.get();
        if (elb == null) return;
        final int ticks = (int) (elb.level.getGameTime() - lastUpdate);
        if (ticks < 1) return;//sometimes time runs backwards
        for (Iterator<Map.Entry<LivingEntity, DetectionData>> it = detectionTracker.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<LivingEntity, DetectionData> next = it.next();
            final DetectionData data = next.getValue();
            if (data.lastUpdate + SEE_COOLDOWN < next.getKey().tickCount)
                data.current -= (0.01f * ticks);
            else data.current = Math.min(data.current + data.perTick * ticks, 1);
            if (next.getKey().isDeadOrDying() || data.current <= 0)
                it.remove();
        }
        ;
        //update max values
        vision = (float) elb.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (elb.hasEffect(FootworkEffects.SLEEP.get()) || elb.hasEffect(FootworkEffects.PARALYSIS.get()) || elb.hasEffect(FootworkEffects.PETRIFY.get()))
            vision = -1;
        //update internal retina values
        int light = StealthOverride.getActualLightLevel(elb.level, elb.blockPosition());
        for (long x = lastUpdate + ticks; x > lastUpdate; x--) {
            if (x % 3 == 0) {
                if (light > retina)
                    retina++;
                if (light < retina)
                    retina--;
            }
        }
        //store motion for further use
        if (ticks > 5 || (lastUpdate + ticks) % 5 != lastUpdate % 5)
            motion = elb.position();
        lastUpdate = elb.level.getGameTime();
        sync();
    }

    @Override
    public void sync() {

        LivingEntity elb = dude.get();
        if (elb == null || elb.level.isClientSide) return;
        StealthChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> elb), new UpdateClientPacket(elb.getId(), write()));
        if (!(elb instanceof FakePlayer) && elb instanceof ServerPlayer)
            StealthChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) elb), new UpdateClientPacket(elb.getId(), write()));

    }

    @Override
    public void read(CompoundTag c) {
        lastUpdate = c.getLong("lastUpdate");
        retina = c.getInt("retina");
        vision = c.getFloat("vision");
        final LivingEntity dude = this.dude.get();
        if (dude instanceof Mob m && m.level.getEntity(c.getInt("target")) instanceof LivingEntity t) {
            m.setTarget(t);
        }
        detectionTracker.clear();
        if (c.get("detecting") instanceof CompoundTag ct && dude != null) {
            for (String i : ct.getAllKeys()) {
                if (dude.level.getEntity(Integer.valueOf(i)) instanceof LivingEntity elb) {
                    detectionTracker.put(elb, new DetectionData(ct.getFloat(i)));
                }
            }
        }
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Vec3 getMotionConsistently() {
        if (dude.get() == null || motion == null) return Vec3.ZERO;
        return dude.get().position().subtract(motion).scale(0.25);
    }

    @Override
    public CompoundTag write() {
        CompoundTag c = new CompoundTag();
        c.putInt("retina", getRetina());
        c.putFloat("vision", visionRange());
        c.putLong("lastUpdate", lastUpdate);
        if (dude.get() instanceof Mob m && m.getTarget() != null) {
            c.putInt("target", m.getTarget().getId());
        }
        CompoundTag list = new CompoundTag();
        detectionTracker.forEach((a, b) -> list.putFloat(String.valueOf(a.getId()), b.current));
        c.put("detecting", list);
        return c;
    }

    @Override
    public int getRetina() {
        return retina;
    }

    @Override
    public float visionRange() {
        return vision;
    }

    @Override
    public void modifyDetection(LivingEntity target, float amnt) {
        final int lastCheck = detectionTracker.getOrDefault(target, DUMMY).lastUpdate;
        if (lastCheck < target.tickCount) {
            //interpolate a bit
//            if (lastCheck + SEE_COOLDOWN > target.tickCount)
//                amnt *= (target.tickCount - lastCheck) / 20f;
            detectionTracker.merge(target, new DetectionData(target.tickCount, amnt), (a, b) -> new DetectionData(target.tickCount, Mth.clamp(b.perTick, 0, getMaxDetection(target)), a.current));
        }
    }

    @Override
    public float getDetection(LivingEntity target) {
        return detectionTracker.getOrDefault(target, DUMMY).current;
    }

    @Override
    public void resetDetection(LivingEntity target) {
        detectionTracker.remove(target);
    }

    @Override
    public LivingEntity getLookingFor() {
        return target;
    }

    @Override
    public void setLookingFor(LivingEntity target) {
        this.target = target;
    }

    private static class DetectionData {
        int lastUpdate = 0;
        float perTick = 0;
        float current = 0;

        public DetectionData(int lastUpdate, float perTick) {
            this.lastUpdate = lastUpdate;
            this.perTick = perTick;
        }

        public DetectionData(int lastUpdate, float perTick, float current) {
            this.lastUpdate = lastUpdate;
            this.perTick = perTick;
            this.current = current;
        }

        public DetectionData(float current) {
            this.current = current;
        }
    }
}
