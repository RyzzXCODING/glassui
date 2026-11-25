/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.music;

import com.glassui.GlassConfig;
import com.glassui.GlassUiClient;
import com.glassui.music.MusicHub;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class Suggest {
    public static final String PROVIDER = "SUGGEST";
    public static final String TAG_RECENT = "Recent";
    public static final String TAG_POPULAR = "Popular";
    public static final String TAG_SUGGEST = "Suggestion";
    private static final int MAX_ROWS = 14;
    private static final int MAX_HISTORY = 20;
    private static final List<String> BUILTIN = List.of("Die With A Smile - Lady Gaga, Bruno Mars", "Garam dan Madu - Tenxi, Naykilla, Jemsii", "Hati-Hati di Jalan - Tulus", "Blinding Lights - The Weeknd", "Lathi - Weird Genius", "Komang - Raim Laode", "Shape of You - Ed Sheeran", "Sial - Mahalini", "Someone You Loved - Lewis Capaldi", "Mangu - Fourtwnty", "Perfect - Ed Sheeran", "Sampai Jadi Debu - Banda Neira", "Bertaut - Nadin Amizah", "Nina - Feby Putri", "Pamer Bojo - Didi Kempot", "Love Me Like You Do - Ellie Goulding");
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
    private static final ScheduledExecutorService EX = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "GlassUI-Suggest");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicInteger GEN = new AtomicInteger();
    private static final List<String> SHUFFLED = Suggest.shuffled();
    private static volatile String typed = "";
    private static volatile List<String> remote = List.of();
    private static volatile String remoteFor = "";
    private static volatile List<MusicHub.Track> shown = List.of();

    private Suggest() {
    }

    private static List<String> shuffled() {
        ArrayList<String> l = new ArrayList<String>(BUILTIN);
        Collections.shuffle(l, new Random());
        return l;
    }

    public static List<MusicHub.Track> list() {
        List<MusicHub.Track> l = shown;
        if (l.isEmpty()) {
            Suggest.rebuild();
            l = shown;
        }
        return l;
    }

    public static List<MusicHub.Track> recentList() {
        ArrayList<MusicHub.Track> l = new ArrayList<MusicHub.Track>();
        for (String s : Suggest.recent()) {
            l.add(new MusicHub.Track(s, TAG_RECENT, PROVIDER, s));
        }
        return l;
    }

    public static List<MusicHub.Track> popularList(int max) {
        ArrayList<MusicHub.Track> l = new ArrayList<MusicHub.Track>();
        for (String s : Suggest.popular()) {
            if (l.size() >= max) break;
            l.add(new MusicHub.Track(s, TAG_POPULAR, PROVIDER, s));
        }
        return l;
    }

    public static void typing(String text) {
        typed = text == null ? "" : text;
        int g = GEN.incrementAndGet();
        Suggest.rebuild();
        String q = typed.trim();
        if (q.isEmpty()) {
            return;
        }
        EX.schedule(() -> Suggest.fetch(g, q), 250L, TimeUnit.MILLISECONDS);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void remember(MusicHub.Track t) {
        String e;
        if (t == null || PROVIDER.equals(t.provider())) {
            return;
        }
        String string = e = t.artist() == null || t.artist().isBlank() ? t.title() : t.title() + " - " + t.artist();
        if (e.length() > 90) {
            e = e.substring(0, 90);
        }
        List<String> h = GlassConfig.get().musicHistory;
        Class<Suggest> clazz = Suggest.class;
        synchronized (Suggest.class) {
            String entry = e;
            h.removeIf(x -> x.equalsIgnoreCase(entry));
            h.add(0, entry);
            while (h.size() > MAX_HISTORY) {
                h.remove(h.size() - 1);
            }
            // ** MonitorExit[var3_3] (shouldn't be in output)
            GlassConfig.save();
            Suggest.rebuild();
            return;
        }
    }

    private static List<String> popular() {
        List<String> custom = GlassConfig.get().musicPopular;
        List<String> src = custom != null && !custom.isEmpty() ? custom : SHUFFLED;
        ArrayList<String> out = new ArrayList<String>();
        for (String s : src) {
            if (s == null || s.isBlank()) continue;
            out.add(s.trim());
        }
        return out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static List<String> recent() {
        Class<Suggest> clazz = Suggest.class;
        synchronized (Suggest.class) {
            // ** MonitorExit[var0] (shouldn't be in output)
            return new ArrayList<String>(GlassConfig.get().musicHistory);
        }
    }

    private static void rebuild() {
        String q = typed.trim().toLowerCase(Locale.ROOT);
        List<String> rec = Suggest.recent();
        List<String> pop = Suggest.popular();
        LinkedHashMap<String, String> out = new LinkedHashMap<String, String>();
        if (q.isEmpty()) {
            int i = 0;
            int j = 0;
            while (out.size() < MAX_ROWS && (i < rec.size() || j < pop.size())) {
                if (i < rec.size()) {
                    Suggest.add(out, rec.get(i++), TAG_RECENT);
                }
                if (out.size() >= MAX_ROWS || j >= pop.size()) continue;
                Suggest.add(out, pop.get(j++), TAG_POPULAR);
            }
        } else {
            int local = 0;
            for (String s : rec) {
                if (local >= 2 || !s.toLowerCase(Locale.ROOT).contains(q) || !Suggest.add(out, s, TAG_RECENT)) continue;
                ++local;
            }
            for (String s : pop) {
                if (local >= 3 || !s.toLowerCase(Locale.ROOT).contains(q) || !Suggest.add(out, s, TAG_POPULAR)) continue;
                ++local;
            }
            if (q.equals(remoteFor)) {
                for (String s : remote) {
                    if (out.size() < MAX_ROWS) {
                        Suggest.add(out, s, TAG_SUGGEST);
                        continue;
                    }
                    break;
                }
            }
        }
        ArrayList<MusicHub.Track> l = new ArrayList<MusicHub.Track>();
        for (Map.Entry e : out.entrySet()) {
            l.add(new MusicHub.Track((String)e.getKey(), (String)e.getValue(), PROVIDER, (String)e.getKey()));
        }
        shown = List.copyOf(l);
    }

    private static boolean add(Map<String, String> out, String text, String tag) {
        for (String k : out.keySet()) {
            if (!k.equalsIgnoreCase(text)) continue;
            return false;
        }
        out.put(text, tag);
        return true;
    }

    private static void fetch(int g, String q) {
        if (g != GEN.get()) {
            return;
        }
        try {
            String url = "https://suggestqueries.google.com/complete/search?client=firefox&ds=yt&hl=en&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            HttpResponse<String> r = HTTP.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5L)).header("User-Agent", "Mozilla/5.0").GET().build(), HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() != 200) {
                return;
            }
            JsonArray root = JsonParser.parseString((String)r.body()).getAsJsonArray();
            ArrayList<String> out = new ArrayList<String>();
            if (root.size() >= 2 && root.get(1).isJsonArray()) {
                for (JsonElement el : root.get(1).getAsJsonArray()) {
                    if (!el.isJsonPrimitive()) continue;
                    out.add(el.getAsString());
                }
            }
            if (g != GEN.get()) {
                return;
            }
            remote = out;
            remoteFor = q.toLowerCase(Locale.ROOT);
            Suggest.rebuild();
        }
        catch (Exception e) {
            GlassUiClient.LOGGER.debug("Suggest fetch failed: {}", (Object)e.toString());
        }
    }
}

