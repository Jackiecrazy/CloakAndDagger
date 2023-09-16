package jackiecrazy.cloakanddagger.capability.action;

import jackiecrazy.cloakanddagger.networking.StealthChannel;
import jackiecrazy.cloakanddagger.networking.UpdateClientPermissionPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

public class AmoPermissions implements IAction {

    Player boundTo;

    boolean see = true, stab = true;

    public AmoPermissions() {

    }

    public AmoPermissions(Player bind) {
        boundTo = bind;
    }

    @Override
    public boolean canSee() {
        return see;
    }

    @Override
    public boolean canStab() {
        return stab;
    }

    @Override
    public void setSee(boolean yes) {
        see = yes;
        sync();
    }

    @Override
    public void setStab(boolean yes) {
        stab = yes;
        sync();
    }

    @Override
    public void read(CompoundTag from) {
        see = from.getBoolean("see");
        stab = from.getBoolean("stab");
    }

    @Override
    public CompoundTag write() {
        CompoundTag ct = new CompoundTag();
        ct.putBoolean("see", see);
        ct.putBoolean("stab", stab);
        return ct;
    }

    private void sync() {
        if (boundTo instanceof ServerPlayer sp)
            StealthChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new UpdateClientPermissionPacket(boundTo.getId(), write()));
    }
}
