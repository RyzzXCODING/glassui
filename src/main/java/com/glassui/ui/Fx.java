/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.GlassConfig;
import com.glassui.ui.FontStyle;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_5348;
import net.minecraft.class_5481;

public final class Fx {
    public static final String[] FONT_NAMES = new String[]{"Default", "Poppins", "Inter", "DejaVu Sans", "DejaVu Mono", "DejaVu Serif"};
    private static final String[] IDS = new String[]{null, "poppins", "inter", "dejavu_sans", "dejavu_mono", "dejavu_serif"};
    private static final class_2583[] CACHE = new class_2583[IDS.length];

    private Fx() {
    }

    private static class_2583 style() {
        int i = GlassConfig.get().font;
        if (i <= 0 || i >= IDS.length) {
            return null;
        }
        class_2583 s = CACHE[i];
        if (s == null) {
            Fx.CACHE[i] = s = FontStyle.of(IDS[i]);
        }
        return s;
    }

    private static class_2561 wrap(String s, class_2583 st) {
        return class_2561.method_43470((String)s).method_10862(st);
    }

    private static class_2561 wrap(class_2561 t, class_2583 st) {
        class_5250 w = class_2561.method_43473().method_10862(st);
        w.method_10852(t);
        return w;
    }

    public static void draw(class_332 ctx, class_327 tr, String s, int x, int y, int color, boolean shadow) {
        class_2583 st = Fx.style();
        if (st == null) {
            ctx.method_51433(tr, s, x, y, color, shadow);
        } else {
            ctx.method_51439(tr, Fx.wrap(s, st), x, y, color, shadow);
        }
    }

    public static void draw(class_332 ctx, class_327 tr, class_2561 t, int x, int y, int color, boolean shadow) {
        class_2583 st = Fx.style();
        ctx.method_51439(tr, st == null ? t : Fx.wrap(t, st), x, y, color, shadow);
    }

    public static void draw(class_332 ctx, class_327 tr, class_5481 t, int x, int y, int color, boolean shadow) {
        ctx.method_51430(tr, t, x, y, color, shadow);
    }

    public static int w(class_327 tr, String s) {
        class_2583 st = Fx.style();
        return st == null ? tr.method_1727(s) : tr.method_27525((class_5348)Fx.wrap(s, st));
    }

    public static int w(class_327 tr, class_2561 t) {
        class_2583 st = Fx.style();
        return tr.method_27525((class_5348)(st == null ? t : Fx.wrap(t, st)));
    }

    public static int w(class_327 tr, class_5481 t) {
        return tr.method_30880(t);
    }

    public static String trim(class_327 tr, String s, int maxW) {
        if (Fx.style() == null) {
            return tr.method_27523(s, maxW);
        }
        if (Fx.w(tr, s) <= maxW) {
            return s;
        }
        int lo = 0;
        int hi = s.length();
        while (lo < hi) {
            int mid = lo + hi + 1 >>> 1;
            if (Fx.w(tr, s.substring(0, mid)) <= maxW) {
                lo = mid;
                continue;
            }
            hi = mid - 1;
        }
        return s.substring(0, lo);
    }
}

