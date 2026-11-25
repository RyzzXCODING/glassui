/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.GlassConfig;
import com.glassui.ui.Raster;
import com.glassui.ui.Theme;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

public final class Glass {
    private static class_332 curCtx;
    private static final Raster.Sink SINK;

    private Glass() {
    }

    public static int c(int a, int rgb) {
        return Math.max(0, Math.min(255, a)) << 24 | rgb & 0xFFFFFF;
    }

    private static int mix(int a, int b, float t) {
        return Math.round((float)a + (float)(b - a) * t);
    }

    public static int lerp(int c1, int c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return Glass.mix(c1 >>> 24, c2 >>> 24, t) << 24 | Glass.mix(c1 >> 16 & 0xFF, c2 >> 16 & 0xFF, t) << 16 | Glass.mix(c1 >> 8 & 0xFF, c2 >> 8 & 0xFF, t) << 8 | Glass.mix(c1 & 0xFF, c2 & 0xFF, t);
    }

    public static float sf() {
        class_310 mc = class_310.method_1551();
        float f = mc == null || mc.method_22683() == null ? 1.0f : Math.max(1.0f, (float)mc.method_22683().method_4495());
        return GlassConfig.get().perfMode ? Math.min(f, 2.0f) : f;
    }

    private static int phys(float v, float sf) {
        return Math.round(v * sf);
    }

