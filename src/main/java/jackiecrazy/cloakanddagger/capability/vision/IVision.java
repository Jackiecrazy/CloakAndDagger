package jackiecrazy.cloakanddagger.capability.vision;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;

public interface IVision {

    void clientTick();

    void serverTick();

    void sync();

    void read(CompoundNBT tag);

    boolean isValid();

    Vector3d getMotionConsistently();//I can't believe I have to do this.

    CompoundNBT write();

    int getRetina();

    float visionRange();
}
