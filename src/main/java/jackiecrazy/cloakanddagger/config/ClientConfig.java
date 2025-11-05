package jackiecrazy.cloakanddagger.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

    private final ForgeConfigSpec.ConfigValue<String> _shout;

    public ClientConfig(ForgeConfigSpec.Builder b) {
        _shout = b.translation("cloakanddagger.config.shout").comment("Change what sound you make when you shout. This is purely cosmetic and the sound will always be of volume 2.").define("shout sound", "minecraft:entity.pillager.ambient", (a) -> (a instanceof String && ResourceLocation.isValidResourceLocation((String) a)));
    }

    public static void bake() {
        shout = new ResourceLocation(CONFIG._shout.get());
    }

    @SubscribeEvent
    public static void loadConfig(ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            bake();
        }
    }
}
