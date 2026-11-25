/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.GlassConfig;
import com.glassui.hud.StreamHudRenderer;
import com.glassui.ui.Anim;
import com.glassui.ui.Fx;
import com.glassui.ui.Glass;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;

public class HudEditorScreen
extends class_437 {
    private final class_437 parent;
    private final List<class_339> ws = new ArrayList<class_339>();
    private StreamHudRenderer.Id sel = StreamHudRenderer.Id.ISLAND;
    private boolean prevDown = true;
    private boolean dragging;
    private float gx;
    private float gy;
    private float gw;
    private long last;

    public HudEditorScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"HUD Layout"));
        this.parent = parent;
    }

    private void btn(String msg, class_4185.class_4241 act, int x, int y, int w) {
        class_4185 b = class_4185.method_46430((class_2561)class_2561.method_43470((String)msg), (class_4185.class_4241)act).method_46434(x, y, w, 20).method_46431();
        this.method_37063((class_364)b);
        this.ws.add((class_339)b);
    }

    protected void method_25426() {
        this.ws.clear();
        GlassConfig cfg = GlassConfig.get();
        int total = 314;
        int x = (this.field_22789 - total) / 2;
        int y = this.field_22790 - 30;
        this.btn("Scale -", b -> this.scale(-0.1f), x, y, 56);
        this.btn("Scale +", b -> this.scale(0.1f), x += 62, y, 56);
        this.btn("On/Off", b -> {
            GlassConfig.Widget w = StreamHudRenderer.widget(cfg, this.sel);
            w.on = !w.on;
            GlassConfig.save();
        }, x += 62, y, 56);
        this.btn("Reset all", b -> {
            cfg.resetLayout();
            GlassConfig.save();
        }, x += 62, y, 66);
        this.btn("Done", b -> this.method_25419(), x += 72, y, 56);
    }

    private void scale(float delta) {
        GlassConfig.Widget w = StreamHudRenderer.widget(GlassConfig.get(), this.sel);
        w.scale = Math.max(0.5f, Math.min(2.5f, (float)Math.round((w.scale + delta) * 10.0f) / 10.0f));
        GlassConfig.save();
    }

    public void method_25419() {
        GlassConfig.save();
        this.field_22787.method_1507(this.parent);
    }

    public void method_25420(class_332 ctx, int mx, int my, float dt) {
    }

    public void method_25394(class_332 ctx, int mx, int my, float dt) {
        boolean down;
        long now = System.nanoTime();
        if (this.last == 0L) {
            this.last = now;
        }
        float d = Anim.smooth((float)(now - this.last) / 1.0E9f);
        this.last = now;
        GlassConfig cfg = GlassConfig.get();
        ctx.method_25294(0, 0, this.field_22789, this.field_22790, 0x60000000);
        StreamHudRenderer.drawPreview(ctx, this.sel, d);
        boolean bl = down = GLFW.glfwGetMouseButton((long)GLFW.glfwGetCurrentContext(), (int)0) == 1;
        if (down && !this.prevDown && my < this.field_22790 - 44) {
            StreamHudRenderer.Id hit = null;
            for (StreamHudRenderer.Id id : new StreamHudRenderer.Id[]{StreamHudRenderer.Id.ALERT, StreamHudRenderer.Id.ISLAND, StreamHudRenderer.Id.COMBO, StreamHudRenderer.Id.PROFILE, StreamHudRenderer.Id.CASSETTE, StreamHudRenderer.Id.LYRICS, StreamHudRenderer.Id.REQUESTS, StreamHudRenderer.Id.TOPTAP, StreamHudRenderer.Id.SONG, StreamHudRenderer.Id.CHAT}) {
                StreamHudRenderer.R r = StreamHudRenderer.rect(id, this.field_22789, this.field_22790);
                if (r == null || !r.has(mx, my)) continue;
                hit = id;
                break;
            }
            if (hit != null) {
                this.sel = hit;
                StreamHudRenderer.R r = StreamHudRenderer.rect(this.sel, this.field_22789, this.field_22790);
                this.gx = (float)mx - r.x();
                this.gy = (float)my - r.y();
                this.gw = r.w();
                this.dragging = true;
            }
        }
        if (this.dragging && down) {
            GlassConfig.Widget w = StreamHudRenderer.widget(cfg, this.sel);
            float left = (float)mx - this.gx;
            float top = (float)my - this.gy;
            w.x = Math.max(0.0f, Math.min(1.0f, (left + this.gw / 2.0f) / (float)this.field_22789));
            w.y = Math.max(0.0f, Math.min(1.0f, top / (float)this.field_22790));
        }
        if (this.dragging && !down) {
            this.dragging = false;
            GlassConfig.save();
        }
        this.prevDown = down;
        int total = 314;
        int x0 = (this.field_22789 - total) / 2;
        Glass.buttonLite(ctx, x0 - 10, this.field_22790 - 38, total + 20, 34, 17, 0xFFFFFF, 1.0f, 1.0f);
        GlassConfig.Widget w = StreamHudRenderer.widget(cfg, this.sel);
        String info = StreamHudRenderer.label(this.sel) + "  -  " + String.format(Locale.ROOT, "%.1fx", Float.valueOf(w.scale)) + (w.on ? "" : "  (off)");
        int iw = Fx.w(this.field_22793, info);
        Glass.buttonLite(ctx, (this.field_22789 - iw) / 2 - 8, this.field_22790 - 57, iw + 16, 15, 7, 0x303040, 1.2f, 1.0f);
        Fx.draw(ctx, this.field_22793, info, (this.field_22789 - iw) / 2, this.field_22790 - 53, -1, true);
        String hint = "Drag a widget to move it - click to select - Esc / Done to save";
        int hw = Fx.w(this.field_22793, hint);
        Glass.buttonLite(ctx, (this.field_22789 - hw) / 2 - 10, 5, hw + 20, 20, 10, 0xFFFFFF, 1.0f, 1.0f);
        Fx.draw(ctx, this.field_22793, hint, (this.field_22789 - hw) / 2, 11, -1513232, true);
        for (class_339 c : this.ws) {
            c.method_25394(ctx, mx, my, dt);
        }
    }
}

