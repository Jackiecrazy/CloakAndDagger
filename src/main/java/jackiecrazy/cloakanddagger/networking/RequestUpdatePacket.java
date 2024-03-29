package jackiecrazy.cloakanddagger.networking;

import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import jackiecrazy.cloakanddagger.handlers.EntityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequestUpdatePacket {
    int e;

    public RequestUpdatePacket(int ent) {
        e = ent;
    }

    public static class RequestUpdateEncoder implements BiConsumer<RequestUpdatePacket, FriendlyByteBuf> {

        @Override
        public void accept(RequestUpdatePacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeInt(updateClientPacket.e);
        }
    }

    public static class RequestUpdateDecoder implements Function<FriendlyByteBuf, RequestUpdatePacket> {

        @Override
        public RequestUpdatePacket apply(FriendlyByteBuf packetBuffer) {
            return new RequestUpdatePacket(packetBuffer.readInt());
        }
    }

    public static class RequestUpdateHandler implements BiConsumer<RequestUpdatePacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(RequestUpdatePacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayer sender = contextSupplier.get().getSender();
                if (sender != null && sender.level().getEntity(updateClientPacket.e) instanceof LivingEntity) {
                    EntityHandler.mustUpdate.put(sender, sender.level().getEntity(updateClientPacket.e));
                    SenseData.getCap(((LivingEntity) (sender.level().getEntity(updateClientPacket.e)))).serverTick();
                } else EntityHandler.mustUpdate.put(sender, null);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
