package jackiecrazy.wardance.networking;

import jackiecrazy.wardance.capability.CombatData;
import jackiecrazy.wardance.handlers.EntityHandler;
import jackiecrazy.wardance.utils.CombatUtils;
import jackiecrazy.wardance.utils.GeneralUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequestSweepPacket {
    boolean main;

    public RequestSweepPacket(boolean isMainHand) {
        main = isMainHand;
    }

    public static class RequestSweepEncoder implements BiConsumer<RequestSweepPacket, PacketBuffer> {

        @Override
        public void accept(RequestSweepPacket updateClientPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeBoolean(updateClientPacket.main);
        }
    }

    public static class RequestSweepDecoder implements Function<PacketBuffer, RequestSweepPacket> {

        @Override
        public RequestSweepPacket apply(PacketBuffer packetBuffer) {
            return new RequestSweepPacket(packetBuffer.readBoolean());
        }
    }

    public static class RequestSweepHandler implements BiConsumer<RequestSweepPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(RequestSweepPacket updateClientPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> {
                ServerPlayerEntity sender = contextSupplier.get().getSender();
                Hand h = updateClientPacket.main ? Hand.MAIN_HAND : Hand.OFF_HAND;
                if (sender != null && CombatUtils.getCooledAttackStrength(sender, h, 1f) >= 0.9f) {
                    double d0 = sender.distanceWalkedModified - sender.prevDistanceWalkedModified;
                    if (!(sender.fallDistance > 0.0F && !sender.isOnLadder() && !sender.isInWater() && !sender.isPotionActive(Effects.BLINDNESS) && !sender.isPassenger()) && !sender.isSprinting() && sender.isOnGround() && d0 < (double) sender.getAIMoveSpeed())
                        CombatUtils.sweep(sender, null, h, GeneralUtils.getAttributeValueSafe(sender, ForgeMod.REACH_DISTANCE.get()));
                }
            });
            contextSupplier.get().setPacketHandled(true);
        }
    }
}