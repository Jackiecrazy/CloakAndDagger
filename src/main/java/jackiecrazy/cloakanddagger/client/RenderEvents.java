package jackiecrazy.cloakanddagger.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import jackiecrazy.cloakanddagger.CloakAndDagger;
import jackiecrazy.cloakanddagger.capability.vision.VisionData;
import jackiecrazy.cloakanddagger.config.ClientConfig;
import jackiecrazy.cloakanddagger.utils.StealthOverride;
import jackiecrazy.footwork.capability.resources.CombatData;
import jackiecrazy.footwork.capability.resources.ICombatCapability;
import jackiecrazy.footwork.config.DisplayConfigUtils;
import jackiecrazy.footwork.utils.StealthUtils;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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
    public static void down(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();

        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        MatrixStack poseStack = event.getMatrixStack();
        float partialTicks = event.getPartialTicks();
        Entity cameraEntity = camera.getEntity() != null ? camera.getEntity() : mc.player;

        Vector3d cameraPos = camera.getPosition();
        final ClippingHelper frustum = new ClippingHelper(poseStack.last().pose(), event.getProjectionMatrix());
        frustum.prepare(cameraPos.x(), cameraPos.y(), cameraPos.z());

        ClientWorld client = mc.level;
        if (client != null && ClientConfig.showEyes) {
            Entity look = getEntityLookedAt(Minecraft.getInstance().player, 32);
            for (Entity entity : client.entitiesForRendering()) {
                if (entity != null && (entity != look || !ClientConfig.CONFIG.stealth.enabled) && entity instanceof LivingEntity && entity != cameraEntity && entity.isAlive() && !entity.getIndirectPassengers().iterator().hasNext() && entity.shouldRender(cameraPos.x(), cameraPos.y(), cameraPos.z()) && (entity.noCulling || frustum.isVisible(entity.getBoundingBox()))) {
                    renderEye((LivingEntity) entity, partialTicks, poseStack);
                }
            }
        }

    }

    @SubscribeEvent
    public static void displayCoolie(RenderGameOverlayEvent.Post event) {
        MainWindow sr = event.getWindow();
        final Minecraft mc = Minecraft.getInstance();
        final MatrixStack stack = event.getMatrixStack();

        if (event.getType().equals(RenderGameOverlayEvent.ElementType.ALL))
            if (mc.getCameraEntity() instanceof PlayerEntity && mc.player != null) {
                ClientPlayerEntity player = mc.player;
                ICombatCapability cap = CombatData.getCap(player);
                int width = sr.getGuiScaledWidth();
                int height = sr.getGuiScaledHeight();
                //render posture bar if not full, displayed even out of combat mode because it's pretty relevant to not dying
                Entity look = getEntityLookedAt(player, 32);
                if (look instanceof LivingEntity) {
                    LivingEntity looked = (LivingEntity) look;
                    stealth:
                    {
                        if (ClientConfig.CONFIG.stealth.enabled) {
                            Pair<Integer, Integer> pair = DisplayConfigUtils.translateCoords(ClientConfig.CONFIG.stealth, width, height);
                            final Tuple<StealthOverride.Awareness, Double> info = stealthInfo(looked);
                            double dist = info.getB();
                            int shift = 0;
                            switch (info.getA()) {
                                case ALERT:
                                    break stealth;
                                case DISTRACTED:
                                    shift = 1;
                                    break;
                                case UNAWARE:
                                    if (Minecraft.getInstance().player != null)
                                        shift = looked.distanceToSqr(Minecraft.getInstance().player) < dist * dist ? 2 : 3;
                                    break;
                            }
                            if (info.getB() < 0)
                                shift = 0;
                            mc.getTextureManager().bind(stealth);
                            AbstractGui.blit(stack, pair.getFirst() - 16, pair.getSecond() - 8, 0, shift * 16, 32, 16, 64, 64);
                        }
                    }
                }
            }
    }


    /**
     * @author Vazkii
     */
    public static Entity getEntityLookedAt(Entity e, double finalDistance) {
        Entity foundEntity = null;
        double distance = finalDistance;
        RayTraceResult pos = raycast(e, finalDistance);
        Vector3d positionVector = e.position();

        if (e instanceof PlayerEntity)
            positionVector = positionVector.add(0, e.getEyeHeight(e.getPose()), 0);

        if (pos != null)
            distance = pos.getLocation().distanceTo(positionVector);

        Vector3d lookVector = e.getLookAngle();
        Vector3d reachVector = positionVector.add(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance);

        Entity lookedEntity = null;
        List<Entity> entitiesInBoundingBox = e.getCommandSenderWorld().getEntities(e, e.getBoundingBox().inflate(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance).expandTowards(1F, 1F, 1F));
        double minDistance = distance;

        for (Entity entity : entitiesInBoundingBox) {
            if (entity.isPickable()) {
                AxisAlignedBB collisionBox = entity.getBoundingBoxForCulling();
                Optional<Vector3d> interceptPosition = collisionBox.clip(positionVector, reachVector);

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

    public static RayTraceResult raycast(Entity e, double len) {
        Vector3d vec = new Vector3d(e.getX(), e.getY(), e.getZ());
        if (e instanceof PlayerEntity)
            vec = vec.add(new Vector3d(0, e.getEyeHeight(e.getPose()), 0));

        Vector3d look = e.getLookAngle();
        if (look == null)
            return null;

        return raycast(vec, look, e, len);
    }

    public static RayTraceResult raycast(Vector3d origin, Vector3d ray, Entity e, double len) {
        Vector3d next = origin.add(ray.normalize().scale(len));
        return e.level.clip(new RayTraceContext(origin, next, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, e));
    }

    private static float updateValue(float f, float to) {
        if (f == -1) return to;
        boolean close = true;
        float temp = f;
        if (to > f) {
            f += MathHelper.clamp((to - temp) / 20, 0.01, 0.1);
            close = false;
        }
        if (to < f) {
            f += MathHelper.clamp((to - temp) / 20, -0.1, -0.01);
            close = !close;
        }
        if (close)
            f = to;
        return f;
    }

    private static Tuple<StealthOverride.Awareness, Double> stealthInfo(LivingEntity at) {
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

    private static void renderEye(LivingEntity passedEntity, float partialTicks, MatrixStack poseStack) {
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

        EntityRendererManager renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        Vector3d renderPos = renderDispatcher.camera.getPosition();

        poseStack.pushPose();
        poseStack.translate((float) (x - renderPos.x()), (float) (y - renderPos.y() + passedEntity.getBbHeight()), (float) (z - renderPos.z()));
        Minecraft.getInstance().getTextureManager().bind(stealth);
        poseStack.translate(0.0D, (double) 0.5, 0.0D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        final float size = MathHelper.clamp(0.002F * getSize(passedEntity), 0.015f, 0.1f);
        poseStack.scale(-size, -size, size);
        AbstractGui.blit(poseStack, -16, -8, 0, shift * 16, 32, 16, 64, 64);
        poseStack.popPose();

        //poseStack.translate(0.0D, -(NeatConfig.backgroundHeight + NeatConfig.barHeight + NeatConfig.backgroundPadding), 0.0D);
    }

    private static float getSize(LivingEntity elb) {
        float ret = CombatData.getCap(elb).getTrueMaxPosture();
        if (ret != 0) return ret;
        return (float) Math.ceil(10 / 1.09 * elb.getBbWidth() * elb.getBbHeight());
    }
}
