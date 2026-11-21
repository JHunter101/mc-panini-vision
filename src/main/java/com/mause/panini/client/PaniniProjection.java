package com.mause.panini.client;

import com.mause.panini.mixin.AccessorPostChain;
import com.mause.panini.mixin.AccessorPostPass;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PaniniProjection {

    private static final ResourceLocation POST_CHAIN_ID = ResourceLocation.fromNamespaceAndPath("panini_vision", "shaders/post/panini.json");
    private static final AtomicBoolean LOGGED_FIRST_PROCESS = new AtomicBoolean();
    private static final AtomicBoolean WARNED_LOAD_FAILURE = new AtomicBoolean();
    private static final AtomicBoolean WARNED_MISSING_UNIFORMS = new AtomicBoolean();

    private static PostChain postChain;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    private PaniniProjection() {
    }

    public static void process(GameRenderer gameRenderer, float partialTick) {
        if (LOGGED_FIRST_PROCESS.compareAndSet(false, true)) {
            PaniniProjectionClient.LOGGER.info("[PaniniProjection] PaniniProjection.process() reached in renderLevel");
        }

        PaniniConfig config = PaniniProjectionClient.getConfig();
        if (config == null || !config.enabled || config.paniniStrength <= 0.0f) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.player.isScoping()) {
            return;
        }

        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        PostChain chain = getOrCreateChain(mc, width, height);
        if (chain == null) {
            return;
        }

        if (width != lastWidth || height != lastHeight) {
            chain.resize(width, height);
            lastWidth = width;
            lastHeight = height;
        }
        
        updateUniforms(chain, mc, gameRenderer, config);
        chain.process(partialTick);
        mc.getMainRenderTarget().bindWrite(true);
    }

    private static PostChain getOrCreateChain(Minecraft mc, int width, int height) {
        if (postChain != null) {
            return postChain;
        }
        try {
            postChain = new PostChain(
                mc.getTextureManager(),
                mc.getResourceManager(),
                mc.getMainRenderTarget(),
                POST_CHAIN_ID
            );
            postChain.resize(width, height);
            lastWidth = width;
            lastHeight = height;
            PaniniProjectionClient.LOGGER.info("[PaniniProjection] Loaded PostChain {}", POST_CHAIN_ID);
            return postChain;
        } catch (IOException | RuntimeException e) {
            if (WARNED_LOAD_FAILURE.compareAndSet(false, true)) {
                PaniniProjectionClient.LOGGER.error("[PaniniProjection] Failed to load post chain {}", POST_CHAIN_ID, e);
            }
            return null;
        }
    }

    private static void updateUniforms(PostChain chain, Minecraft mc, GameRenderer gameRenderer, PaniniConfig config) {
        double fov = mc.options.fov().get().doubleValue();
        Matrix4f projection = gameRenderer.getProjectionMatrix(fov);
        float sourceExtentX = 1.0f / Math.max(1e-5f, Math.abs(projection.m00()));
        float sourceExtentY = 1.0f / Math.max(1e-5f, Math.abs(projection.m11()));

        boolean foundAny = false;
        List<PostPass> passes = ((AccessorPostChain) chain).getPasses();
        for (PostPass pass : passes) {
            EffectInstance effect = ((AccessorPostPass) pass).getEffect();
            if (effect == null) {
                continue;
            }
            Uniform paramsUniform = effect.getUniform("Params");
            if (paramsUniform != null) {
                paramsUniform.set(config.paniniStrength, sourceExtentX, sourceExtentY, config.verticalCompression);
                foundAny = true;
            }
            Uniform viewUniform = effect.getUniform("View");
            if (viewUniform != null) {
                viewUniform.set(config.cameraZoom, 0.0f, 0.0f, 0.0f);
                foundAny = true;
            }
        }

        if (!foundAny && WARNED_MISSING_UNIFORMS.compareAndSet(false, true)) {
            PaniniProjectionClient.LOGGER.warn("[PaniniProjection] 'Params' or 'View' uniform was not found on any post pass.");
        }
    }

    public static void invalidate() {
        if (postChain != null) {
            postChain.close();
            postChain = null;
        }
        lastWidth = -1;
        lastHeight = -1;
    }
}