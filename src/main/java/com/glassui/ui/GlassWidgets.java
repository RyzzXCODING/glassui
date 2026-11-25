/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.ui.Anim;
import com.glassui.ui.CustomGlassWidget;
import com.glassui.ui.Fx;
import com.glassui.ui.Glass;
import com.glassui.ui.IconShapes;
import com.glassui.ui.Icons;
import com.glassui.ui.Spring;
import java.util.function.BooleanSupplier;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;

public final class GlassWidgets {
    private GlassWidgets() {
    }

    private static class_327 tr() {
        return class_310.method_1551().field_1772;
    }

    private static int col(float a, int rgb) {
        return Glass.c((int)(255.0f * Math.max(0.0f, Math.min(1.0f, a))), rgb);
    }

    public static class_342 field(class_327 font, int x, int y, int w, int h, String text, String hint, int max) {
        class_342 f = new class_342(font, x, y, w, h, (class_2561)class_2561.method_43470((String)hint));
        f.method_1880(max);
        f.method_1852(text == null ? "" : text);
        f.method_47404((class_2561)class_2561.method_43470((String)hint));
        f.method_25350(0.0f);
        return f;
    }

    public static void drawFieldBack(class_332 ctx, class_342 f) {
        Glass.inset(ctx, f.method_46426(), f.method_46427(), f.method_25368(), f.method_25364(), 7, f.method_25370(), 1.0f);
    }

    public static void drawMasked(class_332 ctx, class_327 font, class_342 f) {
        GlassWidgets.drawFieldBack(ctx, f);
        String dots = "*".repeat(Math.min(28, f.method_1882().length()));
        Fx.draw(ctx, font, dots, f.method_46426() + 6, f.method_46427() + (f.method_25364() - 8) / 2, -2564888, false);
    }

    public static final class Tab
    extends class_4185
    implements CustomGlassWidget {
        private final class_2561 label;
        private final IconShapes.Shape icon;
        private final BooleanSupplier selected;
        private final Spring sel;
        private final Spring hov = new Spring(0.0f, 0.0f, 300.0f, 24.0f);
        private long last = System.nanoTime();

        public Tab(int x, int y, int w, int h, class_2561 label, IconShapes.Shape icon, BooleanSupplier selected, Runnable action) {
            super(x, y, w, h, label, b -> action.run(), field_40754);
            this.label = label;
            this.icon = icon;
            this.selected = selected;
            this.sel = new Spring(selected.getAsBoolean() ? 1.0f : 0.0f, selected.getAsBoolean() ? 1.0f : 0.0f, 280.0f, 24.0f);
        }

        public void method_75752(class_332 ctx, int mx, int my, float dt) {
        }

        @Override
        public void renderGlass(class_332 ctx, int mx, int my, float dt) {
            long now = System.nanoTime();
            float d = Anim.smooth((float)(now - this.last) / 1.0E9f);
            this.last = now;
            this.sel.target = this.selected.getAsBoolean() ? 1.0f : 0.0f;
            this.hov.target = this.method_49606() ? 1.0f : 0.0f;
            this.sel.step(d);
            this.hov.step(d);
            float k = Math.max(0.0f, Math.min(1.0f, this.sel.pos));
            float hv = Math.max(0.0f, Math.min(1.0f, this.hov.pos));
            int x = this.method_46426();
            int y = this.method_46427();
            int w = this.method_25368();
            int h = this.method_25364();
            int r = h / 2;
            if (k > 0.02f) {
                Glass.layer(ctx, x, y, w, h, r, Glass.c((int)(88.0f * k), 0xFFFFFF), Glass.c((int)(40.0f * k), 0xFFFFFF));
                Glass.layer(ctx, (float)x + 1.5f, y + 1, w - 3, (float)h * 0.45f, Math.max(1.0f, (float)r - 1.5f), Glass.c((int)(68.0f * k), 0xFFFFFF), Glass.c((int)(4.0f * k), 0xFFFFFF));
                Glass.ring(ctx, x, y, w, h, r, 0.5f, Glass.c((int)(200.0f * k), 0xFFFFFF), Glass.c((int)(80.0f * k), 0xFFFFFF));
            } else if (hv > 0.02f) {
                Glass.layer(ctx, x, y, w, h, r, Glass.c((int)(22.0f * hv), 0xFFFFFF), Glass.c((int)(8.0f * hv), 0xFFFFFF));
            }
            int a = (int)(170.0f + 85.0f * Math.max(k, hv * 0.6f));
            int tw = Fx.w(GlassWidgets.tr(), this.label);
            int total = 15 + tw;
            int sx = x + (w - total) / 2;
            Icons.draw(ctx, this.icon, sx, (float)y + (float)(h - 11) / 2.0f, 11.0f, 0xFFFFFF, a);
            Fx.draw(ctx, GlassWidgets.tr(), this.label, sx + 15, y + (h - 8) / 2, Glass.c(a, 0xFFFFFF), k > 0.5f);
        }
    }

