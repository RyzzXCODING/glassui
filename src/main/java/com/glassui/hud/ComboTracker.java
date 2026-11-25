/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassConfig;
import com.glassui.ui.Spring;

public final class ComboTracker {
    private static final float FADE = 0.4f;
    private static final Spring SLIDE = new Spring(0.0f, 0.0f, 330.0f, 26.0f);
    private static final Spring POP = new Spring(1.0f, 1.0f, 380.0f, 22.0f);
    private static int count;
    private static long lastNs;
    private static float appear;
    private static float out;
    private static float left;

    private ComboTracker() {
    }

    private static float elapsed() {
        return (float)(System.nanoTime() - lastNs) / 1.0E9f;
    }

    private static float dur() {
        return Math.max(1, GlassConfig.get().comboSec);
    }

    public static boolean active() {
        return count > 0 && ComboTracker.elapsed() < ComboTracker.dur() + 0.4f;
    }

    public static void reset() {
        count = 0;
    }

    static void hit() {
        boolean fresh = !ComboTracker.active();
        count = fresh ? 1 : count + 1;
        lastNs = System.nanoTime();
        ComboTracker.SLIDE.pos = 14.0f;
        ComboTracker.SLIDE.vel = 0.0f;
        ComboTracker.SLIDE.target = 0.0f;
        ComboTracker.POP.pos = fresh ? 0.7f : 0.84f;
        ComboTracker.POP.vel = 0.0f;
        ComboTracker.POP.target = 1.0f;
        if (fresh) {
            appear = 0.0f;
        }
    }

    public static void step(float dt) {
        if (count <= 0) {
            return;
        }
        if (!ComboTracker.active()) {
            count = 0;
            return;
        }
        SLIDE.step(dt);
        POP.step(dt);
        appear += (1.0f - appear) * Math.min(1.0f, dt * 14.0f);
        float e = ComboTracker.elapsed();
        float d = ComboTracker.dur();
        out = e > d ? Math.max(0.0f, 1.0f - (e - d) / 0.4f) : 1.0f;
        left = Math.max(0.0f, Math.min(1.0f, 1.0f - e / d));
    }

    public static int count() {
        return count;
    }

    public static float alpha() {
        return appear * out;
    }

    public static float slide() {
        return ComboTracker.SLIDE.pos - (1.0f - out) * 10.0f;
    }

    public static float squash() {
        return POP.squash();
    }

    public static float scale() {
        return ComboTracker.POP.pos * (0.92f + 0.08f * out);
    }

    public static float left() {
        return left;
    }

    static {
        out = 1.0f;
        left = 1.0f;
    }
}

