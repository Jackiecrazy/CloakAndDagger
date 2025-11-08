package com.flarelabsmc.missinginaction.config;

import com.flarelabsmc.missinginaction.MissingInAction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class StealthTags {
    public static final TagKey<EntityType<?>> NO_STEALTH = makeKey("no_stealth");
    public static final TagKey<EntityType<?>> IGNORE_FOV = makeKey("ignore_fov");
    public static final TagKey<EntityType<?>> IGNORE_COBWEB = makeKey("ignore_cobweb");
    public static final TagKey<EntityType<?>> IGNORE_SOUND = makeKey("ignore_sound");
    public static final TagKey<EntityType<?>> IGNORE_BLINDNESS = makeKey("ignore_blindness");
    public static final TagKey<EntityType<?>> IGNORE_LOS = makeKey("ignore_los");
    public static final TagKey<EntityType<?>> SKIP_SEARCH = makeKey("skip_search");
    public static final TagKey<EntityType<?>> NOT_DISTRACTED = makeKey("alert_when_attacking_others");
    public static final TagKey<EntityType<?>> IGNORE_LIGHT = makeKey("ignore_light");
    public static final TagKey<EntityType<?>> IGNORE_INVIS = makeKey("ignore_invis");
    public static final TagKey<EntityType<?>> IGNORE_MOTION = makeKey("ignore_motion");
    public static final TagKey<EntityType<?>> NO_SOUND = makeKey("does_not_make_sound");
    public static final TagKey<EntityType<?>> NOT_UNAWARE = makeKey("alert_when_no_target");

    @NotNull
    private static TagKey<EntityType<?>> makeKey(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(MissingInAction.MODID, path));
    }
}
