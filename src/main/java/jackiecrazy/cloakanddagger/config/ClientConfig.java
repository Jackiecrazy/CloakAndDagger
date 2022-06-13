package jackiecrazy.cloakanddagger.config;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.footwork.config.DisplayConfigUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class ClientConfig {
    public static final ClientConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static boolean showEyes;
    public static ResourceLocation shout;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public final DisplayConfigUtils.DisplayData stealth;
    private final ForgeConfigSpec.BooleanValue _displayEyes;
    private final ForgeConfigSpec.ConfigValue<String> _shout;

    public ClientConfig(ForgeConfigSpec.Builder b) {
        stealth = new DisplayConfigUtils.DisplayData(b, "stealth", DisplayConfigUtils.AnchorPoint.CROSSHAIR, 0, 0);
        _displayEyes = b.translation("wardance.config.allStealth").comment("Renders the stealth eye above every mob that can be seen. If this is enabled with the mouseover stealth eye render, the mouseover eye will replace the overhead stealth eye when you are looking directly at an entity.").define("all stealth", true);
        _shout = b.translation("wardance.config.shout").comment("Change what sound you make when you shout. This is purely cosmetic and the sound will always be of volume 2.").define("shout sound", "minecraft:entity.pillager.ambient", (a) -> (a instanceof String && ResourceLocation.isValidResourceLocation((String) a)));
    }

    public static void bake() {
        CONFIG.stealth.bake();
        showEyes = CONFIG._displayEyes.get();
        shout = new ResourceLocation(CONFIG._shout.get());
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            bake();
        }
    }
}
