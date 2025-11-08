package com.flarelabsmc.missinginaction.capability.vision;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class DummySense implements ISense {
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

    @Override
    public void modifyDetection(LivingEntity target, float amnt) {

    }

    @Override
    public float getDetection(LivingEntity target) {
        return 0;
    }

    @Override
    public void resetDetection(LivingEntity target) {

    }

    @Override
    public LivingEntity getLookingFor() {
        return null;
    }

    @Override
    public void setLookingFor(LivingEntity target) {

    }
}
