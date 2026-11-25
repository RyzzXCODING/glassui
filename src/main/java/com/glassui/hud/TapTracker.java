/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TapTracker {
    private static final int MAX_USERS = 2000;
    private static final int KEEP_USERS = 1000;
    private static final int MAX_TOP = 5;
    private static final long CACHE_NS = 150000000L;
    private static final Map<String, Tapper> MAP = new HashMap<String, Tapper>();
    private static View cache;
    private static long cacheAt;
    private static final View PREVIEW;

    private TapTracker() {
    }

    static synchronized void record(String user, long count) {
        if (user == null || user.isBlank() || count <= 0L) {
            return;
        }
        long add = Math.min(count, 5000L);
        String key = user.toLowerCase(Locale.ROOT);
        Tapper t = MAP.get(key);
        if (t == null) {
            if (MAP.size() >= 2000) {
                TapTracker.prune();
                TapTracker.evictToCap();
            }
            t = new Tapper();
            MAP.put(key, t);
        }
        t.name = user;
        t.total += add;
        t.lastNs = System.nanoTime();
        cache = null;
    }

    static synchronized void clear() {
        MAP.clear();
        cache = null;
    }

    public static synchronized List<Entry> tappingNow() {
        long now = System.nanoTime();
        long win = TapTracker.windowNs();
        ArrayList<Tapper> list = new ArrayList<Tapper>();
        for (Tapper t : MAP.values()) {
            if (now - t.lastNs > win) continue;
            list.add(t);
        }
        list.sort((a, b) -> Long.compare(b.lastNs, a.lastNs));
        ArrayList<Entry> out = new ArrayList<Entry>(list.size());
        for (Tapper t : list) {
            out.add(new Entry(t.name, t.total, true));
        }
        return out;
    }

    public static synchronized boolean isTapping(String user) {
        if (user == null) {
            return false;
        }
        Tapper t = MAP.get(user.toLowerCase(Locale.ROOT));
        return t != null && System.nanoTime() - t.lastNs <= TapTracker.windowNs();
    }

    public static synchronized View view(boolean preview) {
        if (preview) {
            return PREVIEW;
        }
        long now = System.nanoTime();
        if (cache != null && now - cacheAt < 150000000L) {
            return cache;
        }
        long win = TapTracker.windowNs();
        ArrayList<Tapper> all = new ArrayList<Tapper>(MAP.values());
        all.sort((a, b) -> Long.compare(b.total, a.total));
        int tapping = 0;
        for (Tapper t : all) {
            if (now - t.lastNs > win) continue;
            ++tapping;
        }
        ArrayList<Entry> top = new ArrayList<Entry>(5);
        for (int i = 0; i < Math.min(5, all.size()); ++i) {
            Tapper t = (Tapper)all.get(i);
            top.add(new Entry(t.name, t.total, now - t.lastNs <= win));
        }
        cacheAt = now;
        cache = new View(top, tapping);
        return cache;
    }

    private static long windowNs() {
        return (long)Math.max(1, GlassConfig.get().tapWindowSec) * 1000000000L;
    }

    private static void prune() {
        long now = System.nanoTime();
        long win = TapTracker.windowNs();
        ArrayList<Map.Entry<String, Tapper>> es = new ArrayList<Map.Entry<String, Tapper>>(MAP.entrySet());
        es.sort((a, b) -> Long.compare(((Tapper)a.getValue()).total, ((Tapper)b.getValue()).total));
        int drop = MAP.size() - 1000;
        for (Map.Entry entry : es) {
            if (drop <= 0) break;
            if (now - ((Tapper)entry.getValue()).lastNs <= win) continue;
            MAP.remove(entry.getKey());
            --drop;
        }
    }

    private static void evictToCap() {
        if (MAP.size() < MAX_USERS) {
            return;
        }
        ArrayList<Map.Entry<String, Tapper>> es = new ArrayList<Map.Entry<String, Tapper>>(MAP.entrySet());
        es.sort((a, b) -> {
            int cmp = Long.compare(a.getValue().total, b.getValue().total);
            return cmp != 0 ? cmp : Long.compare(a.getValue().lastNs, b.getValue().lastNs);
        });
        int drop = MAP.size() - MAX_USERS + 1;
        for (Map.Entry<String, Tapper> e : es) {
            if (drop <= 0) break;
            MAP.remove(e.getKey());
            --drop;
        }
    }

    static {
        PREVIEW = new View(List.of(new Entry("andi", 1280L, true), new Entry("sasa_", 940L, true), new Entry("budi99", 615L, false), new Entry("mira", 402L, false), new Entry("rafi", 188L, false)), 2);
    }

    private static final class Tapper {
        String name;
        long total;
        long lastNs;

        private Tapper() {
        }
    }

    public record View(List<Entry> top, int tappingNow) {
    }

    public record Entry(String name, long taps, boolean tapping) {
    }
}

