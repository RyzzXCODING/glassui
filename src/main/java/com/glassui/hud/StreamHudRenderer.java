/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassConfig;
import com.glassui.GlassUiClient;
import com.glassui.hud.ComboTracker;
import com.glassui.hud.IslandSearch;
import com.glassui.hud.LiveAlerts;
import com.glassui.hud.MediaControl;
import com.glassui.hud.MusicToggle;
import com.glassui.hud.StreamData;
import com.glassui.hud.StreamHud;
import com.glassui.hud.TapTracker;
import com.glassui.music.Lyrics;
import com.glassui.music.MusicHub;
import com.glassui.music.SongRequests;
import com.glassui.ui.Anim;
import com.glassui.ui.Fx;
import com.glassui.ui.Glass;
import com.glassui.ui.HudEditorScreen;
import com.glassui.ui.IconShapes;
import com.glassui.ui.Icons;
import com.glassui.ui.MusicScreen;
import com.glassui.ui.PlayerHead;
import com.glassui.ui.Spring;
import java.text.Bidi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_746;
import net.minecraft.class_9779;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

public final class StreamHudRenderer {
    private static final Id[] DRAW_ORDER = new Id[]{Id.CHAT, Id.SONG, Id.REQUESTS, Id.TOPTAP, Id.CASSETTE, Id.LYRICS, Id.PROFILE, Id.COMBO, Id.ALERT, Id.ISLAND};
    private static final int[] NAME_COLORS = new int[]{16752394, 6607615, 3199320, 12540658, 16737410, 16766474, 6200575, 6739151};
    private static final AnimNum VIEWS = new AnimNum();
    private static final AnimNum LIKES = new AnimNum();
    private static final Spring EXPAND = new Spring(0.0f, 0.0f, 260.0f, 23.0f);
    private static final Spring SEARCH = new Spring(0.0f, 0.0f, 260.0f, 23.0f);
    private static final int SP_W = 204;
    private static final int SP_H = 140;
    private static final int SP_FIELD_Y = 30;
    private static final int SP_ROW0 = 52;
    private static final int SP_ROW_H = 18;
    private static final int SP_ROWS = 4;
    private static boolean expanded;
    private static boolean prevDown;
    private static long lastWarnMs;
    private static boolean volDrag;
    private static R lastIsland;
    private static long lastNs;
    private static final int CTRL_Y0 = 51;
    private static final int CTRL_Y1 = 69;
    private static final int CTRL_HALF = 13;
    private static final int CTRL_GAP = 30;
    private static final int EXP_H = 86;
    private static final int VOL_Y = 75;
    private static final int VOL_Y0 = 70;
    private static final int VOL_Y1 = 84;
    private static final int VOL_X0 = 28;
    private static final int VOL_PAD_R = 16;
    private static Snap cachedSnap;
    private static long snapAt;
    private static Snap demoCache;
    private static int demoLines;
    private static final int REQ_W = 176;
    private static final int REQ_HEAD = 22;
    private static final int REQ_ROW = 22;
    private static final int REQ_MAX_ROWS = 5;
    private static final ReqView REQ_PREVIEW;
    private static final Map<String, String> FIT;
    private static final int TT_W = 176;
    private static final int TT_HEAD = 22;
    private static final int TT_ROW = 20;
    private static final int[] RANK_COLORS;
    private static final int CAS_W = 232;
    private static final int CAS_H = 34;
    private static final int CAS_BTN_X = 198;
    private static final int CAS_BTN_Y = 5;
    private static final int CAS_BTN = 24;
    private static float discAng;
    private static float discVel;
    private static boolean casDown;
    private static final int LYR_MAX_W = 184;
    private static final int LYR_PAD = 7;
    private static final int LYR_LINE = 10;
    private static final int LYR_SIDE = 10;
    private static final int LYR_MAX_LINES = 3;
    private static final float[] LYR_STEPS;
    private static float lyrW;
    private static float lyrH;
    private static float lyrAnim;
    private static int lyrIdx;
    private static final Map<String, LyrFit> LYR_FITS;
    private static final List<Lyrics.Line> LYR_SAMPLE;
    private static final int CHAT_W = 176;
    private static final int CHAT_PAD_X = 6;
    private static final int CHAT_PAD_Y = 4;
    private static final int CHAT_LINE = 10;
    private static final int CHAT_GAP = 3;
    private static final int CHAT_NAME_MAX = 60;
    private static final int CHAT_MAX_LINES = 60;
    private static float chatMaxH;
    private static final Map<StreamData.ChatLine, Wrap> WRAPS;
    private static final Map<StreamData.ChatLine, BubbleAnim> BANIM;
    private static final long FRESH_NS = 700000000L;
    private static final int COMBO_W = 96;
    private static final int COMBO_H = 30;
    private static boolean previewMode;
    private static int comboKey;
    private static String comboTxt;
    private static final int ALERT_W = 176;
    private static final int ALERT_H = 34;
    private static final int PROFILE_W = 164;
    private static final int PROFILE_ROW = 12;
    private static final int PROFILE_H0 = 65;
    private static final int TOP = 20;
    private static final int BAR_X = 56;
    private static final int BAR_W = 64;
    private static final int BAR_H = 7;
    private static final int HP_COL = 16731486;
    private static final int FOOD_COL = 16752394;
    private static final int ARMOR_COL = 10336472;
    private static final int AIR_COL = 4245759;
    private static final int GOLD = 16766474;
    private static final Bar HP;
    private static final Bar FOOD;
    private static final Bar ARMOR;
    private static final Bar AIR;
    private static final String[] V_TXT;
    private static final int[] V_KEY;
    private static float airShow;
    private static String nameSrc;
    private static String nameTxt;
    private static int nameFont;
    private static int wrapFont;
    private static final Map<String, class_2561> BOLD;
    private static final Map<String, class_2561> PLAIN;
    private static int textWFont;
    private static final IdentityHashMap<class_2561, Integer> TEXT_W;
    private static StreamData.NowPlaying subFor;
    private static String subText;

    public static R islandRect() {
        return lastIsland;
    }

    private StreamHudRenderer() {
    }

    public static void render(class_332 ctx, class_9779 tc) {
        try {
            StreamHudRenderer.draw(ctx);
        }
        catch (Throwable t) {
            long now = System.currentTimeMillis();
            if (now - lastWarnMs >= 10000L) {
                lastWarnMs = now;
                GlassUiClient.LOGGER.error("Stream HUD render failed", t);
            }
        }
    }

    private static void draw(class_332 ctx) {
        class_310 mc = class_310.method_1551();
        GlassConfig cfg = GlassConfig.get();
        if (mc == null || !cfg.hudEnabled || mc.field_1690.field_1842) {
            StreamHudRenderer.endVolDrag();
            return;
        }
        if (mc.field_1755 instanceof HudEditorScreen) {
            StreamHudRenderer.endVolDrag();
            return;
        }
        if (!(cfg.island.on || cfg.chat.on || cfg.song.on || cfg.profile.on || cfg.combo.on || cfg.alert.on || cfg.cassette.on || cfg.lyrics.on || cfg.requests.on || cfg.topTap.on)) {
            StreamHudRenderer.endVolDrag();
            return;
        }
        long now = System.nanoTime();
        float dt = lastNs == 0L ? 0.016f : Anim.smooth((float)(now - lastNs) / 1.0E9f);
        lastNs = now;
        float t = (float)(System.currentTimeMillis() % 100000L) / 1000.0f;
        if (mc.field_1755 == null) {
            IslandSearch.close();
        }
        StreamHudRenderer.EXPAND.target = (expanded || IslandSearch.isOpen()) && mc.field_1755 != null ? 1.0f : 0.0f;
        EXPAND.step(dt);
        StreamHudRenderer.SEARCH.target = IslandSearch.isOpen() && mc.field_1755 != null ? 1.0f : 0.0f;
        SEARCH.step(dt);
        StreamHudRenderer.frame(ctx, mc.field_1772, cfg, StreamHudRenderer.snapshot(cfg), dt, t, false, null);
    }

