package jackiecrazy.cloakanddagger.networking;

import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpdateClientPacket {
    int e;
    CompoundTag icc;

    public UpdateClientPacket(int ent, CompoundTag c) {
        e = ent;
        icc = c;
    }

    public static class UpdateClientEncoder implements BiConsumer<UpdateClientPacket, FriendlyByteBuf> {

        @Override
        public void accept(UpdateClientPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.e);
            packetBuffer.writeNbt(updateClientPacket.icc);
        }
    }

    public static class UpdateClientDecoder implements Function<FriendlyByteBuf, UpdateClientPacket> {

        @Override
        public UpdateClientPacket apply(FriendlyByteBuf packetBuffer) {
            return new UpdateClientPacket(packetBuffer.readInt(), packetBuffer.readNbt());
        }
    }

    public static class UpdateClientHandler implements BiConsumer<UpdateClientPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(UpdateClientPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientLevel world = Minecraft.getInstance().level;
                if (world != null) {
                    Entity entity = world.getEntity(updateClientPacket.e);
                    if (entity instanceof LivingEntity) SenseData.getCap((LivingEntity) entity).read(updateClientPacket.icc);
                }
            }));
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
