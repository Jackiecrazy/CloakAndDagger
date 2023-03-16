package jackiecrazy.cloakanddagger.utils;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.config.GeneralConfig;
import jackiecrazy.footwork.api.CombatDamageSource;
import jackiecrazy.footwork.api.FootworkAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CombatUtils {
    public static HashMap<Item, AttributeModifier> armorStats = new HashMap<>();
    private static StabInfo DEFAULTMELEE = new StabInfo(1, 1);
    private static HashMap<Item, StabInfo> combatList = new HashMap<>();

    public static void updateItems(List<? extends String> interpretC, List<? extends String> interpretA) {
        DEFAULTMELEE = new StabInfo(GeneralConfig.distract, GeneralConfig.unaware);
        combatList = new HashMap<>();
        for (String s : interpretC) {
            String[] val = s.split(",");
            String name = val[0];
            double distract = GeneralConfig.distract, unaware = GeneralConfig.unaware;
            try {
                distract = Double.parseDouble(val[1].trim());
                unaware = Double.parseDouble(val[2].trim());
            } catch (Exception e) {
                CloakAndDagger.LOGGER.warn("ambush data for weapon config entry " + s + " is not properly formatted, replacing with default values.");
            }
            ResourceLocation key = null;
            try {
                key = new ResourceLocation(name);
            } catch (Exception e) {
                CloakAndDagger.LOGGER.warn(name + " is not a proper item name, it will not be registered.");
            }
            //System.out.print("\""+key+"\",\n");
            if (ForgeRegistries.ITEMS.containsKey(key)) {
                final Item item = ForgeRegistries.ITEMS.getValue(key);
                combatList.put(item, new StabInfo(distract, unaware));
            }
            //System.out.print("\"" + name+ "\", ");
        }
        for (String s : interpretA) {
            String[] val = s.split(",");
            String name = val[0];
            double stealth = 0;
            try {
                stealth = Double.parseDouble(val[1]);
            } catch (Exception ignored) {
                CloakAndDagger.LOGGER.warn("armor data for config entry " + s + " is not properly formatted, ignoring.");
            }
            ResourceLocation key = null;
            try {
                key = new ResourceLocation(name);
            } catch (Exception e) {
                CloakAndDagger.LOGGER.warn(name + " is not a proper item name, it will not be registered.");
            }
            if (ForgeRegistries.ITEMS.containsKey(key) && (ForgeRegistries.ITEMS.getValue(key)) instanceof ArmorItem) {
                final Item armor = ForgeRegistries.ITEMS.getValue(key);
                UUID touse = FootworkAttributes.MODIFIERS[((ArmorItem) armor).getSlot().getIndex()];
                armorStats.put(armor, new AttributeModifier(touse, "war dance modifier", stealth, AttributeModifier.Operation.ADDITION));
            }
            //System.out.print("\"" + name+ "\", ");
        }
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

    public static double getDamageMultiplier(StealthOverride.Awareness a, ItemStack is) {
        if (is == null) return 1;
        StabInfo ci = combatList.getOrDefault(is.getItem(), DEFAULTMELEE);
        switch (a) {
            case DISTRACTED:
                return ci.distractDamageBonus;
            case UNAWARE:
                return ci.unawareDamageBonus;
            default:
                return 1;

        }
    }

    public static boolean isCrit(CriticalHitEvent e) {
        return e.getResult() == Event.Result.ALLOW || (e.getResult() == Event.Result.DEFAULT && e.isVanillaCritical());
    }

    public static boolean isWeapon(ItemStack itemStack) {
        return combatList.containsKey(itemStack.getItem());
    }

    private static class StabInfo {
        private final double distractDamageBonus, unawareDamageBonus;

        private StabInfo(double distract, double unaware) {
            distractDamageBonus = distract;
            unawareDamageBonus = unaware;
        }
    }
}
