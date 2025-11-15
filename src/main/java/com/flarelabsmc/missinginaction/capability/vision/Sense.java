package com.flarelabsmc.missinginaction.capability.vision;

import com.flarelabsmc.missinginaction.networking.StealthChannel;
import com.flarelabsmc.missinginaction.networking.UpdateClientPacket;
import com.flarelabsmc.missinginaction.utils.StealthOverride;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Sense implements ISense {
    private static final float DETECTION_DECAY = 0.01f;

    private final WeakReference<LivingEntity> entity;
    private final HashMap<LivingEntity, DetectionData> detectionTracker = new HashMap<>();
    private float vision;
    private int retina;
    private long lastUpdate;
    private LivingEntity target;

    private CompoundTag cachedData;
    private boolean isDirty = true;

    public Sense(LivingEntity e) {
        entity = new WeakReference<>(e);
    }

    @Override
    public void clientTick() {
    }

    @Override
    public void serverTick() {
        LivingEntity e = entity.get();
        if (e == null) return;
        final long currentTime = e.level().getGameTime();
        final int ticks = (int) (currentTime - lastUpdate);
        if (ticks < 1) return;
        final float decay = DETECTION_DECAY * ticks;
        Iterator<Map.Entry<LivingEntity, DetectionData>> iterator = detectionTracker.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LivingEntity, DetectionData> entry = iterator.next();
            LivingEntity entity = entry.getKey();
            DetectionData data = entry.getValue();
            if (entity.isDeadOrDying()) {
                iterator.remove();
                continue;
            }
            if (data.lastUpdate < entity.tickCount) {
                data.current -= decay;
            } else {
                data.current = Math.min(data.current + data.perTick * ticks, 1);
            }
            if (data.current <= 0) {
                iterator.remove();
            }
        }
        vision = (float) e.getAttributeValue(Attributes.FOLLOW_RANGE);
        int light = StealthOverride.getActualLightLevel(e.level(), e.blockPosition());
        int lightUpdates = ticks / 3;
        if (light != retina) {
            retina = light > retina
                    ? Math.min(retina + lightUpdates, light)
                    : Math.max(retina - lightUpdates, light);
        }
        lastUpdate = currentTime;
        isDirty = true;
        sync();
    }

    @Override
    public void sync() {
        LivingEntity elb = entity.get();
        if (elb == null || elb.level().isClientSide) return;
        if (isDirty) {
            cachedData = write();
            isDirty = false;
        }
        StealthChannel.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> elb),
                new UpdateClientPacket(elb.getId(), cachedData)
        );
        if (elb instanceof ServerPlayer sp && !(elb instanceof FakePlayer)) {
            StealthChannel.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> sp),
                    new UpdateClientPacket(elb.getId(), cachedData)
            );
        }
    }

    @Override
    public void read(CompoundTag c) {
        lastUpdate = c.getLong("lastUpdate");
        retina = c.getInt("retina");
        vision = c.getFloat("vision");
        final LivingEntity dude = this.entity.get();
        if (dude instanceof Mob m) {
            int targetId = c.getInt("target");
            if (targetId != 0 && m.level().getEntity(targetId) instanceof LivingEntity t) {
                m.setTarget(t);
            }
        }
        detectionTracker.clear();
        if (c.get("detecting") instanceof CompoundTag ct && dude != null) {
            for (String key : ct.getAllKeys()) {
                try {
                    int entityId = Integer.parseInt(key);
                    if (dude.level().getEntity(entityId) instanceof LivingEntity elb) {
                        detectionTracker.put(elb, new DetectionData(ct.getFloat(key)));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        isDirty = true;
    }

    @Override
    public boolean isValid() {
        return entity.get() != null;
    }

    @Override
    public CompoundTag write() {
        CompoundTag c = new CompoundTag();
        c.putInt("retina", retina);
        c.putFloat("vision", vision);
        c.putLong("lastUpdate", lastUpdate);
        LivingEntity ent = entity.get();
        if (ent instanceof Mob m && m.getTarget() != null) {
            c.putInt("target", m.getTarget().getId());
        }
        if (!detectionTracker.isEmpty()) {
            CompoundTag list = new CompoundTag();
            detectionTracker.forEach((entity, data) ->
                    list.putFloat(String.valueOf(entity.getId()), data.current)
            );
            c.put("detecting", list);
        }
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
        final DetectionData existing = detectionTracker.get(target);
        final int lastCheck = existing != null ? existing.lastUpdate : 0;
        if (lastCheck < target.tickCount) {
            float clampedAmount = Mth.clamp(amnt, 0, getMaxDetection(target));
            if (existing != null) {
                existing.lastUpdate = target.tickCount;
                existing.perTick = clampedAmount;
            } else {
                detectionTracker.put(target, new DetectionData(target.tickCount, clampedAmount));
            }
            isDirty = true;
        }
    }

    @Override
    public float getDetection(LivingEntity target) {
        DetectionData data = detectionTracker.get(target);
        return data != null ? data.current : 0;
    }

    @Override
    public float getMaxDetection(LivingEntity target) {
        final LivingEntity dud = entity.get();
        if (dud == null) return 1;
        return 0.5f + dud.getHealth() / dud.getMaxHealth();
    }

    @Override
    public void resetDetection(LivingEntity target) {
        if (detectionTracker.remove(target) != null) {
            isDirty = true;
        }
    }

    @Override
    public LivingEntity getLookingFor() {
        return target;
    }

    @Override
    public void setLookingFor(LivingEntity target) {
        this.target = target;
    }

    public static class DetectionData {
        int lastUpdate = 0;
        float perTick = 0;
        float current = 0;

        public DetectionData(int lastUpdate, float perTick) {
            this.lastUpdate = lastUpdate;
            this.perTick = perTick;
        }

        public DetectionData(float current) {
            this.current = current;
        }
    }
}