    public static final class Chip
    extends class_4185
    implements CustomGlassWidget {
        private class_2561 label;
        private final BooleanSupplier selected;
        private final int accent;
        private final IconShapes.Shape icon;
        private final Spring hov = new Spring(0.0f, 0.0f, 300.0f, 24.0f);
        private final Spring sel;
        private long last = System.nanoTime();

        public Chip(int x, int y, int w, int h, class_2561 label, IconShapes.Shape icon, int accent, BooleanSupplier selected, Runnable action) {
            super(x, y, w, h, label, b -> action.run(), field_40754);
            this.label = label;
            this.icon = icon;
            this.accent = accent;
            this.selected = selected;
            this.sel = new Spring(selected.getAsBoolean() ? 1.0f : 0.0f, selected.getAsBoolean() ? 1.0f : 0.0f, 300.0f, 24.0f);
        }

        public void setLabel(class_2561 t) {
            this.label = t;
            this.method_25355(t);
        }

        public void method_75752(class_332 ctx, int mx, int my, float dt) {
        }

        @Override
        public void renderGlass(class_332 ctx, int mx, int my, float dt) {
            int tc;
            long now = System.nanoTime();
            float d = Anim.smooth((float)(now - this.last) / 1.0E9f);
            this.last = now;
            boolean s = this.selected.getAsBoolean();
            this.sel.target = s ? 1.0f : 0.0f;
            this.hov.target = this.method_49606() && this.field_22763 ? 1.0f : 0.0f;
            this.sel.step(d);
            this.hov.step(d);
            float k = Math.max(0.0f, Math.min(1.0f, this.sel.pos));
            float hv = Math.max(0.0f, Math.min(1.0f, this.hov.pos));
            int x = this.method_46426();
            int y = this.method_46427();
            int w = this.method_25368();
            int h = this.method_25364();
            int r = Math.min(10, h / 2);
            if (k > 0.02f) {
                int top = Glass.lerp(0xFF000000 | this.accent, -1, 0.22f);
                int bot = Glass.lerp(0xFF000000 | this.accent, -16777216, 0.3f);
                Glass.layer(ctx, x, y, w, h, r, Glass.c((int)(216.0f * k), top), Glass.c((int)(216.0f * k), bot));
                Glass.layer(ctx, (float)x + 1.5f, y + 1, w - 3, (float)h * 0.45f, Math.max(1.0f, (float)r - 1.5f), Glass.c((int)(80.0f * k), 0xFFFFFF), Glass.c((int)(4.0f * k), 0xFFFFFF));
                Glass.ring(ctx, x, y, w, h, r, 0.5f, Glass.c((int)(192.0f * k), 0xFFFFFF), Glass.c((int)(64.0f * k), 0xFFFFFF));
            }
            if (k < 0.98f) {
                Glass.button(ctx, x, y, w, h, r, 0xFFFFFF, (0.55f + 0.9f * hv) * (1.0f - k), 1.0f - k);
            }
            int tw = Fx.w(GlassWidgets.tr(), this.label);
            int iw = this.icon == null ? 0 : 11;
            int gap = this.icon == null ? 0 : 5;
            int sx = x + (w - (iw + gap + tw)) / 2;
            int n = tc = this.field_22763 ? -1 : -6250328;
            if (this.icon != null) {
                Icons.drawShadowed(ctx, this.icon, sx, (float)y + (float)(h - 11) / 2.0f, 11.0f, 0xFFFFFF, this.field_22763 ? 255 : 150);
            }
            Fx.draw(ctx, GlassWidgets.tr(), this.label, sx + iw + gap, y + (h - 8) / 2, tc, true);
        }
    }

