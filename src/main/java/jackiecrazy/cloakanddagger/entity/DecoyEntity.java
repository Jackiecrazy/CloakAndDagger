package jackiecrazy.cloakanddagger.entity;

import jackiecrazy.footwork.entity.DummyEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class DecoyEntity extends DummyEntity {
    public DecoyEntity(EntityType<? extends DummyEntity> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(1);
    }

    public void die(DamageSource p_21014_) {
        if(p_21014_.getEntity() instanceof Mob mob){
            mob.setTarget(getBoundTo());
        }
        remove(RemovalReason.KILLED);
    }
}
