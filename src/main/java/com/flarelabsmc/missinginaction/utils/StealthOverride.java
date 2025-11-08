package com.flarelabsmc.missinginaction.utils;

import com.flarelabsmc.missinginaction.MissingInAction;
import com.flarelabsmc.missinginaction.api.Awareness;
import com.flarelabsmc.missinginaction.api.StealthUtils;
import com.flarelabsmc.missinginaction.api.event.EntityAwarenessEvent;
import com.flarelabsmc.missinginaction.capability.vision.SenseData;
import com.flarelabsmc.missinginaction.config.StealthTags;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;

public class StealthOverride extends StealthUtils {
    public static HashMap<SoundEvent, Integer> soundMap = new HashMap<>();

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
                MissingInAction.LOGGER.warn("Improperly formatted sound definition " + s + "!");
            }
        }
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
            return Awareness.ALERT;
        if (target instanceof Player)
            return Awareness.ALERT;
        Awareness a = Awareness.ALERT;
        if (!target.getType().is(StealthTags.NOT_UNAWARE) && target.getLastHurtByMob() == null && (!(target instanceof Mob) || ((Mob) target).getTarget() == null)) {
            double health = target.getHealth();
            double maxHealth = target.getMaxHealth();
            double detect = SenseData.getCap(target).getDetectionPerc(attacker);
            a = target.level().isClientSide || health >= maxHealth * detect ? Awareness.UNAWARE : Awareness.DISTRACTED;
        }
        else if (target.getAirSupply() <= 0)
            a = Awareness.DISTRACTED;
        else if (attacker != null && attacker.isInvisible() && !target.getType().is(StealthTags.IGNORE_INVIS))
            a = Awareness.DISTRACTED;
        else if (inWeb(target) && !target.getType().is(StealthTags.IGNORE_COBWEB))
            a = Awareness.DISTRACTED;
        else if (!target.getType().is(StealthTags.NOT_DISTRACTED) && target.getLastHurtByMob() != attacker && (!(target instanceof Mob) || ((Mob) target).getTarget() != attacker))
            a = Awareness.DISTRACTED;
        EntityAwarenessEvent eae = new EntityAwarenessEvent(target, attacker, a);
        MinecraftForge.EVENT_BUS.post(eae);
        return eae.getAwareness();
    }

}
