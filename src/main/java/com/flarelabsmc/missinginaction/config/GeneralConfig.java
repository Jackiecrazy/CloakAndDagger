package com.flarelabsmc.missinginaction.config;

import com.flarelabsmc.missinginaction.MissingInAction;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = MissingInAction.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneralConfig {
    public static final GeneralConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static float distract, unaware, luck;
    public static int inv;
    public static int baseHorizontalDetection, baseVerticalDetection;

    static {
        final Pair<GeneralConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(GeneralConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.IntValue _invisibilityExposureTime;
    private final ForgeConfigSpec.DoubleValue _distract;
    private final ForgeConfigSpec.DoubleValue _unaware;
    private final ForgeConfigSpec.DoubleValue _luck;
    private final ForgeConfigSpec.IntValue _baseDetectionHorizontal;
    private final ForgeConfigSpec.IntValue _baseDetectionVertical;

    public GeneralConfig(ForgeConfigSpec.Builder b) {
        _invisibilityExposureTime = b.translation("mia.config.removeInvis").comment("how many ticks invisibility should be nullified for after an attack. The invisibility potion effect is not removed, so long-lasting sources are still useful. Set to 0 to disable.").defineInRange("damage expose time", 80, 0, Integer.MAX_VALUE);
        _luck = b.translation("mia.config.luck").comment("on every detection check your stealth is increased by a random number rolled between 0 and the difference of your and your enemy's luck. This multiplier applies to the resultant effective stealth increase (or decrease!). Set to 0 to disable.").defineInRange("luck stealth", 0.5, 0, 100);
        _baseDetectionHorizontal = b.translation("mia.config.detectH").comment("angle of detection on the xz plane").defineInRange("default mob horizontal FoV", 120, 0, 360);
        _baseDetectionVertical = b.translation("mia.config.detectV").comment("angle of detection on the y axis").defineInRange("default mob vertical FoV", 60, 0, 360);
        _distract = b.translation("mia.config.distract").comment("posture and health damage multiplier for distracted stabs").defineInRange("distracted stab multiplier", 1.5, 0, Double.MAX_VALUE);
        _unaware = b.translation("mia.config.unaware").comment("posture and health damage multiplier for unaware stabs").defineInRange("unaware stab multiplier", 1.5, 0, Double.MAX_VALUE);
    }

    private static void bake() {
        distract = CONFIG._distract.get().floatValue();
        unaware = CONFIG._unaware.get().floatValue();
        baseHorizontalDetection = CONFIG._baseDetectionHorizontal.get();
        baseVerticalDetection = CONFIG._baseDetectionVertical.get();
        inv = CONFIG._invisibilityExposureTime.get();
        luck = CONFIG._luck.get().floatValue();
    }

    @SubscribeEvent
    public static void loadConfig(ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            bake();
        }
    }
}
