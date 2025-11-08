package com.flarelabsmc.missinginaction.api;

import com.flarelabsmc.missinginaction.api.event.EntityAwarenessEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;

public class StealthUtils {
    public static StealthUtils INSTANCE = new StealthUtils();

    public Awareness getAwareness(LivingEntity attacker, LivingEntity target) {
        if (target != null && attacker != target) {
            if (target instanceof Player) {
                return Awareness.ALERT;
            } else {
                Awareness a = Awareness.ALERT;
                EntityAwarenessEvent eae = new EntityAwarenessEvent(target, attacker, a);
                MinecraftForge.EVENT_BUS.post(eae);
                return eae.getAwareness();
            }
        } else {
            return Awareness.ALERT;
        }
    }

    public static boolean inWeb(LivingEntity e) {
        if (!e.level().isAreaLoaded(e.blockPosition(), (int) Math.ceil(e.getBbWidth()))) return false;
        double minX = e.getX() - e.getBbWidth() / 2, minY = e.getY(), minZ = e.getZ() - e.getBbWidth() / 2;
        double maxX = e.getX() + e.getBbWidth() / 2, maxY = e.getY() + e.getBbHeight(), maxZ = e.getZ() + e.getBbWidth() / 2;
        for (double x = minX; x <= maxX; x++) {
            for (double y = minY; y <= maxY; y++) {
                for (double z = minZ; z <= maxZ; z++) {
                    if (e.level().getBlockState(e.blockPosition()).is(Blocks.COBWEB))
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
}