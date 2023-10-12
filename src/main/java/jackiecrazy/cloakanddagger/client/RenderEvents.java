package jackiecrazy.cloakanddagger.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.action.PermissionData;
import jackiecrazy.cloakanddagger.capability.vision.SenseData;
import jackiecrazy.cloakanddagger.config.ClientConfig;
import jackiecrazy.footwork.utils.GeneralUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CloakAndDagger.MODID)
public class RenderEvents {
    static final DecimalFormat formatter = new DecimalFormat("#.#");
    private static final ResourceLocation stealth = new ResourceLocation(CloakAndDagger.MODID, "textures/hud/stealth.png");

    /**
     * @Author Vazkii
     */
    @SubscribeEvent
    public static void eyes(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!PermissionData.getCap(mc.player).canSee()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) return;

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
                if (entity != null && (entity != look || !ClientConfig.CONFIG.stealth.enabled) && entity instanceof LivingEntity le && le != cameraEntity && le.isAlive() &&
                        !entity.getIndirectPassengers().iterator().hasNext() &&
                        entity.shouldRender(cameraPos.x(), cameraPos.y(), cameraPos.z()) &&
                        !GeneralUtils.viewBlocked(mc.player, le, false) &&
                        (entity.noCulling || frustum.isVisible(entity.getBoundingBox()))) {
                    renderEye((LivingEntity) entity, partialTicks, poseStack);
                }
            }

        }

    }

    @SubscribeEvent
    public static void stealthystealth(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> e) {
//        if (GeneralUtils.getAttributeValueSafe(e.getEntity(), FootworkAttributes.STEALTH.get()) > 0 && e.getEntity().isInvisible())
//            e.setCanceled(true);
    }

    private static void innerBlit(PoseStack ps, ResourceLocation p_283461_, int x, int x1, int y, int y1, int z, float u, float u1, float v, float v1) {
        RenderSystem.setShaderTexture(0, p_283461_);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = ps.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, (float)x, (float)y, (float)z).uv(u, v).endVertex();
        bufferbuilder.vertex(matrix4f, (float)x, (float)y1, (float)z).uv(u, v1).endVertex();
        bufferbuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix4f, (float)x1, (float)y, (float)z).uv(u1, v).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    private static void blit(PoseStack ps, float p_282942_, int p_281922_) {
        innerBlit(ps, RenderEvents.stealth, -16, -16 + p_281922_, -8, 8, 0, ((float) 0.0 + 0.0F) / (float) 64, ((float) 0.0 + (float) p_281922_) / (float) 64, (p_282942_ + 0.0F) / (float) 64, (p_282942_ + (float) 16) / (float) 64);
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
        return e.level().clip(new ClipContext(origin, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, e));
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

    private static void renderEye(LivingEntity passedEntity, float partialTicks, PoseStack poseStack) {
        final StealthInfo info = StealthInfo.stealthInfo(passedEntity);
        double dist = info.getRange();
        int shift = 0;
        switch (info.getAwareness()) {
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
        if (info.getRange() < 0)
            shift = 0;
        double x = passedEntity.xo + (passedEntity.getX() - passedEntity.xo) * partialTicks;
        double y = passedEntity.yo + (passedEntity.getY() - passedEntity.yo) * partialTicks;
        double z = passedEntity.zo + (passedEntity.getZ() - passedEntity.zo) * partialTicks;

        EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (renderDispatcher == null || renderDispatcher.camera == null) return;//what
        Vec3 renderPos = renderDispatcher.camera.getPosition();

        poseStack.pushPose();
        poseStack.translate((float) (x - renderPos.x()), (float) (y - renderPos.y() + passedEntity.getBbHeight()), (float) (z - renderPos.z()));
        RenderSystem.setShaderTexture(0, stealth);
        poseStack.translate(0.0D, (double) 0.5, 0.0D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        final float size = Mth.clamp(0.002F * getSize(passedEntity), 0.015f, 0.1f);
        poseStack.scale(-size, -size, size);
        //draws the eye
        poseStack.pushPose();
        blit(poseStack, shift * 16, 32);
        poseStack.popPose();
        //draws the red filling overlay
        poseStack.pushPose();
        if (info.getAwareness() == StealthUtils.Awareness.UNAWARE) {
            RenderSystem.setShaderColor(1, 0, 0, 1);
            blit(poseStack, shift * 16, (int) (32 * SenseData.getCap(passedEntity).getDetection(Minecraft.getInstance().player)));
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        poseStack.popPose();
        poseStack.popPose();

        //poseStack.translate(0.0D, -(NeatConfig.backgroundHeight + NeatConfig.barHeight + NeatConfig.backgroundPadding), 0.0D);
    }

    private static float getSize(LivingEntity elb) {
        return (float) Math.ceil(10 / 1.09 * elb.getBbWidth() * elb.getBbHeight());
    }
}
