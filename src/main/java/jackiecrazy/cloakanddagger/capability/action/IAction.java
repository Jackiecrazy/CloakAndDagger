package jackiecrazy.cloakanddagger.capability.action;

import net.minecraft.nbt.CompoundTag;

public interface IAction {
    public boolean canSee();

    public boolean canStab();

    public void setSee(boolean yes);

    public void setStab(boolean yes);

    public void read(CompoundTag from);

    public CompoundTag write();
}
