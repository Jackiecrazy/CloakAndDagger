package jackiecrazy.cloakanddagger.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.config.GeneralConfig;
import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkAttributes;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class CombatUtils {
    public static HashMap<TagKey<Item>, StabInfo> archetypes = new HashMap<>();
    private static StabInfo DEFAULTMELEE = new StabInfo(1, 1);
    private static HashMap<Item, StabInfo> combatList = new HashMap<>();

    public static void updateItems(Map<ResourceLocation, JsonElement> object, ResourceManager rm, ProfilerFiller profiler) {
        DEFAULTMELEE = new StabInfo(GeneralConfig.distract, GeneralConfig.unaware);
        combatList = new HashMap<>();

        object.forEach((key, value) -> {
            JsonObject file = value.getAsJsonObject();
            file.entrySet().forEach(entry -> {
                final String name = entry.getKey();
                if (name.startsWith("#")) {//register tags separately
                    try {
                        JsonObject obj = entry.getValue().getAsJsonObject();
                        StabInfo put = new StabInfo(GeneralConfig.distract, GeneralConfig.unaware);
                        if (obj.has("distracted")) put.distractDamageBonus = obj.get("distracted").getAsDouble();
                        if (obj.has("unaware")) put.unawareDamageBonus = obj.get("unaware").getAsDouble();
                        archetypes.put(ItemTags.create(new ResourceLocation(CloakAndDagger.MODID, name.substring(1))), put);
                    } catch (Exception x) {
                        CloakAndDagger.LOGGER.error("malformed json under " + name + "!");
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
                    CloakAndDagger.LOGGER.error("malformed json under " + name + "!");
                    x.printStackTrace();
                }
            });
        });
    }

    @Nullable
    public static ItemStack getAttackingItemStack(DamageSource ds) {
        if (ds instanceof CombatDamageSource)
            return ((CombatDamageSource) ds).getDamageDealer();
        else if (ds.getEntity() instanceof LivingEntity) {
            LivingEntity e = (LivingEntity) ds.getEntity();
            return e.getMainHandItem();//CombatData.getCap(e).isOffhandAttack() ? e.getHeldItemOffhand() : e.getHeldItemMainhand();
        }
        return null;
    }

    public static boolean isMeleeAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            return ((CombatDamageSource) s).canProcAutoEffects();
        }
        return s.getEntity() == s.getDirectEntity() && !s.isExplosion() && !s.isFire() && !s.isMagic() && !s.isBypassArmor() && !s.isProjectile();
    }

    public static boolean isPhysicalAttack(DamageSource s) {
        if (s instanceof CombatDamageSource) {
            CombatDamageSource cds = (CombatDamageSource) s;
            return cds.getDamageTyping() == CombatDamageSource.TYPE.PHYSICAL;
        }
        return !s.isExplosion() && !s.isFire() && !s.isMagic() && !s.isBypassArmor();
    }

    private static StabInfo lookupStats(ItemStack is) {
        if (combatList.containsKey(is.getItem())) return combatList.get(is.getItem());
        for (TagKey<Item> tag : archetypes.keySet()) {
            if (is.is(tag))
                return archetypes.get(tag);
        }
        return DEFAULTMELEE;
    }

    public static double getDamageMultiplier(StealthOverride.Awareness a, ItemStack is) {
        if (is == null) return 1;
        StabInfo ci = lookupStats(is);
        return switch (a) {
            case DISTRACTED -> ci.distractDamageBonus;
            case UNAWARE -> ci.unawareDamageBonus;
            default -> 1;
        };
    }

    public static boolean isCrit(CriticalHitEvent e) {
        return e.getResult() == Event.Result.ALLOW || (e.getResult() == Event.Result.DEFAULT && e.isVanillaCritical());
    }

    public static boolean isWeapon(ItemStack itemStack) {
        return lookupStats(itemStack) != DEFAULTMELEE;
    }

    private static class StabInfo {
        private double distractDamageBonus;
        private double unawareDamageBonus;

        private StabInfo(double distract, double unaware) {
            distractDamageBonus = distract;
            unawareDamageBonus = unaware;
        }
    }
}
