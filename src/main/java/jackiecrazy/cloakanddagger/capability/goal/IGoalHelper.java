package jackiecrazy.cloakanddagger.capability.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;

public interface IGoalHelper {
    void setForcedTarget(LivingEntity e);
    LivingEntity getForcedTarget();
    void setFearSource(LivingEntity e);
    LivingEntity getFearSource();
    void setSoundLocation(BlockPos pos);
    BlockPos getSoundLocation();
}
