package jackiecrazy.cloakanddagger.capability.vision;

import jackiecrazy.cloakanddagger.networking.StealthChannel;
import jackiecrazy.cloakanddagger.networking.UpdateClientPacket;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.potion.FootworkEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;

import java.lang.ref.WeakReference;

public class Vision implements IVision {

    private final WeakReference<LivingEntity> dude;
    int lastRangeTick = 0;
    private float vision;
    private int retina;
    private long lastUpdate;
    private Vec3 motion;

    public Vision(LivingEntity e) {
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
}
