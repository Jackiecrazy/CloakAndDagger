package com.flarelabsmc.missinginaction.capability.vision;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SenseData implements ICapabilitySerializable<CompoundTag> {
    private static final ISense DUMMY = new DummySense();

    public static Capability<ISense> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static ISense getCap(LivingEntity le) {
        return le.getCapability(CAP).orElse(DUMMY);
    }

    private final ISense instance;

    public SenseData(LivingEntity e) {
        instance = new Sense(e);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, LazyOptional.of(()->instance));
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.write();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.read(nbt);
    }
}
