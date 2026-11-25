/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.ui.Anim;
import com.glassui.ui.Fx;
import com.glassui.ui.Glass;
import com.glassui.ui.Spring;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_339;
import org.joml.Matrix3x2fStack;

public final class GlassButtonRenderer {
    private static final Map<class_339, State> STATES = new WeakHashMap<class_339, State>();

    public static void render(class_332 ctx, class_339 w) {
        State s = STATES.computeIfAbsent(w, k -> new State());
        long now = System.nanoTime();
        float dt = Anim.smooth((float)(now - s.last) / 1.0E9f);
        s.last = now;
        boolean hovered = (w.method_49606() || w.method_25370()) && w.field_22763;
        s.hov.target = hovered ? 1.0f : 0.0f;
        s.pop.step(dt);
        s.hov.step(dt);
        float alpha = Math.max(0.0f, Math.min(1.0f, (s.pop.pos - 0.9f) / 0.1f)) * w.method_75798();
        float sc = s.pop.pos;
        int x = w.method_46426();
        int y = w.method_46427();
        int wd = w.method_25368();
        int h = w.method_25364();
        float cx = (float)x + (float)wd / 2.0f;
        float cy = (float)y + (float)h / 2.0f;
        float hv = Math.max(0.0f, Math.min(1.2f, s.hov.pos));
        Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        if (Math.abs(sc - 1.0f) > 0.002f) {
            m.translate(cx, cy);
            float sq = s.pop.squash();
            m.scale(sc * (1.0f - sq * 0.6f), sc * (1.0f + sq));
            m.translate(-cx, -cy);
        }
        float strength = !w.field_22763 ? 0.45f : 0.9f + 0.7f * hv;
        int r = Math.min(10, h / 2);
        Glass.button(ctx, x, y, wd, h, r, 0xFFFFFF, strength, alpha);
        if (w.method_25370()) {
            Glass.ring(ctx, x - 1, y - 1, wd + 2, h + 2, r + 1, 0.7f, Glass.c((int)(255.0f * alpha), 0xFFFFFF), Glass.c((int)(144.0f * alpha), 0xFFFFFF));
        }
        class_327 tr = class_310.method_1551().field_1772;
        int tw = Fx.w(tr, w.method_25369());
        int col = Glass.c(Math.max(64, (int)(255.0f * alpha)), w.field_22763 ? 0xFFFFFF : 0xA0A0A0);
        Fx.draw(ctx, tr, w.method_25369(), x + (wd - tw) / 2, y + (h - 8) / 2, col, true);
        m.popMatrix();
    }

    private static final class State {
        final Spring pop = new Spring(0.92f, 1.0f, 260.0f, 24.0f);
        final Spring hov = new Spring(0.0f, 0.0f, 300.0f, 24.0f);
        long last = System.nanoTime();

        private State() {
        }
    }
}

