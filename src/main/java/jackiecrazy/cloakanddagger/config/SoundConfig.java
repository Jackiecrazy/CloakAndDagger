package jackiecrazy.cloakanddagger.config;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = CloakAndDagger.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SoundConfig {
    public static final SoundConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    private static final String[] SOUND = {
            "*armor.equip, 4",
            "*arrow, 4",
            "*scream, 16",
            "*door.open, 4",
            "*door.close, 4",
            "*.break, 4",
            "*.place, 4",
            "*music_disc, 8",
            "*note_block, 8",
            "*angry, 8",
            "*click_off, 4",
            "*click_on, 4",
            "entity.bee.loop_aggressive, 4",
            "item.crossbow.shoot, 4",
            "entity.generic.eat, 4",
            "entity.generic.drink, 4",
            "entity.minecart.riding, 4",
            "entity.generic.explode, 16",
            "entity.player.big_fall, 8",
            "entity.player.burp, 6",
            "entity.ravager.roar, 16"
    };
    public static double shout;

    static {
        final Pair<SoundConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(SoundConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _sounds;
    private final ForgeConfigSpec.IntValue _shoutSize;

    public SoundConfig(ForgeConfigSpec.Builder b) {
        //feature toggle, resource, defense, compat, stealth, lists
        _sounds = b.translation("cloakanddagger.config.sound").comment("Define which sounds generate cues for mobs to detect, followed by their size. Use *snippet to select all sounds that include the snippet in their full name. The list is processed top-down, so putting *tags first will allow you to override specific ones later. Shouting disregards this and always generates a sound cue of the defined radius, regardless of which sound clients have it set as.").defineList("sound cue list", Arrays.asList(SOUND), String.class::isInstance);
        _shoutSize=b.translation("cloakanddagger.config.shoutSize").comment("Define the loudness of a shout. All shouts, regardless of which client-defined sound is used, will generate a sound cue of this radius.").defineInRange("shout cue radius", 16, 0, 100);
    }

    private static void bake() {
        shout = CONFIG._shoutSize.get();
        StealthOverride.updateSound(CONFIG._sounds.get());
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            bake();
        }
    }
}
