package jackiecrazy.cloakanddagger.capability.vision;

import jackiecrazy.cloakanddagger.networking.CombatChannel;
import jackiecrazy.cloakanddagger.networking.UpdateClientPacket;
import jackiecrazy.cloakanddagger.potion.WarEffects;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.PacketDistributor;

import java.lang.ref.WeakReference;

public class Vision implements IVision {

    private final WeakReference<LivingEntity> dude;
    int lastRangeTick = 0;
    private float vision;
    private int retina;
    private long lastUpdate;
    private Vector3d motion;

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
        if (elb.hasEffect(WarEffects.SLEEP.get()) || elb.hasEffect(WarEffects.PARALYSIS.get()) || elb.hasEffect(WarEffects.PETRIFY.get()))
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
        CombatChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> elb), new UpdateClientPacket(elb.getId(), write()));
        if (!(elb instanceof FakePlayer) && elb instanceof ServerPlayerEntity)
            CombatChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) elb), new UpdateClientPacket(elb.getId(), write()));

    }

    @Override
    public void read(CompoundNBT c) {
        lastUpdate = c.getLong("lastUpdate");
        retina = c.getInt("retina");
        vision = c.getFloat("vision");
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Vector3d getMotionConsistently() {
        if (dude.get() == null || motion == null) return Vector3d.ZERO;
        return dude.get().position().subtract(motion).scale(0.25);
    }

    @Override
    public CompoundNBT write() {
        CompoundNBT c = new CompoundNBT();
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
