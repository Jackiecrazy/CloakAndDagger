package jackiecrazy.cloakanddagger.capability.vision;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

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
    public void read(CompoundNBT tag) {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public Vector3d getMotionConsistently() {
        return Vector3d.ZERO;
    }

    @Override
    public CompoundNBT write() {
        return new CompoundNBT();
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
