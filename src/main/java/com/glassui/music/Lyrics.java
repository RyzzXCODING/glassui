/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.music;

import com.glassui.GlassConfig;
import com.glassui.GlassUiClient;
import com.glassui.hud.StreamData;
import com.glassui.music.LocalPlayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Lyrics {
    private static final HttpClient HTTP = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(8L)).build();
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) GlassUI-Lyrics/1.3";
    private static final ExecutorService EX = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "GlassUI-Lyrics");
        t.setDaemon(true);
        return t;
    });
    private static final Pattern TAG = Pattern.compile("\\[(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?]");
    private static volatile String key = "";
    private static volatile State state = State.IDLE;
    private static volatile List<Line> lines = List.of();
    private static volatile int gen;
    private static volatile String error;
    private static long estNs;
    private static double estSec;
    private static final String API = "https://lrclib.net/api/search?";

    private Lyrics() {
    }

    public static State state() {
        return state;
    }

    public static String error() {
        return error;
    }

    public static List<Line> lines() {
        return lines;
    }

    public static void track(StreamData.NowPlaying np) {
        if (np == null) {
            if (state != State.IDLE) {
                key = "";
                state = State.IDLE;
                lines = List.of();
                ++gen;
            }
            return;
        }
        String k = np.title() + "\n" + np.artist();
        if (k.equals(key)) {
            return;
        }
        key = k;
        estSec = 0.0;
        estNs = 0L;
        state = State.LOADING;
        error = "";
        lines = List.of();
        int g = ++gen;
        String title = np.title();
        String artist = np.artist();
        EX.execute(() -> Lyrics.load(g, title, artist));
    }

    public static double positionSec(StreamData.NowPlaying np) {
        double sec;
        if (LocalPlayer.active()) {
            sec = LocalPlayer.positionSec();
        } else {
            long now = System.nanoTime();
            if (estNs != 0L && np != null && np.playing()) {
                double delta = (double)(now - estNs) / 1.0E9;
                // Only abnormal gaps (e.g. suspend/stall > 2s) are capped; normal slow frames advance fully.
                estSec += delta > 2.0 ? 0.25 : delta;
            }
            estNs = now;
            sec = estSec;
        }
        return sec + (double)GlassConfig.get().lyricsOffsetMs / 1000.0;
    }

    public static int indexAt(List<Line> ls, double sec) {
        long ms = (long)(sec * 1000.0);
        int lo = 0;
        int hi = ls.size() - 1;
        int ans = -1;
        while (lo <= hi) {
            int mid = lo + hi >>> 1;
            if (ls.get(mid).ms() <= ms) {
                ans = mid;
                lo = mid + 1;
                continue;
            }
            hi = mid - 1;
        }
        return ans;
    }

    private static void load(int g, String rawTitle, String rawArtist) {
        List<Object> found = List.of();
        Object err = "";
        String title = Lyrics.cleanTitle(rawTitle);
        String artist = Lyrics.cleanArtist(rawArtist);
        for (String url : Lyrics.candidates(rawTitle, rawArtist)) {
            if (g != gen) {
                return;
            }
            try {
                String lrc = Lyrics.fetch(url);
                GlassUiClient.LOGGER.info("Lyrics LRCLIB {} -> {}", (Object)url, (Object)(lrc == null ? "none" : "found"));
                if (lrc == null || (found = Lyrics.parse(lrc)).isEmpty()) continue;
                break;
            }
            catch (Exception e) {
                err = "network: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
                GlassUiClient.LOGGER.warn("Lyrics LRCLIB failed: {}", (Object)e.toString());
            }
        }
        if (found.isEmpty() && g == gen) {
            try {
                String q = (title + " " + artist).trim();
                String lrc = Lyrics.netease(q, title);
                if (lrc == null && !artist.isEmpty()) {
                    lrc = Lyrics.netease(title, title);
                }
                GlassUiClient.LOGGER.info("Lyrics NetEase '{}' -> {}", (Object)q, (Object)(lrc == null ? "none" : "found"));
                if (lrc != null) {
                    found = Lyrics.parse(lrc);
                }
            }
            catch (Exception e) {
                if (((String)err).isEmpty()) {
                    err = "network: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
                }
                GlassUiClient.LOGGER.warn("Lyrics NetEase failed: {}", (Object)e.toString());
            }
        }
        if (g != gen) {
            return;
        }
        error = found.isEmpty() ? err : "";
        lines = found;
        state = found.isEmpty() ? State.NONE : State.READY;
    }

    private static String get(String url, boolean netease) throws Exception {
        HttpResponse<String> rs;
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10L)).header("User-Agent", netease ? UA : "GlassUI (Minecraft Fabric mod)").GET();
        if (netease) {
            b.header("Referer", "https://music.163.com/");
        }
        return (rs = HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))).statusCode() == 200 ? rs.body() : null;
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", "");
    }

    private static String netease(String query, String title) throws Exception {
        if (query.isBlank()) {
            return null;
        }
        String body = Lyrics.get("https://music.163.com/api/search/get/web?csrf_token=&s=" + Lyrics.enc(query) + "&type=1&offset=0&total=true&limit=6", true);
        if (body == null) {
            return null;
        }
        JsonElement root = JsonParser.parseString((String)body);
        if (!root.isJsonObject()) {
            return null;
        }
        JsonElement res = root.getAsJsonObject().get("result");
        if (res == null || !res.isJsonObject() || !res.getAsJsonObject().has("songs")) {
            return null;
        }
        String want = Lyrics.norm(title);
        for (JsonElement e : res.getAsJsonObject().getAsJsonArray("songs")) {
            String text;
            JsonElement lrc;
            JsonElement lr;
            String lb;
            JsonObject o = e.getAsJsonObject();
            String name = Lyrics.norm(o.get("name").getAsString());
            if (want.isEmpty() || !name.contains(want) && !want.contains(name) || name.isEmpty() || (lb = Lyrics.get("https://music.163.com/api/song/lyric?id=" + o.get("id").getAsLong() + "&lv=1&kv=1&tv=-1", true)) == null || !(lr = JsonParser.parseString((String)lb)).isJsonObject() || (lrc = lr.getAsJsonObject().get("lrc")) == null || !lrc.isJsonObject() || !lrc.getAsJsonObject().has("lyric") || (text = lrc.getAsJsonObject().get("lyric").getAsString()).isBlank() || !TAG.matcher(text).find()) continue;
            return text;
        }
        return null;
    }

    private static List<String> candidates(String rawTitle, String rawArtist) {
        String title = Lyrics.cleanTitle(rawTitle);
        String artist = Lyrics.cleanArtist(rawArtist);
        ArrayList<String> out = new ArrayList<String>();
        int dash = title.indexOf(" - ");
        String splitArtist = null;
        String splitTitle = null;
        if (dash > 0) {
            splitArtist = title.substring(0, dash).trim();
            splitTitle = title.substring(dash + 3).trim();
        }
        if (!artist.isEmpty()) {
            out.add("https://lrclib.net/api/search?track_name=" + Lyrics.enc(title) + "&artist_name=" + Lyrics.enc(artist));
        }
        if (splitTitle != null && !splitTitle.isEmpty()) {
            out.add("https://lrclib.net/api/search?track_name=" + Lyrics.enc(splitTitle) + "&artist_name=" + Lyrics.enc(splitArtist));
        }
        out.add("https://lrclib.net/api/search?q=" + Lyrics.enc((title + " " + artist).trim()));
        if (splitTitle != null && !splitTitle.isEmpty()) {
            out.add("https://lrclib.net/api/search?q=" + Lyrics.enc(splitArtist + " " + splitTitle));
        }
        if (!artist.isEmpty()) {
            out.add("https://lrclib.net/api/search?q=" + Lyrics.enc(title));
        }
        return out;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String fetch(String url) throws Exception {
        String body = Lyrics.get(url, false);
        if (body == null) {
            return null;
        }
        JsonElement root = JsonParser.parseString((String)body);
        if (!root.isJsonArray()) {
            return null;
        }
        for (JsonElement e : root.getAsJsonArray()) {
            JsonObject o;
            JsonElement sl;
            if (!e.isJsonObject() || (sl = (o = e.getAsJsonObject()).get("syncedLyrics")) == null || sl.isJsonNull() || sl.getAsString().isBlank()) continue;
            return sl.getAsString();
        }
        return null;
    }

    static List<Line> parse(String lrc) {
        ArrayList<Line> out = new ArrayList<Line>();
        for (String raw : lrc.split("\\R")) {
            String text;
            Matcher m = TAG.matcher(raw);
            ArrayList<Long> times = new ArrayList<Long>();
            int end = 0;
            while (m.find() && m.start() == end) {
                long min = Long.parseLong(m.group(1));
                long sec = Long.parseLong(m.group(2));
                String fr = m.group(3);
                long ms = 0L;
                if (fr != null) {
                    ms = Long.parseLong((fr + "00").substring(0, 3));
                }
                times.add((min * 60L + sec) * 1000L + ms);
                end = m.end();
            }
            if (times.isEmpty() || (text = raw.substring(end).trim()).matches("^(\u4f5c\u8bcd|\u4f5c\u66f2|\u7f16\u66f2|\u5236\u4f5c\u4eba|\u4f5c\u8a5e|\u4f5c\u66f2|\u7de8\u66f2|\u8bcd|\u66f2|Lyrics by|Composer)\\s*[:\uff1a].*")) continue;
            Iterator iterator = times.iterator();
            while (iterator.hasNext()) {
                long t = (Long)iterator.next();
                out.add(new Line(t, text));
            }
        }
        out.sort((a, b) -> Long.compare(a.ms(), b.ms()));
        return List.copyOf(out);
    }

    private static String cleanTitle(String s) {
        if (s == null) {
            return "";
        }
        s = s.replaceAll("\\s*[(\\[\u3010][^)\\]\u3011]*[)\\]\u3011]", " ");
        s = s.replaceAll("(?i)\\b(official\\s*(music\\s*)?(video|audio|lyric\\s*video)|lyric\\s*video|lyrics?|music\\s*video|audio|visuali[sz]er|hd|4k)\\b", " ");
        s = s.replaceAll("\\s*\\|.*$", " ");
        return s.replaceAll("\\s+", " ").trim();
    }

    private static String cleanArtist(String s) {
        if (s == null) {
            return "";
        }
        s = s.replaceAll("(?i)\\s*-\\s*topic$", "").replaceAll("(?i)\\s*vevo$", "").replaceAll("(?i)\\s*official$", "");
        return s.trim();
    }

    static {
        error = "";
    }

    public static enum State {
        IDLE,
        LOADING,
        READY,
        NONE;

    }

    public record Line(long ms, String text) {
    }
}

