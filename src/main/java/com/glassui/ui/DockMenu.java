/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.ui.Anim;
import com.glassui.ui.CustomGlassWidget;
import com.glassui.ui.Fx;
import com.glassui.ui.Glass;
import com.glassui.ui.GlassAlertScreen;
import com.glassui.ui.IconShapes;
import com.glassui.ui.Icons;
import com.glassui.ui.MusicScreen;
import com.glassui.ui.Spring;
import com.glassui.ui.StreamHudConfigScreen;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_4749;
import net.minecraft.class_500;
import net.minecraft.class_526;
import org.joml.Matrix3x2fStack;

public final class DockMenu {
    public static Layout create(class_437 screen) {
        class_310 mc = class_310.method_1551();
        ArrayList<Entry> es = new ArrayList<Entry>();
        es.add(new Entry((class_2561)class_2561.method_43471((String)"menu.singleplayer"), 4431943, IconShapes.Shape.PERSON, () -> mc.method_1507((class_437)new class_526(screen))));
        es.add(new Entry((class_2561)class_2561.method_43471((String)"menu.multiplayer"), 2001125, IconShapes.Shape.GLOBE, () -> mc.method_1507((class_437)(mc.field_1690.field_21840 ? new class_500(screen) : new class_4749(screen)))));
        es.add(new Entry((class_2561)class_2561.method_43471((String)"menu.options"), 7901340, IconShapes.Shape.GEAR, () -> mc.method_1507((class_437)new class_429(screen, mc.field_1690))));
        es.add(new Entry((class_2561)class_2561.method_43470((String)"Stream HUD"), 15022389, IconShapes.Shape.STREAM, () -> mc.method_1507((class_437)new StreamHudConfigScreen(screen))));
        es.add(new Entry((class_2561)class_2561.method_43470((String)"Music"), 1947988, IconShapes.Shape.NOTE, () -> mc.method_1507((class_437)new MusicScreen(screen))));
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            es.add(new Entry((class_2561)class_2561.method_43470((String)"Mods"), 9315498, IconShapes.Shape.GRID, () -> {
                try {
                    Class<?> c = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
                    mc.method_1507((class_437)c.getConstructor(class_437.class).newInstance(screen));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }));
        }
        es.add(new Entry((class_2561)class_2561.method_43471((String)"menu.quit"), 12986408, IconShapes.Shape.POWER, () -> mc.method_1507((class_437)new GlassAlertScreen(screen, (class_2561)class_2561.method_43470((String)"Quit Minecraft?"), (class_2561)class_2561.method_43470((String)"The game will close."), (class_2561)class_2561.method_43471((String)"menu.quit"), () -> ((class_310)mc).method_1592()))));
        int tile = 36;
        int gap = 10;
        int pad = 12;
        int n = es.size();
        int barW = n * tile + (n - 1) * gap + pad * 2;
        int barH = tile + pad * 2;
        int bx = (screen.field_22789 - barW) / 2;
        int by = screen.field_22790 - barH - 14;
        ArrayList<class_4185> items = new ArrayList<class_4185>();
        for (int i = 0; i < n; ++i) {
            Entry e = (Entry)es.get(i);
            items.add(new Item(bx + pad + i * (tile + gap), by + pad, tile, e, i));
        }
        return new Layout(new Bar(bx, by, barW, barH), items);
    }

    private record Entry(class_2561 label, int tint, IconShapes.Shape icon, Runnable action) {
    }

    private static final class Item
    extends class_4185
    implements CustomGlassWidget {
        private final Entry entry;
        private final Spring hov = new Spring(1.0f, 1.0f, 320.0f, 22.0f);
        private final Spring pop = new Spring(0.0f, 1.0f, 200.0f, 20.0f);
        private final long startAt;
        private long last = System.nanoTime();

        Item(int x, int y, int size, Entry e, int index) {
            super(x, y, size, size, e.label(), b -> e.action().run(), field_40754);
            this.entry = e;
            this.startAt = System.nanoTime() + 120000000L + (long)index * 60000000L;
        }

        public void method_75752(class_332 ctx, int mx, int my, float dt) {
        }

        @Override
        public void renderGlass(class_332 ctx, int mx, int my, float dt) {
            long now = System.nanoTime();
            if (now < this.startAt) {
                this.last = now;
                return;
            }
            float d = Anim.smooth((float)(now - this.last) / 1.0E9f);
            this.last = now;
            this.hov.target = this.method_49606() ? 1.22f : 1.0f;
            this.hov.step(d);
            this.pop.step(d);
            int x = this.method_46426();
            int y = this.method_46427();
            int sz = this.method_25368();
            float cx = (float)x + (float)sz / 2.0f;
            float bottom = y + sz;
            float s = this.hov.pos * this.pop.pos;
            float rise = (1.0f - Math.min(1.0f, this.pop.pos)) * 24.0f;
            Matrix3x2fStack m = ctx.method_51448();
            m.pushMatrix();
            m.translate(cx, bottom + rise);
            m.scale(s, s);
            m.translate(-cx, -bottom);
            int top = Glass.lerp(0xFF000000 | this.entry.tint(), -1, 0.25f);
            int bot = Glass.lerp(0xFF000000 | this.entry.tint(), -16777216, 0.35f);
            Glass.layer(ctx, x, y, sz, sz, 10.0f, Glass.c(230, top), Glass.c(230, bot));
            Glass.layer(ctx, (float)x + 1.5f, y + 1, sz - 3, (float)sz * 0.46f, 8.5f, Glass.c(88, 0xFFFFFF), Glass.c(5, 0xFFFFFF));
            Glass.ring(ctx, x, y, sz, sz, 10.0f, 0.5f, Glass.c(192, 0xFFFFFF), Glass.c(56, 0xFFFFFF));
            Icons.drawShadowed(ctx, this.entry.icon(), (float)x + (float)(sz - 20) / 2.0f, (float)y + (float)(sz - 20) / 2.0f, 20.0f, 0xFFFFFF, 255);
            m.popMatrix();
            if (this.method_49606()) {
                class_327 tr = class_310.method_1551().field_1772;
                int tw = Fx.w(tr, this.entry.label());
                int pw = tw + 16;
                int px = (int)(cx - (float)pw / 2.0f);
                int py = y - 30;
                Glass.button(ctx, px, py, pw, 17, 8, 0x303040, 1.4f, 1.0f);
                Fx.draw(ctx, tr, this.entry.label(), px + 8, py + 5, -1, true);
            }
        }
    }

    public record Layout(class_4068 bar, List<class_4185> items) {
    }

    private static final class Bar
    implements class_4068 {
        final int x;
        final int y;
        final int w;
        final int h;
        final Spring pop = new Spring(0.0f, 1.0f, 190.0f, 21.0f);
        long last;

        Bar(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public void method_25394(class_332 ctx, int mx, int my, float dt) {
            long now = System.nanoTime();
            if (this.last == 0L) {
                this.last = now;
            }
            this.pop.step(Anim.smooth((float)(now - this.last) / 1.0E9f));
            this.last = now;
            float a = Math.max(0.0f, Math.min(1.0f, this.pop.pos));
            float off = (1.0f - this.pop.pos) * 30.0f;
            Matrix3x2fStack m = ctx.method_51448();
            m.pushMatrix();
            m.translate(0.0f, off);
            Glass.panel(ctx, this.x, this.y, this.w, this.h, 24, 0xFFFFFF, 1.1f, a);
            m.popMatrix();
        }
    }
}

