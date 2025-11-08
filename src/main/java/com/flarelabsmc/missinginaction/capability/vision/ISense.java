package com.flarelabsmc.missinginaction.capability.vision;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface ISense {

    void clientTick();

    void serverTick();

    void sync();

    void read(CompoundTag tag);

    boolean isValid();

    CompoundTag write();

    int getRetina();

    float visionRange();

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
