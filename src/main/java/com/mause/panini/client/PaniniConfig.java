package com.mause.panini.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PaniniConfig {
    public boolean enabled = true;
    public float paniniStrength = 0.5f;
    public float verticalCompression = 0.06f;
    public float cameraZoom = 0.7f;
    public boolean viewmodelCompensationEnabled = false;
    public float viewmodelScale = 0.92f;
    public float viewmodelOffsetX = 0.0f;
    public float viewmodelOffsetY = 0.0f;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Path getConfigPath() {
        return Path.of("config", "panini_vision", "config.json");
    }

    public static PaniniConfig load() {
        Path path = getConfigPath();
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path, StandardCharsets.UTF_8);
                PaniniConfig loaded = GSON.fromJson(json, PaniniConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException | JsonSyntaxException e) {
                System.err.println("[PaniniProjection] Failed to load config: " + e.getMessage());
            }
        }
        return new PaniniConfig();
    }

    public void save() {
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            String json = GSON.toJson(this);
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(json);
            }
            System.out.println("[PaniniProjection] Config saved to " + path);
        } catch (IOException e) {
            System.err.println("[PaniniProjection] Failed to save config: " + e.getMessage());
        }
    }
}
