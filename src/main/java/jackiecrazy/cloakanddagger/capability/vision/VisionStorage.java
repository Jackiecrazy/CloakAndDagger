package jackiecrazy.cloakanddagger.capability.vision;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class VisionStorage implements Capability.IStorage<IVision> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<IVision> capability, IVision iCombatCapability, Direction direction) {
        return iCombatCapability.write();
    }

    @Override
    public void readNBT(Capability<IVision> capability, IVision iCombatCapability, Direction direction, INBT inbt) {
        if(inbt instanceof CompoundNBT) {
            iCombatCapability.read((CompoundNBT) inbt);
        }
    }
}
