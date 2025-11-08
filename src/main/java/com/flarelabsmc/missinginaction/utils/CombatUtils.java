package com.flarelabsmc.missinginaction.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.flarelabsmc.missinginaction.MissingInAction;
import com.flarelabsmc.missinginaction.api.Awareness;
import com.flarelabsmc.missinginaction.config.GeneralConfig;
import com.flarelabsmc.missinginaction.networking.StealthChannel;
import com.flarelabsmc.missinginaction.networking.SyncItemDataPacket;
import com.flarelabsmc.missinginaction.networking.SyncTagDataPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CombatUtils {
    public static HashMap<TagKey<Item>, StabInfo> archetypes = new HashMap<>();
    private static StabInfo DEFAULTMELEE = new StabInfo(1, 1);
    private static HashMap<Item, StabInfo> combatList = new HashMap<>();

    public static void sendItemData(ServerPlayer p) {
        StealthChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), new SyncItemDataPacket(combatList));
        StealthChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> p), new SyncTagDataPacket(archetypes));
    }

    public static void clientWeaponOverride(Map<Item, StabInfo> server) {
        combatList = new HashMap<>(server);
    }

    public static void clientTagOverride(Map<TagKey<Item>, StabInfo> server) {
        archetypes = new HashMap<>(server);
    }

    public static void updateItems(Map<ResourceLocation, JsonElement> object, ResourceManager rm, ProfilerFiller profiler) {
        DEFAULTMELEE = new StabInfo(GeneralConfig.distract, GeneralConfig.unaware);
        combatList = new HashMap<>();

        object.forEach((key, value) -> {
            JsonObject file = value.getAsJsonObject();
            file.entrySet().forEach(entry -> {
                final String name = entry.getKey();
                if (name.startsWith("#")) {
                    try {
                        JsonObject obj = entry.getValue().getAsJsonObject();
                        StabInfo put = new StabInfo(GeneralConfig.distract, GeneralConfig.unaware);
                        if (obj.has("distracted")) put.distractDamageBonus = obj.get("distracted").getAsDouble();
                        if (obj.has("unaware")) put.unawareDamageBonus = obj.get("unaware").getAsDouble();
                        ResourceLocation rl = null;
                        if (name.contains(":"))
                            rl = new ResourceLocation(name.substring(1));
                        else
                            rl = new ResourceLocation(MissingInAction.MODID, name.substring(1));
                        archetypes.put(ItemTags.create(rl), put);
                    } catch (Exception x) {
                        MissingInAction.LOGGER.error("malformed json under " + name + "!");
                        x.printStackTrace();
                    }
                    return;
                }
                ResourceLocation i = new ResourceLocation(name);
                Item item = ForgeRegistries.ITEMS.getValue(i);
                if (item == null || item == Items.AIR) {
                    return;
                }
                try {
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    StabInfo put = new StabInfo(GeneralConfig.distract, GeneralConfig.unaware);
                    if (obj.has("distracted")) put.distractDamageBonus = obj.get("distracted").getAsDouble();
                    if (obj.has("unaware")) put.unawareDamageBonus = obj.get("unaware").getAsDouble();
                    combatList.put(item, put);
                } catch (Exception x) {
                    MissingInAction.LOGGER.error("malformed json under " + name + "!");
                    x.printStackTrace();
                }
            });
        });
    }

    @Nullable
    public static ItemStack getAttackingItemStack(DamageSource ds) {
        if (ds.getDirectEntity() instanceof LivingEntity e) {
            return e.getMainHandItem();
        }
        return null;
    }

    public static boolean isPhysicalAttack(DamageSource s) {
        return !s.is(DamageTypeTags.IS_EXPLOSION) && !s.is(DamageTypeTags.IS_FIRE) && !s.is(DamageTypeTags.WITCH_RESISTANT_TO) && !s.is(DamageTypeTags.BYPASSES_ARMOR);
    }

    private static StabInfo lookupStats(ItemStack is) {
        if (combatList.containsKey(is.getItem())) return combatList.get(is.getItem());
        for (TagKey<Item> tag : archetypes.keySet()) {
            if (is.is(tag))
                return archetypes.get(tag);
        }
        return DEFAULTMELEE;
    }

    public static double getDamageMultiplier(Awareness a, ItemStack is) {
        if (is == null) return 1;
        StabInfo ci = lookupStats(is);
        return switch (a) {
            case DISTRACTED -> ci.distractDamageBonus;
            case UNAWARE -> ci.unawareDamageBonus;
            default -> 1;
        };
    }

    public static boolean isWeapon(ItemStack itemStack) {
        return lookupStats(itemStack) != DEFAULTMELEE;
    }

    public static class StabInfo {
        private double distractDamageBonus;
        private double unawareDamageBonus;

        private StabInfo(double distract, double unaware) {
            distractDamageBonus = distract;
            unawareDamageBonus = unaware;
        }

        public static StabInfo read(FriendlyByteBuf f) {
            StabInfo ret = new StabInfo(0, 0);
            ret.distractDamageBonus = f.readDouble();
            ret.unawareDamageBonus = f.readDouble();
            return ret;
        }

        public void write(FriendlyByteBuf f) {
            f.writeDouble(distractDamageBonus);
            f.writeDouble(unawareDamageBonus);
        }
    }
}
