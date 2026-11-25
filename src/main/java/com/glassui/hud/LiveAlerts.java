/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassConfig;
import com.glassui.ui.Spring;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public final class LiveAlerts {
    public static final Alert SAMPLE = new Alert(Kind.GIFT, "viewer123", "Rose", 5, 5L, 0L);
    private static final float HOLD = 3.6f;
    private static final float HOLD_FAST = 2.0f;
    private static final float FADE = 0.35f;
    private static final long STALE_MS = 60000L;
    private static final ConcurrentLinkedQueue<Alert> Q = new ConcurrentLinkedQueue();
    private static final Spring SLIDE = new Spring(0.0f, 0.0f, 330.0f, 26.0f);
    private static final Spring POP = new Spring(1.0f, 1.0f, 380.0f, 22.0f);
    private static Alert cur;
    private static long startNs;
    private static float appear;
    private static float out;

    private LiveAlerts() {
    }

    public static void push(Alert a) {
        GlassConfig c = GlassConfig.get();
        if (!c.hudEnabled || !c.alert.on) {
            return;
        }
        if (a.kind() == Kind.GIFT && !c.alertGift) {
            return;
        }
        if (a.kind() == Kind.FOLLOW && !c.alertFollow) {
            return;
        }
        while (Q.size() >= 25) {
            Q.poll();
        }
        Q.add(a);
    }

    public static boolean active() {
        return cur != null || !Q.isEmpty();
    }

    public static void clear() {
        Q.clear();
        cur = null;
    }

    public static Alert current() {
        return cur;
    }

    public static float alpha() {
        return appear * out;
    }

    public static float slide() {
        return LiveAlerts.SLIDE.pos - (1.0f - out) * 8.0f;
    }

    public static float scale() {
        return LiveAlerts.POP.pos * (0.94f + 0.06f * out);
    }

    public static float squash() {
        return POP.squash();
    }

    private static float hold() {
        return Q.size() > 3 ? 2.0f : 3.6f;
    }

    public static void step(float dt) {
        long now = System.nanoTime();
        if (cur != null && (float)(now - startNs) / 1.0E9f > LiveAlerts.hold() + 0.35f) {
            cur = null;
        }
        while (cur == null) {
            Alert n = Q.poll();
            if (n == null) {
                return;
            }
            if (System.currentTimeMillis() - n.at() > 60000L) continue;
            cur = n;
            startNs = now;
            appear = 0.0f;
            out = 1.0f;
            LiveAlerts.SLIDE.pos = 16.0f;
            LiveAlerts.SLIDE.vel = 0.0f;
            LiveAlerts.SLIDE.target = 0.0f;
            LiveAlerts.POP.pos = 0.72f;
            LiveAlerts.POP.vel = 0.0f;
            LiveAlerts.POP.target = 1.0f;
            LiveAlerts.sound(n);
        }
        SLIDE.step(dt);
        POP.step(dt);
        appear += (1.0f - appear) * Math.min(1.0f, dt * 14.0f);
        float e = (float)(now - startNs) / 1.0E9f;
        float h = LiveAlerts.hold();
        out = e > h ? Math.max(0.0f, 1.0f - (e - h) / 0.35f) : 1.0f;
    }

    private static void sound(Alert a) {
        if (!GlassConfig.get().alertSound) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1724 == null) {
            return;
        }
        class_3414 ev = a.kind() == Kind.GIFT ? class_3417.field_14709 : class_3417.field_14627;
        mc.field_1724.method_5783(ev, 0.6f, a.kind() == Kind.GIFT ? 1.2f : 1.5f);
    }

    static {
        out = 1.0f;
    }

    public record Alert(Kind kind, String user, String gift, int count, long diamonds, long at) {
    }

    public static enum Kind {
        GIFT,
        FOLLOW;

    }
}

