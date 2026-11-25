/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassConfig;
import com.glassui.ui.Anim;
import com.glassui.ui.Glass;
import com.glassui.ui.Spring;
import net.minecraft.class_1306;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_746;

public final class GlassHotbar {
    private static final int W = 182;
    private static final int H = 22;
    private static final int SLOT = 20;
    private static final Spring SEL = new Spring(0.0f, 0.0f, 430.0f, 30.0f);
    private static boolean init;
    private static long lastNs;

    private GlassHotbar() {
    }

    public static boolean render(class_332 ctx) {
        class_310 mc = class_310.method_1551();
        class_746 p = mc.field_1724;
        if (p == null || p.method_7325() || !GlassConfig.get().glassHotbar) {
            init = false;
            return false;
        }
        long now = System.nanoTime();
        float dt = lastNs == 0L ? 0.016f : Anim.smooth((float)(now - lastNs) / 1.0E9f);
        lastNs = now;
        class_1661 inv = p.method_31548();
        int sel = inv.method_67532();
        if (!init) {
            GlassHotbar.SEL.pos = sel;
            GlassHotbar.SEL.vel = 0.0f;
            init = true;
        }
        GlassHotbar.SEL.target = sel;
        SEL.step(dt);
        int sw = ctx.method_51421();
        int sh = ctx.method_51443();
        int x0 = sw / 2 - 91;
        int y0 = sh - 22 - 2;
        Glass.buttonLite(ctx, x0, y0, 182, 22, 11, 0xFFFFFF, 1.0f, 1.0f);
        float lx = (float)(x0 + 1) + GlassHotbar.SEL.pos * 20.0f;
        Glass.layer(ctx, lx, y0 + 1, 20.0f, 20.0f, 10.0f, Glass.c(112, 0xFFFFFF), Glass.c(52, 0xFFFFFF));
        Glass.layer(ctx, lx + 1.5f, (float)y0 + 1.8f, 17.0f, 8.0f, 4.0f, Glass.c(102, 0xFFFFFF), Glass.c(4, 0xFFFFFF));
        Glass.ringHard(ctx, lx, y0 + 1, 20.0f, 20.0f, 10.0f, 0.5f, Glass.c(240, 0xFFFFFF), Glass.c(112, 0xFFFFFF));
        class_327 tr = mc.field_1772;
        for (int i = 0; i < 9; ++i) {
            class_1799 st = inv.method_5438(i);
            if (st.method_7960()) continue;
            int ix = x0 + 1 + i * 20 + 2;
            int iy = y0 + 3;
            ctx.method_51427(st, ix, iy);
            ctx.method_51431(tr, st, ix, iy);
        }
        class_1799 off = p.method_6079();
        if (!off.method_7960()) {
            int ox = p.method_6068() == class_1306.field_6183 ? x0 - 32 : x0 + 182 + 10;
            Glass.buttonLite(ctx, ox, y0, 22, 22, 11, 0xFFFFFF, 1.0f, 1.0f);
            ctx.method_51427(off, ox + 3, y0 + 3);
            ctx.method_51431(tr, off, ox + 3, y0 + 3);
        }
        return true;
    }
}

