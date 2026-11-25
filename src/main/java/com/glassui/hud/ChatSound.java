/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassConfig;
import com.glassui.hud.StreamHud;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public final class ChatSound {
    public static final String[] NAMES = new String[]{"Pop", "Plop", "Ding"};
    private static final long MIN_GAP_MS = 140L;
    private static long seen;
    private static long lastMs;

    private ChatSound() {
    }

    public static void reset() {
        seen = 0L;
        lastMs = 0L;
    }

    public static void tick() {
        long c = StreamHud.DATA.chatCount;
        if (c == seen) {
            return;
        }
        boolean fresh = c > seen;
        seen = c;
        GlassConfig cfg = GlassConfig.get();
        if (!(fresh && cfg.chatSound && cfg.hudEnabled && cfg.chat.on)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastMs < 140L) {
            return;
        }
        lastMs = now;
        ChatSound.play(cfg.chatSoundType, cfg.chatSoundVol);
    }

    public static void play(int type, int volPct) {
        class_3414 ev;
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null || volPct <= 0) {
            return;
        }
        switch (type) {
            case 1: {
                ev = class_3417.field_15219;
                float pitch = 1.5f;
                break;
            }
            case 2: {
                ev = class_3417.field_14627;
                float pitch = 1.35f;
                break;
            }
            default: {
                ev = class_3417.field_15197;
                float pitch = 1.7f;
            }
        }
        mc.field_1724.method_5783(ev, Math.min(1.0f, (float)volPct / 100.0f), pitch += (float)(Math.random() - 0.5) * 0.12f);
    }
}

