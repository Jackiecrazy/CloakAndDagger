package jackiecrazy.cloakanddagger.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.vision.VisionData;
import jackiecrazy.cloakanddagger.config.ClientConfig;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.config.DisplayConfigUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CloakAndDagger.MODID)
public class RenderEvents {
    static final DecimalFormat formatter = new DecimalFormat("#.#");
    private static final Cache<LivingEntity, Tuple<StealthOverride.Awareness, Double>> cache = CacheBuilder.newBuilder().weakKeys().expireAfterWrite(1, TimeUnit.SECONDS).build();
    private static final ResourceLocation stealth = new ResourceLocation(CloakAndDagger.MODID, "textures/hud/stealth.png");

    /**
     * @Author Vazkii
     */
    @SubscribeEvent
    public static void down(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();

        Camera camera = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();
        float partialTicks = event.getPartialTick();
        Entity cameraEntity = camera.getEntity() != null ? camera.getEntity() : mc.player;

        Vec3 cameraPos = camera.getPosition();
        final Frustum frustum = new Frustum(poseStack.last().pose(), event.getProjectionMatrix());
        frustum.prepare(cameraPos.x(), cameraPos.y(), cameraPos.z());

        ClientLevel client = mc.level;
        if (client != null && ClientConfig.showEyes) {
            Entity look = getEntityLookedAt(Minecraft.getInstance().player, 32);
            for (Entity entity : client.entitiesForRendering()) {
                if (entity != null && (entity != look || !ClientConfig.CONFIG.stealth.enabled) && entity instanceof LivingEntity && entity != cameraEntity && entity.isAlive() && !entity.getIndirectPassengers().iterator().hasNext() && entity.shouldRender(cameraPos.x(), cameraPos.y(), cameraPos.z()) && (entity.noCulling || frustum.isVisible(entity.getBoundingBox()))) {
                    renderEye((LivingEntity) entity, partialTicks, poseStack);
                }
            }
        }

    }

    /**
     * @author Vazkii
     */
    static Entity getEntityLookedAt(Entity e, double finalDistance) {
        Entity foundEntity = null;
        double distance = finalDistance;
        HitResult pos = raycast(e, finalDistance);
        Vec3 positionVector = e.position();

        if (e instanceof Player)
            positionVector = positionVector.add(0, e.getEyeHeight(e.getPose()), 0);

        if (pos != null)
            distance = pos.getLocation().distanceTo(positionVector);

        Vec3 lookVector = e.getLookAngle();
        Vec3 reachVector = positionVector.add(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance);

        Entity lookedEntity = null;
        List<Entity> entitiesInBoundingBox = e.getCommandSenderWorld().getEntities(e, e.getBoundingBox().inflate(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance).expandTowards(1F, 1F, 1F));
        double minDistance = distance;

        for (Entity entity : entitiesInBoundingBox) {
            if (entity.isPickable()) {
                AABB collisionBox = entity.getBoundingBoxForCulling();
                Optional<Vec3> interceptPosition = collisionBox.clip(positionVector, reachVector);

                if (collisionBox.contains(positionVector)) {
                    if (0.0D < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = 0.0D;
                    }
                } else if (interceptPosition.isPresent()) {
                    double distanceToEntity = positionVector.distanceTo(interceptPosition.get());

                    if (distanceToEntity < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = distanceToEntity;
                    }
                }
            }

            if (lookedEntity != null && (minDistance < distance || pos == null))
                foundEntity = lookedEntity;
        }

        return foundEntity;
    }

    static HitResult raycast(Entity e, double len) {
        Vec3 vec = new Vec3(e.getX(), e.getY(), e.getZ());
        if (e instanceof Player)
            vec = vec.add(new Vec3(0, e.getEyeHeight(e.getPose()), 0));

        Vec3 look = e.getLookAngle();
        if (look == null)
            return null;

        return raycast(vec, look, e, len);
    }

    static HitResult raycast(Vec3 origin, Vec3 ray, Entity e, double len) {
        Vec3 next = origin.add(ray.normalize().scale(len));
        return e.level.clip(new ClipContext(origin, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, e));
    }

    private static float updateValue(float f, float to) {
        if (f == -1) return to;
        boolean close = true;
        float temp = f;
        if (to > f) {
            f += Mth.clamp((to - temp) / 20, 0.01, 0.1);
            close = false;
        }
        if (to < f) {
            f += Mth.clamp((to - temp) / 20, -0.1, -0.01);
            close = !close;
        }
        if (close)
            f = to;
        return f;
    }

    static Tuple<StealthOverride.Awareness, Double> stealthInfo(LivingEntity at) {
        try {
            return cache.get(at, () -> {
                StealthOverride.Awareness a = StealthUtils.INSTANCE.getAwareness(Minecraft.getInstance().player, at);
                double mult = Minecraft.getInstance().player.getVisibilityPercent(at);
                return new Tuple<>(a, mult * VisionData.getCap(at).visionRange());
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new Tuple<>(StealthOverride.Awareness.ALERT, 1d);
    }

    private static void renderEye(LivingEntity passedEntity, float partialTicks, PoseStack poseStack) {
        final Tuple<StealthOverride.Awareness, Double> info = stealthInfo(passedEntity);
        double dist = info.getB();
        int shift = 0;
        switch (info.getA()) {
            case ALERT:
                return;
            case DISTRACTED:
                shift = 1;
                break;
            case UNAWARE:
                if (Minecraft.getInstance().player != null)
                    shift = passedEntity.distanceToSqr(Minecraft.getInstance().player) < dist * dist ? 2 : 3;
                break;
        }
        if (info.getB() < 0)
            shift = 0;
        double x = passedEntity.xo + (passedEntity.getX() - passedEntity.xo) * partialTicks;
        double y = passedEntity.yo + (passedEntity.getY() - passedEntity.yo) * partialTicks;
        double z = passedEntity.zo + (passedEntity.getZ() - passedEntity.zo) * partialTicks;

        EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Vec3 renderPos = renderDispatcher.camera.getPosition();

        poseStack.pushPose();
        poseStack.translate((float) (x - renderPos.x()), (float) (y - renderPos.y() + passedEntity.getBbHeight()), (float) (z - renderPos.z()));
        RenderSystem.setShaderTexture(0, stealth);
        poseStack.translate(0.0D, (double) 0.5, 0.0D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        final float size = Mth.clamp(0.002F * getSize(passedEntity), 0.015f, 0.1f);
        poseStack.scale(-size, -size, size);
        GuiComponent.blit(poseStack, -16, -8, 0, shift * 16, 32, 16, 64, 64);
        poseStack.popPose();

        //poseStack.translate(0.0D, -(NeatConfig.backgroundHeight + NeatConfig.barHeight + NeatConfig.backgroundPadding), 0.0D);
    }

    private static float getSize(LivingEntity elb) {
        float ret = CombatData.getCap(elb).getTrueMaxPosture();
        if (ret != 0) return ret;
        return (float) Math.ceil(10 / 1.09 * elb.getBbWidth() * elb.getBbHeight());
    }
}
