package jackiecrazy.cloakanddagger.entity.ai;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import jackiecrazy.footwork.capability.goal.GoalCapabilityProvider;
import jackiecrazy.footwork.capability.goal.IGoalHelper;
import jackiecrazy.footwork.utils.GeneralUtils;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;

public class SearchLookGoal extends Goal {
    public static final float DEFAULT_PROBABILITY = 0.02F;
    protected final Mob mob;
    protected double spread;
    @Nullable
    protected Vec3 lookAt;
    private Vec3 randomized = Vec3.ZERO;
    private int lookTime;

    public SearchLookGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));

    }

    public boolean canUse() {
        if (mob.getTarget() != null) return false;
        Optional<IGoalHelper> goals = mob.getCapability(GoalCapabilityProvider.CAP).resolve();
        if (goals.isPresent() && goals.get().getSoundLocation() != null && goals.get().getSoundLocation().getY()>-50) {
            lookAt = goals.get().getSoundLocation().getCenter();
            return true;
        }
        if (SenseData.getCap(mob).getLookingFor() != null) {
            lookAt = SenseData.getCap(mob).getLookingFor().getEyePosition();
            this.spread = (1 - SenseData.getCap(mob).getDetectionPerc(SenseData.getCap(mob).getLookingFor())) * GeneralUtils.getAttributeValueSafe(mob, Attributes.FOLLOW_RANGE);
            return true;
        }
        return false;
    }

    public boolean canContinueToUse() {
        if (lookAt == null||lookAt==Vec3.ZERO) return false;
        if (mob.getTarget() != null) {
            return false;
        } else {
            return this.lookTime > 0;
        }
    }

    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
        this.randomized = new Vec3((CloakAndDagger.rand.nextFloat() - 0.5) * spread, (CloakAndDagger.rand.nextFloat() - 0.5) * spread / 3, (CloakAndDagger.rand.nextFloat() - 0.5) * spread);
    }

    public void stop() {
        this.lookAt = null;
        SenseData.getCap(mob).setLookingFor(null);
    }

    public void tick() {
        if (lookAt != null) {
            this.mob.getLookControl().setLookAt(lookAt.add(randomized));
            --this.lookTime;
        }
    }
}
