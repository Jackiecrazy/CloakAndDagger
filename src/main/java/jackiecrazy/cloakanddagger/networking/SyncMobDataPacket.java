package jackiecrazy.cloakanddagger.networking;

import jackiecrazy.cloakanddagger.utils.CombatUtils;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncMobDataPacket {
    private static final FriendlyByteBuf.Writer<StealthOverride.StealthData> info = (f, info) -> info.write(f);

    private static final FriendlyByteBuf.Reader<StealthOverride.StealthData> rinfo = StealthOverride.StealthData::read;
    private final Map<ResourceLocation, StealthOverride.StealthData> map;

    public SyncMobDataPacket(Map<ResourceLocation, StealthOverride.StealthData> map) {
        this.map = map;
    }

    public static class Encoder implements BiConsumer<SyncMobDataPacket, FriendlyByteBuf> {

        @Override
        public void accept(SyncMobDataPacket packet, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeMap(packet.map, FriendlyByteBuf::writeResourceLocation, info);
        }
    }

    public static class Decoder implements Function<FriendlyByteBuf, SyncMobDataPacket> {

        @Override
        public SyncMobDataPacket apply(FriendlyByteBuf packetBuffer) {
            return new SyncMobDataPacket(packetBuffer.readMap(FriendlyByteBuf::readResourceLocation, rinfo));
        }
    }

    public static class Handler implements BiConsumer<SyncMobDataPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SyncMobDataPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {

            //prevent client overriding server
            if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
                contextSupplier.get().enqueueWork(() -> {
                    StealthOverride.clientMobOverride(updateClientPacket.map);
                });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
