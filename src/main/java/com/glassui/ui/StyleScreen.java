/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.GlassConfig;
import com.glassui.ui.Anim;
import com.glassui.ui.Fx;
import com.glassui.ui.Glass;
import com.glassui.ui.GlassWidgets;
import com.glassui.ui.IconShapes;
import com.glassui.ui.Icons;
import com.glassui.ui.Spring;
import com.glassui.ui.Theme;
import com.glassui.ui.UiSound;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_364;
import net.minecraft.class_437;
import org.joml.Matrix3x2fStack;

public class StyleScreen
extends class_437 {
    private static final int PW = 324;
    private static final int PH = 318;
    private static final int PAD = 18;
    private static final int BLUE = 3111935;
    private final class_437 parent;
    private final List<class_339> ws = new ArrayList<class_339>();
    private final Spring pop = new Spring(0.92f, 1.0f, 230.0f, 21.0f);
    private long last;
    private int px;
    private int py;

    public StyleScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"Style"));
        this.parent = parent;
    }

    private <T extends class_339> T reg(T w) {
        this.method_37063((class_364)w);
        this.ws.add(w);
        return w;
    }

    protected void method_25426() {
        this.ws.clear();
        GlassConfig cfg = GlassConfig.get();
        this.px = (this.field_22789 - 324) / 2;
        this.py = (this.field_22790 - 318) / 2;
        int fx = this.px + 18;
        int fw = 288;
        int cw = (fw - 15) / 4;
        for (int i = 0; i < Theme.ALL.length; ++i) {
            int idx = i;
            this.reg(new GlassWidgets.Chip(fx + i % 4 * (cw + 5), this.py + 58 + i / 4 * 26, cw, 22, (class_2561)class_2561.method_43470((String)Theme.ALL[i].name()), null, Theme.ALL[i].tint(), () -> cfg.theme == idx, () -> {
                cfg.theme = idx;
                GlassConfig.save();
            }));
        }
        int fw3 = (fw - 10) / 3;
        for (int i = 0; i < Fx.FONT_NAMES.length; ++i) {
            int idx = i;
            this.reg(new GlassWidgets.Chip(fx + i % 3 * (fw3 + 5), this.py + 126 + i / 3 * 26, fw3, 22, (class_2561)class_2561.method_43470((String)Fx.FONT_NAMES[i]), null, 3111935, () -> cfg.font == idx, () -> {
                cfg.font = idx;
                GlassConfig.save();
            }));
        }
        this.reg(new GlassWidgets.Toggle(fx, this.py + 184, fw, 24, (class_2561)class_2561.method_43470((String)"Jelly animations"), () -> cfg.jelly, () -> {
            cfg.jelly = !cfg.jelly;
            GlassConfig.save();
        }));
        this.reg(new GlassWidgets.Toggle(fx, this.py + 208, fw, 24, (class_2561)class_2561.method_43470((String)"Pop sound on every UI"), () -> cfg.uiSound, () -> {
            cfg.uiSound = !cfg.uiSound;
            GlassConfig.save();
        }));
        this.reg(new GlassWidgets.Toggle(fx, this.py + 232, fw, 24, (class_2561)class_2561.method_43470((String)"Island docked to top"), () -> cfg.islandDock, () -> {
            cfg.islandDock = !cfg.islandDock;
            GlassConfig.save();
        }));
        this.reg(new GlassWidgets.Chip(this.px + 324 - 18 - 96, this.py + 318 - 34, 96, 22, (class_2561)class_2561.method_43470((String)"Done"), null, 3111935, () -> true, this::method_25419));
    }

    public void method_25419() {
        this.field_22787.method_1507(this.parent);
    }

    public void method_25394(class_332 ctx, int mx, int my, float dt) {
        long now = System.nanoTime();
        if (this.last == 0L) {
            this.last = now;
            UiSound.open();
        }
        this.pop.step(Anim.smooth((float)(now - this.last) / 1.0E9f));
        this.last = now;
        float a = Math.max(0.0f, Math.min(1.0f, (this.pop.pos - 0.9f) / 0.1f));
        float cx = (float)this.field_22789 / 2.0f;
        float cy = (float)this.field_22790 / 2.0f;
        Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        float sq = this.pop.squash();
        m.translate(cx, cy);
        m.scale(this.pop.pos * (1.0f - sq * 0.6f), this.pop.pos * (1.0f + sq));
        m.translate(-cx, -cy);
        Glass.panel(ctx, this.px, this.py, 324, 318, 20, 0xFFFFFF, 1.2f, a);
        int white = Glass.c(Math.max(64, (int)(255.0f * a)), 0xFFFFFF);
        int grey = Glass.c(Math.max(64, (int)(170.0f * a)), 13158608);
        Icons.drawShadowed(ctx, IconShapes.Shape.SPARKLE, this.px + 18, this.py + 13, 13.0f, 12572927, (int)(255.0f * a));
        Fx.draw(ctx, this.field_22793, "Style", this.px + 18 + 18, this.py + 15, white, true);
        Fx.draw(ctx, this.field_22793, "Theme", this.px + 18 + 2, this.py + 46, grey, false);
        Fx.draw(ctx, this.field_22793, "Font", this.px + 18 + 2, this.py + 114, grey, false);
        Glass.inset(ctx, this.px + 18, this.py + 182, 288, 76, 10, false, a);
        for (class_339 w : this.ws) {
            w.method_25394(ctx, mx, my, dt);
        }
        m.popMatrix();
    }
}

