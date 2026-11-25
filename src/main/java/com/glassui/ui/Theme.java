/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.GlassConfig;

public final class Theme {
    public static final T[] ALL = new T[]{new T("Clear", 0xFFFFFF, 790040, 1.0f, 0xFFFFFF), new T("Midnight", 5009407, 395806, 1.6f, 12110079), new T("Sakura", 16743856, 1969172, 1.6f, 16765156), new T("Emerald", 3069050, 399888, 1.6f, 12910560), new T("Sunset", 16747069, 1969670, 1.6f, 0xFFDDBB), new T("Violet", 10906623, 1182244, 1.6f, 14732543), new T("Mono", 0x9A9AA8, 0, 0.7f, 0xFFFFFF)};

    private Theme() {
    }

    public static T cur() {
        int i = GlassConfig.get().theme;
        return ALL[Math.max(0, Math.min(ALL.length - 1, i))];
    }

    public static boolean isDefault() {
        return GlassConfig.get().theme <= 0;
    }

    public record T(String name, int tint, int base, float strength, int rim) {
    }
}