    public static final class Toggle
    extends class_4185
    implements CustomGlassWidget {
        private final class_2561 label;
        private final BooleanSupplier get;
        private final Spring knob;
        private final Spring hov = new Spring(0.0f, 0.0f, 300.0f, 24.0f);
        private long last = System.nanoTime();

        public Toggle(int x, int y, int w, int h, class_2561 label, BooleanSupplier get, Runnable flip) {
            super(x, y, w, h, label, b -> flip.run(), field_40754);
            this.label = label;
            this.get = get;
            this.knob = new Spring(get.getAsBoolean() ? 1.0f : 0.0f, get.getAsBoolean() ? 1.0f : 0.0f, 360.0f, 27.0f);
        }

        public void method_75752(class_332 ctx, int mx, int my, float dt) {
        }

        @Override
        public void renderGlass(class_332 ctx, int mx, int my, float dt) {
            long now = System.nanoTime();
            float d = Anim.smooth((float)(now - this.last) / 1.0E9f);
            this.last = now;
            boolean on = this.get.getAsBoolean();
            this.knob.target = on ? 1.0f : 0.0f;
            this.hov.target = this.method_49606() ? 1.0f : 0.0f;
            this.knob.step(d);
            this.hov.step(d);
            int x = this.method_46426();
            int y = this.method_46427();
            int w = this.method_25368();
            int h = this.method_25364();
            float hv = Math.max(0.0f, Math.min(1.0f, this.hov.pos));
            if (hv > 0.01f) {
                Glass.layer(ctx, x + 2, y + 1, w - 4, h - 2, 8.0f, Glass.c((int)(28.0f * hv), 0xFFFFFF), Glass.c((int)(12.0f * hv), 0xFFFFFF));
            }
            Fx.draw(ctx, GlassWidgets.tr(), this.label, x + 8, y + (h - 8) / 2, -1, true);
            int tw = 30;
            int th = 16;
            int tx = x + w - 8 - tw;
            int ty = y + (h - th) / 2;
            float k = Math.max(0.0f, Math.min(1.0f, this.knob.pos));
            int offTop = Glass.c(112, 9080736);
            int offBot = Glass.c(80, 6975360);
            int onTop = Glass.c(232, 5038190);
            int onBot = Glass.c(232, 2078792);
            Glass.layer(ctx, tx, ty, tw, th, (float)th / 2.0f, Glass.lerp(offTop, onTop, k), Glass.lerp(offBot, onBot, k));
            Glass.ring(ctx, tx, ty, tw, th, (float)th / 2.0f, 0.5f, Glass.c(96, 0xFFFFFF), Glass.c(144, 0xFFFFFF));
            float kd = th - 4;
            float kx = (float)(tx + 2) + k * ((float)(tw - 4) - kd);
            float ky = ty + 2;
            Glass.layer(ctx, kx - 0.5f, ky + 0.5f, kd + 1.0f, kd + 1.0f, (kd + 1.0f) / 2.0f, Glass.c(48, 0), Glass.c(48, 0));
            Glass.fill(ctx, kx, ky, kd, kd, kd / 2.0f, -1, -1775374);
        }
    }
}

