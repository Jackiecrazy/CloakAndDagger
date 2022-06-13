package jackiecrazy.cloakanddagger.capability.vision;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VisionData implements ICapabilitySerializable<CompoundNBT> {
    private static IVision OHNO=new DummyVision();

    @CapabilityInject(IVision.class)
    public static Capability<IVision> CAP = null;

    public static IVision getCap(LivingEntity le) {
        return le.getCapability(CAP).orElse(OHNO);//.orElseThrow(() -> new IllegalArgumentException("attempted to find a nonexistent capability"));
    }

    private final LazyOptional<IVision> instance;

    public VisionData(LivingEntity e) {
        instance = LazyOptional.of(() -> new Vision(e));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, instance);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return (CompoundNBT) CAP.getStorage().writeNBT(
                CAP,
                instance.orElseThrow(() ->
                        new IllegalArgumentException("LazyOptional cannot be empty!")),
                null);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        CAP.getStorage().readNBT(
                CAP,
                instance.orElseThrow(() ->
                        new IllegalArgumentException("LazyOptional cannot be empty!")),
                null, nbt);
    }
}
