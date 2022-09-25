package jackiecrazy.cloakanddagger.capability.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GoalCapabilityProvider implements ICapabilitySerializable<Tag> {

    @CapabilityInject(IGoalHelper.class)
    public static Capability<IGoalHelper> CAP = null;

    public static LazyOptional<IGoalHelper> getCap(LivingEntity le) {
        return le.getCapability(CAP);//.orElseThrow(() -> new IllegalArgumentException("attempted to find a nonexistent capability"));
    }


    private LazyOptional<IGoalHelper> instance = LazyOptional.of(CAP::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CAP ? instance.cast() : LazyOptional.empty();    }

    @Override
    public Tag serializeNBT() {
        return CAP.getStorage().writeNBT(CAP, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        CAP.getStorage().readNBT(CAP, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null, nbt);
    }
}
