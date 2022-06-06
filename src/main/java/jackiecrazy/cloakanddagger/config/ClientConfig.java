package jackiecrazy.cloakanddagger.config;

import jackiecrazy.cloakanddagger.CloakAndDagger;
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

    public final DisplayData stealth;
    private final ForgeConfigSpec.BooleanValue _displayEyes;
    private final ForgeConfigSpec.ConfigValue<String> _shout;

    public ClientConfig(ForgeConfigSpec.Builder b) {
        stealth = new DisplayData(b, "stealth", AnchorPoint.CROSSHAIR, 0, 0);
        _displayEyes = b.translation("wardance.config.allStealth").comment("Renders the stealth eye above every mob that can be seen. If this is enabled with the mouseover stealth eye render, the mouseover eye will replace the overhead stealth eye when you are looking directly at an entity.").define("all stealth", true);b.pop();
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
            if (GeneralConfig.debug)
                CloakAndDagger.LOGGER.debug("loading client config!");
            bake();
        }
    }

    public enum AnchorPoint {
        TOPLEFT,
        TOPCENTER,
        TOPRIGHT,
        MIDDLELEFT,
        CROSSHAIR,
        MIDDLERIGHT,
        BOTTOMLEFT,
        BOTTOMCENTER,
        BOTTOMRIGHT
    }

    public static enum BarType {
        CLASSIC,
        AMO,
        DARKMEGA
    }

    public static class DisplayData {
        private final ForgeConfigSpec.EnumValue<AnchorPoint> _anchor;
        private final ForgeConfigSpec.IntValue _numberX;
        private final ForgeConfigSpec.IntValue _numberY;
        private final ForgeConfigSpec.BooleanValue _display;
        public AnchorPoint anchorPoint;
        public int numberX;
        public int numberY;
        public boolean enabled;

        private DisplayData(ForgeConfigSpec.Builder b, String s, AnchorPoint ap, int defX, int defY) {
            _display = b.translation("wardance.config." + s + "enabled").comment("enable displaying this feature").define("enable " + s, true);
            _anchor = b.translation("wardance.config." + s + "anchor").comment("the point from which offsets will calculate").defineEnum(s + " anchor point", ap);
            _numberX = b.translation("wardance.config." + s + "X").comment("where the center of the HUD element should be in relation to the anchor point").defineInRange(s + " x offset", defX, -Integer.MAX_VALUE, Integer.MAX_VALUE);
            _numberY = b.translation("wardance.config." + s + "Y").comment("where the center of the HUD element should be in relation to the anchor point").defineInRange(s + " y offset", defY, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        protected void bake() {
            anchorPoint = _anchor.get();
            numberX = _numberX.get();
            numberY = _numberY.get();
            enabled = _display.get();
        }
    }
}
