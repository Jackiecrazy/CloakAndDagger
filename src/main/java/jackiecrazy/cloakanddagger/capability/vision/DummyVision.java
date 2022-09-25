package jackiecrazy.cloakanddagger.capability.vision;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.world.phys.Vec3;

public class DummyVision implements IVision {

    @Override
    public void clientTick() {

    }

    @Override
    public void serverTick() {

    }

    @Override
    public void sync() {

    }

    @Override
    public void read(CompoundTag tag) {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public Vec3 getMotionConsistently() {
        return Vec3.ZERO;
    }

    @Override
    public CompoundTag write() {
        return new CompoundTag();
    }

    @Override
    public int getRetina() {
        return 0;
    }

    @Override
    public float visionRange() {
        return 0;
    }
}
