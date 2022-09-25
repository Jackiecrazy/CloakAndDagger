package jackiecrazy.cloakanddagger.capability.vision;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public interface IVision {

    void clientTick();

    void serverTick();

    void sync();

    void read(CompoundTag tag);

    boolean isValid();

    Vec3 getMotionConsistently();//I can't believe I have to do this.

    CompoundTag write();

    int getRetina();

    float visionRange();
}