    public static void drawPreview(class_332 ctx, Id selected, float dt) {
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return;
        }
        float t = (float)(System.currentTimeMillis() % 100000L) / 1000.0f;
        StreamHudRenderer.frame(ctx, mc.field_1772, GlassConfig.get(), StreamHudRenderer.demo(), dt, t, true, selected);
    }

    public static R rect(Id id, int sw, int sh) {
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return null;
        }
        float[] sz;
        class_327 tr = mc.field_1772;
        GlassConfig cfg = GlassConfig.get();
        chatMaxH = id == Id.CHAT ? (float)sh / Math.max(0.5f, Math.min(2.5f, StreamHudRenderer.widget(cfg, id).scale)) - 4.0f : 1.0E9f;
        previewMode = true;
        if (id == Id.LYRICS) {
            StreamHudRenderer.lyrFit(sw, StreamHudRenderer.widget((GlassConfig)cfg, (Id)id).scale);
        }
        if ((sz = StreamHudRenderer.size(id, StreamHudRenderer.demo(), tr, id == Id.PROFILE ? 1.0f : 0.0f)) == null) {
            return null;
        }
        R r = StreamHudRenderer.place(StreamHudRenderer.widget(cfg, id), sz[0], sz[1], sw, sh);
        return id == Id.ISLAND && cfg.islandDock ? new R(r.x, 0.0f, r.w, r.h, r.s) : r;
    }

    public static GlassConfig.Widget widget(GlassConfig cfg, Id id) {
        return switch (id.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> cfg.island;
            case 1 -> cfg.chat;
            case 2 -> cfg.song;
            case 3 -> cfg.profile;
            case 4 -> cfg.combo;
            case 5 -> cfg.alert;
            case 6 -> cfg.cassette;
            case 7 -> cfg.lyrics;
            case 8 -> cfg.requests;
            case 9 -> cfg.topTap;
        };
    }

    public static String label(Id id) {
        return switch (id.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "Island (views + likes)";
            case 1 -> "Live chat";
            case 2 -> "Now playing";
            case 3 -> "Profile (HP / food / armor / air)";
            case 4 -> "Combo counter";
            case 5 -> "Gift / follow pop-up";
            case 6 -> "Music disc (on/off)";
            case 7 -> "Lyrics";
            case 8 -> "Song requests";
            case 9 -> "Top tappers";
        };
    }

    private static Snap snapshot(GlassConfig cfg) {
        long now = System.nanoTime();
        if (cachedSnap != null && now - snapAt < 100000000L) {
            return cachedSnap;
        }
        snapAt = now;
        cachedSnap = StreamHudRenderer.buildSnapshot(cfg);
        return cachedSnap;
    }

    private static Snap buildSnapshot(GlassConfig cfg) {
        StreamData d = StreamHud.DATA;
        long v = d.watching >= 0L && d.live ? d.watching : d.views;
        ArrayList<StreamData.ChatLine> all = new ArrayList<StreamData.ChatLine>(d.chat);
        ArrayList<StreamData.ChatLine> lines = new ArrayList<StreamData.ChatLine>(all.subList(Math.max(0, all.size() - cfg.chatLines), all.size()));
        return new Snap(cfg.isLinkReady(), d.live, v, d.likes, d.linkState, lines, d.song, StreamHudRenderer.vitals());
    }

    private static Snap demo() {
        GlassConfig cfg = GlassConfig.get();
        if (demoCache != null && demoLines == cfg.chatLines) {
            return demoCache;
        }
        demoLines = cfg.chatLines;
        demoCache = StreamHudRenderer.buildDemo(cfg);
        return demoCache;
    }

    private static Snap buildDemo(GlassConfig cfg) {
        List<StreamData.ChatLine> lines = new ArrayList<StreamData.ChatLine>(List.of(new StreamData.ChatLine("andi", "halo kak!"), new StreamData.ChatLine("sasa_", "gas terus"), new StreamData.ChatLine("budi99", "rumahnya keren banget kak, desain interiornya rapi dan lampunya bagus, tutorial dong"), new StreamData.ChatLine("mira", "subscribe ya")));
        lines = lines.subList(0, Math.min(lines.size(), cfg.chatLines));
        return new Snap(true, true, 1280L, 3450L, "LIVE", lines, new StreamData.NowPlaying("Blinding Lights", "The Weeknd", "Spotify", true, ""), new Vitals(14.0f, 20.0f, 0.0f, 16, 12, 210, 300, 27, 0.6f, "Steve"));
    }

    private static float[] size(Id id, Snap s, class_327 tr, float e) {
        switch (id.ordinal()) {
            case 0: {
                float base = !s.ready ? (float)(24 + Fx.w(tr, StreamHudRenderer.notReadyText(s)) + 12) : (float)(37 + Fx.w(tr, StreamHudRenderer.fmt(s.views)) + 9 + 12 + Fx.w(tr, StreamHudRenderer.fmt(s.likes)) + 14);
                float w0 = Math.max(124.0f, base);
                float w1 = Math.max(w0, 184.0f);
                e = Math.max(0.0f, e);
                float ww = w0 + (w1 - w0) * e;
                float hh = 24.0f + 62.0f * e;
                float q = Math.max(0.0f, Math.min(1.0f, StreamHudRenderer.SEARCH.pos));
                return new float[]{ww + (Math.max(ww, 204.0f) - ww) * q, hh + (140.0f - hh) * q};
            }
            case 1: {
                if (s.chat.isEmpty()) {
                    return null;
                }
                int first = StreamHudRenderer.chatFirst(s.chat, tr, chatMaxH);
                float total = 0.0f;
                for (int i = first; i < s.chat.size(); ++i) {
                    total += (float)(StreamHudRenderer.wrapOf((class_327)tr, (StreamData.ChatLine)s.chat.get((int)i)).h + (i > first ? 3 : 0));
                }
                return new float[]{176.0f, total};
            }
            case 2: {
                float[] fArray;
                if (s.song == null) {
                    fArray = null;
                } else {
                    float[] fArray2 = new float[2];
                    fArray2[0] = 176.0f;
                    fArray = fArray2;
                    fArray2[1] = 36.0f;
                }
                return fArray;
            }
            case 4: {
                float[] fArray;
                if (previewMode || ComboTracker.active()) {
                    float[] fArray3 = new float[2];
                    fArray3[0] = 96.0f;
                    fArray = fArray3;
                    fArray3[1] = 30.0f;
                } else {
                    fArray = null;
                }
                return fArray;
            }
            case 5: {
                float[] fArray;
                if (previewMode || LiveAlerts.active()) {
                    float[] fArray4 = new float[2];
                    fArray4[0] = 176.0f;
                    fArray = fArray4;
                    fArray4[1] = 34.0f;
                } else {
                    fArray = null;
                }
                return fArray;
            }
            case 6: {
                return new float[]{232.0f, 34.0f};
            }
            case 7: {
                float[] fArray;
                if (previewMode || s.song != null) {
                    float[] fArray5 = new float[2];
                    fArray5[0] = lyrW;
                    fArray = fArray5;
                    fArray5[1] = lyrH;
                } else {
                    fArray = null;
                }
                return fArray;
            }
            case 8: {
                return StreamHudRenderer.reqSize(StreamHudRenderer.reqView(previewMode));
            }
            case 9: {
                return StreamHudRenderer.ttSize(TapTracker.view(previewMode));
            }
            case 3: {
                float[] fArray;
                if (s.vitals == null) {
                    fArray = null;
                } else {
                    float[] fArray6 = new float[2];
                    fArray6[0] = 164.0f;
                    fArray = fArray6;
                    fArray6[1] = 65.0f + 12.0f * Math.max(0.0f, Math.min(1.0f, e));
                }
                return fArray;
            }
        }
        return null;
    }

    private static R place(GlassConfig.Widget w, float W, float H, int sw, int sh) {
        float sc = Math.max(0.5f, Math.min(2.5f, w.scale));
        float pw = W * sc;
        float ph = H * sc;
        float left = w.x * (float)sw - pw / 2.0f;
        float top = w.y * (float)sh;
        left = Math.max(0.0f, Math.min(Math.max(0.0f, (float)sw - pw), left));
        top = Math.max(0.0f, Math.min(Math.max(0.0f, (float)sh - ph), top));
        return new R(left, top, pw, ph, sc);
    }

    /*
     * Reconstructed from bytecode: CFR emitted this method as unstructured
     * pseudocode. It was rewritten with explicit local declarations and a
     * normal switch, preserving the original draw order and matrix handling.
     */
    private static void frame(class_332 ctx, class_327 tr, GlassConfig cfg, Snap s, float dt, float t, boolean preview, Id selected) {
        int sw = ctx.method_51421();
        int sh = ctx.method_51443();
        Matrix3x2fStack m = ctx.method_51448();
        StreamHudRenderer.previewMode = preview;
        for (Id id : StreamHudRenderer.DRAW_ORDER) {
            GlassConfig.Widget wc = StreamHudRenderer.widget(cfg, id);
            if (id == Id.ISLAND && !wc.on && !preview) {
                StreamHudRenderer.endVolDrag();
            }
            if (!wc.on && !preview) continue;
            float e = id == Id.ISLAND && preview == false ? StreamHudRenderer.EXPAND.pos : 0.0f;
            if (id == Id.PROFILE) {
                e = StreamHudRenderer.airAnim(cfg, s, dt, preview);
            }
            StreamHudRenderer.chatMaxH = id == Id.CHAT ? (float)sh / Math.max(0.5f, Math.min(2.5f, wc.scale)) - 4.0f : 1.0E9f;
            if (id == Id.LYRICS) {
                StreamHudRenderer.lyrFit(sw, wc.scale);
            }
            float[] sz = StreamHudRenderer.size(id, s, tr, e);
            if (sz == null) continue;
            R r = StreamHudRenderer.place(wc, sz[0], sz[1], sw, sh);
            if (id == Id.ISLAND && cfg.islandDock) {
                r = new R(r.x, 0.0f, r.w, r.h, r.s);
            }
            if (id == Id.ISLAND && !preview) {
                StreamHudRenderer.lastIsland = r;
            }
            double lx = -1000.0;
            double ly = -1000.0;
            if (!(id != Id.ISLAND && id != Id.CASSETTE || preview)) {
                class_310 mc = class_310.method_1551();
                if (mc != null && mc.field_1755 != null) {
                    double mx = mc.field_1729.method_1603() * (double)sw / (double)mc.method_22683().method_4480();
                    double my = mc.field_1729.method_1604() * (double)sh / (double)mc.method_22683().method_4507();
                    lx = (mx - (double)r.x) / (double)r.s;
                    ly = (my - (double)r.y) / (double)r.s;
                }
            }
            m.pushMatrix();
            m.translate(r.x, r.y);
            m.scale(r.s, r.s);
            try {
                switch (id.ordinal()) {
                    case 0: {
                        StreamHudRenderer.island(ctx, tr, s, sz[0], sz[1], e, t, dt, lx, ly);
                        break;
                    }
                    case 1: {
                        StreamHudRenderer.chat(ctx, tr, s.chat, dt, preview);
                        break;
                    }
                    case 2: {
                        StreamHudRenderer.song(ctx, tr, s.song, (int)sz[0], t);
                        break;
                    }
                    case 3: {
                        StreamHudRenderer.profile(ctx, tr, s.vitals, sz[0], sz[1], e, t, dt);
                        break;
                    }
                    case 4: {
                        StreamHudRenderer.combo(ctx, tr, dt, preview);
                        break;
                    }
                    case 5: {
                        StreamHudRenderer.alert(ctx, tr, dt, preview);
                        break;
                    }
                    case 6: {
                        StreamHudRenderer.cassette(ctx, tr, s.song, t, dt, lx, ly);
                        break;
                    }
                    case 7: {
                        StreamHudRenderer.lyrics(ctx, tr, s.song, dt, preview);
                        break;
                    }
                    case 8: {
                        StreamHudRenderer.requests(ctx, tr, preview);
                        break;
                    }
                    case 9: {
                        StreamHudRenderer.topTap(ctx, tr, preview);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
            finally {
                m.popMatrix();
            }
            if (preview) {
                StreamHudRenderer.outline(ctx, tr, r, StreamHudRenderer.label(id), id == selected, wc.on);
                continue;
            }
            if (id == Id.ISLAND) {
                StreamHudRenderer.clickIsland(ctx, r, s);
                continue;
            }
            if (id != Id.CASSETTE) continue;
            StreamHudRenderer.clickCassette(ctx, r);
        }
    }

    private static void outline(class_332 ctx, class_327 tr, R r, String name, boolean sel, boolean on) {
        int col = !on ? -47814 : (sel ? -10742 : -1862270977);
        int x0 = Math.round(r.x) - 3;
        int y0 = Math.round(r.y) - 3;
        int x1 = Math.round(r.x + r.w) + 3;
        int y1 = Math.round(r.y + r.h) + 3;
        Glass.ringHard(ctx, x0, y0, x1 - x0, y1 - y0, 8.0f, sel ? 0.9f : 0.6f, col, col);
        String text = name + (on ? "" : " (off)");
        int tw = Fx.w(tr, text);
        int ty = Math.max(2, y0 - 15);
        Glass.buttonLite(ctx, x0, ty, tw + 12, 13, 6, 0x202030, 1.2f, 1.0f);
        Fx.draw(ctx, tr, text, x0 + 6, ty + 3, col, true);
    }

    private static void clickIsland(class_332 ctx, R r, Snap s) {
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            StreamHudRenderer.endVolDrag();
            return;
        }
        if (mc.field_1755 == null) {
            expanded = false;
            IslandSearch.close();
            prevDown = true;
            StreamHudRenderer.endVolDrag();
            return;
        }
        boolean down = GLFW.glfwGetMouseButton((long)GLFW.glfwGetCurrentContext(), (int)0) == 1;
        boolean edge = down && !prevDown;
        prevDown = down;
        double mx = mc.field_1729.method_1603() * (double)ctx.method_51421() / (double)mc.method_22683().method_4480();
        double my = mc.field_1729.method_1604() * (double)ctx.method_51443() / (double)mc.method_22683().method_4507();
        if (volDrag) {
            if (down) {
                MusicHub.setVolume(StreamHudRenderer.volAt((mx - (double)r.x) / (double)r.s, r.w / r.s));
            } else {
                StreamHudRenderer.endVolDrag();
            }
            return;
        }
        if (!edge) {
            return;
        }
        if (!r.has(mx, my)) {
            expanded = false;
            IslandSearch.close();
            return;
        }
        if (GlassConfig.get().islandOpensMusic) {
            class_437 class_4372 = mc.field_1755;
            if (class_4372 instanceof MusicScreen) {
                MusicScreen ms = (MusicScreen)class_4372;
                ms.requestClose();
            } else {
                mc.method_1507((class_437)new MusicScreen(mc.field_1755));
            }
            return;
        }
        double lx = (mx - (double)r.x) / (double)r.s;
        double ly = (my - (double)r.y) / (double)r.s;
        double rw = r.w / r.s;
        double rh = r.h / r.s;
        boolean inSearch = IslandSearch.isOpen();
        if ((expanded || inSearch) && lx >= rw - 26.0 && lx <= rw - 4.0 && ly >= 2.0 && ly <= 22.0) {
            if (inSearch) {
                IslandSearch.close();
            } else {
                IslandSearch.open();
                expanded = true;
            }
            return;
        }
        if (inSearch) {
            if (ly >= 30.0 && ly < 48.0) {
                IslandSearch.focus();
                return;
            }
            int i = (int)((ly - 52.0) / 18.0);
            List<MusicHub.Track> res = MusicHub.listing();
            if (ly >= 52.0 && i >= 0 && i < 4 && i < res.size()) {
                MusicHub.play(res.get(i));
                return;
            }
            if (ly >= rh - 14.0 && lx > rw / 2.0) {
                GlassConfig c = GlassConfig.get();
                c.musicProvider = "YOUTUBE";
                GlassConfig.save();
                if (!IslandSearch.text().isBlank()) {
                    MusicHub.search(IslandSearch.text());
                }
            }
            return;
        }
        if (expanded && s.song != null && ly >= 70.0 && ly <= 84.0) {
            if (MusicHub.canVolume()) {
                volDrag = true;
                MusicHub.setVolume(StreamHudRenderer.volAt(lx, rw));
            }
            return;
        }
        if (expanded && s.song != null && ly >= 51.0 && ly <= 69.0) {
            double c = (double)(r.w / r.s) / 2.0;
            if (Math.abs(lx - (c - 30.0)) <= 13.0) {
                MediaControl.send(MediaControl.Cmd.PREV);
                return;
            }
            if (Math.abs(lx - c) <= 13.0) {
                MediaControl.send(MediaControl.Cmd.TOGGLE);
                return;
            }
            if (Math.abs(lx - (c + 30.0)) <= 13.0) {
                MediaControl.send(MediaControl.Cmd.NEXT);
                return;
            }
        }
        expanded = !expanded;
    }

    private static int volAt(double lx, double rw) {
        double x0 = 28.0;
        double x1 = rw - 16.0;
        return (int)Math.round(Math.max(0.0, Math.min(1.0, (lx - x0) / Math.max(1.0, x1 - x0))) * 100.0);
    }

    private static void endVolDrag() {
        if (!volDrag) {
            return;
        }
        volDrag = false;
        MusicHub.commitVolume();
    }

    private static void searchPanel(class_332 ctx, class_327 tr, int w, int h, int A, double lx, double ly) {
        int tc;
        float a = (float)A / 255.0f;
        boolean typing = IslandSearch.typing();
        Glass.inset(ctx, 10, 30, w - 20, 18, 7, typing, a);
        Object t = IslandSearch.text();
        if (((String)t).isEmpty() && !typing) {
            t = "Search a song...";
            tc = Glass.c(Math.max(24, A * 6 / 10), 0xA0A0AA);
        } else {
            if (typing && System.currentTimeMillis() / 500L % 2L == 0L) {
                t = (String)t + "_";
            }
            tc = Glass.c(A, 0xFFFFFF);
        }
        while (((String)t).length() > 1 && Fx.w(tr, (String)t) > w - 34) {
            String s = (String)t;
            t = s.substring(0, s.length() - 1);
        }
        Fx.draw(ctx, tr, (String)t, 16, 35, tc, false);
        List<MusicHub.Track> res = MusicHub.listing();
        boolean sugg = MusicHub.listingIsSuggestions();
        for (int i = 0; i < 4 && i < res.size(); ++i) {
            boolean hov;
            MusicHub.Track tk = res.get(i);
            int y = 52 + i * 18;
            boolean bl = hov = lx >= 8.0 && lx <= (double)(w - 8) && ly >= (double)y && ly < (double)(y + 18);
            if (hov) {
                Glass.fill(ctx, 8.0f, y, w - 16, 17.0f, 7.0f, Glass.c((int)(48.0f * a), 0xFFFFFF), Glass.c((int)(24.0f * a), 0xFFFFFF));
            }
            boolean isSug = "SUGGEST".equals(tk.provider());
            IconShapes.Shape shape = hov ? IconShapes.Shape.PLAY : (!isSug ? IconShapes.Shape.NOTE : ("Recent".equals(tk.artist()) ? IconShapes.Shape.CLOCK : ("Popular".equals(tk.artist()) ? IconShapes.Shape.TREND : IconShapes.Shape.PLAY)));
            int rowTint = isSug ? 6607615 : ("SPOTIFY".equals(tk.provider()) ? 3199320 : 16729402);
            Icons.drawShadowed(ctx, shape, 13.0f, y + 4, 9.0f, rowTint, A);
            Fx.draw(ctx, tr, Fx.trim(tr, tk.title(), w - 44), 28, y + 1, Glass.c(A, 0xFFFFFF), true);
            Fx.draw(ctx, tr, Fx.trim(tr, tk.artist(), w - 44), 28, y + 9, Glass.c(Math.max(24, A * 2 / 3), 0xB0B0BA), false);
        }
        String st = MusicHub.status();
        if (st.isEmpty()) {
            st = sugg ? "Tap a song to play, or type" : "";
        }
        Fx.draw(ctx, tr, Fx.trim(tr, st, w / 2 - 14), 12, h - 11, Glass.c(Math.max(24, A * 2 / 3), 16758891), false);
        String src = "SPOTIFY".equals(GlassConfig.get().musicProvider) ? "Spotify" : "YouTube";
        boolean srcHov = ly >= (double)(h - 14) && lx > (double)w / 2.0;
        String lab = "Source: " + src;
        Fx.draw(ctx, tr, lab, w - 12 - Fx.w(tr, lab), h - 11, Glass.c(A, srcHov ? 0xFFFFFF : 11061503), false);
    }

    private static String notReadyText(Snap s) {
        return s.link == null || s.link.isEmpty() ? "Not connected" : s.link;
    }

    private static void island(class_332 ctx, class_327 tr, Snap s, float W, float H, float e, float t, float dt, double lx, double ly) {
        boolean clip;
        int w = Math.round(W);
        int h = Math.round(H);
        float ex = Math.max(0.0f, Math.min(1.0f, e));
        int r = Math.round(12.0f + 6.0f * ex);
        if (GlassConfig.get().islandDock) {
            int up = r + 2;
            Glass.panel(ctx, 0, -up, w, h + up, r, 0xFFFFFF, 0.9f, 1.0f);
            Glass.layer(ctx, 1.0f, -up, w - 2, h + up - 1, Math.max(1, r - 1), Glass.c(58, 0), Glass.c(74, 0));
        } else {
            if (!GlassConfig.get().perfMode) {
                for (int i = 2; i >= 1; --i) {
                    Glass.roundedFill(ctx, -i, -i + 2, w + 2 * i, h + 2 * i, r + i, Glass.c(14, 0));
                }
            }
            Glass.layer(ctx, 0.0f, 0.0f, w, h, r, Glass.c(236, 328968), Glass.c(236, 328968));
            Glass.rim(ctx, 0, 0, w, h, r, Glass.c(85, 0xFFFFFF), Glass.c(20, 0xFFFFFF));
        }
        float pulse = s.live ? 0.65f + 0.35f * (float)Math.sin((double)t * 4.5) : 0.5f;
        int x = 10;
        Glass.roundedFill(ctx, x, 9, 6, 6, 3, Glass.c((int)(255.0f * pulse), s.live ? 16726832 : 9342611));
        x += 14;
        if (!s.ready) {
            Fx.draw(ctx, tr, StreamHudRenderer.notReadyText(s), x, 8, -5197640, false);
        } else {
            long v = VIEWS.show(s.views, dt);
            long l = LIKES.show(s.likes, dt);
            Icons.draw(ctx, IconShapes.Shape.EYE, x, 7.0f, 10.0f, 0xFFFFFF);
            String vt = StreamHudRenderer.fmt(v);
            Fx.draw(ctx, tr, vt, x += 13, 8, -1, true);
            Icons.draw(ctx, IconShapes.Shape.HEART, x += Fx.w(tr, vt) + 9, 7.0f, 10.0f, 16725855);
            Fx.draw(ctx, tr, StreamHudRenderer.fmt(l), x += 12, 8, -1, true);
        }
        if (ex < 0.35f) {
            return;
        }
        float a = Math.max(0.0f, Math.min(1.0f, (ex - 0.45f) / 0.55f));
        int A = (int)(255.0f * a);
        Glass.fill(ctx, 10.0f, 25.0f, w - 20, 0.5f, 0.0f, Glass.c((int)(48.0f * a), 0xFFFFFF), Glass.c((int)(48.0f * a), 0xFFFFFF));
        if (A < 24) {
            return;
        }
        float q = Math.max(0.0f, Math.min(1.0f, StreamHudRenderer.SEARCH.pos));
        boolean noteHov = lx >= (double)(w - 26) && lx <= (double)(w - 4) && ly >= 2.0 && ly <= 22.0;
        Icons.drawShadowed(ctx, IconShapes.Shape.NOTE, w - 21, 6.0f, 12.0f, q > 0.5f ? 3199320 : (noteHov ? 0xFFFFFF : 13158608), A);
        if (q > 0.5f) {
            StreamHudRenderer.searchPanel(ctx, tr, w, h, A, lx, ly);
            return;
        }
        StreamData.NowPlaying np = s.song;
        if (np == null) {
            String a1 = "Nothing playing";
            String a2 = "Open Spotify / YT Music / YouTube";
            Fx.draw(ctx, tr, a1, (w - Fx.w(tr, a1)) / 2, 33, Glass.c(A, 0xE0E0E8), false);
            Fx.draw(ctx, tr, a2, (w - Fx.w(tr, a2)) / 2, 46, Glass.c(Math.max(24, A * 2 / 3), 0xA0A0AA), false);
            return;
        }
        int tx0 = 12;
        int tx1 = w - 12;
        class_2561 it = StreamHudRenderer.bold(np.title());
        class_2561 is = StreamHudRenderer.plain(StreamHudRenderer.subtitle(np));
        boolean bl = clip = StreamHudRenderer.widthOf(tr, it) > tx1 - tx0 || StreamHudRenderer.widthOf(tr, is) > tx1 - tx0;
        if (clip) {
            ctx.method_44379(tx0, 28, tx1, 51);
        }
        StreamHudRenderer.marquee(ctx, tr, it, tx0, 29, tx1 - tx0, Glass.c(A, 0xFFFFFF));
        StreamHudRenderer.marquee(ctx, tr, is, tx0, 40, tx1 - tx0, Glass.c(Math.max(24, A * 3 / 4), 0xB4B4BE));
        if (clip) {
            ctx.method_44380();
        }
        double c = (double)w / 2.0;
        int[] cx = new int[]{(int)(c - 30.0), (int)c, (int)(c + 30.0)};
        IconShapes.Shape[] sh = new IconShapes.Shape[]{IconShapes.Shape.PREV, np.playing() ? IconShapes.Shape.PAUSE : IconShapes.Shape.PLAY, IconShapes.Shape.NEXT};
        for (int i = 0; i < 3; ++i) {
            boolean hov;
            boolean bl2 = hov = Math.abs(lx - (double)cx[i]) <= 13.0 && ly >= 51.0 && ly <= 69.0;
            if (hov) {
                Glass.fill(ctx, cx[i] - 13, 51.0f, 26.0f, 18.0f, 9.0f, Glass.c((int)(56.0f * a), 0xFFFFFF), Glass.c((int)(32.0f * a), 0xFFFFFF));
            }
            Icons.draw(ctx, sh[i], cx[i] - 6, 54.0f, 12.0f, 0xFFFFFF, A);
        }
        boolean canVol = MusicHub.canVolume();
        int vol = MusicHub.volume();
        int VA = canVol ? A : A * 2 / 5;
        float bx0 = 28.0f;
        float bx1 = w - 16;
        float bw = bx1 - bx0;
        float fw = bw * (float)vol / 100.0f;
        Icons.draw(ctx, vol == 0 ? IconShapes.Shape.MUTE : IconShapes.Shape.VOLUME, 11.0f, 69.0f, 12.0f, 0xFFFFFF, VA);
        Glass.fill(ctx, bx0, 73.0f, bw, 4.0f, 2.0f, Glass.c((int)((float)(64 * VA) / 255.0f), 0xFFFFFF), Glass.c((int)((float)(40 * VA) / 255.0f), 0xFFFFFF));
        if (fw >= 1.5f) {
            Glass.fill(ctx, bx0, 73.0f, fw, 4.0f, 2.0f, Glass.c(VA, 0xFFFFFF), Glass.c(VA * 3 / 4, 15132400));
        }
        boolean volHov = canVol && (volDrag || lx >= 22.0 && lx <= (double)(w - 6) && ly >= 70.0 && ly <= 84.0);
        Glass.roundedFill(ctx, Math.round(bx0 + fw) - (volHov ? 4 : 3), 75 - (volHov ? 4 : 3), volHov ? 8 : 6, volHov ? 8 : 6, volHov ? 4 : 3, Glass.c(VA, 0xFFFFFF));
    }

    private static void song(class_332 ctx, class_327 tr, StreamData.NowPlaying s, int W, float t) {
        boolean clip;
        Glass.panel(ctx, 0, 0, W, 36, 12, 0xFFFFFF, 1.0f, 1.0f);
        int base = switch (s.source()) {
            case "Spotify" -> 1947988;
            case "YT Music" -> 16731717;
            case "YouTube" -> 0xFF1A1A;
            default -> 8158346;
        };
        Glass.layer(ctx, 6.0f, 6.0f, 24.0f, 24.0f, 7.0f, Glass.c(255, Glass.lerp(0xFF000000 | base, -1, 0.2f)), Glass.c(255, Glass.lerp(0xFF000000 | base, -16777216, 0.35f)));
        Glass.ringHard(ctx, 6.0f, 6.0f, 24.0f, 24.0f, 7.0f, 0.5f, Glass.c(144, 0xFFFFFF), Glass.c(32, 0xFFFFFF));
        Icons.draw(ctx, IconShapes.Shape.NOTE, 12.0f, 12.0f, 12.0f, 0xFFFFFF);
        int tx0 = 36;
        int tx1 = W - 30;
        class_2561 st = StreamHudRenderer.bold(s.title());
        class_2561 ss = StreamHudRenderer.plain(StreamHudRenderer.subtitle(s));
        boolean bl = clip = StreamHudRenderer.widthOf(tr, st) > tx1 - tx0 || StreamHudRenderer.widthOf(tr, ss) > tx1 - tx0;
        if (clip) {
            ctx.method_44379(tx0, 4, tx1, 32);
        }
        StreamHudRenderer.marquee(ctx, tr, st, tx0, 8, tx1 - tx0, s.playing() ? -1 : -6250328);
        StreamHudRenderer.marquee(ctx, tr, ss, tx0, 20, tx1 - tx0, -4934466);
        if (clip) {
            ctx.method_44380();
        }
        for (int i = 0; i < 4; ++i) {
            int h = s.playing() ? 3 + (int)((Math.sin((double)t * (5.0 + (double)i * 1.7) + (double)i * 1.3) + 1.0) * 0.5 * 11.0) : 2;
            Glass.fill(ctx, W - 22 + i * 4, 26 - h, 2.0f, h, 1.0f, Glass.c(224, base), Glass.c(224, base));
        }
    }

    private static ReqView reqView(boolean preview) {
        return preview ? REQ_PREVIEW : new ReqView(SongRequests.current(), SongRequests.pending());
    }

    private static float[] reqSize(ReqView v) {
        int n = v.list().size();
        int shown = Math.min(5, n);
        int h = 22 + (v.cur() != null ? 22 : 0) + shown * 22 + 6;
        if (n > shown) {
            h += 12;
        }
        if (v.cur() == null && n == 0) {
            h += 12;
        }
        return new float[]{176.0f, h};
    }

    private static String fit(class_327 tr, String s, int maxW) {
        if (s == null) {
            return "";
        }
        String key = GlassConfig.get().font + "|" + maxW + "|" + s;
        String r = FIT.get(key);
        if (r == null) {
            String c = s.replaceAll("\\s+", " ").trim();
            if (Fx.w(tr, c) > maxW) {
                c = Fx.trim(tr, c, maxW - Fx.w(tr, "..")) + "..";
            }
            r = c;
            FIT.put(key, r);
        }
        return r;
    }

    private static void requests(class_332 ctx, class_327 tr, boolean preview) {
        ReqView v = StreamHudRenderer.reqView(preview);
        int W = 176;
        int H = (int)StreamHudRenderer.reqSize(v)[1];
        Glass.panel(ctx, 0, 0, W, H, 12, 0xFFFFFF, 1.0f, 1.0f);
        Icons.draw(ctx, IconShapes.Shape.NOTE, 8.0f, 6.0f, 10.0f, 0xFFFFFF);
        Fx.draw(ctx, tr, "Song requests", 22, 7, -1, true);
        int n = v.list().size();
        if (n > 0) {
            String c = n + " waiting";
            Fx.draw(ctx, tr, c, W - 8 - Fx.w(tr, c), 7, -4934466, false);
        }
        Glass.fill(ctx, 8.0f, 18.0f, W - 16, 1.0f, 0.5f, Glass.c(48, 0xFFFFFF), Glass.c(48, 0xFFFFFF));
        int y = 22;
        if (v.cur() != null) {
            StreamHudRenderer.reqRow(ctx, tr, v.cur(), y, 0, W);
            y += 22;
        }
        if (v.cur() == null && n == 0) {
            Fx.draw(ctx, tr, "Type !req song title", 8, y + 1, -4934466, false);
        }
        int shown = Math.min(5, n);
        for (int i = 0; i < shown; ++i) {
            StreamHudRenderer.reqRow(ctx, tr, v.list().get(i), y, i + 1, W);
            y += 22;
        }
        if (n > shown) {
            Fx.draw(ctx, tr, "+" + (n - shown) + " more", 8, y + 1, -4934466, false);
        }
    }

    private static void reqRow(class_332 ctx, class_327 tr, SongRequests.Req r, int y, int idx, int W) {
        int base = idx == 0 ? 3199320 : 6607615;
        Glass.fill(ctx, 8.0f, y + 3, 14.0f, 14.0f, 5.0f, Glass.c(192, base), Glass.c(192, base));
        if (idx == 0) {
            Icons.draw(ctx, IconShapes.Shape.NOTE, 11.0f, 6.0f, 8.0f, 0xFFFFFF);
        } else {
            String lab = String.valueOf(idx);
            Fx.draw(ctx, tr, lab, 8 + (14 - Fx.w(tr, lab)) / 2, y + 6, -1, true);
        }
        int tw = W - 28 - 8;
        Fx.draw(ctx, tr, StreamHudRenderer.fit(tr, r.track().title(), tw), 28, y + 2, idx == 0 ? -1 : -1513232, false);
        Fx.draw(ctx, tr, StreamHudRenderer.fit(tr, "by " + r.by(), tw), 28, y + 12, -4934466, false);
    }

    private static int ttRows(TapTracker.View v) {
        return Math.min(GlassConfig.get().topTapCount, v.top().size());
    }

    private static float[] ttSize(TapTracker.View v) {
        int rows = StreamHudRenderer.ttRows(v);
        return new float[]{176.0f, 22 + (rows == 0 ? 14 : rows * 20) + 6};
    }

    private static void topTap(class_332 ctx, class_327 tr, boolean preview) {
        TapTracker.View v = TapTracker.view(preview);
        int W = 176;
        int H = (int)StreamHudRenderer.ttSize(v)[1];
        float pulse = 0.6f + 0.4f * (float)Math.sin((float)(System.currentTimeMillis() % 100000L) / 1000.0f * 9.0f);
        Glass.panel(ctx, 0, 0, W, H, 12, 0xFFFFFF, 1.0f, 1.0f);
        Icons.draw(ctx, IconShapes.Shape.HEART, 8.0f, 6.0f, 10.0f, 16737410);
        Fx.draw(ctx, tr, "Top tappers", 22, 7, -1, true);
        if (v.tappingNow() > 0) {
            String c = v.tappingNow() + " tapping";
            int cw = Fx.w(tr, c);
            int dot = Glass.c((int)(255.0f * pulse), 3199320);
            Glass.fill(ctx, W - 8 - cw - 8, 10.0f, 5.0f, 5.0f, 2.5f, dot, dot);
            Fx.draw(ctx, tr, c, W - 8 - cw, 7, -13577896, false);
        }
        Glass.fill(ctx, 8.0f, 18.0f, W - 16, 1.0f, 0.5f, Glass.c(48, 0xFFFFFF), Glass.c(48, 0xFFFFFF));
        int rows = StreamHudRenderer.ttRows(v);
        int y = 22;
        if (rows == 0) {
            Fx.draw(ctx, tr, "Waiting for taps...", 8, y + 1, -4934466, false);
            return;
        }
        int i = 0;
        while (i < rows) {
            StreamHudRenderer.ttRow(ctx, tr, v.top().get(i), y, i + 1, W, pulse);
            ++i;
            y += 20;
        }
    }

    private static void ttRow(class_332 ctx, class_327 tr, TapTracker.Entry e, int y, int rank, int W, float pulse) {
        if (e.tapping()) {
            int glow = Glass.c((int)(20.0f + 28.0f * pulse), 3199320);
            Glass.fill(ctx, 4.0f, y, W - 8, 18.0f, 6.0f, glow, glow);
        }
        int base = RANK_COLORS[Math.min(rank, RANK_COLORS.length) - 1];
        Glass.fill(ctx, 8.0f, y + 2, 14.0f, 14.0f, 5.0f, Glass.c(192, base), Glass.c(192, base));
        String lab = String.valueOf(rank);
        Fx.draw(ctx, tr, lab, 8 + (14 - Fx.w(tr, lab)) / 2, y + 5, -1, true);
        String cnt = StreamHudRenderer.fmt(e.taps());
        int cw = Fx.w(tr, cnt);
        int right = W - 8;
        Fx.draw(ctx, tr, cnt, right - cw, y + 5, e.tapping() ? -13577896 : -4934466, e.tapping());
        Fx.draw(ctx, tr, StreamHudRenderer.fit(tr, e.name(), right - cw - 6 - 28), 28, y + 5, e.tapping() ? -1 : -1513232, false);
    }

    private static int sourceColor(String src) {
        return switch (src) {
            case "Spotify" -> 1947988;
            case "YT Music" -> 16731717;
            case "YouTube" -> 0xFF1A1A;
            default -> 16756768;
        };
    }

    private static void cassette(class_332 ctx, class_327 tr, StreamData.NowPlaying np, float t, float dt, double lx, double ly) {
        boolean clip;
        boolean on;
        GlassConfig cfg = GlassConfig.get();
        boolean has = np != null;
        boolean bl = on = has && np.playing();
        if (cfg.perfMode) {
            Glass.buttonLite(ctx, 0, 0, 232, 34, 12, 0xFFFFFF, 1.0f, 1.0f);
        } else {
            Glass.panel(ctx, 0, 0, 232, 34, 12, 0xFFFFFF, 1.0f, 1.0f);
        }
        discVel += ((on ? 4.2f : 0.0f) - discVel) * Math.min(1.0f, dt * 3.0f);
        discAng += discVel * dt;
        int base = has ? StreamHudRenderer.sourceColor(np.source()) : 8158346;
        float cx = 19.0f;
        float cy = 17.0f;
        float R2 = 13.0f;
        Glass.layer(ctx, cx - R2, cy - R2, R2 * 2.0f, R2 * 2.0f, R2, Glass.c(244, 2894902), Glass.c(244, 526348));
        for (float rr : new float[]{11.0f, 9.0f}) {
            Glass.ringHard(ctx, cx - rr, cy - rr, rr * 2.0f, rr * 2.0f, rr, 0.4f, Glass.c(56, 0xFFFFFF), Glass.c(24, 0xFFFFFF));
        }
        Glass.layer(ctx, cx - 6.0f, cy - 6.0f, 12.0f, 12.0f, 6.0f, Glass.c(255, Glass.lerp(0xFF000000 | base, -1, 0.25f)), Glass.c(255, Glass.lerp(0xFF000000 | base, -16777216, 0.3f)));
        for (int i = 0; i < 2; ++i) {
            double a = (double)discAng + (double)i * Math.PI;
            Glass.layer(ctx, cx + (float)Math.cos(a) * 9.5f - 1.2f, cy + (float)Math.sin(a) * 9.5f - 1.2f, 2.4f, 2.4f, 1.2f, Glass.c(176, 0xFFFFFF), Glass.c(96, 0xFFFFFF));
        }
        double pa = (double)discAng * 1.0 + 1.5707963267948966;
        Glass.layer(ctx, cx + (float)Math.cos(pa) * 3.6f - 0.9f, cy + (float)Math.sin(pa) * 3.6f - 0.9f, 1.8f, 1.8f, 0.9f, Glass.c(240, 0xFFFFFF), Glass.c(240, 0xFFFFFF));
        Glass.layer(ctx, cx - 1.6f, cy - 1.6f, 3.2f, 3.2f, 1.6f, Glass.c(255, 657934), Glass.c(255, 657934));
        Glass.ringHard(ctx, cx - R2, cy - R2, R2 * 2.0f, R2 * 2.0f, R2, 0.5f, Glass.c(176, 0xFFFFFF), Glass.c(64, 0xFFFFFF));
        int tx0 = 40;
        int tx1 = 166;
        String sub = has ? StreamHudRenderer.subtitle(np) : (cfg.cassetteKey > 0 ? "Press " + MusicToggle.keyLabel(cfg.cassetteKey) + " to play music" : "Tap the button to play");
        class_2561 st = StreamHudRenderer.bold(has ? np.title() : "Music off");
        class_2561 ss = StreamHudRenderer.plain(sub);
        boolean bl2 = clip = StreamHudRenderer.widthOf(tr, st) > tx1 - tx0 || StreamHudRenderer.widthOf(tr, ss) > tx1 - tx0;
        if (clip) {
            ctx.method_44379(tx0, 3, tx1, 31);
        }
        StreamHudRenderer.marquee(ctx, tr, st, tx0, 8, tx1 - tx0, on ? -1 : -6250328);
        StreamHudRenderer.marquee(ctx, tr, ss, tx0, 20, tx1 - tx0, -4934466);
        if (clip) {
            ctx.method_44380();
        }
        if (cfg.cassetteKey > 0) {
            String k = MusicToggle.keyLabel(cfg.cassetteKey);
            int kw = Math.max(14, Fx.w(tr, k) + 8);
            int kx = 192 - kw;
            Glass.layer(ctx, kx, 10.0f, kw, 14.0f, 5.0f, Glass.c(96, 0), Glass.c(64, 0));
            Glass.ringHard(ctx, kx, 10.0f, kw, 14.0f, 5.0f, 0.5f, Glass.c(112, 0xFFFFFF), Glass.c(48, 0xFFFFFF));
            Fx.draw(ctx, tr, k, kx + (kw - Fx.w(tr, k)) / 2, 14, -2039574, false);
        }
        int accent = on ? 3199320 : (has ? 16756768 : 7237242);
        boolean hov = lx >= 196.0 && lx <= 224.0 && ly >= 3.0 && ly <= 31.0;
        int top = Glass.lerp(0xFF000000 | accent, -1, hov ? 0.45f : 0.25f);
        int bot = Glass.lerp(0xFF000000 | accent, -16777216, 0.3f);
        Glass.layer(ctx, 198.0f, 5.0f, 24.0f, 24.0f, 12.0f, Glass.c(240, top), Glass.c(240, bot));
        Glass.ringHard(ctx, 198.0f, 5.0f, 24.0f, 24.0f, 12.0f, 0.5f, Glass.c(192, 0xFFFFFF), Glass.c(64, 0xFFFFFF));
        Icons.drawShadowed(ctx, IconShapes.Shape.POWER, 204.0f, 11.0f, 12.0f, 0xFFFFFF, 255);
    }

    private static void clickCassette(class_332 ctx, R r) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1755 == null) {
            casDown = true;
            return;
        }
        boolean down = GLFW.glfwGetMouseButton((long)GLFW.glfwGetCurrentContext(), (int)0) == 1;
        boolean edge = down && !casDown;
        casDown = down;
        if (!edge) {
            return;
        }
        double mx = mc.field_1729.method_1603() * (double)ctx.method_51421() / (double)mc.method_22683().method_4480();
        double my = mc.field_1729.method_1604() * (double)ctx.method_51443() / (double)mc.method_22683().method_4507();
        double lx = (mx - (double)r.x) / (double)r.s;
        double ly = (my - (double)r.y) / (double)r.s;
        if (lx >= 196.0 && lx <= 224.0 && ly >= 3.0 && ly <= 31.0) {
            MusicToggle.toggle();
        }
    }

    private static void lyrFit(int sw, float sc) {
        lyrW = Math.max(96.0f, Math.min(184.0f, (float)sw / Math.max(0.5f, sc) - 8.0f));
    }

    private static String visual(String s) {
        boolean rtl = false;
        for (int i = 0; i < s.length() && !rtl; ++i) {
            byte d = Character.getDirectionality(s.charAt(i));
            rtl = d == 1 || d == 2;
        }
        if (!rtl) {
            return s;
        }
        Bidi b = new Bidi(s, -2);
        int n = b.getRunCount();
        byte[] lv = new byte[n];
        Object[] idx = new Integer[n];
        for (int i = 0; i < n; ++i) {
            lv[i] = (byte)b.getRunLevel(i);
            idx[i] = i;
        }
        Bidi.reorderVisually(lv, 0, idx, 0, n);
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < n; ++k) {
            int r = (Integer)idx[k];
            String run = s.substring(b.getRunStart(r), b.getRunLimit(r));
            sb.append((b.getRunLevel(r) & 1) == 1 ? new StringBuilder(run).reverse() : run);
        }
        return sb.toString();
    }

    private static int lw(class_327 tr, String s, boolean bold) {
        return bold ? Fx.w(tr, (class_2561)class_2561.method_43470((String)s).method_27692(class_124.field_1067)) : Fx.w(tr, s);
    }

    private static String lyrTrim(class_327 tr, String s, int maxW, boolean bold) {
        int lo = 0;
        int hi = s.length();
        while (lo < hi) {
            int mid = lo + hi + 1 >>> 1;
            if (StreamHudRenderer.lw(tr, s.substring(0, mid), bold) <= maxW) {
                lo = mid;
                continue;
            }
            hi = mid - 1;
        }
        if (lo > 0 && lo < s.length() && Character.isHighSurrogate(s.charAt(lo - 1))) {
            --lo;
        }
        return s.substring(0, lo);
    }

    private static Wrapped wrapRaw(class_327 tr, String text, int maxW, int maxLines, boolean bold) {
        ArrayList<String> all = new ArrayList<String>();
        StringBuilder line = new StringBuilder();
        String t = text.replaceAll("\\s+", " ").trim();
        if (t.length() > 400) {
            t = t.substring(0, 400);
        }
        String[] stringArray = t.split(" ");
        int n = stringArray.length;
        for (int i = 0; i < n; ++i) {
            String w;
            String word = w = stringArray[i];
            while (!word.isEmpty()) {
                String cand;
                String string = cand = line.length() == 0 ? word : String.valueOf(line) + " " + word;
                if (StreamHudRenderer.lw(tr, cand, bold) <= maxW) {
                    line.setLength(0);
                    line.append(cand);
                    word = "";
                    continue;
                }
                if (line.length() > 0) {
                    all.add(line.toString());
                    line.setLength(0);
                    continue;
                }
                String head = StreamHudRenderer.lyrTrim(tr, word, maxW, bold);
                int c = head.isEmpty() ? Character.charCount(word.codePointAt(0)) : head.length();
                all.add(word.substring(0, c));
                word = word.substring(c);
            }
        }
        if (line.length() > 0) {
            all.add(line.toString());
        }
        boolean cut = all.size() > maxLines;
        ArrayList<String> out = new ArrayList<String>(all.subList(0, Math.min(all.size(), maxLines)));
        if (cut) {
            String last = (String)out.get(out.size() - 1);
            while (!last.isEmpty() && StreamHudRenderer.lw(tr, last + "...", bold) > maxW) {
                last = last.substring(0, last.length() - Character.charCount(last.codePointBefore(last.length())));
            }
            out.set(out.size() - 1, last + "...");
        }
        if (out.isEmpty()) {
            out.add("");
        }
        return new Wrapped(out, cut);
    }

    private static LyrFit lyrBody(class_327 tr, String text, int maxW, boolean bold, int maxLines) {
        String key = GlassConfig.get().font + "|" + maxW + "|" + bold + "|" + maxLines + "|" + text;
        LyrFit f = LYR_FITS.get(key);
        if (f != null) {
            return f;
        }
        Wrapped w = null;
        float fs = 1.0f;
        float[] fArray = LYR_STEPS;
        int n = fArray.length;
        for (int i = 0; i < n; ++i) {
            float st;
            fs = st = fArray[i];
            w = StreamHudRenderer.wrapRaw(tr, text, (int)((float)maxW / st), maxLines, bold);
            if (!w.cut()) break;
        }
        ArrayList<String> vis = new ArrayList<String>();
        for (String l : w.lines()) {
            vis.add(StreamHudRenderer.visual(l));
        }
        f = new LyrFit(vis, fs);
        LYR_FITS.put(key, f);
        return f;
    }

    private static LyrFit lyrSide(class_327 tr, String text, int maxW) {
        int mw;
        Object s = text == null || text.isBlank() ? ". . ." : text.replaceAll("\\s+", " ").trim();
        String key = "S|" + GlassConfig.get().font + "|" + maxW + "|" + (String)s;
        LyrFit f = LYR_FITS.get(key);
        if (f != null) {
            return f;
        }
        int w0 = StreamHudRenderer.lw(tr, (String)s, false);
        float sc = LYR_STEPS[LYR_STEPS.length - 1];
        for (float st : LYR_STEPS) {
            if (!((float)w0 * st <= (float)maxW)) continue;
            sc = st;
            break;
        }
        if (w0 > (mw = (int)((float)maxW / sc))) {
            s = StreamHudRenderer.lyrTrim(tr, (String)s, mw - Fx.w(tr, "..."), false) + "...";
        }
        f = new LyrFit(List.of(StreamHudRenderer.visual((String)s)), sc);
        LYR_FITS.put(key, f);
        return f;
    }

    private static void lyrLine(class_332 ctx, class_327 tr, String s, float w, float y, float sc, int color, boolean bold) {
        if (s.isEmpty()) {
            return;
        }
        class_2561 tx = bold ? StreamHudRenderer.bold(s) : StreamHudRenderer.plain(s);
        int tw = StreamHudRenderer.widthOf(tr, tx);
        Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        m.translate(w / 2.0f, y);
        if (sc != 1.0f) {
            m.scale(sc, sc);
        }
        Fx.draw(ctx, tr, tx, Math.round((float)(-tw) / 2.0f), 0, color, true);
        m.popMatrix();
    }

    private static void lyrics(class_332 ctx, class_327 tr, StreamData.NowPlaying np, float dt, boolean preview) {
        List<String> body;
        List<Lyrics.Line> ls;
        GlassConfig cfg = GlassConfig.get();
        int w = Math.round(lyrW);
        int maxW = w - 14 - 4;
        int cur = 0;
        Object msg = null;
        if (preview) {
            ls = LYR_SAMPLE;
            cur = 1;
        } else {
            Lyrics.track(np);
            switch (Lyrics.state()) {
                case READY: {
                    ls = Lyrics.lines();
                    cur = Lyrics.indexAt(ls, Lyrics.positionSec(np));
                    break;
                }
                case LOADING: {
                    ls = List.of();
                    msg = "Loading lyrics...";
                    break;
                }
                default: {
                    ls = List.of();
                    msg = Lyrics.error().isEmpty() ? "No synced lyrics for this song" : "Lyrics: " + Lyrics.error();
                }
            }
        }
        float fs = 1.0f;
        float sPrev = 1.0f;
        float sNext = 1.0f;
        String prev = "";
        String next = "";
        if (msg != null) {
            LyrFit f = StreamHudRenderer.lyrBody(tr, (String)msg, maxW, false, 2);
            body = f.lines();
            fs = f.fs();
        } else {
            String ct;
            String string = ct = cur < 0 ? ". . ." : ls.get(cur).text();
            if (ct.isBlank()) {
                ct = ". . .";
            }
            LyrFit f = StreamHudRenderer.lyrBody(tr, ct, maxW, true, 3);
            body = f.lines();
            fs = f.fs();
            LyrFit pf = StreamHudRenderer.lyrSide(tr, cur - 1 >= 0 ? ls.get(cur - 1).text() : "", maxW);
            LyrFit nf = StreamHudRenderer.lyrSide(tr, cur + 1 < ls.size() ? ls.get(cur + 1).text() : "", maxW);
            prev = pf.lines().get(0);
            sPrev = pf.fs();
            next = nf.lines().get(0);
            sNext = nf.fs();
        }
        if (cur != lyrIdx) {
            lyrIdx = cur;
            lyrAnim = preview ? 1.0f : 0.0f;
        }
        lyrAnim += (1.0f - lyrAnim) * Math.min(1.0f, dt * 9.0f);
        float lineH = 10.0f * fs;
        float target = msg != null ? 14.0f + (float)body.size() * lineH + 2.0f : 38.0f + (float)body.size() * lineH;
        if (Math.abs(target - (lyrH += (target - lyrH) * Math.min(1.0f, dt * (target > lyrH ? 28.0f : 14.0f)))) < 0.3f) {
            lyrH = target;
        }
        int h = Math.round(lyrH);
        if (cfg.perfMode) {
            Glass.buttonLite(ctx, 0, 0, w, h, 11, 0xFFFFFF, 1.0f, 1.0f);
        } else {
            Glass.panel(ctx, 0, 0, w, h, 11, 0xFFFFFF, 1.0f, 1.0f);
        }
        Glass.layer(ctx, 1.0f, 1.0f, w - 2, h - 2, 10.0f, Glass.c(52, 0), Glass.c(72, 0));
        float k = Math.max(0.0f, Math.min(1.0f, lyrAnim));
        float off = (1.0f - k) * 3.0f;
        int aCur = 90 + (int)(165.0f * k);
        Matrix3x2fStack lm = ctx.method_51448();
        lm.pushMatrix();
        lm.translate(0.0f, off);
        if (msg != null) {
            for (int i = 0; i < body.size(); ++i) {
                StreamHudRenderer.lyrLine(ctx, tr, body.get(i), w, 8.0f + (float)i * lineH, fs, -4934466, false);
            }
        } else {
            float y = 7.0f;
            StreamHudRenderer.lyrLine(ctx, tr, prev, w, y + 4.0f * (1.0f - sPrev), sPrev, Glass.c(100, 13158610), false);
            y += 12.0f;
            for (String l : body) {
                StreamHudRenderer.lyrLine(ctx, tr, l, w, y, fs, Glass.c(aCur, 0xFFFFFF), true);
                y += lineH;
            }
            StreamHudRenderer.lyrLine(ctx, tr, next, w, y + 2.0f + 4.0f * (1.0f - sNext), sNext, Glass.c(Math.max(24, 100 - (int)(40.0f * (1.0f - k))), 13158610), false);
        }
        lm.popMatrix();
    }

    private static Wrap wrapOf(class_327 tr, StreamData.ChatLine cl) {
        int fnt = GlassConfig.get().font;
        if (fnt != wrapFont) {
            WRAPS.clear();
            wrapFont = fnt;
        }
        Wrap w = WRAPS.get(cl);
        if (w == null) {
            w = StreamHudRenderer.buildWrap(tr, cl);
            WRAPS.put(cl, w);
        }
        return w;
    }

    private static Wrap buildWrap(class_327 tr, StreamData.ChatLine cl) {
        String author = cl.author() == null ? "" : cl.author();
        String name = Fx.trim(tr, author + ":", 60);
        int nameW = Fx.w(tr, name) + 4;
        int textW = 164;
        List<String> lines = StreamHudRenderer.wrap(tr, cl.text() == null ? "" : cl.text(), textW - nameW, textW);
        if (lines.isEmpty()) {
            lines = List.of("");
        }
        int widest = nameW + Fx.w(tr, lines.get(0));
        for (int i = 1; i < lines.size(); ++i) {
            widest = Math.max(widest, Fx.w(tr, lines.get(i)));
        }
        int bw = Math.min(176, widest + 12);
        int h = lines.size() * 10 + 8 - 1;
        int color = NAME_COLORS[Math.floorMod(author.hashCode(), NAME_COLORS.length)];
        return new Wrap(name, nameW, color, lines, bw, h);
    }

    private static List<String> wrap(class_327 tr, String text, int firstW, int restW) {
        ArrayList<String> out = new ArrayList<String>();
        String t = text.replaceAll("\\s+", " ").trim();
        if (t.isEmpty()) {
            return out;
        }
        StringBuilder line = new StringBuilder();
        int limit = firstW;
        for (String word : t.split(" ")) {
            String cand;
            if (word.isEmpty()) continue;
            String string = cand = line.length() == 0 ? word : String.valueOf(line) + " " + word;
            if (Fx.w(tr, cand) <= limit) {
                line.setLength(0);
                line.append(cand);
                continue;
            }
            if (line.length() > 0) {
                out.add(line.toString());
                line.setLength(0);
                limit = restW;
            }
            while (Fx.w(tr, word) > limit && out.size() < 60) {
                String head = Fx.trim(tr, word, limit);
                int cut = head.length();
                if (cut <= 0) {
                    cut = Character.charCount(word.codePointAt(0));
                }
                out.add(word.substring(0, cut));
                word = word.substring(cut);
                limit = restW;
            }
            line.append(word);
            if (out.size() >= 60) break;
        }
        if (line.length() > 0 && out.size() < 60) {
            out.add(line.toString());
        }
        return out;
    }

    private static int chatFirst(List<StreamData.ChatLine> lines, class_327 tr, float maxH) {
        float h = 0.0f;
        int first = lines.size();
        int i = lines.size() - 1;
        while (i >= 0) {
            float add = StreamHudRenderer.wrapOf((class_327)tr, (StreamData.ChatLine)lines.get((int)i)).h + (first == lines.size() ? 0 : 3);
            if (first < lines.size() && h + add > maxH) break;
            h += add;
            first = i--;
        }
        return first;
    }

    private static void chat(class_332 ctx, class_327 tr, List<StreamData.ChatLine> lines, float dt, boolean preview) {
        int n = lines.size();
        int first = StreamHudRenderer.chatFirst(lines, tr, chatMaxH);
        int shown = n - first;
        int y = 0;
        boolean lite = GlassConfig.get().perfMode;
        long now = System.nanoTime();
        Matrix3x2fStack m = ctx.method_51448();
        for (int i = first; i < n; ++i) {
            StreamData.ChatLine cl = lines.get(i);
            Wrap w = StreamHudRenderer.wrapOf(tr, cl);
            float k = (float)(i - first + 1) / (float)shown;
            int a = 128 + (int)(127.0f * k);
            float al = 0.55f + 0.45f * k;
            float ay = y;
            float sc = 1.0f;
            float ap = 1.0f;
            if (!preview) {
                BubbleAnim an = BANIM.get(cl);
                if (an == null) {
                    an = new BubbleAnim(y, now - cl.at() < 700000000L);
                    BANIM.put(cl, an);
                }
                an.y.target = y;
                an.y.step(dt);
                an.pop.step(dt);
                an.appear += (1.0f - an.appear) * Math.min(1.0f, dt * 13.0f);
                ay = an.y.pos;
                sc = Math.max(0.5f, an.pop.pos);
                ap = Math.max(0.0f, Math.min(1.0f, an.appear));
            }
            if (ap > 0.02f) {
                float hh = (float)w.h() / 2.0f;
                m.pushMatrix();
                m.translate(0.0f, ay + hh);
                if (sc != 1.0f) {
                    m.scale(sc, sc);
                }
                m.translate(0.0f, -hh);
                float ba = al * ap;
                if (lite && i < n - 2) {
                    Glass.buttonLite(ctx, 0, 0, w.bubbleW(), w.h(), 8, 0xFFFFFF, 1.0f, ba);
                } else {
                    Glass.button(ctx, 0, 0, w.bubbleW(), w.h(), 8, 0xFFFFFF, 1.0f, ba);
                }
                int ta = (int)((float)a * ap);
                int ty = 4;
                Fx.draw(ctx, tr, w.name(), 6, ty, Glass.c(ta, w.nameColor()), true);
                List<String> ls = w.lines();
                for (int j = 0; j < ls.size(); ++j) {
                    Fx.draw(ctx, tr, ls.get(j), j == 0 ? 6 + w.nameW() : 6, ty + j * 10, Glass.c(ta, 0xFFFFFF), false);
                }
                m.popMatrix();
            }
            y += w.h() + 3;
        }
        if (BANIM.size() > n + 8) {
            BANIM.keySet().retainAll(new HashSet<StreamData.ChatLine>(lines));
        }
    }

    private static void combo(class_332 ctx, class_327 tr, float dt, boolean preview) {
        float left;
        float alpha;
        float sc;
        float slide;
        int count;
        if (preview) {
            count = 12;
            slide = 0.0f;
            sc = 1.0f;
            alpha = 1.0f;
            left = 0.65f;
        } else {
            ComboTracker.step(dt);
            count = ComboTracker.count();
            if (count <= 0) {
                return;
            }
            slide = ComboTracker.slide();
            sc = ComboTracker.scale();
            alpha = ComboTracker.alpha();
            left = ComboTracker.left();
        }
        int A = (int)(255.0f * Math.max(0.0f, Math.min(1.0f, alpha)));
        if (A < 10) {
            return;
        }
        if (comboKey != count) {
            comboKey = count;
            comboTxt = Integer.toString(count);
        }
        int accent = count >= 20 ? 16731450 : (count >= 10 ? 16752394 : (count >= 5 ? 16766474 : 6596863));
        Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        m.translate(48.0f, 15.0f + slide);
        float sq = preview ? 0.0f : ComboTracker.squash();
        m.scale(sc * (1.0f - sq * 0.6f), sc * (1.0f + sq));
        m.translate(-48.0f, -15.0f);
        float ns = 1.7f;
        String lab = "COMBO";
        float numW = (float)Fx.w(tr, comboTxt) * ns;
        float labW = Fx.w(tr, lab);
        float x0 = (96.0f - (numW + 5.0f + labW)) / 2.0f;
        m.pushMatrix();
        m.translate(x0, 5.0f);
        m.scale(ns, ns);
        Fx.draw(ctx, tr, comboTxt, 0, 0, Glass.c(A, Glass.lerp(0xFF000000 | accent, -1, 0.35f)), true);
        m.popMatrix();
        Fx.draw(ctx, tr, lab, Math.round(x0 + numW + 5.0f), 9, Glass.c(Math.max(10, A * 4 / 5), 0xE6E6EE), true);
        int bw = 72;
        int bf = Math.round((float)bw * left);
        Glass.layer(ctx, 12.0f, 24.0f, bw, 2.0f, 1.0f, Glass.c((int)(64.0f * alpha), 0xFFFFFF), Glass.c((int)(64.0f * alpha), 0xFFFFFF));
        if (bf >= 2) {
            Glass.layer(ctx, 12.0f, 24.0f, bf, 2.0f, 1.0f, Glass.c(A, accent), Glass.c(A, accent));
        }
        m.popMatrix();
    }

    private static void alert(class_332 ctx, class_327 tr, float dt, boolean preview) {
        boolean gift;
        float alpha;
        float sc;
        float slide;
        LiveAlerts.Alert a;
        if (preview) {
            a = LiveAlerts.SAMPLE;
            slide = 0.0f;
            sc = 1.0f;
            alpha = 1.0f;
        } else {
            LiveAlerts.step(dt);
            a = LiveAlerts.current();
            if (a == null) {
                return;
            }
            slide = LiveAlerts.slide();
            sc = LiveAlerts.scale();
            alpha = LiveAlerts.alpha();
        }
        int A = (int)(255.0f * Math.max(0.0f, Math.min(1.0f, alpha)));
        if (A < 10) {
            return;
        }
        boolean bl = gift = a.kind() == LiveAlerts.Kind.GIFT;
        int accent = !gift ? 16731514 : (a.diamonds() >= 1000L ? 12540658 : (a.diamonds() >= 100L ? 16766474 : 16752394));
        Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        m.translate(88.0f, 17.0f + slide);
        float sq = preview ? 0.0f : LiveAlerts.squash();
        m.scale(sc * (1.0f - sq * 0.6f), sc * (1.0f + sq));
        m.translate(-88.0f, -17.0f);
        Glass.buttonLite(ctx, 0, 0, 176, 34, 15, accent, 1.6f, alpha);
        int top = Glass.lerp(0xFF000000 | accent, -1, 0.25f);
        int bot = Glass.lerp(0xFF000000 | accent, -16777216, 0.25f);
        Glass.layer(ctx, 5.0f, 5.0f, 24.0f, 24.0f, 12.0f, Glass.c(A * 9 / 10, top), Glass.c(A * 9 / 10, bot));
        Icons.drawShadowed(ctx, gift ? IconShapes.Shape.SPARKLE : IconShapes.Shape.HEART, 11.0f, 11.0f, 12.0f, 0xFFFFFF, A);
        String l2 = gift ? "sent " + a.gift() + (String)(a.count() > 1 ? " x" + a.count() : "") : "followed you";
        Fx.draw(ctx, tr, Fx.trim(tr, a.user(), 132), 36, 8, Glass.c(A, 0xFFFFFF), true);
        Fx.draw(ctx, tr, Fx.trim(tr, l2, 132), 36, 19, Glass.c(Math.max(10, A * 4 / 5), 0xE6E6EE), false);
        m.popMatrix();
    }

    private static Vitals vitals() {
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return null;
        }
        class_746 p = mc.field_1724;
        if (p == null || p.method_68878() || p.method_7325()) {
            return null;
        }
        return new Vitals(p.method_6032(), p.method_6063(), p.method_6067(), p.method_7344().method_7586(), p.method_6096(), p.method_5669(), p.method_5748(), p.field_7520, p.field_7510, p.method_5477().getString());
    }

    private static float airAnim(GlassConfig cfg, Snap s, float dt, boolean preview) {
        if (preview) {
            airShow = 1.0f;
            return 1.0f;
        }
        Vitals v = s.vitals;
        float target = v != null && (cfg.profileAirAlways || v.air() < v.maxAir()) ? 1.0f : 0.0f;
        if (Math.abs(target - (airShow += (target - airShow) * Math.min(1.0f, dt * 8.0f))) < 0.01f) {
            airShow = target;
        }
        return airShow;
    }

    private static int clampI(float f) {
        return Math.max(0, Math.min(1023, (int)Math.ceil(f)));
    }

    private static void profile(class_332 ctx, class_327 tr, Vitals v, float W, float H, float e, float t, float dt) {
        int aw;
        String nm;
        int w = Math.round(W);
        int h = Math.round(H);
        float k = Math.max(0.0f, Math.min(1.0f, e));
        int rows = 32 + Math.round(12.0f * k);
        if (GlassConfig.get().perfMode) {
            Glass.buttonLite(ctx, 0, 0, w, h, 12, 0xFFFFFF, 1.0f, 1.0f);
        } else {
            Glass.panel(ctx, 0, 0, w, h, 12, 0xFFFFFF, 1.0f, 1.0f);
        }
        HP.set(v.maxHp() <= 0.0f ? 0.0f : v.hp() / v.maxHp(), dt);
        FOOD.set((float)v.food() / 20.0f, dt);
        ARMOR.set((float)v.armor() / 20.0f, dt);
        AIR.set(v.maxAir() <= 0 ? 0.0f : (float)v.air() / (float)v.maxAir(), dt);
        String string = nm = v.name() == null ? "" : v.name();
        int fontNow = GlassConfig.get().font;
        if (!nm.equals(nameSrc) || fontNow != nameFont) {
            nameSrc = nm;
            nameFont = fontNow;
            nameTxt = Fx.trim(tr, nm, 116);
        }
        Fx.draw(ctx, tr, nameTxt, 40, 8, -1, true);
        int hs = 26;
        int hx = 8;
        int hy = 20 + (rows - hs) / 2;
        Glass.layer(ctx, hx - 1, hy - 1, hs + 2, hs + 2, 6.0f, Glass.c(176, 329228), Glass.c(176, 329228));
        if (!PlayerHead.draw(ctx, hx, hy, hs)) {
            Glass.layer(ctx, hx, hy, hs, hs, 5.0f, Glass.c(255, 9345716), Glass.c(255, 4871280));
        }
        Glass.ringHard(ctx, hx - 1, hy - 1, hs + 2, hs + 2, 6.0f, 0.5f, Glass.c(200, 0xFFFFFF), Glass.c(80, 0xFFFFFF));
        int lvl = Math.max(0, Math.min(9999, v.level()));
        if (V_TXT[4] == null || V_KEY[4] != lvl) {
            StreamHudRenderer.V_KEY[4] = lvl;
            StreamHudRenderer.V_TXT[4] = Integer.toString(lvl);
        }
        int bw = Math.max(14, Fx.w(tr, V_TXT[4]) + 6);
        int bx = hx + hs - bw + 4;
        int by = hy + hs - 7;
        Glass.layer(ctx, bx, by, bw, 11.0f, 5.5f, Glass.c(242, 4881648), Glass.c(242, 2905288));
        Glass.ringHard(ctx, bx, by, bw, 11.0f, 5.5f, 0.5f, Glass.c(208, 0xFFFFFF), Glass.c(96, 0xFFFFFF));
        Fx.draw(ctx, tr, V_TXT[4], bx + (bw - Fx.w(tr, V_TXT[4])) / 2, by + 2, -1, true);
        int hp = StreamHudRenderer.clampI(v.hp());
        int mx = StreamHudRenderer.clampI(v.maxHp());
        int ab = StreamHudRenderer.clampI(v.absorb());
        int key = (hp * 1024 + mx) * 1024 + ab;
        if (V_TXT[0] == null || V_KEY[0] != key) {
            StreamHudRenderer.V_KEY[0] = key;
            StreamHudRenderer.V_TXT[0] = ab > 0 ? hp + "+" + ab : hp + "/" + mx;
        }
        float pulse = v.hp() <= 6.0f ? 0.72f + 0.28f * (float)Math.sin(t * 7.0f) : 1.0f;
        StreamHudRenderer.bar(ctx, tr, 20, 0, "HP", 16731486, HP, V_TXT[0], ab > 0 ? 16769126 : 0xFFFFFF, 1.0f, pulse, t, true);
        if (ab > 0 && v.maxHp() > 0.0f && (aw = Math.round(64.0f * Math.min(1.0f, v.absorb() / v.maxHp()))) >= 2) {
            Glass.layer(ctx, 56.0f, 26.0f, aw, 2.0f, 1.0f, Glass.c(255, 16766474), Glass.c(255, 16766474));
        }
        int fd = Math.max(0, Math.min(99, v.food()));
        if (V_TXT[1] == null || V_KEY[1] != fd) {
            StreamHudRenderer.V_KEY[1] = fd;
            StreamHudRenderer.V_TXT[1] = fd + "/20";
        }
        pulse = v.food() <= 6 ? 0.72f + 0.28f * (float)Math.sin(t * 7.0f) : 1.0f;
        StreamHudRenderer.bar(ctx, tr, 32, 1, "FD", 16752394, FOOD, V_TXT[1], 0xFFFFFF, 1.0f, pulse, t, false);
        int ar = Math.max(0, Math.min(99, v.armor()));
        if (V_TXT[2] == null || V_KEY[2] != ar) {
            StreamHudRenderer.V_KEY[2] = ar;
            StreamHudRenderer.V_TXT[2] = Integer.toString(ar);
        }
        StreamHudRenderer.bar(ctx, tr, 44, 2, "AR", 10336472, ARMOR, V_TXT[2], ar > 0 ? 0xFFFFFF : -6645084, 1.0f, 1.0f, t, false);
        if (k > 0.05f) {
            boolean clip;
            boolean bl = clip = k < 0.99f;
            if (clip) {
                ctx.method_44379(0, 0, w, 20 + rows);
            }
            int secs = Math.max(0, Math.min(99, (int)Math.ceil((float)Math.max(0, v.air()) / 20.0f)));
            if (V_TXT[3] == null || V_KEY[3] != secs) {
                StreamHudRenderer.V_KEY[3] = secs;
                StreamHudRenderer.V_TXT[3] = secs + "s";
            }
            pulse = v.air() <= 100 ? 0.72f + 0.28f * (float)Math.sin(t * 7.0f) : 1.0f;
            StreamHudRenderer.bar(ctx, tr, 56, 3, "O2", 4245759, AIR, V_TXT[3], v.air() <= 100 ? -30080 : -1, k, pulse, t, false);
            if (clip) {
                ctx.method_44380();
            }
        }
        int xy = 20 + rows + 5;
        int xw = w - 16;
        Glass.layer(ctx, 8.0f, xy, xw, 3.0f, 1.5f, Glass.c(120, 329228), Glass.c(80, 329228));
        int xf = Math.round((float)xw * Math.max(0.0f, Math.min(1.0f, v.xp())));
        if (xf >= 2) {
            Glass.layer(ctx, 8.0f, xy, xf, 3.0f, 1.5f, Glass.c(240, 10813318), Glass.c(240, 4177738));
        }
    }

    private static void bar(class_332 ctx, class_327 tr, int ry, int idx, String lab, int rgb, Bar b, String val, int valCol, float alpha, float pulse, float t, boolean ghost) {
        int a = Math.max(8, (int)(255.0f * alpha));
        Fx.draw(ctx, tr, lab, 40, ry, Glass.c(a, rgb), true);
        Glass.layer(ctx, 56.0f, ry + 1, 64.0f, 7.0f, 3.5f, Glass.c((int)(120.0f * alpha), 329228), Glass.c((int)(80.0f * alpha), 329228));
        Glass.ringHard(ctx, 56.0f, ry + 1, 64.0f, 7.0f, 3.5f, 0.5f, Glass.c((int)(64.0f * alpha), 0xFFFFFF), Glass.c((int)(112.0f * alpha), 0xFFFFFF));
        int fw = Math.round(64.0f * b.v);
        int gw = Math.round(64.0f * b.ghost);
        if (ghost && gw > fw + 1) {
            Glass.layer(ctx, 56.0f, ry + 1, gw, 7.0f, Math.min(3.5f, (float)gw / 2.0f), Glass.c((int)(144.0f * alpha), 0xFFFFFF), Glass.c((int)(96.0f * alpha), 0xFFFFFF));
        }
        if (fw >= 2) {
            int top = Glass.lerp(0xFF000000 | rgb, -1, 0.3f);
            int bot = Glass.lerp(0xFF000000 | rgb, -16777216, 0.28f);
            int fa = (int)(242.0f * alpha * pulse);
            Glass.layer(ctx, 56.0f, ry + 1, fw, 7.0f, Math.min(3.5f, (float)fw / 2.0f), Glass.c(fa, top), Glass.c(fa, bot));
            if (fw >= 8) {
                Glass.layer(ctx, 58.0f, (float)ry + 1.5f, fw - 4, 2.5f, 1.2f, Glass.c((int)(112.0f * alpha), 0xFFFFFF), Glass.c((int)(16.0f * alpha), 0xFFFFFF));
            }
            if (fw >= 14) {
                float ph = (t * 0.32f + (float)idx * 0.21f) % 1.0f * (float)(fw + 16) - 12.0f;
                int s0 = Math.max(0, Math.round(ph));
                int s1 = Math.min(fw, Math.round(ph + 12.0f));
                if (s1 - s0 >= 3) {
                    Glass.layer(ctx, 56 + s0, ry + 1, s1 - s0, 7.0f, 3.0f, Glass.c((int)(56.0f * alpha), 0xFFFFFF), Glass.c((int)(10.0f * alpha), 0xFFFFFF));
                }
            }
        }
        Fx.draw(ctx, tr, val, 156 - Fx.w(tr, val), ry, Glass.c(a, valCol), true);
    }

    private static int widthOf(class_327 tr, class_2561 text) {
        Integer w;
        int fnt = GlassConfig.get().font;
        if (fnt != textWFont) {
            TEXT_W.clear();
            textWFont = fnt;
        }
        if ((w = TEXT_W.get(text)) == null) {
            if (TEXT_W.size() > 64) {
                TEXT_W.clear();
            }
            w = Fx.w(tr, text);
            TEXT_W.put(text, w);
        }
        return w;
    }

    private static class_2561 bold(String s) {
        return BOLD.computeIfAbsent(s, k -> class_2561.method_43470((String)k).method_27692(class_124.field_1067));
    }

    private static class_2561 plain(String s) {
        return PLAIN.computeIfAbsent(s, class_2561::method_43470);
    }

    private static String subtitle(StreamData.NowPlaying np) {
        if (np != subFor) {
            subFor = np;
            subText = np.artist().isBlank() ? np.source() : np.artist() + " - " + np.source();
        }
        return subText;
    }

    private static void marquee(class_332 ctx, class_327 tr, class_2561 text, int x, int y, int maxW, int color) {
        int tw = StreamHudRenderer.widthOf(tr, text);
        if (tw <= maxW) {
            Fx.draw(ctx, tr, text, x, y, color, true);
            return;
        }
        int span = tw + 24;
        float off = (float)(System.currentTimeMillis() % (long)span) * 0.04f;
        Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        m.translate(-off, 0.0f);
        Fx.draw(ctx, tr, text, x, y, color, true);
        Fx.draw(ctx, tr, text, x + span, y, color, true);
        m.popMatrix();
    }

    static String fmt(long n) {
        if (n < 0L) {
            return "--";
        }
        if (n >= 1000000L) {
            return StreamHudRenderer.trim(n, 1000000L) + "M";
        }
        if (n >= 1000L) {
            return StreamHudRenderer.trim(n, 1000L) + "K";
        }
        return Long.toString(n);
    }

    private static String trim(long n, long unit) {
        long tenths = Math.round((double)n * 10.0 / (double)unit);
        long whole = tenths / 10L;
        long frac = tenths % 10L;
        return frac == 0L ? Long.toString(whole) : whole + "." + frac;
    }

    static {
        demoLines = -1;
        REQ_PREVIEW = new ReqView(new SongRequests.Req("andi", new MusicHub.Track("Sempurna", "Andra and The Backbone", "YOUTUBE", "aaaaaaaaaaa")), List.of(new SongRequests.Req("sasa_", new MusicHub.Track("Kangen", "Dewa 19", "YOUTUBE", "bbbbbbbbbbb")), new SongRequests.Req("budi99", new MusicHub.Track("Blinding Lights", "The Weeknd", "YOUTUBE", "ccccccccccc")), new SongRequests.Req("mira", new MusicHub.Track("Hati-Hati di Jalan", "Tulus", "YOUTUBE", "ddddddddddd"))));
        FIT = new LinkedHashMap<String, String>(16, 0.75f, true){

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> e) {
                return this.size() > 96;
            }
        };
        RANK_COLORS = new int[]{16766474, 13092817, 16752394, 6607615, 6607615};
        LYR_STEPS = new float[]{1.0f, 0.9f, 0.8f, 0.7f, 0.6f};
        lyrW = 184.0f;
        lyrH = 46.0f;
        lyrAnim = 1.0f;
        lyrIdx = Integer.MIN_VALUE;
        LYR_FITS = new LinkedHashMap<String, LyrFit>(16, 0.75f, true){

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, LyrFit> e) {
                return this.size() > 24;
            }
        };
        LYR_SAMPLE = List.of(new Lyrics.Line(0L, "I've been walking down this road"), new Lyrics.Line(4000L, "With the lights turned down so low, sing it out and never let it go"), new Lyrics.Line(8000L, "Feel the music take control"));
        chatMaxH = 1.0E9f;
        WRAPS = new LinkedHashMap<StreamData.ChatLine, Wrap>(16, 0.75f, true){

            @Override
            protected boolean removeEldestEntry(Map.Entry<StreamData.ChatLine, Wrap> e) {
                return this.size() > 32;
            }
        };
        BANIM = new HashMap<StreamData.ChatLine, BubbleAnim>();
        comboKey = -1;
        comboTxt = "";
        HP = new Bar();
        FOOD = new Bar();
        ARMOR = new Bar();
        AIR = new Bar();
        V_TXT = new String[5];
        V_KEY = new int[5];
        nameSrc = "";
        nameTxt = "";
        BOLD = new LinkedHashMap<String, class_2561>(16, 0.75f, true){

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, class_2561> e) {
                return this.size() > 24;
            }
        };
        PLAIN = new LinkedHashMap<String, class_2561>(16, 0.75f, true){

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, class_2561> e) {
                return this.size() > 24;
            }
        };
        TEXT_W = new IdentityHashMap();
        subText = "";
    }

    public record R(float x, float y, float w, float h, float s) {
        public boolean has(double mx, double my) {
            return mx >= (double)this.x && mx <= (double)(this.x + this.w) && my >= (double)this.y && my <= (double)(this.y + this.h);
        }
    }

    public record Snap(boolean ready, boolean live, long views, long likes, String link, List<StreamData.ChatLine> chat, StreamData.NowPlaying song, Vitals vitals) {
    }

    public static enum Id {
        ISLAND,
        CHAT,
        SONG,
        PROFILE,
        COMBO,
        ALERT,
        CASSETTE,
        LYRICS,
        REQUESTS,
        TOPTAP;

    }

    public record Vitals(float hp, float maxHp, float absorb, int food, int armor, int air, int maxAir, int level, float xp, String name) {
    }

    private record Wrap(String name, int nameW, int nameColor, List<String> lines, int bubbleW, int h) {
    }

    private record ReqView(SongRequests.Req cur, List<SongRequests.Req> list) {
    }

    private static final class AnimNum {
        float v;
        boolean init;

        private AnimNum() {
        }

        long show(long target, float dt) {
            if (target < 0L) {
                return -1L;
            }
            if (!this.init) {
                this.v = target;
                this.init = true;
            }
            this.v += ((float)target - this.v) * Math.min(1.0f, dt * 5.0f);
            if (Math.abs((float)target - this.v) < 0.5f) {
                this.v = target;
            }
            return Math.round(this.v);
        }
    }

    private record Wrapped(List<String> lines, boolean cut) {
    }

    private record LyrFit(List<String> lines, float fs) {
    }

    private static final class BubbleAnim {
        final Spring y;
        final Spring pop;
        float appear;

        BubbleAnim(float ty, boolean fresh) {
            this.y = new Spring(fresh ? ty + 14.0f : ty, ty, 330.0f, 27.0f);
            this.pop = new Spring(fresh ? 0.78f : 1.0f, 1.0f, 380.0f, 21.0f);
            this.appear = fresh ? 0.0f : 1.0f;
        }
    }

    private static final class Bar {
        float v;
        float ghost;
        boolean init;

        private Bar() {
        }

        void set(float target, float dt) {
            target = Math.max(0.0f, Math.min(1.0f, target));
            if (!this.init) {
                this.v = this.ghost = target;
                this.init = true;
            }
            this.v += (target - this.v) * Math.min(1.0f, dt * 10.0f);
            if (Math.abs(target - this.v) < 0.002f) {
                this.v = target;
            }
            this.ghost = this.v >= this.ghost ? this.v : (this.ghost += (this.v - this.ghost) * Math.min(1.0f, dt * 1.6f));
        }
    }
}

