package jackiecrazy.cloakanddagger.networking;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.config.SoundConfig;
import jackiecrazy.cloakanddagger.handlers.EntityHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ShoutPacket {
    private final ResourceLocation voice;

    public ShoutPacket(ResourceLocation sound) {
        voice = sound;
    }

    public static class ShoutEncoder implements BiConsumer<ShoutPacket, FriendlyByteBuf> {

        @Override
        public void accept(ShoutPacket updateClientPacket, FriendlyByteBuf packetBuffer) {
            packetBuffer.writeResourceLocation(updateClientPacket.voice);
        }
    }

    public static class ShoutDecoder implements Function<FriendlyByteBuf, ShoutPacket> {

        @Override
        public ShoutPacket apply(FriendlyByteBuf packetBuffer) {
            return new ShoutPacket(packetBuffer.readResourceLocation());
        }
    }

    public static class ShoutHandler implements BiConsumer<ShoutPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(ShoutPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                Player uke = contextSupplier.get().getSender();
                SoundEvent se = ForgeRegistries.SOUND_EVENTS.getValue(updateClientPacket.voice);
                if (uke == null) return;
                if (se == null) se = SoundEvents.PILLAGER_AMBIENT;
                uke.level().playSound(null, uke.getX(), uke.getY(), uke.getZ(), se, SoundSource.PLAYERS, 0.75f + CloakAndDagger.rand.nextFloat() * 0.5f, 0.75f + CloakAndDagger.rand.nextFloat() * 0.5f);
                EntityHandler.alertTracker.put(new Tuple<>(uke.level(), BlockPos.containing(uke.getX(), uke.getY(), uke.getZ())), (float) SoundConfig.shout);
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
