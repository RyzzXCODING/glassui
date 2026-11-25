/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.GlassConfig;
import com.glassui.hud.MediaControl;
import com.glassui.hud.StreamData;
import com.glassui.hud.StreamHud;
import com.glassui.hud.StreamHudRenderer;
import com.glassui.music.LocalPlayer;
import com.glassui.music.MusicHub;
import com.glassui.music.Suggest;
import com.glassui.ui.Anim;
import com.glassui.ui.CustomGlassWidget;
import com.glassui.ui.Fx;
import com.glassui.ui.Glass;
import com.glassui.ui.GlassWidgets;
import com.glassui.ui.IconShapes;
import com.glassui.ui.Icons;
import com.glassui.ui.Spring;
import com.glassui.ui.Theme;
import com.glassui.ui.UiSound;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

public class MusicScreen
extends class_437 {
    private static final int SP_GREEN = 1947988;
    private static final int YT_RED = 16729402;
    private static final int WHITE = 0xFFFFFF;
    private static final int PAD = 8;
    private static final int SIDE_W = 104;
    private static final int PLAYER_H = 46;
    private static final int GAP = 6;
    private static final int ROW_H = 15;
    private static final int GREY = -5658187;
    private static final int DIM = -7697770;
    private static View view = View.HOME;
    private static String lastQuery = "";
    private final class_437 parent;
    private final List<class_339> ws = new ArrayList<class_339>();
    private final List<class_339> rowWs = new ArrayList<class_339>();
    private final List<class_339> tileWs = new ArrayList<class_339>();
    private final Spring pop = new Spring(0.0f, 1.0f, 230.0f, 21.0f);
    private boolean closing;
    private boolean finished;
    private float ocx;
    private float ocy;
    private float ow = 124.0f;
    private float oh = 24.0f;
    private long last;
    private boolean enterDown = true;
    private boolean mouseWasDown = true;
    private boolean dragVol;
    private int px;
    private int py;
    private int pw;
    private int ph;
    private int sx;
    private int sy;
    private int contentH;
    private int mx0;
    private int my0;
    private int mw;
    private int barX;
    private int barY;
    private int barW;
    private class_342 queryF;
    private GlassWidgets.Chip homeShuffle;
    private GlassWidgets.Chip installChip;
    private GlassWidgets.Chip loginChip;
    private GlassWidgets.Chip shuffleChip;
    private GlassWidgets.Chip randomChip;
    private GlassWidgets.Chip musicChip;
    private GlassWidgets.Chip volChip;
    private String musicLbl = "";
    private String volLbl = "";
    private List<MusicHub.Track> home = List.of();
    private List<MusicHub.Track> lib = List.of();
    private int listVer = -1;
    private int tileCount;

    public MusicScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"Music"));
        this.parent = parent;
    }

    private <T extends class_339> T reg(T w) {
        this.method_37063((class_364)w);
        this.ws.add(w);
        return w;
    }

    protected void method_25426() {
        int minTop;
        this.ws.clear();
        this.rowWs.clear();
        this.tileWs.clear();
        GlassConfig cfg = GlassConfig.get();
        this.pw = Math.min(this.field_22789 - 12, Math.min(720, Math.round((float)this.field_22789 * 0.9f)));
        this.ph = Math.min(330, this.field_22790 - 12);
        this.px = (this.field_22789 - this.pw) / 2;
        this.py = (this.field_22790 - this.ph) / 2;
        StreamHudRenderer.R ir = StreamHudRenderer.islandRect();
        if (ir != null) {
            this.ocx = ir.x() + ir.w() / 2.0f;
            this.ocy = ir.y() + ir.h() / 2.0f;
            this.ow = ir.w();
            this.oh = ir.h();
        } else {
            this.ocx = (float)this.field_22789 / 2.0f;
            this.ocy = 12.0f;
            this.ow = 124.0f;
            this.oh = 24.0f;
        }
        int n = minTop = ir == null ? 0 : Math.round(ir.y() + ir.h()) + 6;
        if (this.py < minTop) {
            this.py = minTop;
            this.ph = Math.max(150, Math.min(this.ph, this.field_22790 - this.py - 6));
        }
        this.sx = this.px + 8;
        this.sy = this.py + 8;
        this.contentH = this.ph - 16 - 46 - 6;
        this.mx0 = this.sx + 104 + 6;
        this.my0 = this.sy;
        this.mw = this.pw - 16 - 104 - 6;
        this.barX = this.px + 8;
        this.barY = this.sy + this.contentH + 6;
        this.barW = this.pw - 16;
        LocalPlayer.probeAsync();
        this.reg(new Nav(this.sx + 6, this.sy + 28, 92, 20, "Home", IconShapes.Shape.HOME, () -> view == View.HOME, () -> this.setView(View.HOME)));
        this.reg(new Nav(this.sx + 6, this.sy + 50, 92, 20, "Search", IconShapes.Shape.SEARCH, () -> view == View.SEARCH, () -> this.setView(View.SEARCH)));
        this.reg(new Nav(this.sx + 6, this.sy + 72, 92, 20, "Library", IconShapes.Shape.LIBRARY, () -> view == View.LIBRARY, () -> this.setView(View.LIBRARY)));
        this.shuffleChip = this.reg(new GlassWidgets.Chip(this.sx + 6, this.sy + 112, 92, 18, (class_2561)class_2561.method_43470((String)(cfg.shuffle ? "Shuffle: On" : "Shuffle: Off")), IconShapes.Shape.SPARKLE, 1947988, () -> cfg.shuffle, () -> {
            cfg.shuffle = !cfg.shuffle;
            GlassConfig.save();
            this.shuffleChip.setLabel((class_2561)class_2561.method_43470((String)(cfg.shuffle ? "Shuffle: On" : "Shuffle: Off")));
        }));
        this.randomChip = this.reg(new GlassWidgets.Chip(this.sx + 6, this.sy + 132, 92, 18, (class_2561)class_2561.method_43470((String)"Play random"), IconShapes.Shape.PLAY, 16729402, () -> false, MusicHub::playRandom));
        this.musicChip = this.reg(new GlassWidgets.Chip(this.sx + 6, this.sy + 152, 92, 18, (class_2561)class_2561.method_43470((String)"Music: On"), IconShapes.Shape.NOTE, 1947988, MusicHub::enabled, () -> MusicHub.setEnabled(!MusicHub.enabled())));
        this.volChip = this.reg(new GlassWidgets.Chip(this.sx + 6, this.sy + 172, 92, 18, (class_2561)class_2561.method_43470((String)"Volume: On"), IconShapes.Shape.VOLUME, 1947988, () -> !MusicHub.muted(), MusicHub::toggleMute));
        this.installChip = this.reg(new GlassWidgets.Chip(this.sx + 6, this.sy + this.contentH - 24, 92, 18, (class_2561)class_2561.method_43470((String)"Set up player"), IconShapes.Shape.SPARKLE, 1947988, () -> true, LocalPlayer::setupAsync));
        this.loginChip = this.reg(new GlassWidgets.Chip(this.sx + 6, this.sy + this.contentH - 24, 92, 18, (class_2561)class_2561.method_43470((String)"Log in"), IconShapes.Shape.PERSON, 1947988, () -> true, MusicHub::spotifyLogin));
        this.queryF = this.reg(GlassWidgets.field(this.field_22793, this.mx0 + 10 + 18, this.my0 + 9, this.mw - 20 - 18 - 6, 18, lastQuery, "What do you want to play?", 80));
        this.queryF.method_1863(MusicHub::typing);
        if (lastQuery.isEmpty()) {
            MusicHub.typing("");
        }
        int rows = Math.max(3, Math.min(18, (this.contentH - 52) / 15));
        for (int i = 0; i < rows; ++i) {
            this.rowWs.add((class_339)this.reg(new Row(this.mx0 + 6, this.my0 + 50 + i * 15, this.mw - 12, 13, i, this::currentList, this::playTrack)));
        }
        int tileRows = Math.max(2, Math.min(4, (this.contentH - 44) / 32));
        this.tileCount = tileRows * 3;
        int tw = (this.mw - 20 - 16) / 3;
        for (int i = 0; i < this.tileCount; ++i) {
            this.tileWs.add((class_339)this.reg(new Tile(this.mx0 + 10 + i % 3 * (tw + 8), this.my0 + 40 + i / 3 * 32, tw, 27, i, this::currentList, this::playTrack)));
        }
        this.homeShuffle = this.reg(new GlassWidgets.Chip(this.mx0 + this.mw - 104, this.my0 + 10, 94, 18, (class_2561)class_2561.method_43470((String)"Shuffle play"), IconShapes.Shape.SPARKLE, 1947988, () -> true, MusicHub::playRandom));
        int cx = this.barX + 150 + (this.barW - 270) / 2;
        this.reg(new IconBtn(cx - 44, this.barY + 8, 20, 20, IconShapes.Shape.PREV, () -> MediaControl.send(MediaControl.Cmd.PREV)));
        this.reg(new PlayBtn(cx - 12, this.barY + 5, 24));
        this.reg(new IconBtn(cx + 24, this.barY + 8, 20, 20, IconShapes.Shape.NEXT, () -> MediaControl.send(MediaControl.Cmd.NEXT)));
        this.applyView();
    }

    private void setView(View v) {
        if (view == v) {
            return;
        }
        view = v;
        if (v == View.SEARCH && this.queryF != null && this.queryF.method_1882().isBlank()) {
            MusicHub.typing("");
        }
        this.applyView();
    }

    private void applyView() {
        boolean search = view == View.SEARCH;
        for (class_339 w : this.rowWs) {
            w.field_22763 = view != View.HOME;
            w.field_22764 = w.field_22763;
        }
        for (class_339 w : this.tileWs) {
            w.field_22763 = view == View.HOME;
            w.field_22764 = w.field_22763;
        }
        if (this.homeShuffle != null) {
            this.homeShuffle.field_22763 = view == View.HOME;
            this.homeShuffle.field_22764 = this.homeShuffle.field_22763;
        }
        if (this.queryF != null) {
            this.queryF.field_22764 = this.queryF.field_22763 = search;
            this.method_25395((class_364)(search ? this.queryF : null));
        }
    }

    private void setProvider(String p) {
        GlassConfig cfg = GlassConfig.get();
        if (p.equals(cfg.musicProvider) || !"YOUTUBE".equals(p)) {
            return;
        }
        cfg.musicProvider = p;
        GlassConfig.save();
        if (view == View.SEARCH && this.queryF != null && !this.queryF.method_1882().isBlank()) {
            this.search();
        }
    }

    private void search() {
        if (this.queryF == null) {
            return;
        }
        String q = this.queryF.method_1882().trim();
        if (q.isEmpty()) {
            return;
        }
        lastQuery = q;
        GlassConfig.save();
        MusicHub.search(q);
    }

    private void playTrack(MusicHub.Track t) {
        MusicHub.play(t);
    }

    public void requestClose() {
        if (this.closing) {
            return;
        }
        this.closing = true;
        UiSound.close();
        this.pop.target = 0.0f;
        if (this.queryF != null) {
            lastQuery = this.queryF.method_1882().trim();
        }
        GlassConfig.save();
    }

    public void method_25419() {
        this.requestClose();
    }

    private List<MusicHub.Track> currentList() {
        int ver = MusicHub.version() * 31 + GlassConfig.get().musicHistory.size();
        if (ver != this.listVer) {
            this.listVer = ver;
            List<MusicHub.Track> rec = Suggest.recentList();
            List<MusicHub.Track> pop = Suggest.popularList(30);
            this.lib = rec;
            ArrayList<MusicHub.Track> out = new ArrayList<MusicHub.Track>();
            HashSet<String> seen = new HashSet<String>();
            int i = 0;
            int j = 0;
            while (out.size() < 12 && (i < rec.size() || j < pop.size())) {
                MusicHub.Track t;
                if (i < rec.size() && seen.add((t = rec.get(i++)).title().toLowerCase(Locale.ROOT))) {
                    out.add(t);
                }
                if (out.size() >= 12 || j >= pop.size() || !seen.add((t = pop.get(j++)).title().toLowerCase(Locale.ROOT))) continue;
                out.add(t);
            }
            this.home = out;
        }
        return switch (view.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.home;
            case 2 -> this.lib;
            case 1 -> MusicHub.listing();
        };
    }

    private static float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static int hsv(float h, float s, float v) {
        float b;
        float g;
        float r;
        float c = v * s;
        float x = c * (1.0f - Math.abs(h / 60.0f % 2.0f - 1.0f));
        float m = v - c;
        if (h < 60.0f) {
            r = c;
            g = x;
            b = 0.0f;
        } else if (h < 120.0f) {
            r = x;
            g = c;
            b = 0.0f;
        } else if (h < 180.0f) {
            r = 0.0f;
            g = c;
            b = x;
        } else if (h < 240.0f) {
            r = 0.0f;
            g = x;
            b = c;
        } else if (h < 300.0f) {
            r = x;
            g = 0.0f;
            b = c;
        } else {
            r = c;
            g = 0.0f;
            b = x;
        }
        return Math.round((r + m) * 255.0f) << 16 | Math.round((g + m) * 255.0f) << 8 | Math.round((b + m) * 255.0f);
    }

    private static void art(class_332 ctx, float x, float y, int size, String key) {
        int c = 0xFF000000 | MusicScreen.hsv(Math.floorMod(key.hashCode(), 360), 0.55f, 0.8f);
        Glass.layer(ctx, x, y, size, size, Math.max(3.0f, (float)size * 0.18f), Glass.lerp(c, -1, 0.22f), Glass.lerp(c, -16777216, 0.4f));
        float is = (float)size * 0.5f;
        Icons.drawShadowed(ctx, IconShapes.Shape.NOTE, x + ((float)size - is) / 2.0f, y + ((float)size - is) / 2.0f, is, 0xFFFFFF, 230);
    }

    private static void eq(class_332 ctx, float x, float y, float h, int rgb, boolean moving) {
        float t = (float)(System.currentTimeMillis() % 100000L) / 1000.0f;
        for (int i = 0; i < 3; ++i) {
            float k = moving ? 0.3f + 0.7f * Math.abs((float)Math.sin(t * 5.0f + (float)i * 1.9f)) : 0.3f;
            float bh = Math.max(2.0f, h * k);
            Glass.fill(ctx, x + (float)i * 3.5f, y + h - bh, 2.0f, bh, 1.0f, Glass.c(255, rgb), Glass.c(255, rgb));
        }
    }

    private static int[] cols(int x, int w, int h) {
        int titleX = x + 22 + (h - 2) + 5;
        int avail = x + w - 62 - titleX;
        int tw = Math.round((float)avail * 0.58f);
        return new int[]{titleX, tw, titleX + tw + 8, avail - tw - 8};
    }

    private static boolean isSug(MusicHub.Track t) {
        return "SUGGEST".equals(t.provider());
    }

    private static String titleOf(MusicHub.Track t) {
        if (MusicScreen.isSug(t)) {
            int i = t.title().indexOf(" - ");
            return i > 0 ? t.title().substring(0, i) : t.title();
        }
        return t.title();
    }

    private static String artistOf(MusicHub.Track t) {
        if (MusicScreen.isSug(t)) {
            int i = t.title().indexOf(" - ");
            return i > 0 ? t.title().substring(i + 3) : "";
        }
        return t.artist() == null ? "" : t.artist();
    }

    private static String tagOf(MusicHub.Track t) {
        return MusicScreen.isSug(t) ? t.artist() : ("SPOTIFY".equals(t.provider()) ? "Spotify" : "YouTube");
    }

    private static boolean playingNow(MusicHub.Track t) {
        StreamData.NowPlaying np = StreamHud.DATA.song;
        return np != null && np.playing() && np.title() != null && np.title().equalsIgnoreCase(MusicScreen.titleOf(t));
    }

    public void method_25394(class_332 ctx, int mx, int my, float dt) {
        boolean spot;
        String vl;
        boolean down;
        boolean en;
        long now = System.nanoTime();
        if (this.last == 0L) {
            this.last = now;
            UiSound.open();
        }
        this.pop.step(Anim.smooth((float)(now - this.last) / 1.0E9f));
        this.last = now;
        if (this.closing && !this.finished && this.pop.pos < 0.04f) {
            this.finished = true;
            this.field_22787.execute(() -> this.field_22787.method_1507(this.parent));
        }
        GlassConfig cfg = GlassConfig.get();
        class_327 tr = this.field_22793;
        long win = GLFW.glfwGetCurrentContext();
        boolean bl = en = GLFW.glfwGetKey((long)win, (int)257) == 1 || GLFW.glfwGetKey((long)win, (int)335) == 1;
        if (en && !this.enterDown && view == View.SEARCH && this.queryF != null && this.queryF.method_25370()) {
            this.search();
        }
        this.enterDown = en;
        int vw = 76;
        int vx = this.barX + this.barW - 12 - vw;
        int vy = this.barY + 23 - 2;
        boolean over = mx >= vx - 4 && mx <= vx + vw + 4 && my >= vy - 7 && my <= vy + 11;
        boolean bl2 = down = GLFW.glfwGetMouseButton((long)win, (int)0) == 1;
        if (down && !this.mouseWasDown && over) {
            this.dragVol = true;
        }
        if (!down && this.dragVol) {
            this.dragVol = false;
            MusicHub.commitVolume();
        }
        this.mouseWasDown = down;
        if (this.dragVol) {
            MusicHub.setVolume(Math.round((float)(mx - vx) * 100.0f / (float)vw));
        }
        String ml = MusicHub.enabled() ? "Music: On" : "Music: Off";
        String string = vl = MusicHub.muted() ? "Volume: Off" : "Volume: On";
        if (!ml.equals(this.musicLbl)) {
            this.musicLbl = ml;
            this.musicChip.setLabel((class_2561)class_2561.method_43470((String)ml));
        }
        if (!vl.equals(this.volLbl)) {
            this.volLbl = vl;
            this.volChip.setLabel((class_2561)class_2561.method_43470((String)vl));
        }
        boolean needLogin = (spot = "SPOTIFY".equals(cfg.musicProvider)) && !MusicHub.spotifyLoggedIn();
        boolean needSetup = !needLogin && cfg.localPlayer && !LocalPlayer.ready() && !LocalPlayer.settingUp();
        this.loginChip.field_22764 = this.loginChip.field_22763 = needLogin;
        this.installChip.field_22764 = this.installChip.field_22763 = needSetup;
        float p = Math.max(0.0f, this.pop.pos);
        float a = MusicScreen.clamp01((p - 0.35f) / 0.45f);
        float pa = MusicScreen.clamp01(p * 4.0f);
        float pcx = (float)this.px + (float)this.pw / 2.0f;
        float pcy = (float)this.py + (float)this.ph / 2.0f;
        float sxs = Math.max(0.02f, this.ow / (float)this.pw + (1.0f - this.ow / (float)this.pw) * p);
        float sys = Math.max(0.02f, this.oh / (float)this.ph + (1.0f - this.oh / (float)this.ph) * p);
        Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        m.translate(pcx + (1.0f - p) * (this.ocx - pcx), pcy + (1.0f - p) * (this.ocy - pcy));
        float sq = this.pop.squash();
        m.scale(sxs * (1.0f - sq * 0.6f), sys * (1.0f + sq));
        m.translate(-pcx, -pcy);
        int white = Glass.c(Math.max(64, (int)(255.0f * a)), 0xFFFFFF);
        int grey = Glass.c(Math.max(64, (int)(255.0f * a)), 11119029);
        int dim = Glass.c(Math.max(64, (int)(255.0f * a)), 9079446);
        Glass.panel(ctx, this.px, this.py, this.pw, this.ph, 18, 0xFFFFFF, 1.2f, pa);
        Glass.layer(ctx, this.px + 1, this.py + 1, this.pw - 2, this.ph - 2, 17.0f, Glass.c((int)(120.0f * pa), 0), Glass.c((int)(144.0f * pa), 0));
        if (a > 0.02f) {
            Glass.layer(ctx, this.sx, this.sy, 104.0f, this.contentH, 12.0f, Glass.c((int)(112.0f * a), 0), Glass.c((int)(128.0f * a), 0));
            Glass.ringHard(ctx, this.sx, this.sy, 104.0f, this.contentH, 12.0f, 0.5f, Glass.c((int)(48.0f * a), 0xFFFFFF), Glass.c((int)(20.0f * a), 0xFFFFFF));
            Icons.drawShadowed(ctx, IconShapes.Shape.NOTE, this.sx + 10, this.sy + 8, 13.0f, 1947988, (int)(255.0f * a));
            Fx.draw(ctx, tr, "Music", this.sx + 28, this.sy + 10, white, true);
            Fx.draw(ctx, tr, "PLAYBACK", this.sx + 10, this.sy + 101, dim, false);
            boolean ready = LocalPlayer.ready();
            boolean setting = LocalPlayer.settingUp();
            if (!needLogin) {
                int dot;
                int n = setting ? 16766474 : (ready ? 3199320 : (dot = cfg.localPlayer ? 16752394 : 9079446));
                String lab = setting ? "Installing player..." : (ready ? "Player ready" : (cfg.localPlayer ? "Player not set up" : "Background off"));
                int ly = this.sy + this.contentH - (needSetup ? 38 : 20);
                Glass.fill(ctx, this.sx + 10, ly + 1, 6.0f, 6.0f, 3.0f, Glass.c((int)(255.0f * a), dot), Glass.c((int)(255.0f * a), dot));
                Fx.draw(ctx, tr, Fx.trim(tr, lab, 74), this.sx + 21, ly, grey, false);
            } else {
                Fx.draw(ctx, tr, Fx.trim(tr, "Not logged in", 84), this.sx + 10, this.sy + this.contentH - 38, grey, false);
            }
            int tint = Theme.isDefault() ? (spot ? 1947988 : 16729402) : Theme.cur().tint();
            Glass.layer(ctx, this.mx0, this.my0, this.mw, this.contentH, 12.0f, Glass.c((int)(92.0f * a), tint), Glass.c((int)(112.0f * a), 0));
            Glass.ringHard(ctx, this.mx0, this.my0, this.mw, this.contentH, 12.0f, 0.5f, Glass.c((int)(48.0f * a), 0xFFFFFF), Glass.c((int)(20.0f * a), 0xFFFFFF));
            this.drawMain(ctx, tr, a, white, grey, dim);
            this.drawPlayerBar(ctx, tr, a, white, grey, dim, vx, vy, vw, over);
        }
        if (p > 0.55f) {
            for (class_339 w : this.ws) {
                w.method_25394(ctx, mx, my, dt);
            }
        }
        m.popMatrix();
    }

    private void drawMain(class_332 ctx, class_327 tr, float a, int white, int grey, int dim) {
        GlassConfig cfg = GlassConfig.get();
        boolean spot = "SPOTIFY".equals(cfg.musicProvider);
        Matrix3x2fStack m = ctx.method_51448();
        switch (view.ordinal()) {
            case 0: {
                int h = LocalTime.now().getHour();
                String hello = h < 5 ? "Good night" : (h < 12 ? "Good morning" : (h < 18 ? "Good afternoon" : "Good evening"));
                m.pushMatrix();
                m.translate((float)(this.mx0 + 10), (float)(this.my0 + 9));
                m.scale(1.5f, 1.5f);
                Fx.draw(ctx, tr, hello, 0, 0, white, true);
                m.popMatrix();
                String sub = spot ? "Spotify Premium - plays on your Spotify device" : (LocalPlayer.ready() ? "YouTube - plays in the background, no browser" : "YouTube - background player installs on first play");
                Fx.draw(ctx, tr, Fx.trim(tr, sub, this.mw - 20), this.mx0 + 10, this.my0 + 25, grey, false);
                if (!this.currentList().isEmpty()) break;
                Fx.draw(ctx, tr, "Pick a song from Search.", this.mx0 + 10, this.my0 + 44, dim, false);
                break;
            }
            case 1: 
            case 2: {
                boolean search;
                boolean bl = search = view == View.SEARCH;
                if (search) {
                    Glass.inset(ctx, this.mx0 + 10, this.my0 + 8, this.mw - 20, 20, 10, this.queryF.method_25370(), a);
                    Icons.draw(ctx, IconShapes.Shape.SEARCH, this.mx0 + 16, this.my0 + 13, 10.0f, 12105924, (int)(255.0f * a));
                } else {
                    m.pushMatrix();
                    m.translate((float)(this.mx0 + 10), (float)(this.my0 + 9));
                    m.scale(1.3f, 1.3f);
                    Fx.draw(ctx, tr, "Your Library", 0, 0, white, true);
                    m.popMatrix();
                    Fx.draw(ctx, tr, "Songs you played from here", this.mx0 + 10, this.my0 + 24, grey, false);
                }
                int hy = this.my0 + 36;
                int[] c = MusicScreen.cols(this.mx0 + 6, this.mw - 12, 13);
                Fx.draw(ctx, tr, "#", this.mx0 + 12, hy, dim, false);
                Fx.draw(ctx, tr, "TITLE", c[0], hy, dim, false);
                Fx.draw(ctx, tr, "ARTIST", c[2], hy, dim, false);
                String src = "SOURCE";
                Fx.draw(ctx, tr, src, this.mx0 + this.mw - 14 - Fx.w(tr, src), hy, dim, false);
                Glass.layer(ctx, this.mx0 + 10, hy + 10, this.mw - 20, 1.0f, 0.0f, Glass.c((int)(56.0f * a), 0xFFFFFF), Glass.c((int)(56.0f * a), 0xFFFFFF));
                if (!this.currentList().isEmpty()) break;
                String msg = MusicHub.busy() ? "Searching..." : (!search ? "Songs you play will show up here." : (spot && !MusicHub.spotifyLoggedIn() ? "Log in to Spotify (left) to search, or use YouTube." : "Type a song or artist, then press Enter."));
                Fx.draw(ctx, tr, msg, this.mx0 + (this.mw - Fx.w(tr, msg)) / 2, this.my0 + this.contentH / 2 + 8, grey, true);
            }
        }
    }

    private void drawPlayerBar(class_332 ctx, class_327 tr, float a, int white, int grey, int dim, int vx, int vy, int vw, boolean overVol) {
        int fc;
        GlassConfig cfg = GlassConfig.get();
        Glass.layer(ctx, this.barX, this.barY, this.barW, 46.0f, 12.0f, Glass.c((int)(120.0f * a), 0), Glass.c((int)(136.0f * a), 0));
        Glass.ringHard(ctx, this.barX, this.barY, this.barW, 46.0f, 12.0f, 0.5f, Glass.c((int)(48.0f * a), 0xFFFFFF), Glass.c((int)(20.0f * a), 0xFFFFFF));
        StreamData.NowPlaying np = StreamHud.DATA.song;
        if (np != null) {
            MusicScreen.art(ctx, this.barX + 8, this.barY + 7, 32, np.title() == null ? "" : np.title());
            Fx.draw(ctx, tr, Fx.trim(tr, np.title() == null ? "" : np.title(), 88), this.barX + 46, this.barY + 11, white, true);
            Fx.draw(ctx, tr, Fx.trim(tr, np.artist() == null || np.artist().isBlank() ? np.source() : np.artist(), 88), this.barX + 46, this.barY + 23, grey, false);
            MusicScreen.eq(ctx, this.barX + 140, this.barY + 14, 12.0f, 1947988, np.playing());
        } else {
            Glass.layer(ctx, this.barX + 8, this.barY + 7, 32.0f, 32.0f, 6.0f, Glass.c((int)(48.0f * a), 0xFFFFFF), Glass.c((int)(24.0f * a), 0xFFFFFF));
            Icons.draw(ctx, IconShapes.Shape.NOTE, this.barX + 16, this.barY + 15, 16.0f, 12105924, (int)(140.0f * a));
            Fx.draw(ctx, tr, "Nothing playing", this.barX + 46, this.barY + 11, grey, false);
            Fx.draw(ctx, tr, "Pick a song", this.barX + 46, this.barY + 23, dim, false);
        }
        Object st = MusicHub.status();
        if (((String)st).isEmpty() && LocalPlayer.settingUp()) {
            st = LocalPlayer.statusLine();
        }
        if (((String)st).isEmpty() && np != null) {
            st = np.source() + (np.playing() ? "  -  playing" : "  -  paused");
        }
        int cxm = this.barX + 150 + (this.barW - 270) / 2;
        int sw = Math.min(this.barW - 270 + 60, 220);
        String shown = Fx.trim(tr, (String)st, sw);
        int col = MusicHub.busy() || LocalPlayer.settingUp() ? grey : (((String)st).isEmpty() ? dim : Glass.c(255, 16758891));
        Fx.draw(ctx, tr, shown, cxm - Fx.w(tr, shown) / 2, this.barY + 34, col, false);
        int vol = cfg.musicVolume;
        Icons.draw(ctx, vol <= 0 ? IconShapes.Shape.MUTE : IconShapes.Shape.VOLUME, vx - 18, vy - 4, 12.0f, 13158610, (int)(255.0f * a));
        boolean hot = overVol || this.dragVol;
        Glass.layer(ctx, vx, vy, vw, 4.0f, 2.0f, Glass.c(85, 0xFFFFFF), Glass.c(85, 0xFFFFFF));
        int fw = Math.max(0, Math.round((float)(vw * vol) / 100.0f));
        int n = fc = hot ? 1947988 : 0xFFFFFF;
        if (fw >= 2) {
            Glass.layer(ctx, vx, vy, fw, 4.0f, 2.0f, Glass.c(240, fc), Glass.c(240, fc));
        }
        if (hot) {
            Glass.fill(ctx, vx + fw - 4, vy - 2, 8.0f, 8.0f, 4.0f, Glass.c(255, 0xFFFFFF), Glass.c(255, 0xFFFFFF));
        }
    }

    private static class_327 font() {
        return class_310.method_1551().field_1772;
    }

    private static final class Nav
    extends class_4185
    implements CustomGlassWidget {
        private final String label;
        private final IconShapes.Shape icon;
        private final BooleanSupplier sel;
        private final Spring hov = new Spring(0.0f, 0.0f, 300.0f, 24.0f);
        private final Spring on = new Spring(0.0f, 0.0f, 300.0f, 24.0f);
        private long last = System.nanoTime();

        Nav(int x, int y, int w, int h, String label, IconShapes.Shape icon, BooleanSupplier sel, Runnable act) {
            super(x, y, w, h, (class_2561)class_2561.method_43470((String)label), b -> act.run(), field_40754);
            this.label = label;
            this.icon = icon;
            this.sel = sel;
            this.on.target = sel.getAsBoolean() ? 1.0f : 0.0f;
            this.on.pos = this.on.target;
        }

        public void method_75752(class_332 ctx, int mx, int my, float dt) {
        }

        @Override
        public void renderGlass(class_332 ctx, int mx, int my, float dt) {
            long now = System.nanoTime();
            float d = Anim.smooth((float)(now - this.last) / 1.0E9f);
            this.last = now;
            boolean s = this.sel.getAsBoolean();
            this.on.target = s ? 1.0f : 0.0f;
            this.hov.target = this.method_49606() ? 1.0f : 0.0f;
            this.on.step(d);
            this.hov.step(d);
            float k = MusicScreen.clamp01(this.on.pos);
            float hv = MusicScreen.clamp01(this.hov.pos);
            int x = this.method_46426();
            int y = this.method_46427();
            int w = this.method_25368();
            int h = this.method_25364();
            float bg = Math.max(0.55f * k, 0.3f * hv);
            if (bg > 0.02f) {
                Glass.layer(ctx, x, y, w, h, 8.0f, Glass.c((int)(72.0f * bg * 1.8f), 0xFFFFFF), Glass.c((int)(48.0f * bg * 1.8f), 0xFFFFFF));
            }
            int col = s ? -1 : Glass.lerp(-5658187, -1, hv);
            Icons.draw(ctx, this.icon, x + 8, (float)y + (float)(h - 12) / 2.0f, 12.0f, col & 0xFFFFFF, 255);
            Fx.draw(ctx, MusicScreen.font(), this.label, x + 26, y + (h - 8) / 2, col, s);
        }
    }

    private static final class Row
    extends class_4185
    implements CustomGlassWidget {
        private final int index;
        private final Supplier<List<MusicHub.Track>> src;
        private final Spring hov = new Spring(0.0f, 0.0f, 300.0f, 24.0f);
        private long last = System.nanoTime();

        Row(int x, int y, int w, int h, int index, Supplier<List<MusicHub.Track>> src, Consumer<MusicHub.Track> onPlay) {
            super(x, y, w, h, (class_2561)class_2561.method_43470((String)"Song"), b -> {
                List r = (List)src.get();
                if (index < r.size()) {
                    onPlay.accept((MusicHub.Track)r.get(index));
                }
            }, field_40754);
            this.index = index;
            this.src = src;
        }

        public void method_75752(class_332 ctx, int mx, int my, float dt) {
        }

        @Override
        public void renderGlass(class_332 ctx, int mx, int my, float dt) {
            List<MusicHub.Track> r = this.src.get();
            if (this.index >= r.size()) {
                return;
            }
            MusicHub.Track t = r.get(this.index);
            long now = System.nanoTime();
            float d = Anim.smooth((float)(now - this.last) / 1.0E9f);
            this.last = now;
            this.hov.target = this.method_49606() ? 1.0f : 0.0f;
            this.hov.step(d);
            float hv = MusicScreen.clamp01(this.hov.pos);
            int x = this.method_46426();
            int y = this.method_46427();
            int w = this.method_25368();
            int h = this.method_25364();
            class_327 tr = MusicScreen.font();
            boolean playing = MusicScreen.playingNow(t);
            if (hv > 0.02f) {
                Glass.layer(ctx, x, y, w, h, 8.0f, Glass.c((int)(44.0f * hv), 0xFFFFFF), Glass.c((int)(32.0f * hv), 0xFFFFFF));
            }
            if (hv > 0.5f) {
                Icons.draw(ctx, IconShapes.Shape.PLAY, x + 6, (float)y + (float)(h - 8) / 2.0f, 8.0f, 0xFFFFFF, 255);
            } else if (playing) {
                MusicScreen.eq(ctx, x + 7, (float)y + (float)(h - 8) / 2.0f, 8.0f, 1947988, true);
            } else {
                Fx.draw(ctx, tr, Integer.toString(this.index + 1), x + 6, y + (h - 8) / 2, -6645082, false);
            }
            MusicScreen.art(ctx, x + 22, y + 1, h - 2, MusicScreen.titleOf(t));
            int[] c = MusicScreen.cols(x, w, h);
            int ty = y + (h - 8) / 2;
            Fx.draw(ctx, tr, Fx.trim(tr, MusicScreen.titleOf(t), c[1]), c[0], ty, playing ? -14756000 : -1, playing);
            String sub = MusicScreen.artistOf(t);
            if (!sub.isEmpty()) {
                Fx.draw(ctx, tr, Fx.trim(tr, sub, c[3]), c[2], ty, -5658187, false);
            }
            String tag = MusicScreen.tagOf(t);
            int tc = MusicScreen.isSug(t) ? -7697770 : ("SPOTIFY".equals(t.provider()) ? -14756000 : -38045);
            Fx.draw(ctx, tr, tag, x + w - 8 - Fx.w(tr, tag), ty, tc, false);
        }
    }

    private static final class Tile
    extends class_4185
    implements CustomGlassWidget {
        private final int index;
        private final Supplier<List<MusicHub.Track>> src;
        private final Spring hov = new Spring(0.0f, 0.0f, 300.0f, 24.0f);
        private long last = System.nanoTime();

        Tile(int x, int y, int w, int h, int index, Supplier<List<MusicHub.Track>> src, Consumer<MusicHub.Track> onPlay) {
            super(x, y, w, h, (class_2561)class_2561.method_43470((String)"Song"), b -> {
                List r = (List)src.get();
                if (index < r.size()) {
                    onPlay.accept((MusicHub.Track)r.get(index));
                }
            }, field_40754);
            this.index = index;
            this.src = src;
        }

        public void method_75752(class_332 ctx, int mx, int my, float dt) {
        }

        @Override
        public void renderGlass(class_332 ctx, int mx, int my, float dt) {
            List<MusicHub.Track> r = this.src.get();
            if (this.index >= r.size()) {
                return;
            }
            MusicHub.Track t = r.get(this.index);
            long now = System.nanoTime();
            float d = Anim.smooth((float)(now - this.last) / 1.0E9f);
            this.last = now;
            this.hov.target = this.method_49606() ? 1.0f : 0.0f;
            this.hov.step(d);
            float hv = MusicScreen.clamp01(this.hov.pos);
            int x = this.method_46426();
            int y = this.method_46427();
            int w = this.method_25368();
            int h = this.method_25364();
            class_327 tr = MusicScreen.font();
            boolean playing = MusicScreen.playingNow(t);
            Glass.layer(ctx, x, y, w, h, 8.0f, Glass.c((int)(38.0f + 36.0f * hv), 0xFFFFFF), Glass.c((int)(22.0f + 28.0f * hv), 0xFFFFFF));
            MusicScreen.art(ctx, x, y, h, MusicScreen.titleOf(t));
            int tx = x + h + 8;
            int avail = w - h - 8 - 28;
            String sub = MusicScreen.artistOf(t);
            Fx.draw(ctx, tr, Fx.trim(tr, MusicScreen.titleOf(t), avail), tx, y + 6, playing ? -14756000 : -1, true);
            Fx.draw(ctx, tr, Fx.trim(tr, sub.isEmpty() ? MusicScreen.tagOf(t) : sub, avail), tx, y + 17, -5000259, false);
            if (playing && hv < 0.1f) {
                MusicScreen.eq(ctx, x + w - 16, (float)y + (float)(h - 9) / 2.0f, 9.0f, 1947988, true);
            }
            if (hv > 0.02f) {
                int A = (int)(255.0f * hv);
                float bx = x + w - 26;
                float by = (float)y + (float)(h - 18) / 2.0f + (1.0f - hv) * 4.0f;
                Glass.fill(ctx, bx, by, 18.0f, 18.0f, 9.0f, Glass.c(A, 4055158), Glass.c(A, 1353802));
                Icons.draw(ctx, IconShapes.Shape.PLAY, bx + 5.5f, by + 4.5f, 9.0f, 463627, A);
            }
        }
    }

    private static final class IconBtn
    extends class_4185
    implements CustomGlassWidget {
        private final IconShapes.Shape icon;
        private final Spring hov = new Spring(0.0f, 0.0f, 300.0f, 24.0f);
        private long last = System.nanoTime();

        IconBtn(int x, int y, int w, int h, IconShapes.Shape icon, Runnable act) {
            super(x, y, w, h, (class_2561)class_2561.method_43470((String)"btn"), b -> act.run(), field_40754);
            this.icon = icon;
        }

        public void method_75752(class_332 ctx, int mx, int my, float dt) {
        }

        @Override
        public void renderGlass(class_332 ctx, int mx, int my, float dt) {
            long now = System.nanoTime();
            float d = Anim.smooth((float)(now - this.last) / 1.0E9f);
            this.last = now;
            this.hov.target = this.method_49606() ? 1.0f : 0.0f;
            this.hov.step(d);
            float hv = MusicScreen.clamp01(this.hov.pos);
            float s = 12.0f + 2.0f * hv;
            int c = Glass.lerp(-5000259, -1, hv) & 0xFFFFFF;
            Icons.draw(ctx, this.icon, (float)this.method_46426() + ((float)this.method_25368() - s) / 2.0f, (float)this.method_46427() + ((float)this.method_25364() - s) / 2.0f, s, c, 255);
        }
    }

    private static final class PlayBtn
    extends class_4185
    implements CustomGlassWidget {
        private final Spring hov = new Spring(0.0f, 0.0f, 300.0f, 24.0f);
        private long last = System.nanoTime();

        PlayBtn(int x, int y, int d) {
            super(x, y, d, d, (class_2561)class_2561.method_43470((String)"play"), b -> MediaControl.send(MediaControl.Cmd.TOGGLE), field_40754);
        }

        public void method_75752(class_332 ctx, int mx, int my, float dt) {
        }

        @Override
        public void renderGlass(class_332 ctx, int mx, int my, float dt) {
            long now = System.nanoTime();
            float d = Anim.smooth((float)(now - this.last) / 1.0E9f);
            this.last = now;
            this.hov.target = this.method_49606() ? 1.0f : 0.0f;
            this.hov.step(d);
            float hv = MusicScreen.clamp01(this.hov.pos);
            StreamData.NowPlaying np = StreamHud.DATA.song;
            boolean playing = np != null && np.playing();
            float grow = hv * 1.5f;
            float x = (float)this.method_46426() - grow;
            float y = (float)this.method_46427() - grow;
            float sz = (float)this.method_25368() + 2.0f * grow;
            Glass.fill(ctx, x, y, sz, sz, sz / 2.0f, -1, Glass.lerp(-1644820, -1, hv));
            float is = sz * 0.5f;
            Icons.draw(ctx, playing ? IconShapes.Shape.PAUSE : IconShapes.Shape.PLAY, x + (sz - is) / 2.0f + (playing ? 0.0f : is * 0.06f), y + (sz - is) / 2.0f, is, 0x101014, 255);
        }
    }

    private static enum View {
        HOME,
        SEARCH,
        LIBRARY;

    }
}

