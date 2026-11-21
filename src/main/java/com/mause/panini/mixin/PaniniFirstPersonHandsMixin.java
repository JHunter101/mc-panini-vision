package com.mause.panini.mixin;

import com.mause.panini.client.PaniniConfig;
import com.mause.panini.client.PaniniProjectionClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class PaniniFirstPersonHandsMixin {

    @Unique
    private boolean paniniVision$armWithItemPushed;

    @Unique
    private boolean paniniVision$playerArmPushed;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void paniniVision$beforeRenderArmWithItem(
        AbstractClientPlayer player,
        float partialTicks,
        float pitch,
        InteractionHand hand,
        float swingProgress,
        ItemStack stack,
        float equippedProgress,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int combinedLight,
        CallbackInfo ci
    ) {
        if (!paniniVision$isCompensationEnabled()) {
            return;
        }
        poseStack.pushPose();
        paniniVision$applyTransform(poseStack);
        paniniVision$armWithItemPushed = true;
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void paniniVision$afterRenderArmWithItem(
        AbstractClientPlayer player,
        float partialTicks,
        float pitch,
        InteractionHand hand,
        float swingProgress,
        ItemStack stack,
        float equippedProgress,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int combinedLight,
        CallbackInfo ci
    ) {
        if (paniniVision$armWithItemPushed) {
            poseStack.popPose();
            paniniVision$armWithItemPushed = false;
        }
    }

    @Inject(method = "renderPlayerArm", at = @At("HEAD"))
    private void paniniVision$beforeRenderPlayerArm(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int combinedLight,
        float equippedProgress,
        float swingProgress,
        HumanoidArm arm,
        CallbackInfo ci
    ) {
        if (!paniniVision$isCompensationEnabled()) {
            return;
        }
        poseStack.pushPose();
        paniniVision$applyTransform(poseStack);
        paniniVision$playerArmPushed = true;
    }

    @Inject(method = "renderPlayerArm", at = @At("RETURN"))
    private void paniniVision$afterRenderPlayerArm(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int combinedLight,
        float equippedProgress,
        float swingProgress,
        HumanoidArm arm,
        CallbackInfo ci
    ) {
        if (paniniVision$playerArmPushed) {
            poseStack.popPose();
            paniniVision$playerArmPushed = false;
        }
    }

    @Unique
    private static boolean paniniVision$isCompensationEnabled() {
        PaniniConfig config = PaniniProjectionClient.getConfig();
        return config != null && config.viewmodelCompensationEnabled;
    }

    @Unique
    private static void paniniVision$applyTransform(PoseStack poseStack) {
        PaniniConfig config = PaniniProjectionClient.getConfig();
        poseStack.translate(config.viewmodelOffsetX, config.viewmodelOffsetY, 0.0f);
        poseStack.scale(config.viewmodelScale, config.viewmodelScale, config.viewmodelScale);
    }
}