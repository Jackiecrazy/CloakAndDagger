package jackiecrazy.cloakanddagger.config;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;

public class StealthTags {
    public static final HashMap<TagKey<EntityType<?>>, Tuple<MutableComponent, MutableComponent>> MAP=new HashMap<>();
    public static final TagKey<EntityType<?>> NO_STEALTH = makeKey("bumbling_fool");
    public static final TagKey<EntityType<?>> IGNORE_FOV = makeKey("ignore_fov");
    public static final TagKey<EntityType<?>> IGNORE_COBWEB = makeKey("ignore_cobweb");
    public static final TagKey<EntityType<?>> IGNORE_SOUND = makeKey("ignore_sound", ChatFormatting.GREEN);
    public static final TagKey<EntityType<?>> IGNORE_BLINDNESS = makeKey("ignore_blindness");
    public static final TagKey<EntityType<?>> IGNORE_LOS = makeKey("ignore_los");
    public static final TagKey<EntityType<?>> SKIP_SEARCH = makeKey("skip_search");
    public static final TagKey<EntityType<?>> NEVER_LOOK = makeKey("never_look", ChatFormatting.GREEN);
    public static final TagKey<EntityType<?>> NOT_DISTRACTED = makeKey("alert_when_attacking_others");
    public static final TagKey<EntityType<?>> IGNORE_LIGHT = makeKey("ignore_light", ChatFormatting.RED);
    public static final TagKey<EntityType<?>> IGNORE_INVIS = makeKey("ignore_invis");
    public static final TagKey<EntityType<?>> IGNORE_MOTION = makeKey("ignore_motion");
    public static final TagKey<EntityType<?>> NO_SOUND = makeKey("does_not_make_sound", ChatFormatting.GREEN);
    public static final TagKey<EntityType<?>> NOT_UNAWARE = makeKey("alert_when_no_target");
    public static final TagKey<EntityType<?>> IGNORE_LUCK = makeKey("ignore_luck");

    @NotNull
    private static TagKey<EntityType<?>> makeKey(String path, ChatFormatting... formattings) {
        TagKey<EntityType<?>> ret= TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(CloakAndDagger.MODID, path));
        MAP.put(ret, new Tuple<>(Component.translatable("cloak.tag." + path).withStyle(formattings),Component.translatable("cloak.desc." + path).withStyle(formattings)));
        return ret;
    }

}
