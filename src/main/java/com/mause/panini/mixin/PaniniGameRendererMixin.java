package com.mause.panini.mixin;

import com.mause.panini.client.PaniniProjection;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class PaniniGameRendererMixin {

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void paniniVision$afterRenderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        PaniniProjection.process((GameRenderer) (Object) this, deltaTracker.getGameTimeDeltaPartialTick(true));
    }
}