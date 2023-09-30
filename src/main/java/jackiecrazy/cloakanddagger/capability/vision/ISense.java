package jackiecrazy.cloakanddagger.capability.vision;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface ISense {

    void clientTick();

    void serverTick();

    void sync();

    void read(CompoundTag tag);

    boolean isValid();

    Vec3 getMotionConsistently();//I can't believe I have to do this.

    CompoundTag write();

    int getRetina();

    float visionRange();

    //per target: current, max, update (ticks up a variable), reset
    //a capability that holds a search target object, and an alertness float.
    // Each targeting check the alertness goes either up or down depending on how far the target is in detection range as per usual stealth formula
    // and hitting a certain threshold leads to alert
    // (todo: mobs turn to sound sources like warden)
    void modifyDetection(LivingEntity target, float amnt);
    float getDetection(LivingEntity target);
    default float getMaxDetection(LivingEntity target){
        return 1;
    }
    default float getDetectionPerc(LivingEntity target){
        return getDetection(target)/getMaxDetection(target);
    }
    void resetDetection(LivingEntity target);

    LivingEntity getLookingFor();
    void setLookingFor(LivingEntity target);
}
