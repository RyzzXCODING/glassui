/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.ui.Anim;
import com.glassui.ui.Fx;
import com.glassui.ui.Glass;
import com.glassui.ui.Spring;
import com.glassui.ui.UiSound;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5244;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import org.joml.Matrix3x2fStack;

public class GlassAlertScreen
extends class_437 {
    private final class_437 parent;
    private final class_2561 message;
    private final class_2561 confirm;
    private final Runnable onConfirm;
    private final Spring pop = new Spring(0.84f, 1.0f, 230.0f, 21.0f);
    private long last;
    private int px;
    private int py;
    private static final int PW = 220;
    private static final int PH = 100;
    private class_4185 yes;
    private class_4185 no;

    public GlassAlertScreen(class_437 parent, class_2561 title, class_2561 message, class_2561 confirm, Runnable onConfirm) {
        super(title);
        this.parent = parent;
        this.message = message;
        this.confirm = confirm;
        this.onConfirm = onConfirm == null ? () -> {} : onConfirm;
    }

    protected void method_25426() {
        this.px = (this.field_22789 - 220) / 2;
        this.py = (this.field_22790 - 100) / 2;
        int bw = 92;
        this.no = class_4185.method_46430((class_2561)class_5244.field_24335, b -> this.method_25419()).method_46434(this.px + 12, this.py + 100 - 32, bw, 20).method_46431();
        this.yes = class_4185.method_46430((class_2561)this.confirm, b -> this.onConfirm.run()).method_46434(this.px + 220 - 12 - bw, this.py + 100 - 32, bw, 20).method_46431();
        this.method_37063((class_364)this.no);
        this.method_37063((class_364)this.yes);
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
        float a = Math.max(0.0f, Math.min(1.0f, (this.pop.pos - 0.8f) / 0.2f));
        float cx = (float)this.field_22789 / 2.0f;
        float cy = (float)this.field_22790 / 2.0f;
        Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        m.translate(cx, cy);
        float sq = this.pop.squash();
        m.scale(this.pop.pos * (1.0f - sq * 0.6f), this.pop.pos * (1.0f + sq));
        m.translate(-cx, -cy);
        Glass.panel(ctx, this.px, this.py, 220, 100, 20, 0xFFFFFF, 1.2f, a);
        int col = Glass.c(Math.max(64, (int)(255.0f * a)), 0xFFFFFF);
        Fx.draw(ctx, this.field_22793, this.field_22785, (this.field_22789 - Fx.w(this.field_22793, this.field_22785)) / 2, this.py + 14, col, true);
        List lines = this.field_22793.method_1728((class_5348)this.message, 192);
        int ly = this.py + 30;
        for (class_5481 l : lines) {
            Fx.draw(ctx, this.field_22793, l, (this.field_22789 - Fx.w(this.field_22793, l)) / 2, ly, Glass.c(Math.max(64, (int)(200.0f * a)), 0xD0D0D8), false);
            ly += 10;
        }
        Glass.fill(ctx, this.px + 12, this.py + 100 - 38, 196.0f, 0.5f, 0.0f, Glass.c((int)(48.0f * a), 0xFFFFFF), Glass.c((int)(48.0f * a), 0xFFFFFF));
        m.popMatrix();
        this.no.method_25394(ctx, mx, my, dt);
        this.yes.method_25394(ctx, mx, my, dt);
    }
}

