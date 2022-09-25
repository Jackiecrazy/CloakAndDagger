package jackiecrazy.cloakanddagger.capability.vision;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class VisionStorage implements Capability.IStorage<IVision> {
    @Nullable
    @Override
    public Tag writeNBT(Capability<IVision> capability, IVision iCombatCapability, Direction direction) {
        return iCombatCapability.write();
    }

    @Override
    public void readNBT(Capability<IVision> capability, IVision iCombatCapability, Direction direction, Tag inbt) {
        if(inbt instanceof CompoundTag) {
            iCombatCapability.read((CompoundTag) inbt);
        }
    }
}
