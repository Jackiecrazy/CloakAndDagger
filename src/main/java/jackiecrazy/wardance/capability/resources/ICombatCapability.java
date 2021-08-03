package jackiecrazy.wardance.capability.resources;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;

public interface ICombatCapability {
    //might, spirit, posture, combo
    //might cooldown, spirit cooldown, posture cooldown, combo grace period
    //stagger timer, stagger counter for downed attacks
    //offhand cooldown, shield parry time, sidestep/roll timer
    //set, get, increment/decrement, consume (resource only)
    //is offhand attack, combat mode
    //shatter, shatter cooldown
    float getMight();

    void setMight(float amount);

    float addMight(float amount);

    default boolean consumeMight(float amount) {
        return consumeMight(amount, 0);
    }

    boolean consumeMight(float amount, float above);

    int getMightGrace();

    void setMightGrace(int amount);

    int decrementMightGrace(int amount);

    float getSpirit();

    void setSpirit(float amount);

    float addSpirit(float amount);

    default boolean consumeSpirit(float amount) {
        return consumeSpirit(amount, 0);
    }

    boolean consumeSpirit(float amount, float above);

    int getSpiritGrace();

    void setSpiritGrace(int amount);

    int decrementSpiritGrace(int amount);

    float getPosture();

    void setPosture(float amount);

    float addPosture(float amount);

    default boolean doConsumePosture(float amount) {
        return consumePosture(amount, 0) == 0;
    }

    default float consumePosture(float amount) {
        return consumePosture(amount, 0);
    }

    default float consumePosture(LivingEntity attacker, float amount) {
        return consumePosture(attacker, amount, 0);
    }

    default float consumePosture(float amount, float above) {
        return consumePosture(null, amount, above, false);
    }

    default float consumePosture(LivingEntity attacker, float amount, float above) {
        return consumePosture(attacker, amount, above, false);
    }

    boolean isFirstStaggerStrike();

    float consumePosture(LivingEntity assailant, float amount, float above, boolean force);

    int getPostureGrace();

    void setPostureGrace(int amount);

    int decrementPostureGrace(int amount);

    float getCombo();

    void setCombo(float amount);

    float addCombo(float amount);

    default boolean consumeCombo(float amount) {
        return consumeCombo(amount, 0);
    }

    boolean consumeCombo(float amount, float above);

    float getTrueMaxPosture();

    void setTrueMaxPosture(float amount);

    float getTrueMaxSpirit();

    void setTrueMaxSpirit(float amount);

    default float getMaxPosture() {
        return Math.max(0.1f, getTrueMaxPosture() - getFatigue());
    }

    default float getMaxSpirit() {
        return Math.max(0.1f, getTrueMaxSpirit() - getBurnout());
    }

    int getComboGrace();

    void setComboGrace(int amount);

    int decrementComboGrace(int amount);

    int getStaggerTime();

    void setStaggerTime(int amount);

    int decrementStaggerTime(int amount);

    int getStaggerCount();

    void setStaggerCount(int amount);

    void decrementStaggerCount(int amount);

    int getShieldTime();

    void setShieldTime(int amount);

    void decrementShieldTime(int amount);

    int getShieldCount();

    void setShieldCount(int amount);

    void decrementShieldCount(int amount);

    int getOffhandCooldown();

    void setOffhandCooldown(int amount);

    void addOffhandCooldown(int amount);

    /**
     * for the sake of convenience, positive is subject to cooldown and negatives are free
     */
    int getRollTime();

    void setRollTime(int amount);

    void decrementRollTime(int amount);

    boolean isOffhandAttack();

    void setOffhandAttack(boolean off);

    boolean isCombatMode();

    void toggleCombatMode(boolean on);

    float getWounding();

    void setWounding(float amount);

    float getFatigue();

    void setFatigue(float amount);

    float getBurnout();

    void setBurnout(float amount);

    void addWounding(float amount);

    void addFatigue(float amount);

    void addBurnout(float amount);

    int getHandBind(Hand h);

    void setHandBind(Hand h, int amount);

    void decrementHandBind(Hand h, int amount);

    float getHandReel(Hand hand);

    void setHandReel(Hand hand, float value);

    boolean consumeShatter(float value);

    int getShatterCooldown();

    void setShatterCooldown(int value);

    int decrementShatterCooldown(int value);

    float getCachedCooldown();

    void setCachedCooldown(float value);

    int getForcedSweep();

    void setForcedSweep(int angle);

    void clientTick();

    void serverTick();

    void sync();

    ItemStack getTempItemStack();

    void setTempItemStack(ItemStack is);

    void read(CompoundNBT tag);

    void setParryingTick(int parrying);

    int getParryingTick();//hey, it's useful for future "smart" entities as well.

    boolean isValid();

    CompoundNBT write();
}
