package com.mause.panini.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class PaniniSlider extends AbstractSliderButton {

    private final float minValue;
    private final float maxValue;
    private final boolean snapToInt;
    private final ValueCallback callback;

    @FunctionalInterface
    public interface ValueCallback {
        void onValueChanged(float value);
    }

    public PaniniSlider(int x, int y, int width, int height, float min, float max,
                        float current, boolean snapToInt, ValueCallback callback) {
        super(x, y, width, height, Component.literal(""),
              normalize(min, max, clamp(current, min, max)));
        this.minValue = min;
        this.maxValue = max;
        this.snapToInt = snapToInt;
        this.callback = callback;
        updateMessage();
    }

    private static double normalize(float min, float max, float value) {
        if (max == min) {
            return 0.0;
        }
        return (value - min) / (max - min);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void applyValue() {
        float actual = actualValue();
        callback.onValueChanged(actual);
    }

    @Override
    protected void updateMessage() {
        float actual = actualValue();
        if (snapToInt) {
            this.setMessage(Component.literal(String.valueOf(Math.round(actual))));
        } else {
            this.setMessage(Component.literal(String.format("%.2f", actual)));
        }
    }

    private float actualValue() {
        float v = minValue + (float) (this.value * (maxValue - minValue));
        if (snapToInt) {
            v = Math.round(v);
        }
        return v;
    }

    public void setActualValue(float value) {
        this.value = normalize(minValue, maxValue, clamp(value, minValue, maxValue));
        updateMessage();
    }
}