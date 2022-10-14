package jackiecrazy.cloakanddagger.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import jackiecrazy.cloakanddagger.capability.vision.VisionData;
import jackiecrazy.cloakanddagger.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinMobStealth<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    private int lastcalculation = 0;

    private float cache = 1;

    private T mob;

    protected MixinMobStealth(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    /*@Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void ah(T f3, float f4, float direction, PoseStack ivertexbuilder, MultiBufferSource i, int layerrenderer, CallbackInfo ci) {
        mob = f3;
    }

    @ModifyConstant(method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", require = 0,
            slice = @Slice(from = @At(value = "INVOKE", target = "")),
            constant = {
                    @Constant(floatValue = 1.0F, ordinal = 7)
            })
    private float invisible(float constant) {
        if (!GeneralConfig.playerStealth || Minecraft.getInstance().player == null) return constant;
        if (mob.tickCount == lastcalculation) return cache;
        lastcalculation = mob.tickCount;
        double visible = VisionData.getCap(Minecraft.getInstance().player).visionRange() * mob.getVisibilityPercent(Minecraft.getInstance().player);
        visible *= visible;
        double distsq = Minecraft.getInstance().player.distanceToSqr(mob);
        float ret;
        if (distsq > visible) ret = 0;
        else ret = (float) ((visible - distsq) / visible);
        cache = ret;
        return ret;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "STORE"), ordinal = 0, require = 0)
    private RenderType rt(RenderType former) {
        if (!GeneralConfig.playerStealth || cache >= 0.9) return former;
        return RenderType.itemEntityTranslucentCull(getTextureLocation(mob));
    }*/
}