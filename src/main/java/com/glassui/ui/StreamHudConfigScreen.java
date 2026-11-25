/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.ui;

import com.glassui.GlassConfig;
import com.glassui.hud.ChatSound;
import com.glassui.hud.LiveAlerts;
import com.glassui.hud.MusicToggle;
import com.glassui.hud.StreamData;
import com.glassui.hud.StreamHud;
import com.glassui.music.LocalPlayer;
import com.glassui.music.MusicHub;
import com.glassui.ui.Anim;
import com.glassui.ui.Fx;
import com.glassui.ui.Glass;
import com.glassui.ui.GlassWidgets;
import com.glassui.ui.HudEditorScreen;
import com.glassui.ui.IconShapes;
import com.glassui.ui.Icons;
import com.glassui.ui.MusicScreen;
import com.glassui.ui.Spring;
import com.glassui.ui.StyleScreen;
import com.glassui.ui.UiSound;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_437;
import org.joml.Matrix3x2fStack;

public class StreamHudConfigScreen
extends class_437 {
    private static final int PAD = 18;
    private int pw = 324;
    private int ph = 396;
    private static final String[] TABS = new String[]{"Connect", "HUD", "Alerts", "Music", "Look"};
    private static final int T_CONNECT = 0;
    private static final int T_HUD = 1;
    private static final int T_ALERTS = 2;
    private static final int T_MUSIC = 3;
    private static final int T_LOOK = 4;
    private static final IconShapes.Shape[] TAB_ICONS = new IconShapes.Shape[]{IconShapes.Shape.STREAM, IconShapes.Shape.LAYOUT, IconShapes.Shape.HEART, IconShapes.Shape.NOTE, IconShapes.Shape.SPARKLE};
    private static final String[] MUSIC = new String[]{"AUTO", "Spotify", "YT Music", "YouTube"};
    private static final int[] KEYS = new int[]{75, 79, 74, 72, 297, 344, 0};
    private static final String[] KEY_NAMES = new String[]{"K", "O", "J", "H", "F8", "Right Shift", "Off"};
    private static final int[] MKEYS = new int[]{77, 78, 66, 296, 0};
    private static final String[] MKEY_NAMES = new String[]{"M", "N", "B", "F7", "Off"};
    private static final int[] CKEYS = new int[]{78, 66, 80, 295, 296, 0};
    private static final int BLUE = 3111935;
    private static final int GREEN = 3126359;
    private static final int RED = 15026239;
    private static int tab = 0;
    private final class_437 parent;
    private final List<class_339> ws = new ArrayList<class_339>();
    private final List<Lbl> labels = new ArrayList<Lbl>();
    private final List<Box> groups = new ArrayList<Box>();
    private final Spring pop = new Spring(0.92f, 1.0f, 230.0f, 21.0f);
    private long last;
    private int px;
    private int py;
    private class_342 userF;
    private class_342 euKeyF;
    private class_342 ytKeyF;
    private class_342 vidF;
    private class_342 spotF;

    public StreamHudConfigScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"GlassUI"));
        this.parent = parent;
    }

    private <T extends class_339> T reg(T w) {
        this.method_37063((class_364)w);
        this.ws.add(w);
        return w;
    }

    private GlassWidgets.Chip chip(String text, IconShapes.Shape icon, int accent, BooleanSupplier sel, Runnable act, int x, int y, int w, int h) {
        return this.reg(new GlassWidgets.Chip(x, y, w, h, (class_2561)class_2561.method_43470((String)text), icon, accent, sel, act));
    }

    private void toggleRow(String text, int x, int y, int w, BooleanSupplier get, Runnable flip) {
        this.reg(new GlassWidgets.Toggle(x, y, w, 24, (class_2561)class_2561.method_43470((String)text), get, flip));
    }

    private class_342 field(int x, int y, int w, String text, String hint, int max) {
        class_342 f = GlassWidgets.field(this.field_22793, x, y, w, 20, text, hint, max);
        return this.reg(f);
    }

    private void label(String text, int x, int y) {
        this.labels.add(new Lbl(text, x, y, true));
    }

    protected void method_25426() {
        this.ws.clear();
        this.labels.clear();
        this.groups.clear();
        this.spotF = null;
        this.vidF = null;
        this.ytKeyF = null;
        this.euKeyF = null;
        this.userF = null;
        GlassConfig cfg = GlassConfig.get();
        boolean wide = tab == 1 || tab == 4;
        int n = this.pw = wide ? 540 : 324;
        this.ph = tab == 1 ? 346 : (tab == 4 ? 268 : 396);
        this.px = (this.field_22789 - this.pw) / 2;
        this.py = (this.field_22790 - this.ph) / 2;
        int fx = this.px + 18;
        int fw = this.pw - 36;
        int gap = 6;
        int bw = (fw - gap) / 2;
        int x1 = fx;
        int x2 = fx + bw + gap;
        int tw = (fw - 8) / TABS.length;
        for (int i = 0; i < TABS.length; ++i) {
            int idx = i;
            this.reg(new GlassWidgets.Tab(fx + 4 + i * tw, this.py + 40, tw, 22, (class_2561)class_2561.method_43470((String)TABS[i]), TAB_ICONS[i], () -> tab == idx, () -> {
                this.saveFields();
                tab = idx;
                this.method_41843();
            }));
        }
        switch (tab) {
            case 0: {
                this.initConnect(cfg, fx, fw, x1, x2, bw);
                break;
            }
            case 1: {
                this.initHud(cfg, fx, fw, x1, x2, bw);
                break;
            }
            case 2: {
                this.initAlerts(cfg, fx, fw, x1, x2, bw);
                break;
            }
            case 3: {
                this.initMusic(cfg, fx, fw, x1, x2, bw);
                break;
            }
            default: {
                this.initLook(cfg, fx, fw, x1, x2, bw);
            }
        }
        this.chip("Done", null, 3111935, () -> true, this::method_25419, this.px + this.pw - 18 - 96, this.py + this.ph - 34, 96, 22);
    }

    private void initConnect(GlassConfig cfg, int fx, int fw, int x1, int x2, int bw) {
        int y = this.py + 74;
        this.chip("TikTok Live", IconShapes.Shape.STREAM, 3111935, cfg::isTikTok, () -> this.setSource(cfg, "TIKTOK"), x1, y, bw, 22);
        this.chip("YouTube", IconShapes.Shape.PLAY, 3111935, () -> !cfg.isTikTok(), () -> this.setSource(cfg, "YOUTUBE"), x2, y, bw, 22);
        if (cfg.isTikTok()) {
            this.label("TikTok username (@name or live URL)", fx + 2, y + 32);
            this.userF = this.field(fx, y + 43, fw, cfg.tiktokUser, "@username", 80);
            boolean euler = !"TIKFINITY".equals(cfg.tiktokMode);
            this.chip("Euler Stream", null, 3111935, () -> !"TIKFINITY".equals(cfg.tiktokMode), () -> this.setMode(cfg, "EULER"), x1, y + 74, bw, 22);
            this.chip("TikFinity (local)", null, 3111935, () -> "TIKFINITY".equals(cfg.tiktokMode), () -> this.setMode(cfg, "TIKFINITY"), x2, y + 74, bw, 22);
            if (euler) {
                this.label("Euler Stream API key  (from eulerstream.com)", fx + 2, y + 106);
                this.euKeyF = this.field(fx, y + 117, fw, cfg.tiktokKey, "paste key here", 200);
            } else {
                this.label("Run TikFinity on this PC and log in to TikTok there.", fx + 2, y + 108);
            }
        } else {
            this.label("YouTube Data API v3 key", fx + 2, y + 32);
            this.ytKeyF = this.field(fx, y + 43, fw, cfg.apiKey, "AIza...", 128);
            this.label("Video / live stream URL or ID", fx + 2, y + 74);
            this.vidF = this.field(fx, y + 85, fw, cfg.videoInput, "https://youtube.com/watch?v=...", 200);
        }
        boolean on = StreamHud.isConnecting();
        this.chip(on ? "Disconnect" : "Connect", IconShapes.Shape.STREAM, on ? 15026239 : 3126359, () -> true, () -> {
            this.saveFields();
            GlassConfig.save();
            if (StreamHud.isConnecting()) {
                StreamHud.disconnect();
            } else if (!cfg.isLinkReady()) {
                StreamHud.DATA.status = "Fill in all fields first";
            } else {
                StreamHud.restart();
            }
            this.method_41843();
        }, fx, this.py + this.ph - 34, 112, 22);
    }

    private void setSource(GlassConfig cfg, String s) {
        if (s.equals(cfg.source)) {
            return;
        }
        this.saveFields();
        cfg.source = s;
        GlassConfig.save();
        this.method_41843();
    }

    private void setMode(GlassConfig cfg, String m) {
        if (m.equals(cfg.tiktokMode)) {
            return;
        }
        this.saveFields();
        cfg.tiktokMode = m;
        GlassConfig.save();
        this.method_41843();
    }

    private void initHud(GlassConfig cfg, int fx, int fw, int x1, int x2, int bw) {
        int y = this.py + 74;
        int gap = 10;
        int colW = (fw - gap) / 2;
        int rx = fx + colW + gap;
        int hb = (colW - 6) / 2;
        this.groups.add(new Box(fx, y, colW, 196));
        this.groups.add(new Box(rx, y, colW, 148));
        this.toggleRow("Stream HUD", fx, y + 2, colW, () -> cfg.hudEnabled, () -> {
            cfg.hudEnabled = !cfg.hudEnabled;
        });
        this.toggleRow("Island (views + likes)", fx, y + 26, colW, () -> cfg.island.on, () -> {
            cfg.island.on = !cfg.island.on;
        });
        this.toggleRow("Live chat", fx, y + 50, colW, () -> cfg.chat.on, () -> {
            cfg.chat.on = !cfg.chat.on;
        });
        this.toggleRow("Now playing", fx, y + 74, colW, () -> cfg.song.on, () -> {
            cfg.song.on = !cfg.song.on;
        });
        this.toggleRow("Profile (HP / food / armor / air)", fx, y + 98, colW, () -> cfg.profile.on, () -> {
            cfg.profile.on = !cfg.profile.on;
        });
        this.toggleRow("Combo counter", fx, y + 122, colW, () -> cfg.combo.on, () -> {
            cfg.combo.on = !cfg.combo.on;
        });
        this.toggleRow("Damage numbers", fx, y + 146, colW, () -> cfg.damageNumbers, () -> {
            cfg.damageNumbers = !cfg.damageNumbers;
        });
        this.toggleRow("Top tappers", fx, y + 170, colW, () -> cfg.topTap.on, () -> {
            cfg.topTap.on = !cfg.topTap.on;
            GlassConfig.save();
        });
        this.toggleRow("Gift / follow pop-up", rx, y + 2, colW, () -> cfg.alert.on, () -> {
            cfg.alert.on = !cfg.alert.on;
        });
        this.toggleRow("Music disc", rx, y + 26, colW, () -> cfg.cassette.on, () -> {
            cfg.cassette.on = !cfg.cassette.on;
            GlassConfig.save();
        });
        this.toggleRow("Lyrics", rx, y + 50, colW, () -> cfg.lyrics.on, () -> {
            cfg.lyrics.on = !cfg.lyrics.on;
            GlassConfig.save();
        });
        this.toggleRow("Request list", rx, y + 74, colW, () -> cfg.requests.on, () -> {
            cfg.requests.on = !cfg.requests.on;
            GlassConfig.save();
        });
        this.toggleRow("Accept !req / !play", rx, y + 98, colW, () -> cfg.requestsEnabled, () -> {
            cfg.requestsEnabled = !cfg.requestsEnabled;
            GlassConfig.save();
        });
        this.toggleRow("Request cuts current music", rx, y + 122, colW, () -> cfg.reqInterrupt, () -> {
            cfg.reqInterrupt = !cfg.reqInterrupt;
            GlassConfig.save();
        });
        this.reg(new GlassWidgets.Chip(rx, y + 152, hb, 20, (class_2561)class_2561.method_43470((String)("Chat lines: " + cfg.chatLines)), null, 3111935, () -> false, () -> {
            cfg.chatLines = cfg.chatLines >= 6 ? 2 : cfg.chatLines + 1;
            GlassConfig.save();
            this.method_41843();
        }));
        this.chip("Combo time: " + cfg.comboSec + "s", null, 3111935, () -> false, () -> {
            cfg.comboSec = cfg.comboSec >= 6 ? 2 : cfg.comboSec + 1;
            GlassConfig.save();
            this.method_41843();
        }, rx + hb + 6, y + 152, hb, 20);
        this.reg(new GlassWidgets.Chip(rx, y + 176, hb, 20, (class_2561)class_2561.method_43470((String)("Top tappers: " + cfg.topTapCount)), null, 3111935, () -> false, () -> {
            cfg.topTapCount = cfg.topTapCount >= 5 ? 1 : cfg.topTapCount + 1;
            GlassConfig.save();
            this.method_41843();
        }));
        this.chip("Tap window: " + cfg.tapWindowSec + "s", null, 3111935, () -> false, () -> {
            cfg.tapWindowSec = cfg.tapWindowSec >= 6 ? 2 : cfg.tapWindowSec + 1;
            GlassConfig.save();
            this.method_41843();
        }, rx + hb + 6, y + 176, hb, 20);
        this.chip("Reset layout", null, 3111935, () -> false, () -> {
            cfg.resetLayout();
            GlassConfig.save();
        }, fx, y + 206, colW, 24);
        this.chip("Edit layout", IconShapes.Shape.LAYOUT, 3111935, () -> true, () -> {
            this.saveFields();
            GlassConfig.save();
            this.field_22787.method_1507((class_437)new HudEditorScreen(this));
        }, rx, y + 206, colW, 24);
        this.label("Tip: click the island in any menu to open the music controls.", fx + 2, this.py + this.ph - 27);
    }

    private void initAlerts(GlassConfig cfg, int fx, int fw, int x1, int x2, int bw) {
        int y = this.py + 74;
        this.groups.add(new Box(fx, y, fw, 100));
        this.toggleRow("Gift / follow pop-up", fx, y + 2, fw, () -> cfg.alert.on, () -> {
            cfg.alert.on = !cfg.alert.on;
            GlassConfig.save();
        });
        this.toggleRow("Show gifts", fx, y + 26, fw, () -> cfg.alertGift, () -> {
            cfg.alertGift = !cfg.alertGift;
            GlassConfig.save();
        });
        this.toggleRow("Show follows", fx, y + 50, fw, () -> cfg.alertFollow, () -> {
            cfg.alertFollow = !cfg.alertFollow;
            GlassConfig.save();
        });
        this.toggleRow("Pop-up sound", fx, y + 74, fw, () -> cfg.alertSound, () -> {
            cfg.alertSound = !cfg.alertSound;
            GlassConfig.save();
        });
        this.chip("Test gift", IconShapes.Shape.SPARKLE, 3111935, () -> false, () -> LiveAlerts.push(new LiveAlerts.Alert(LiveAlerts.Kind.GIFT, "viewer123", "Rose", 5, 5L, System.currentTimeMillis())), x1, y + 112, bw, 24);
        this.chip("Test follow", IconShapes.Shape.HEART, 3111935, () -> false, () -> LiveAlerts.push(new LiveAlerts.Alert(LiveAlerts.Kind.FOLLOW, "viewer123", "", 0, 0L, System.currentTimeMillis())), x2, y + 112, bw, 24);
        this.chip("Edit layout", IconShapes.Shape.LAYOUT, 3111935, () -> true, () -> {
            this.saveFields();
            GlassConfig.save();
            this.field_22787.method_1507((class_437)new HudEditorScreen(this));
        }, x1, y + 142, bw, 24);
        this.label("Shows in-game, not in menus.", fx + 2, this.py + this.ph - 46);
        this.label("TikTok only. YouTube live has no", fx + 2, this.py + this.ph - 36);
        this.label("gift / follow events.", fx + 2, this.py + this.ph - 26);
    }

    private void initMusic(GlassConfig cfg, int fx, int fw, int x1, int x2, int bw) {
        int y = this.py + 74;
        this.label("Which music app do you use?", fx + 2, y);
        for (int i = 0; i < MUSIC.length; ++i) {
            String app = MUSIC[i];
            String name = app.equals("AUTO") ? "Auto-detect" : app;
            this.chip(name, i == 0 ? IconShapes.Shape.SPARKLE : IconShapes.Shape.NOTE, 3111935, () -> app.equals(cfg.musicApp), () -> {
                cfg.musicApp = app;
                GlassConfig.save();
            }, i % 2 == 0 ? x1 : x2, y + 12 + i / 2 * 28, bw, 22);
        }
        this.groups.add(new Box(fx, y + 68, fw, 40));
        this.label("Search & play from this mod", fx + 2, y + 116);
        boolean sp = "SPOTIFY".equals(cfg.musicProvider);
        this.chip("YouTube Music", IconShapes.Shape.PLAY, 15026239, () -> !"SPOTIFY".equals(cfg.musicProvider), () -> this.setProvider(cfg, "YOUTUBE"), x1, y + 128, bw, 22);
        if (sp) {
            this.label("Spotify Client ID (developer.spotify.com/dashboard)", fx + 2, y + 158);
            this.spotF = this.field(fx, y + 169, fw, cfg.spotifyClientId, "paste Client ID", 64);
            boolean in = MusicHub.spotifyLoggedIn();
            this.chip(in ? "Log out" : "Log in", IconShapes.Shape.PERSON, in ? 15026239 : 3126359, () -> true, () -> {
                this.saveFields();
                GlassConfig.save();
                if (MusicHub.spotifyLoggedIn()) {
                    MusicHub.spotifyLogout();
                } else {
                    MusicHub.spotifyLogin();
                }
                this.method_41843();
            }, x1, y + 196, bw, 22);
            this.label("Redirect URI to add in the dashboard:", fx + 2, y + 224);
            this.label("http://127.0.0.1:8898/callback", fx + 2, y + 235);
        } else {
            this.label("No API key or account. ffmpeg plays; yt-dlp is only a fallback.", fx + 2, y + 158);
            this.label(LocalPlayer.statusLine(), fx + 2, y + 170);
            this.chip("Background: " + (cfg.localPlayer ? "On" : "Off"), IconShapes.Shape.PLAY, 3126359, () -> cfg.localPlayer, () -> {
                this.saveFields();
                cfg.localPlayer = !cfg.localPlayer;
                GlassConfig.save();
                this.method_41843();
            }, x1, y + 196, bw, 22);
        }
        this.chip("Search songs", IconShapes.Shape.NOTE, 3111935, () -> true, () -> {
            this.saveFields();
            GlassConfig.save();
            this.field_22787.method_1507((class_437)new MusicScreen(this));
        }, x2, y + 196, bw, 22);
        this.label("Controls (prev / play / next) work with any app.", fx + 2, this.py + this.ph - 46);
    }

    private void setProvider(GlassConfig cfg, String p) {
        if (p.equals(cfg.musicProvider) || !"YOUTUBE".equals(p)) {
            return;
        }
        this.saveFields();
        cfg.musicProvider = p;
        GlassConfig.save();
        this.method_41843();
    }

    private void initLook(GlassConfig cfg, int fx, int fw, int x1, int x2, int bw) {
        int y = this.py + 74;
        int gap = 10;
        int colW = (fw - gap) / 2;
        int rx = fx + colW + gap;
        int hb = (colW - 6) / 2;
        int rx2 = rx + hb + 6;
        this.groups.add(new Box(fx, y, colW, 148));
        this.toggleRow("Glass buttons", fx, y + 2, colW, () -> cfg.glassButtons, () -> {
            cfg.glassButtons = !cfg.glassButtons;
        });
        this.toggleRow("Dock menu", fx, y + 26, colW, () -> cfg.dockMenu, () -> {
            cfg.dockMenu = !cfg.dockMenu;
        });
        this.toggleRow("Auto-connect", fx, y + 50, colW, () -> cfg.autoConnect, () -> {
            cfg.autoConnect = !cfg.autoConnect;
        });
        this.toggleRow("Performance mode", fx, y + 74, colW, () -> cfg.perfMode, () -> {
            cfg.perfMode = !cfg.perfMode;
            GlassConfig.save();
        });
        this.toggleRow("Chat pop sound", fx, y + 98, colW, () -> cfg.chatSound, () -> {
            cfg.chatSound = !cfg.chatSound;
            GlassConfig.save();
            if (cfg.chatSound) {
                ChatSound.play(cfg.chatSoundType, cfg.chatSoundVol);
            }
        });
        this.toggleRow("Arrows: skip / volume", fx, y + 122, colW, () -> cfg.arrowKeys, () -> {
            cfg.arrowKeys = !cfg.arrowKeys;
            GlassConfig.save();
        });
        this.chip("Menu key: " + StreamHudConfigScreen.keyName(cfg.menuKey), IconShapes.Shape.GEAR, 3111935, () -> false, () -> {
            int i = 0;
            for (int k = 0; k < KEYS.length; ++k) {
                if (KEYS[k] != cfg.menuKey) continue;
                i = k;
            }
            cfg.menuKey = KEYS[(i + 1) % KEYS.length];
            GlassConfig.save();
            this.method_41843();
        }, rx, y, hb, 24);
        this.chip("Music key: " + StreamHudConfigScreen.mkeyName(cfg.musicKey), IconShapes.Shape.NOTE, 3111935, () -> false, () -> {
            int i = 0;
            for (int k = 0; k < MKEYS.length; ++k) {
                if (MKEYS[k] != cfg.musicKey) continue;
                i = k;
            }
            cfg.musicKey = MKEYS[(i + 1) % MKEYS.length];
            GlassConfig.save();
            this.method_41843();
        }, rx2, y, hb, 24);
        this.chip("Hotbar: " + (cfg.glassHotbar ? "Glass" : "Vanilla"), null, 3111935, () -> false, () -> {
            cfg.glassHotbar = !cfg.glassHotbar;
            GlassConfig.save();
            this.method_41843();
        }, rx, y + 28, hb, 24);
        this.chip("Bars: " + (cfg.hideVanillaBars ? "Hidden" : "Shown"), null, 3111935, () -> false, () -> {
            cfg.hideVanillaBars = !cfg.hideVanillaBars;
            GlassConfig.save();
            this.method_41843();
        }, rx2, y + 28, hb, 24);
        this.chip("Air bar: " + (cfg.profileAirAlways ? "Always" : "Auto"), null, 3111935, () -> false, () -> {
            cfg.profileAirAlways = !cfg.profileAirAlways;
            GlassConfig.save();
            this.method_41843();
        }, rx, y + 56, hb, 24);
        this.chip("Style...", IconShapes.Shape.SPARKLE, 3111935, () -> false, () -> {
            this.saveFields();
            this.field_22787.method_1507((class_437)new StyleScreen(this));
        }, rx2, y + 56, hb, 24);
        this.chip("Song on/off key: " + MusicToggle.keyLabel(cfg.cassetteKey), IconShapes.Shape.POWER, 3111935, () -> false, () -> {
            int i = 0;
            for (int k = 0; k < CKEYS.length; ++k) {
                if (CKEYS[k] != cfg.cassetteKey) continue;
                i = k;
            }
            cfg.cassetteKey = CKEYS[(i + 1) % CKEYS.length];
            GlassConfig.save();
            this.method_41843();
        }, rx, y + 84, colW, 24);
        this.chip("Sound: " + ChatSound.NAMES[cfg.chatSoundType], IconShapes.Shape.NOTE, 3111935, () -> false, () -> {
            cfg.chatSoundType = (cfg.chatSoundType + 1) % ChatSound.NAMES.length;
            GlassConfig.save();
            ChatSound.play(cfg.chatSoundType, cfg.chatSoundVol);
            this.method_41843();
        }, rx, y + 112, hb, 22);
        this.chip("Volume: " + cfg.chatSoundVol + "%", null, 3111935, () -> false, () -> {
            int next = 25;
            for (int v : new int[]{25, 50, 75, 100}) {
                if (v <= cfg.chatSoundVol) continue;
                next = v;
                break;
            }
            cfg.chatSoundVol = next;
            GlassConfig.save();
            ChatSound.play(cfg.chatSoundType, cfg.chatSoundVol);
            this.method_41843();
        }, rx2, y + 112, hb, 22);
        this.label("Menu key opens this in-game. Performance mode = lighter glass, more FPS.", fx + 2, this.py + this.ph - 27);
    }

    private static String keyName(int code) {
        for (int i = 0; i < KEYS.length; ++i) {
            if (KEYS[i] != code) continue;
            return KEY_NAMES[i];
        }
        return "?";
    }

    private static String mkeyName(int code) {
        for (int i = 0; i < MKEYS.length; ++i) {
            if (MKEYS[i] != code) continue;
            return MKEY_NAMES[i];
        }
        return "?";
    }

    private void saveFields() {
        GlassConfig cfg = GlassConfig.get();
        if (this.userF != null) {
            cfg.tiktokUser = this.userF.method_1882().trim();
        }
        if (this.euKeyF != null) {
            cfg.tiktokKey = this.euKeyF.method_1882().trim();
        }
        if (this.ytKeyF != null) {
            cfg.apiKey = this.ytKeyF.method_1882().trim();
        }
        if (this.vidF != null) {
            cfg.videoInput = this.vidF.method_1882().trim();
        }
        if (this.spotF != null) {
            cfg.spotifyClientId = this.spotF.method_1882().trim();
        }
    }

    public void method_25419() {
        this.saveFields();
        GlassConfig.save();
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
        m.translate(cx, cy);
        float sq = this.pop.squash();
        m.scale(this.pop.pos * (1.0f - sq * 0.6f), this.pop.pos * (1.0f + sq));
        m.translate(-cx, -cy);
        Glass.panel(ctx, this.px, this.py, this.pw, this.ph, 20, 0xFFFFFF, 1.2f, a);
        int white = Glass.c(Math.max(64, (int)(255.0f * a)), 0xFFFFFF);
        int grey = Glass.c(Math.max(64, (int)(170.0f * a)), 13158608);
        Icons.drawShadowed(ctx, IconShapes.Shape.SPARKLE, this.px + 18, this.py + 13, 13.0f, 12572927, (int)(255.0f * a));
        Fx.draw(ctx, this.field_22793, "GlassUI", this.px + 18 + 18, this.py + 15, white, true);
        Fx.draw(ctx, this.field_22793, "Stream HUD & look", this.px + 18 + 18 + Fx.w(this.field_22793, "GlassUI") + 6, this.py + 15, grey, false);
        Glass.inset(ctx, this.px + 18, this.py + 38, this.pw - 36, 26, 13, false, a);
        for (Box g : this.groups) {
            Glass.inset(ctx, g.x(), g.y(), g.w(), g.h(), 10, false, a);
        }
        if (this.groups.size() > 0) {
            this.drawDividers(ctx, a);
        }
        for (Lbl l : this.labels) {
            Fx.draw(ctx, this.field_22793, l.text(), l.x(), l.y(), grey, false);
        }
        GlassConfig cfg = GlassConfig.get();
        StreamData d = StreamHud.DATA;
        if (tab == 0) {
            String st = !d.status.isEmpty() ? d.status : (d.linkState.isEmpty() ? "Not connected" : d.linkState);
            int col = !d.status.isEmpty() ? -38037 : (d.live ? -13577896 : -3618608);
            int dotCol = !d.status.isEmpty() ? 16729402 : (d.live ? 3199320 : 9342611);
            Glass.fill(ctx, this.px + 18 + 128, this.py + this.ph - 27, 7.0f, 7.0f, 3.5f, Glass.c(255, dotCol), Glass.c(255, dotCol));
            Fx.draw(ctx, this.field_22793, Fx.trim(this.field_22793, st, this.pw - 18 - 140 - 110), this.px + 18 + 140, this.py + this.ph - 28, col, false);
        } else if (tab == 3) {
            String t1;
            StreamData.NowPlaying np = d.song;
            int by = this.py + 74 + 68;
            Icons.drawShadowed(ctx, IconShapes.Shape.NOTE, this.px + 18 + 12, by + 14, 12.0f, np == null ? 0x909098 : 3199320, 255);
            String string = t1 = np == null ? "Nothing detected" : (np.title() == null ? "" : np.title());
            String t2 = np == null ? "Play something in your music app" : (np.artist() == null || np.artist().isBlank() ? np.source() : np.artist() + " - " + np.source());
            Fx.draw(ctx, this.field_22793, Fx.trim(this.field_22793, t1, this.pw - 36 - 44), this.px + 18 + 32, by + 9, -1, true);
            Fx.draw(ctx, this.field_22793, Fx.trim(this.field_22793, t2, this.pw - 36 - 44), this.px + 18 + 32, by + 22, grey, false);
        }
        for (class_339 w : this.ws) {
            if (w instanceof class_342) {
                boolean secret;
                class_342 f = (class_342)w;
                boolean bl = secret = (w == this.euKeyF || w == this.ytKeyF) && !w.method_25370() && f.method_1882().length() > 0;
                if (secret) {
                    GlassWidgets.drawMasked(ctx, this.field_22793, f);
                    continue;
                }
                GlassWidgets.drawFieldBack(ctx, f);
            }
            w.method_25394(ctx, mx, my, dt);
        }
        m.popMatrix();
    }

    private void drawDividers(class_332 ctx, float a) {
        block5: {
            block6: {
                block4: {
                    if (tab != 1) break block4;
                    for (Box g : this.groups) {
                        int rows = (g.h() - 4) / 24;
                        for (int i = 1; i < rows; ++i) {
                            Glass.fill(ctx, g.x() + 10, g.y() + 2 + i * 24, g.w() - 20, 0.5f, 0.0f, Glass.c((int)(40.0f * a), 0xFFFFFF), Glass.c((int)(40.0f * a), 0xFFFFFF));
                        }
                    }
                    break block5;
                }
                if (tab != 2) break block6;
                Box g = this.groups.get(0);
                for (int i = 1; i < 4; ++i) {
                    Glass.fill(ctx, g.x() + 10, g.y() + 2 + i * 24, g.w() - 20, 0.5f, 0.0f, Glass.c((int)(40.0f * a), 0xFFFFFF), Glass.c((int)(40.0f * a), 0xFFFFFF));
                }
                break block5;
            }
            if (tab != 4) break block5;
            Box g = this.groups.get(0);
            for (int i = 1; i < 5; ++i) {
                Glass.fill(ctx, g.x() + 10, g.y() + 2 + i * 24, g.w() - 20, 0.5f, 0.0f, Glass.c((int)(40.0f * a), 0xFFFFFF), Glass.c((int)(40.0f * a), 0xFFFFFF));
            }
        }
    }

    private record Lbl(String text, int x, int y, boolean dim) {
    }

    private record Box(int x, int y, int w, int h) {
    }
}

