/*
 * Decompiled with CFR 0.152.
 */
package com.glassui;

import com.glassui.GlassUiClient;
import com.glassui.ui.Fx;
import com.glassui.ui.Theme;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;

public class GlassConfig {
    public boolean glassButtons = true;
    public boolean dockMenu = true;
    public boolean hudEnabled = true;
    public boolean perfMode = true;
    public boolean glassHotbar = true;
    public boolean hideVanillaBars = true;
    public Widget island = new Widget(0.5f, 0.012f);
    public boolean islandDock = true;
    public boolean islandOpensMusic = true;
    public boolean jelly = true;
    public boolean uiSound = true;
    public int uiSoundVol = 60;
    public int theme = 0;
    public int font = 0;
    public Widget chat = new Widget(0.14f, 0.42f);
    public Widget song = new Widget(0.86f, 0.05f);
    public Widget profile = new Widget(0.88f, 0.66f);
    public Widget combo = new Widget(0.5f, 0.34f);
    public int comboSec = 3;
    public boolean damageNumbers = true;
    public Widget alert = new Widget(0.5f, 0.17f);
    public boolean alertGift = true;
    public boolean alertFollow = true;
    public boolean alertSound = true;
    public Widget cassette = new Widget(0.2f, 0.03f);
    public Widget lyrics = new Widget(0.5f, 0.6f);
    public int cassetteKey = 78;
    public boolean arrowKeys = true;
    public int lyricsOffsetMs = 0;
    public Widget requests = new Widget(0.86f, 0.2f);
    public Widget topTap = new Widget(0.86f, 0.3f);
    public int topTapCount = 5;
    public int tapWindowSec = 3;
    public boolean requestsEnabled = true;
    public boolean reqInterrupt = true;
    public int reqMaxQueue = 15;
    public int reqPerUser = 2;
    public int chatLines = 4;
    public boolean profileAirAlways = false;
    public boolean chatSound = true;
    public int chatSoundType = 0;
    public int chatSoundVol = 70;
    public String source = "TIKTOK";
    public boolean autoConnect = true;
    public String tiktokUser = "";
    public String tiktokMode = "EULER";
    public String tiktokKey = "";
    public String apiKey = "";
    public String videoInput = "";
    public int chatPollSec = 10;
    public int statsPollSec = 30;
    public String musicApp = "AUTO";
    public String musicProvider = "YOUTUBE";
    public String spotifyClientId = "";
    public String spotifyRefresh = "";
    public int musicKey = 77;
    public boolean localPlayer = true;
    public boolean autoSetupPlayer = true;
    public boolean browserFallback = false;
    public boolean spotifyMirror = true;
    public String ffmpegPath = "";
    public String ytdlpPath = "";
    public boolean shuffle = true;
    public int musicVolume = 70;
    public boolean musicEnabled = true;
    public int musicVolBeforeMute = 70;
    public List<String> musicHistory = new ArrayList<String>();
    public List<String> musicPopular = new ArrayList<String>();
    public int menuKey = 75;
    private static GlassConfig inst;
    private static final Gson GSON;
    private static final Pattern ID;
    private static final Pattern TT;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("glassui.json");
    }

    public static GlassConfig get() {
        if (inst == null) {
            GlassConfig.load();
        }
        return inst;
    }

    public static void load() {
        try {
            if (Files.exists(GlassConfig.path(), new LinkOption[0])) {
                inst = (GlassConfig)GSON.fromJson(Files.readString(GlassConfig.path()), GlassConfig.class);
            }
        }
        catch (Exception e) {
            GlassUiClient.LOGGER.warn("Config load failed", (Throwable)e);
        }
        if (inst == null) {
            inst = new GlassConfig();
        }
        if (GlassConfig.inst.island == null) {
            GlassConfig.inst.island = new Widget(0.5f, 0.012f);
        }
        if (GlassConfig.inst.chat == null) {
            GlassConfig.inst.chat = new Widget(0.14f, 0.42f);
        }
        if (GlassConfig.inst.song == null) {
            GlassConfig.inst.song = new Widget(0.86f, 0.05f);
        }
        if (GlassConfig.inst.profile == null) {
            GlassConfig.inst.profile = new Widget(0.88f, 0.66f);
        }
        if (GlassConfig.inst.combo == null) {
            GlassConfig.inst.combo = new Widget(0.5f, 0.34f);
        }
        if (GlassConfig.inst.alert == null) {
            GlassConfig.inst.alert = new Widget(0.5f, 0.17f);
        }
        if (GlassConfig.inst.cassette == null) {
            GlassConfig.inst.cassette = new Widget(0.2f, 0.03f);
        }
        if (GlassConfig.inst.lyrics == null) {
            GlassConfig.inst.lyrics = new Widget(0.5f, 0.6f);
        }
        if (GlassConfig.inst.requests == null) {
            GlassConfig.inst.requests = new Widget(0.86f, 0.2f);
        }
        if (GlassConfig.inst.topTap == null) {
            GlassConfig.inst.topTap = new Widget(0.86f, 0.3f);
        }
        GlassConfig.inst.topTapCount = Math.max(1, Math.min(5, GlassConfig.inst.topTapCount <= 0 ? 5 : GlassConfig.inst.topTapCount));
        GlassConfig.inst.tapWindowSec = Math.max(2, Math.min(6, GlassConfig.inst.tapWindowSec <= 0 ? 3 : GlassConfig.inst.tapWindowSec));
        GlassConfig.inst.reqMaxQueue = Math.max(1, Math.min(50, GlassConfig.inst.reqMaxQueue));
        GlassConfig.inst.reqPerUser = Math.max(1, Math.min(10, GlassConfig.inst.reqPerUser));
        GlassConfig.inst.lyricsOffsetMs = Math.max(-5000, Math.min(5000, GlassConfig.inst.lyricsOffsetMs));
        if (GlassConfig.inst.musicProvider == null || GlassConfig.inst.musicProvider.isBlank()) {
            GlassConfig.inst.musicProvider = "YOUTUBE";
        }
        if (GlassConfig.inst.spotifyClientId == null) {
            GlassConfig.inst.spotifyClientId = "";
        }
        if (GlassConfig.inst.spotifyRefresh == null) {
            GlassConfig.inst.spotifyRefresh = "";
        }
        if (GlassConfig.inst.ffmpegPath == null) {
            GlassConfig.inst.ffmpegPath = "";
        }
        if (GlassConfig.inst.ytdlpPath == null) {
            GlassConfig.inst.ytdlpPath = "";
        }
        if (GlassConfig.inst.musicHistory == null) {
            GlassConfig.inst.musicHistory = new ArrayList<String>();
        }
        if (GlassConfig.inst.musicPopular == null) {
            GlassConfig.inst.musicPopular = new ArrayList<String>();
        }
        GlassConfig.inst.musicVolume = Math.max(0, Math.min(100, GlassConfig.inst.musicVolume));
        GlassConfig.inst.theme = Math.max(0, Math.min(Theme.ALL.length - 1, GlassConfig.inst.theme));
        GlassConfig.inst.font = Math.max(0, Math.min(Fx.FONT_NAMES.length - 1, GlassConfig.inst.font));
        GlassConfig.inst.uiSoundVol = Math.max(0, Math.min(100, GlassConfig.inst.uiSoundVol));
        GlassConfig.inst.musicVolBeforeMute = Math.max(1, Math.min(100, GlassConfig.inst.musicVolBeforeMute));
        GlassConfig.inst.comboSec = Math.max(2, Math.min(8, GlassConfig.inst.comboSec));
        GlassConfig.inst.chatSoundType = Math.max(0, Math.min(2, GlassConfig.inst.chatSoundType));
        GlassConfig.inst.chatSoundVol = Math.max(0, Math.min(100, GlassConfig.inst.chatSoundVol));
        if (GlassConfig.inst.source == null) {
            GlassConfig.inst.source = "TIKTOK";
        }
        if (GlassConfig.inst.tiktokMode == null) {
            GlassConfig.inst.tiktokMode = "EULER";
        }
        if (GlassConfig.inst.musicApp == null) {
            GlassConfig.inst.musicApp = "AUTO";
        }
        if (GlassConfig.inst.tiktokUser == null) {
            GlassConfig.inst.tiktokUser = "";
        }
        if (GlassConfig.inst.tiktokKey == null) {
            GlassConfig.inst.tiktokKey = "";
        }
        if (GlassConfig.inst.apiKey == null) {
            GlassConfig.inst.apiKey = "";
        }
        if (GlassConfig.inst.videoInput == null) {
            GlassConfig.inst.videoInput = "";
        }
        GlassConfig.inst.chatLines = Math.max(2, Math.min(6, GlassConfig.inst.chatLines));
    }

    public static void save() {
        try {
            Path path = GlassConfig.path();
            Path tmp = path.resolveSibling(path.getFileName().toString() + ".tmp");
            Files.writeString(tmp, (CharSequence)GSON.toJson((Object)GlassConfig.get()), new OpenOption[0]);
            try {
                Files.move(tmp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (Exception e) {
            GlassUiClient.LOGGER.warn("Config save failed", (Throwable)e);
        }
    }

    public void resetLayout() {
        this.island = new Widget(0.5f, 0.012f);
        this.chat = new Widget(0.14f, 0.42f);
        this.song = new Widget(0.86f, 0.05f);
        this.profile = new Widget(0.88f, 0.66f);
        this.combo = new Widget(0.5f, 0.34f);
        this.alert = new Widget(0.5f, 0.17f);
        this.cassette = new Widget(0.2f, 0.03f);
        this.lyrics = new Widget(0.5f, 0.6f);
        this.requests = new Widget(0.86f, 0.2f);
        this.topTap = new Widget(0.86f, 0.3f);
    }

    public boolean isTikTok() {
        return !"YOUTUBE".equals(this.source);
    }

    public String videoId() {
        String s;
        String string = s = this.videoInput == null ? "" : this.videoInput.trim();
        if (s.matches("[A-Za-z0-9_-]{11}")) {
            return s;
        }
        Matcher m = ID.matcher(s);
        return m.find() ? m.group(1) : "";
    }

    public String tiktokName() {
        Matcher m;
        String s = this.tiktokUser == null ? "" : this.tiktokUser.trim();
        int at = s.indexOf(64);
        if (at >= 0) {
            s = s.substring(at);
        }
        return (m = TT.matcher(s)).find() ? m.group(1) : "";
    }

    public boolean isYoutubeReady() {
        return this.apiKey != null && !this.apiKey.isBlank() && !this.videoId().isEmpty();
    }

    public boolean isTikTokReady() {
        return !this.tiktokName().isEmpty() && ("TIKFINITY".equals(this.tiktokMode) || !this.tiktokKey.isBlank());
    }

    public boolean isLinkReady() {
        return this.isTikTok() ? this.isTikTokReady() : this.isYoutubeReady();
    }

    static {
        GSON = new GsonBuilder().setPrettyPrinting().create();
        ID = Pattern.compile("(?:v=|youtu\\.be/|/live/|/shorts/)([A-Za-z0-9_-]{11})");
        TT = Pattern.compile("@?([A-Za-z0-9._]{2,40})");
    }

    public static final class Widget {
        public boolean on = true;
        public float x;
        public float y;
        public float scale = 1.0f;

        public Widget() {
        }

        public Widget(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}

