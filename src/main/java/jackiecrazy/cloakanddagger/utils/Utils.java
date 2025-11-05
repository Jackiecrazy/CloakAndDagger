package jackiecrazy.cloakanddagger.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class Utils {
    public static double getAttributeValueSafe(LivingEntity e, Attribute a) {
        if (e.getAttribute(a) != null) return e.getAttributeValue(a);
        return a.getDefaultValue();
    }

    public static boolean viewBlocked(Entity viewer, Entity viewed, boolean flimsy) {
        if (viewer.distanceToSqr(viewed) > 1000) return true;//what
        AABB viewerBoundBox = viewer.getBoundingBox();
        AABB angelBoundingBox = viewed.getBoundingBox();
        Vec3[] viewerPoints = {
                new Vec3(viewerBoundBox.minX, viewerBoundBox.minY, viewerBoundBox.minZ),
                new Vec3(viewerBoundBox.minX, viewerBoundBox.minY, viewerBoundBox.maxZ),
                new Vec3(viewerBoundBox.minX, viewerBoundBox.maxY, viewerBoundBox.minZ),
                new Vec3(viewerBoundBox.minX, viewerBoundBox.maxY, viewerBoundBox.maxZ),
                new Vec3(viewerBoundBox.maxX, viewerBoundBox.maxY, viewerBoundBox.minZ),
                new Vec3(viewerBoundBox.maxX, viewerBoundBox.maxY, viewerBoundBox.maxZ),
                new Vec3(viewerBoundBox.maxX, viewerBoundBox.minY, viewerBoundBox.maxZ),
                new Vec3(viewerBoundBox.maxX, viewerBoundBox.minY, viewerBoundBox.minZ),
        };

        Vec3[] angelPoints = {
                new Vec3(angelBoundingBox.minX, angelBoundingBox.minY, angelBoundingBox.minZ),
                new Vec3(angelBoundingBox.minX, angelBoundingBox.minY, angelBoundingBox.maxZ),
                new Vec3(angelBoundingBox.minX, angelBoundingBox.maxY, angelBoundingBox.minZ),
                new Vec3(angelBoundingBox.minX, angelBoundingBox.maxY, angelBoundingBox.maxZ),
                new Vec3(angelBoundingBox.maxX, angelBoundingBox.maxY, angelBoundingBox.minZ),
                new Vec3(angelBoundingBox.maxX, angelBoundingBox.maxY, angelBoundingBox.maxZ),
                new Vec3(angelBoundingBox.maxX, angelBoundingBox.minY, angelBoundingBox.maxZ),
                new Vec3(angelBoundingBox.maxX, angelBoundingBox.minY, angelBoundingBox.minZ),
        };

        for (int i = 0; i < viewerPoints.length; i++) {
            if (viewer.level().clip(new ClipContext(viewerPoints[i], angelPoints[i], flimsy ? ClipContext.Block.OUTLINE : ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, viewer)).getType() == HitResult.Type.MISS) {
                return false;
            }
            if (rayTraceBlocks(viewer, viewer.level(), viewerPoints[i], angelPoints[i], pos -> {
                BlockState state = viewer.level().getBlockState(pos);
                return flimsy ? !state.liquid() : !canSeeThrough(state, viewer.level(), pos);
            }) == null) return false;
        }

        return true;
    }

    @Nullable
    private static HitResult rayTraceBlocks(Entity livingEntity,
                                            Level world,
                                            Vec3 vec31,
                                            Vec3 vec32,
                                            Predicate<BlockPos> stopOn) {
        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
            if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
                int i = Mth.floor(vec32.x);
                int j = Mth.floor(vec32.y);
                int k = Mth.floor(vec32.z);
                int l = Mth.floor(vec31.x);
                int i1 = Mth.floor(vec31.y);
                int j1 = Mth.floor(vec31.z);
                BlockPos blockpos = new BlockPos(l, i1, j1);
                if (stopOn.test(blockpos)) {
                    HitResult raytraceresult = world.clip(new ClipContext(vec31, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, livingEntity));
                    return raytraceresult;
                }

                int k1 = 200;

                while (k1-- >= 0) {
                    if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
                        return null;
                    }

                    if (l == i && i1 == j && j1 == k) {
                        return null;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l) {
                        d0 = (double) l + 1.0D;
                    } else if (i < l) {
                        d0 = (double) l + 0.0D;
                    } else {
                        flag2 = false;
                    }

                    if (j > i1) {
                        d1 = (double) i1 + 1.0D;
                    } else if (j < i1) {
                        d1 = (double) i1 + 0.0D;
                    } else {
                        flag = false;
                    }

                    if (k > j1) {
                        d2 = (double) j1 + 1.0D;
                    } else if (k < j1) {
                        d2 = (double) j1 + 0.0D;
                    } else {
                        flag1 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = vec32.x - vec31.x;
                    double d7 = vec32.y - vec31.y;
                    double d8 = vec32.z - vec31.z;

                    if (flag2) {
                        d3 = (d0 - vec31.x) / d6;
                    }

                    if (flag) {
                        d4 = (d1 - vec31.y) / d7;
                    }

                    if (flag1) {
                        d5 = (d2 - vec31.z) / d8;
                    }

                    if (d3 == -0.0D) {
                        d3 = -1.0E-4D;
                    }

                    if (d4 == -0.0D) {
                        d4 = -1.0E-4D;
                    }

                    if (d5 == -0.0D) {
                        d5 = -1.0E-4D;
                    }

                    Direction enumfacing;

                    if (d3 < d4 && d3 < d5) {
                        enumfacing = i > l ? Direction.WEST : Direction.EAST;
                        vec31 = new Vec3(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
                    } else if (d4 < d5) {
                        enumfacing = j > i1 ? Direction.DOWN : Direction.UP;
                        vec31 = new Vec3(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
                    } else {
                        enumfacing = k > j1 ? Direction.NORTH : Direction.SOUTH;
                        vec31 = new Vec3(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
                    }

                    l = Mth.floor(vec31.x) - (enumfacing == Direction.EAST ? 1 : 0);
                    i1 = Mth.floor(vec31.y) - (enumfacing == Direction.UP ? 1 : 0);
                    j1 = Mth.floor(vec31.z) - (enumfacing == Direction.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(l, i1, j1);
                    if (stopOn.test(blockpos)) {
                        return world.clip(new ClipContext(vec31, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, livingEntity));
                    }
                }
            }
        }

        return null;
    }

    public static boolean canSeeThrough(BlockState blockState, Level world, BlockPos pos) {
        if (!blockState.canOcclude() || !blockState.isSolidRender(world, pos)) {
            return true;
        }

        Block block = blockState.getBlock();

        if (block instanceof DoorBlock) {
            return blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER;
        }

        return blockState.getCollisionShape(world, pos) == Shapes.empty();
    }

    public static double getSpeedSq(Entity e) {
        return e.getDeltaMovement().lengthSqr();
    }

    public static boolean isFacingEntity(Entity entity1, Entity entity2, int horAngle, int vertAngle) {
        horAngle = Math.min(horAngle, 360);
        vertAngle = Math.min(vertAngle, 360);
        if (horAngle < 0) return isBehindEntity(entity2, entity1, -horAngle, Math.abs(vertAngle));
        double xDiff = entity1.getX() - entity2.getX(), zDiff = entity1.getZ() - entity2.getZ();
        if (vertAngle != 360) {
            double distIgnoreY = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            double relativeHeadVec = entity2.getY() - entity1.getY() - entity1.getEyeHeight() + entity2.getBbHeight();
            double relativeFootVec = entity2.getY() - entity1.getY() - entity1.getEyeHeight();
            double angleHead = -Mth.atan2(relativeHeadVec, distIgnoreY);
            double angleFoot = -Mth.atan2(relativeFootVec, distIgnoreY);
            double maxRot = Math.toRadians(entity1.getXRot() + vertAngle / 2f);
            double minRot = Math.toRadians(entity1.getXRot() - vertAngle / 2f);
            if (angleHead > maxRot || angleFoot < minRot) return false;
        }
        if (horAngle != 360) {
            Vec3 lookVec = entity1.getViewVector(1.0F);
            Vec3 bodyVec = getBodyOrientation(entity1);
            Vec3 relativePosVec = entity2.position().subtract(entity1.position());
            double angleLook = Mth.atan2(lookVec.z, lookVec.x);
            double angleBody = Mth.atan2(bodyVec.z, bodyVec.x);
            double anglePos = Mth.atan2(relativePosVec.z, relativePosVec.x);
            angleBody += Math.PI;
            angleLook += Math.PI;
            anglePos += Math.PI;
            double rad = Math.toRadians(horAngle / 2f);
            return !(Math.abs(angleLook - anglePos) > rad) || !(Math.abs(angleBody - anglePos) > rad);
        }
        return true;
    }

    public static boolean isBehindEntity(Entity entity, Entity reference, int horAngle, int vertAngle) {
        if (horAngle < 0) return isFacingEntity(reference, entity, -horAngle, Math.abs(vertAngle));
        Vec3 posVec = reference.position().add(0, reference.getEyeHeight(), 0);
        double xDiff = reference.getX() - entity.getX(), zDiff = reference.getZ() - entity.getZ();
        double distIgnoreY = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double relativeHeadVec = reference.getY() - entity.getY() - entity.getEyeHeight() + reference.getBbHeight();
        double relativeFootVec = reference.getY() - entity.getY() - entity.getEyeHeight();
        double angleHead = -Mth.atan2(relativeHeadVec, distIgnoreY);
        double angleFoot = -Mth.atan2(relativeFootVec, distIgnoreY);
        double maxRot = Math.toRadians(reference.getXRot() + vertAngle / 2f);
        double minRot = Math.toRadians(reference.getXRot() - vertAngle / 2f);
        if (angleHead > maxRot || angleFoot < minRot) return false;
        double xDiffCompensated;
        if (xDiff < 0) {
            xDiffCompensated = Math.min(-0.1, xDiff + entity.getBbWidth() / 2 + reference.getBbWidth() / 2);
        } else {
            xDiffCompensated = Math.max(0.1, xDiff - entity.getBbWidth() / 2 - reference.getBbWidth() / 2);
        }
        double zDiffCompensated;
        if (zDiff < 0) {
            zDiffCompensated = Math.min(-0.1, zDiff + entity.getBbWidth() / 2 + reference.getBbWidth() / 2);
        } else {
            zDiffCompensated = Math.max(0.1, zDiff - entity.getBbWidth() / 2 - reference.getBbWidth() / 2);
        }
        Vec3 bodyVec = getBodyOrientation(reference);
        Vec3 lookVec = reference.getViewVector(1f);
        Vec3 relativePosVec = new Vec3(xDiffCompensated, 0, zDiffCompensated);
        double dotsqLook = ((relativePosVec.dot(lookVec) * Math.abs(relativePosVec.dot(lookVec))) / (relativePosVec.lengthSqr() * lookVec.lengthSqr()));
        double dotsqBody = ((relativePosVec.dot(bodyVec) * Math.abs(relativePosVec.dot(bodyVec))) / (relativePosVec.lengthSqr() * bodyVec.lengthSqr()));
        double cos = Mth.cos((float) Math.toRadians(horAngle / 2f));
        return dotsqBody > cos * cos || dotsqLook > cos * cos;
    }

    public static Vec3 getBodyOrientation(Entity e) {
        float f = Mth.cos(-e.getYRot() * 0.017453292F - (float) Math.PI);
        float f1 = Mth.sin(-e.getYRot() * 0.017453292F - (float) Math.PI);
        float f2 = -Mth.cos(-e.getXRot() * 0.017453292F);
        float f3 = Mth.sin(-e.getXRot() * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }
}
