package jackiecrazy.cloakanddagger.entity.ai;

import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.footwork.capability.goal.GoalCapabilityProvider;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

public class InvestigateSoundGoal extends MoveToBlockGoal {
    int hesitation;

    public InvestigateSoundGoal(PathfinderMob c) {
        super(c, 0.6, 32);
    }

    @Override
    public void start() {
        super.start();
        hesitation = CloakAndDagger.rand.nextInt(60) + 20;
    }

    @Override
    public boolean canUse() {
        //only use if idle
        if (mob.getTarget() != null) return false;
        return findNearestBlock();
    }

    @Override
    protected boolean isValidTarget(LevelReader w, BlockPos b) {
        double rangesq = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        rangesq *= rangesq;
        return (mob.blockPosition().distSqr(b) < rangesq);
    }

    @Override
    protected boolean findNearestBlock() {
        mob.getCapability(GoalCapabilityProvider.CAP).ifPresent(a -> {
            if (isValidTarget(mob.level, a.getSoundLocation()))
                this.blockPos = a.getSoundLocation();
            else this.blockPos = BlockPos.ZERO;
        });
        return this.blockPos != BlockPos.ZERO;
    }

    @Override
    public double acceptedDistance() {
        return 1 + (int) (mob.getX() * mob.getZ()) & 15;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getTarget() == null && mob.getLastHurtMob() == null && super.canContinueToUse();
    }

    @Override
    public void tick() {
        if (--hesitation < 0)
            super.tick();
    }

    @Override
    public void stop() {
        super.stop();
        mob.getCapability(GoalCapabilityProvider.CAP).ifPresent(a -> a.setSoundLocation(BlockPos.ZERO));
    }
}
