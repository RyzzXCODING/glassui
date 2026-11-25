/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.ui.Glass;
import com.glassui.ui.IconShapes;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;

public final class Icons {
    private Icons() {
    }

    public static void draw(class_332 ctx, IconShapes.Shape shape, float x, float y, float size, int rgb, int alpha) {
        Icons.draw(ctx, shape, x, y, size, rgb, alpha, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void draw(class_332 ctx, IconShapes.Shape shape, float x, float y, float size, int rgb, int alpha, boolean binary) {
        if (alpha <= 2) {
            return;
        }
        float sf = Glass.sf();
        int n = Math.max(6, Math.round(size * sf));
        int[][] ops = IconShapes.ops(shape, n, binary);
        int px = Math.round(x * sf);
        int py = Math.round(y * sf);
        Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        m.scale(1.0f / sf, 1.0f / sf);
        try {
            for (int[] o : ops) {
                ctx.method_25294(px + o[0], py + o[1], px + o[2], py + o[3], Glass.c(alpha * o[4] / 16, rgb));
            }
        }
        finally {
            m.popMatrix();
        }
    }

    public static void draw(class_332 ctx, IconShapes.Shape shape, float x, float y, float size, int rgb) {
        Icons.draw(ctx, shape, x, y, size, rgb, 255);
    }

    public static void drawShadowed(class_332 ctx, IconShapes.Shape shape, float x, float y, float size, int rgb, int alpha) {
        Icons.draw(ctx, shape, x, y + 0.7f, size, 0, alpha / 4, true);
        Icons.draw(ctx, shape, x, y, size, rgb, alpha);
    }
}

