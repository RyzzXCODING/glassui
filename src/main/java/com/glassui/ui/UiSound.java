/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.GlassConfig;
import net.minecraft.class_310;
import net.minecraft.class_3417;

public final class UiSound {
    private static long lastMs;

    private UiSound() {
    }

    public static void pop(float pitch) {
        GlassConfig c = GlassConfig.get();
        if (!c.uiSound || c.uiSoundVol <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastMs < 35L) {
            return;
        }
        lastMs = now;
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return;
        }
        float p = pitch + (float)((Math.random() - 0.5) * 0.12);
        mc.field_1724.method_5783(class_3417.field_15197, Math.min(1.0f, (float)c.uiSoundVol / 100.0f), p);
    }

    public static void click() {
        UiSound.pop(1.65f);
    }

    public static void open() {
        UiSound.pop(1.35f);
    }

    public static void close() {
        UiSound.pop(1.1f);
    }
}

