package com.mause.panini.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class PaniniKeyBinding {

    public static final String CATEGORY = "key.categories.panini_vision";
    public static final String KEY_OPEN_CONFIG = "key.panini_vision.open_config";

    public static final KeyMapping CONFIG_KEY = new KeyMapping(
        KEY_OPEN_CONFIG,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_P,
        CATEGORY
    );

    public static void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(CONFIG_KEY);
    }

    public static void onClientTick(final ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        while (CONFIG_KEY.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new PaniniConfigScreen(null, PaniniProjectionClient.getConfig()));
            }
        }
    }
}
