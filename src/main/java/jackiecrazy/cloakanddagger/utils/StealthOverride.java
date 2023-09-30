package jackiecrazy.cloakanddagger.utils;

import io.netty.util.CharsetUtil;
import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.action.PermissionData;
import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import jackiecrazy.cloakanddagger.networking.StealthChannel;
import jackiecrazy.cloakanddagger.networking.SyncMobDataPacket;
import jackiecrazy.footwork.event.EntityAwarenessEvent;
import jackiecrazy.footwork.potion.FootworkEffects;
import jackiecrazy.footwork.utils.StealthUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StealthOverride extends StealthUtils {
    public static final StealthData STEALTH = new StealthData("");
    public static HashMap<ResourceLocation, StealthData> stealthMap = new HashMap<>();
    public static HashMap<SoundEvent, Integer> soundMap = new HashMap<>();

    public static void sendMobData(ServerPlayer p) {
        StealthChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), new SyncMobDataPacket(stealthMap));
    }

    public static void clientMobOverride(Map<ResourceLocation, StealthData> server) {
        stealthMap = new HashMap<>(server);
    }

    public static StealthData getStealth(LivingEntity e) {
        return stealthMap.getOrDefault(EntityType.getKey(e.getType()), STEALTH);
    }

    public static void updateMobDetection(List<? extends String> interpretS) {
        stealthMap.clear();
        for (String s : interpretS) {
            try {
                String[] val = s.split(",");
                final ResourceLocation key = new ResourceLocation(val[0]);
                String value = val[1];
                stealthMap.put(key, new StealthData(value.toLowerCase(Locale.ROOT)));
//                String print = val[0]+", ";
//                StealthData sd = stealthMap.get(key);
//                print = print.concat(sd.deaf ? "d" : "");
//                print = print.concat(sd.nightvision ? "n" : "");
//                print = print.concat(sd.illuminati ? "a" : "");
//                print = print.concat(sd.atheist ? "o" : "");
//                print = print.concat(sd.vigil ? "v" : "");
//                System.out.println("\"" + print + "\",");
            } catch (Exception e) {
                CloakAndDagger.LOGGER.warn("improperly formatted mob stealth definition " + s + "!");
            }
        }
    }

    public static void updateSound(List<? extends String> interpretS) {
        soundMap.clear();
        for (String s : interpretS) {
            try {
                String[] val = s.split(",");
                if (s.startsWith("*")) {
                    String contain = val[0].substring(1);
                    for (SoundEvent se : ForgeRegistries.SOUND_EVENTS) {
                        if (se.getLocation().toString().contains(contain))
                            soundMap.put(se, Integer.parseInt(val[1].trim()));
                    }
                } else {
                    final ResourceLocation key = new ResourceLocation(val[0]);
                    if (ForgeRegistries.SOUND_EVENTS.getValue(key) != null) {
                        Integer value = Integer.parseInt(val[1].trim());
                        soundMap.put(ForgeRegistries.SOUND_EVENTS.getValue(key), value);
                    }
                }
            } catch (Exception e) {
                CloakAndDagger.LOGGER.warn("improperly formatted sound definition " + s + "!");
            }
        }
    }

    public static boolean inWeb(LivingEntity e) {
        if (!e.level.isAreaLoaded(e.blockPosition(), (int) Math.ceil(e.getBbWidth()))) return false;
        double minX = e.getX() - e.getBbWidth() / 2, minY = e.getY(), minZ = e.getZ() - e.getBbWidth() / 2;
        double maxX = e.getX() + e.getBbWidth() / 2, maxY = e.getY() + e.getBbHeight(), maxZ = e.getZ() + e.getBbWidth() / 2;
        for (double x = minX; x <= maxX; x++) {
            for (double y = minY; y <= maxY; y++) {
                for (double z = minZ; z <= maxZ; z++) {
                    if (e.level.getBlockState(e.blockPosition()).getMaterial().equals(Material.WEB))
                        return true;
                }
            }
        }
        return false;
    }

    public static int getActualLightLevel(Level world, BlockPos pos) {
        int i = 0;
        if (world.dimensionType().hasSkyLight()) {
            world.updateSkyBrightness();
            int dark = world.getSkyDarken();
            i = world.getBrightness(LightLayer.SKY, pos) - dark;
        }

        i = Mth.clamp(Math.max(world.getBrightness(LightLayer.BLOCK, pos), i), 0, 15);
        return i;
    }

    public Awareness getAwareness(LivingEntity attacker, LivingEntity target) {
        if (target == null || attacker == target)
            return Awareness.ALERT;//the cases that don't make sense.
        //players are alert because being jumped with 2.5x daggers feel bad
        if (target instanceof Player)
            return Awareness.ALERT;
        //no permission to stab, permanently treated as alert
        if (attacker instanceof Player p && !PermissionData.getCap(p).canStab())
            return Awareness.ALERT;
        StealthData sd = stealthMap.getOrDefault(EntityType.getKey(target.getType()), STEALTH);
        Awareness a = Awareness.ALERT;
        //sleep, paralysis, and petrify take highest priority
        if (target.hasEffect(FootworkEffects.SLEEP.get()) || target.hasEffect(FootworkEffects.PARALYSIS.get()) || target.hasEffect(FootworkEffects.PETRIFY.get()))
            a = Awareness.UNAWARE;
            //idle and not vigilant
        else if (!sd.vigil && target.getLastHurtByMob() == null && (!(target instanceof Mob) || ((Mob) target).getTarget() == null))
            a = target.level.isClientSide || target.getHealth() > target.getMaxHealth() * (1 - SenseData.getCap(target).getDetectionPerc(attacker)) ? Awareness.UNAWARE : Awareness.DISTRACTED;
            //distraction, confusion, and choking take top priority in inferior tier
        else if (target.hasEffect(FootworkEffects.DISTRACTION.get()) || target.hasEffect(FootworkEffects.CONFUSION.get()) || target.getAirSupply() <= 0)
            a = Awareness.DISTRACTED;
            //looking around for you, but cannot see
        else if (attacker != null && attacker.isInvisible() && !sd.observant)
            a = Awareness.DISTRACTED;
            //webbed and not a spider
        else if (inWeb(target) && !sd.cheliceric)
            a = Awareness.DISTRACTED;
            //hurt by something else
        else if (!sd.mindful && target.getLastHurtByMob() != attacker && (!(target instanceof Mob) || ((Mob) target).getTarget() != attacker))
            a = Awareness.DISTRACTED;
        //event for more compat
        EntityAwarenessEvent eae = new EntityAwarenessEvent(target, attacker, a);
        MinecraftForge.EVENT_BUS.post(eae);
        return eae.getAwareness();
    }

    public static class StealthData {

        private final String string;
        public final boolean allSeeing, blind, cheliceric, deaf, eyeless, heatSeeking, instant, lazy, mindful, nightvision, observant, perceptive, skeptical, quiet, vigil, wary;

        public StealthData(String value) {
            allSeeing = value.contains("a");
            blind = value.contains("b");
            cheliceric = value.contains("c");
            deaf = value.contains("d");
            eyeless = value.contains("e");
            heatSeeking = value.contains("h");
            instant = value.contains("i");
            lazy = value.contains("l");
            mindful = value.contains("m");
            nightvision = value.contains("n");
            observant = value.contains("o");
            perceptive = value.contains("p");
            skeptical = value.contains("s");
            quiet = value.contains("s");
            vigil = value.contains("v");
            wary = value.contains("w");
            string = value;
        }

        public static StealthData read(FriendlyByteBuf f) {
            int length = f.readInt();
            return new StealthData(String.valueOf(f.readCharSequence(length, CharsetUtil.US_ASCII)));
        }

        public void write(FriendlyByteBuf f) {
            f.writeInt(string.length());
            f.writeCharSequence(string, CharsetUtil.US_ASCII);
        }


    }

}
