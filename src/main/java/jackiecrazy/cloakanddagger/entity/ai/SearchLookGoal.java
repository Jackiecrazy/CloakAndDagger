package jackiecrazy.cloakanddagger.entity.ai;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import jackiecrazy.footwork.utils.GeneralUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class SearchLookGoal extends Goal {
    public static final float DEFAULT_PROBABILITY = 0.02F;
    protected final Mob mob;
    protected double spread;
    @Nullable
    protected LivingEntity lookAt;
    private Vec3 randomized = Vec3.ZERO;
    private int lookTime;

    public SearchLookGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));

    }

    public boolean canUse() {
        if (SenseData.getCap(mob).getLookingFor() == null || mob.getTarget() != null) {
            return false;
        } else {
            lookAt = SenseData.getCap(mob).getLookingFor();
            return this.lookAt != null;
        }
    }

    public boolean canContinueToUse() {
        if (lookAt == null) return false;
        if (!this.lookAt.isAlive()) {
            return false;
        } else if (mob.getTarget()!=null) {
            return false;
        } else {
            return this.lookTime > 0;
        }
    }

    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
        this.spread = (1 - SenseData.getCap(mob).getDetectionPerc(lookAt)) * GeneralUtils.getAttributeValueSafe(mob, Attributes.FOLLOW_RANGE);
        this.randomized = new Vec3((CloakAndDagger.rand.nextFloat()-0.5) * spread, (CloakAndDagger.rand.nextFloat()-0.5) * spread/3, (CloakAndDagger.rand.nextFloat()-0.5) * spread);
    }

    public void stop() {
        this.lookAt = null;
        SenseData.getCap(mob).setLookingFor(null);
    }

    public void tick() {
        if (lookAt != null && this.lookAt.isAlive()) {
            double eyeY = this.lookAt.getEyeY();
            this.mob.getLookControl().setLookAt(this.lookAt.getX() + randomized.x, eyeY + randomized.y, this.lookAt.getZ() + randomized.z);
            --this.lookTime;
        }
    }
}
