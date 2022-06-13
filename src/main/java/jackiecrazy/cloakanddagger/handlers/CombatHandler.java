package jackiecrazy.cloakanddagger.handlers;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.utils.CombatUtils;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.utils.StealthUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CloakAndDagger.MODID)
public class CombatHandler {
    @SubscribeEvent
    public static void projectileParry(final ProjectileImpactEvent e) {
        //deflection stealth checks are handled by PWD for sanity reasons.
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)//because compat with BHT...
    public static void parry(final LivingAttackEvent e) {
        //parrying stealth checks are handled by PWD for sanity reasons.
    }

    @SubscribeEvent
    public static void critHooks(CriticalHitEvent e) {
        if (!e.getEntityLiving().level.isClientSide && e.getTarget() instanceof LivingEntity) {
            LivingEntity uke = (LivingEntity) e.getTarget();
            LivingEntity seme = e.getPlayer();
            //stealth attacks automatically crit
            StealthUtils.Awareness awareness = StealthUtils.INSTANCE.getAwareness(seme, uke);
            if (awareness == StealthUtils.Awareness.UNAWARE)
                e.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pain(LivingHurtEvent e) {
        LivingEntity uke = e.getEntityLiving();
        //FIXME it is physically impossible for effects to factor in stealth if they are removed first
        LivingEntity kek = null;
        DamageSource ds = e.getSource();
        if (ds.getDirectEntity() instanceof LivingEntity) {
            kek = (LivingEntity) ds.getDirectEntity();
        }
        StealthOverride.Awareness awareness = StealthUtils.INSTANCE.getAwareness(kek, uke);
        if (ds.getEntity() instanceof LivingEntity) {
            LivingEntity seme = ((LivingEntity) ds.getEntity());
            if (CombatUtils.isPhysicalAttack(e.getSource())) {
                if (awareness != StealthOverride.Awareness.ALERT) {
                    e.setAmount((float) (e.getAmount() * CombatUtils.getDamageMultiplier(awareness, CombatUtils.getAttackingItemStack(ds))));
                }
            }
        }
    }
}
