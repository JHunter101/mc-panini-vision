package com.mause.panini.client;

import com.mause.panini.PaniniProjectionMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = PaniniProjectionMod.MOD_ID, dist = Dist.CLIENT)
public class PaniniProjectionClient {

    public static final Logger LOGGER = LoggerFactory.getLogger(PaniniProjectionMod.MOD_ID);

    private static PaniniConfig config = PaniniConfig.load();

    public PaniniProjectionClient(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[PaniniProjection] Initializing client mod");
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(PaniniKeyBinding::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(PaniniKeyBinding::onClientTick);

        modContainer.registerExtensionPoint(
            IConfigScreenFactory.class,
            (container, parentScreen) -> new PaniniConfigScreen(parentScreen, getConfig())
        );
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            config = PaniniConfig.load();
            LOGGER.info("[PaniniProjection] Config loaded: enabled={}, strength={}, zoom={}",
                config.enabled, config.paniniStrength, config.cameraZoom);
        });
    }

    public static PaniniConfig getConfig() {
        if (config == null) {
            config = PaniniConfig.load();
        }
        return config;
    }

    public static void setConfig(PaniniConfig newConfig) {
        config = newConfig;
        if (config != null) {
            config.save();
        }
    }
}