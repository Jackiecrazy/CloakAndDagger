package jackiecrazy.cloakanddagger.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class DecoyEntity extends Mob {
    public DecoyEntity setTicksToLive(int ticksToLive) {
        this.ticksToLive = ticksToLive;
        return this;
    }

    private int ticksToLive;
    private LivingEntity boundTo;

    public DecoyEntity(EntityType<? extends DecoyEntity> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        ticksToLive = 100;
    }

    public void die(DamageSource p_21014_) {
        if(p_21014_.getEntity() instanceof Mob mob){
            mob.setTarget(getBoundTo());
        }
        remove(RemovalReason.KILLED);
    }

    @Override
    public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes();
    }

    public LivingEntity getBoundTo() {
        return boundTo;
    }

    public DecoyEntity setBoundTo(LivingEntity boundTo) {
        this.boundTo = boundTo;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        if (ticksToLive-- < 0) {
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void aiStep() {
        // NO-OP
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("tickstolive", ticksToLive);
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        ticksToLive = tag.getInt("tickstolive");
        super.readAdditionalSaveData(tag);
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource p_21239_) {
        return null;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public boolean isInvisible() {
        return true;
    }
}