    private static void draw(class_332 ctx, float x, float y, float w, float h, float r, float thick, int top, int bot) {
        Glass.draw(ctx, x, y, w, h, r, thick, top, bot, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void draw(class_332 ctx, float x, float y, float w, float h, float r, float thick, int top, int bot, boolean hard) {
        float sf = Glass.sf();
        int px = Glass.phys(x, sf);
        int py = Glass.phys(y, sf);
        int pw = Math.max(1, Glass.phys(x + w, sf) - px);
        int ph = Math.max(1, Glass.phys(y + h, sf) - py);
        int pr = Math.max(0, Glass.phys(r, sf));
        int pt = thick <= 0.0f ? 0 : Math.max(1, Math.round(thick * sf));
        Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        m.scale(1.0f / sf, 1.0f / sf);
        curCtx = ctx;
        try {
            Raster.roundRect(SINK, px, py, pw, ph, pr, pt, top, bot, hard);
        }
        finally {
            curCtx = null;
            m.popMatrix();
        }
    }

    public static void roundedFill(class_332 ctx, int x, int y, int w, int h, int r, int color) {
        Glass.draw(ctx, x, y, w, h, r, 0.0f, color, color);
    }

    public static void roundedGradient(class_332 ctx, int x, int y, int w, int h, int r, int top, int bot) {
        Glass.draw(ctx, x, y, w, h, r, 0.0f, top, bot);
    }

    public static void rim(class_332 ctx, int x, int y, int w, int h, int r, int top, int bot) {
        Glass.draw(ctx, x, y, w, h, r, 0.5f, top, bot, GlassConfig.get().perfMode);
    }

    public static void fill(class_332 ctx, float x, float y, float w, float h, float r, int top, int bot) {
        Glass.draw(ctx, x, y, w, h, r, 0.0f, top, bot);
    }

    public static void layer(class_332 ctx, float x, float y, float w, float h, float r, int top, int bot) {
        Glass.draw(ctx, x, y, w, h, r, 0.0f, top, bot, true);
    }

    public static void ring(class_332 ctx, float x, float y, float w, float h, float r, float thick, int top, int bot) {
        Glass.draw(ctx, x, y, w, h, r, thick, top, bot, GlassConfig.get().perfMode);
    }

    public static void ringHard(class_332 ctx, float x, float y, float w, float h, float r, float thick, int top, int bot) {
        Glass.draw(ctx, x, y, w, h, r, thick, top, bot, true);
    }

    public static void buttonLite(class_332 ctx, int x, int y, int w, int h, int r, int tint, float strength, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        Glass.body(ctx, x, y, w, h, r, tint, strength, alpha);
        Glass.highlight(ctx, x, y, w, h, r, alpha, 52);
        Glass.ringHard(ctx, x, y, w, h, r, 0.5f, Glass.c((int)(176.0f * alpha), Theme.cur().rim()), Glass.c((int)(56.0f * alpha), Theme.cur().rim()));
    }

    public static int over(int src, int dst) {
        float sa = (float)(src >>> 24) / 255.0f;
        float da = (float)(dst >>> 24) / 255.0f;
        float oa = sa + da * (1.0f - sa);
        if (oa <= 0.0f) {
            return 0;
        }
        int r = Math.round(((float)(src >> 16 & 0xFF) * sa + (float)(dst >> 16 & 0xFF) * da * (1.0f - sa)) / oa);
        int g = Math.round(((float)(src >> 8 & 0xFF) * sa + (float)(dst >> 8 & 0xFF) * da * (1.0f - sa)) / oa);
        int b = Math.round(((float)(src & 0xFF) * sa + (float)(dst & 0xFF) * da * (1.0f - sa)) / oa);
        return Math.round(oa * 255.0f) << 24 | r << 16 | g << 8 | b;
    }

    public static void panel(class_332 ctx, int x, int y, int w, int h, int r, int tint, float strength, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        boolean lite = GlassConfig.get().perfMode;
        if (!lite) {
            for (int i = 3; i >= 1; --i) {
                int sh = Glass.c((int)(7.0f * alpha), 0);
                Glass.draw(ctx, (float)x - (float)i * 2.0f, (float)y - (float)i * 2.0f + 3.0f, (float)w + (float)i * 4.0f, (float)h + (float)i * 4.0f, (float)r + (float)i * 2.0f, 0.0f, sh, sh);
            }
        }
        Glass.body(ctx, x, y, w, h, r, tint, strength, alpha);
        if (!lite) {
            Glass.draw(ctx, (float)x + 0.5f, (float)y + 0.5f, w - 1, h - 1, (float)r - 0.5f, 2.2f, Glass.c(0, 0xFFFFFF), Glass.c((int)(44.0f * alpha), 12572927));
        }
        Glass.highlight(ctx, x, y, w, h, r, alpha, 58);
        Glass.rim(ctx, x, y, w, h, r, Glass.c((int)(184.0f * alpha), Theme.cur().rim()), Glass.c((int)(64.0f * alpha), Theme.cur().rim()));
    }

    public static void button(class_332 ctx, int x, int y, int w, int h, int r, int tint, float strength, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        Glass.body(ctx, x, y, w, h, r, tint, strength, alpha);
        Glass.highlight(ctx, x, y, w, h, r, alpha, 52);
        Glass.rim(ctx, x, y, w, h, r, Glass.c((int)(176.0f * alpha), Theme.cur().rim()), Glass.c((int)(56.0f * alpha), Theme.cur().rim()));
    }

    private static void body(class_332 ctx, int x, int y, int w, int h, int r, int tint, float strength, float alpha) {
        Theme.T th = Theme.cur();
        if ((tint & 0xFFFFFF) == 0xFFFFFF) {
            tint = th.tint();
            strength *= th.strength();
        }
        int dTop = Glass.c((int)(64.0f * alpha), th.base());
        int dBot = Glass.c((int)(74.0f * alpha), th.base());
        int tTop = Glass.c((int)(64.0f * strength * alpha), tint);
        int tBot = Glass.c((int)(26.0f * strength * alpha), tint);
        Glass.draw(ctx, x, y, w, h, r, 0.0f, Glass.over(tTop, dTop), Glass.over(tBot, dBot), true);
    }

    private static void highlight(class_332 ctx, int x, int y, int w, int h, int r, float alpha, int a0) {
        float hh = Math.max(5.0f, (float)h * 0.44f);
        Glass.draw(ctx, x + 2, (float)y + 1.2f, w - 4, hh, Math.max(1, r - 2), 0.0f, Glass.c((int)((float)a0 * alpha), 0xFFFFFF), Glass.c((int)(2.0f * alpha), 0xFFFFFF), true);
    }

    public static void inset(class_332 ctx, int x, int y, int w, int h, int r, boolean focus, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        Glass.draw(ctx, x, y, w, h, r, 0.0f, Glass.c((int)(112.0f * alpha), 329228), Glass.c((int)(72.0f * alpha), 329228), true);
        if (!GlassConfig.get().perfMode) {
            Glass.draw(ctx, x, y, w, (float)h * 0.5f, r, 0.0f, Glass.c((int)(34.0f * alpha), 0), Glass.c(0, 0), true);
        }
        if (focus) {
            Glass.draw(ctx, (float)x - 1.5f, (float)y - 1.5f, w + 3, h + 3, (float)r + 1.5f, 1.2f, Glass.c((int)(80.0f * alpha), 6596863), Glass.c((int)(48.0f * alpha), 6596863));
            Glass.rim(ctx, x, y, w, h, r, Glass.c((int)(224.0f * alpha), 12310783), Glass.c((int)(160.0f * alpha), 8435967));
        } else {
            Glass.rim(ctx, x, y, w, h, r, Glass.c((int)(64.0f * alpha), 0xFFFFFF), Glass.c((int)(112.0f * alpha), 0xFFFFFF));
        }
    }

    static {
        SINK = (x0, y0, x1, y1, t, b) -> {
            class_332 c = curCtx;
            if (t == b) {
                c.method_25294(x0, y0, x1, y1, t);
            } else {
                c.method_25296(x0, y0, x1, y1, t, b);
            }
        };
    }
}

