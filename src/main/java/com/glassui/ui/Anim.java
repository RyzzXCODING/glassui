/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

public final class Anim {
    private static float typ = 0.016666668f;

    private Anim() {
    }

    public static float smooth(float raw) {
        if (raw <= 5.0E-4f) {
            return Math.max(0.0f, raw);
        }
        if (raw > 0.1f) {
            raw = 0.1f;
        }
        typ += (raw - typ) * 0.05f;
        return Math.min(raw, typ * 3.0f) * 0.35f + typ * 0.65f;
    }
}

