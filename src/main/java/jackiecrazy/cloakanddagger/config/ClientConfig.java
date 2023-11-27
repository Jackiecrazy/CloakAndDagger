package jackiecrazy.cloakanddagger.config;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.footwork.config.DisplayConfigUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;

public class ClientConfig {
    public static final ClientConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static boolean showEyes;
    public static ResourceLocation shout;
    public static Color dis, una;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public final DisplayConfigUtils.DisplayData stealth;
    private final ForgeConfigSpec.BooleanValue _displayEyes;
    private final ForgeConfigSpec.ConfigValue<String> _shout;
    private final ForgeConfigSpec.ConfigValue<String> _unawareColor;
    private final ForgeConfigSpec.ConfigValue<String> _distractedColor;

    public ClientConfig(ForgeConfigSpec.Builder b) {
        stealth = new DisplayConfigUtils.DisplayData(b, "stealth", DisplayConfigUtils.AnchorPoint.CROSSHAIR, 0, 0);
        _displayEyes = b.translation("cloakanddagger.config.allStealth").comment("Renders the stealth eye above every mob that can be seen. If this is enabled with the mouseover stealth eye render, the mouseover eye will replace the overhead stealth eye when you are looking directly at an entity.").define("all stealth", true);
        _shout = b.translation("cloakanddagger.config.shout").comment("Change what sound you make when you shout. This is purely cosmetic and the sound will always be of volume 2.").define("shout sound", "minecraft:entity.pillager.ambient", (a) -> (a instanceof String && ResourceLocation.isValidResourceLocation((String) a)));
        _distractedColor = b.translation("cloakanddagger.config.distract").comment("Change the color of the distracted eye of a mob on low health.").define("distracted eye color", "#FFAE42", (a) -> (a instanceof String));
        _unawareColor = b.translation("cloakanddagger.config.unaware").comment("Change the color of the unaware eye filling up.").define("unaware eye color", "#FF0000", (a) -> (a instanceof String));
    }

    public static void bake() {
        CONFIG.stealth.bake();
        showEyes = CONFIG._displayEyes.get();
        shout = new ResourceLocation(CONFIG._shout.get());
        try {
            dis = Color.decode(CONFIG._distractedColor.get());
        }catch (NumberFormatException nfe){
            dis=Color.ORANGE;
        }
        try {
            una = Color.decode(CONFIG._unawareColor.get());
        }catch (NumberFormatException nfe){
            una=Color.RED;
        }
    }

    @SubscribeEvent
    public static void loadConfig(ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            bake();
        }
    }
}
