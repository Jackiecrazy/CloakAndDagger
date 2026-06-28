package jackiecrazy.cloakanddagger.entity.ai;

import jackiecrazy.cloakanddagger.capability.vision.ISense;
import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import jackiecrazy.cloakanddagger.config.StealthTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BreakLoSSearchGoal extends Goal {
    private final Mob mob;
    private final int searchDurationMin = 40; // ticks
    private final int searchDurationMax = 100;
    private BlockPos lastSeenPos;
    private int searchTicksLeft;

    public BreakLoSSearchGoal(Mob mob) {
        this.mob = mob;
        // Block movement and look so attack goals can't run while investigating
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity tgt = mob.getTarget();
        if (tgt == null) return false;
        // do not block retaliation / true revenge
        //if (mob.getLastHurtByMob() == tgt) return false;
        // skip for types that shouldn't search
        if (mob.getType().is(StealthTags.SKIP_SEARCH)) return false;
        // only when mob cannot directly see
        // check your sense/detection cap
        ISense cap = SenseData.getCap(mob);
        if (cap == null) return false;
        // only investigate if not fully detected
        if (cap.getDetection(tgt) >= 1.0f) return false;

        // optionally add a luck/stochastic check so mobs occasionally instantly lock on
        if (mob.getRandom().nextDouble() < immediateLockChance(mob, tgt)) return false;

        lastSeenPos = tgt.blockPosition();
        searchTicksLeft = searchDurationMin + mob.getRandom().nextInt(searchDurationMax - searchDurationMin);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity tgt = mob.getTarget();
        if (tgt == null) return false;
        ISense cap = SenseData.getCap(mob);
        // stop once detection is full
        if (cap != null && cap.getDetection(tgt) >= 1.0f) return false;
        // continue while we still have ticks left and nav in progress (or still searching)
        return searchTicksLeft > 0;
    }

    @Override
    public void start() {
        // path to last seen position, not the player directly
        if (lastSeenPos != null) {
            mob.getNavigation().moveTo(lastSeenPos.getX() + 0.5, lastSeenPos.getY(), lastSeenPos.getZ() + 0.5, 1.0D);
        }
    }

    @Override
    public void stop() {
        // let the normal goals resume
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity tgt = mob.getTarget();
        if (tgt == null) return;

        // decrement search timer
        searchTicksLeft = Math.max(0, searchTicksLeft - 1);

        // If we can see the target and detection is rising, update lastSeenPos
        if (mob.getSensing().hasLineOfSight(tgt)) {
            lastSeenPos = tgt.blockPosition();
            // optionally nudge navigation toward the last seen pos so the mob approaches
            mob.getNavigation().moveTo(lastSeenPos.getX() + 0.5, lastSeenPos.getY(), lastSeenPos.getZ() + 0.5, 1.0D);
        }

        // Look at the last seen area — makes the mob visibly "search"
        if (lastSeenPos != null) {
            mob.getLookControl().setLookAt(lastSeenPos.getX() + 0.5, lastSeenPos.getY(), lastSeenPos.getZ() + 0.5, 30.0F, 30.0F);
        }

        // Optional: small probabilistic jump to full lock based on luck/stealth (can avoid rigid timing)
        ISense cap = SenseData.getCap(mob);
        if (cap != null && cap.getDetection(tgt) >= 1.0f) {
            // detection full → finish goal so vanilla attack goals run
            this.stop();
        }
    }

    private double immediateLockChance(Mob mob, LivingEntity target) {
        // tune this: e.g. higher luck for target reduces chance, etc.
        return 0.0; // default: no instant lock here; you can vary it
    }
}
