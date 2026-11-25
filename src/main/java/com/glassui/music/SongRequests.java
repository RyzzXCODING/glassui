/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.music;

import com.glassui.GlassConfig;
import com.glassui.GlassUiClient;
import com.glassui.hud.StreamData;
import com.glassui.hud.StreamHud;
import com.glassui.music.LocalPlayer;
import com.glassui.music.MusicHub;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SongRequests {
    private static final Pattern CMD = Pattern.compile("^!(?:req|play)\\s+(.+)$", 2);
    private static final Pattern YT_LINK = Pattern.compile("(?:v=|youtu\\.be/|/shorts/|/live/)([A-Za-z0-9_-]{11})");
    private static final long USER_GAP_MS = 5000L;
    private static final Object LOCK = new Object();
    private static final ArrayDeque<Req> QUEUE = new ArrayDeque();
    private static final Map<String, Long> LAST = new HashMap<String, Long>();
    private static volatile List<Req> view = List.of();
    private static volatile Req cur;
    private static volatile long curSince;
    private static final ScheduledExecutorService EX;

    private SongRequests() {
    }

    public static List<Req> pending() {
        return view;
    }

    public static Req current() {
        return SongRequests.playingRequest() ? cur : null;
    }

    public static void onChat(String author, String text) {
        if (text == null || text.length() < 5 || text.charAt(0) != '!') {
            return;
        }
        GlassConfig cfg = GlassConfig.get();
        if (!cfg.requestsEnabled) {
            return;
        }
        Matcher m = CMD.matcher(text.trim());
        if (!m.matches()) {
            return;
        }
        String q = m.group(1).replaceAll("\\p{Cntrl}", " ").replaceAll("\\s+", " ").trim();
        Matcher yl = YT_LINK.matcher(q);
        if (yl.find()) {
            q = yl.group(1);
        } else if (q.contains("://")) {
            return;
        }
        if (q.length() < 2) {
            return;
        }
        if (q.length() > 80) {
            q = q.substring(0, 80);
        }
        String by = author == null || author.isBlank() ? "viewer" : author.trim();
        String query = q;
        EX.execute(() -> {
            try {
                SongRequests.handle(by, query);
            }
            catch (Throwable t) {
                GlassUiClient.LOGGER.debug("Song request failed: {}", (Object)t.toString());
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void handle(String by, String query) throws Exception {
        GlassConfig cfg = GlassConfig.get();
        if (!MusicHub.enabled()) {
            return;
        }
        long now = System.currentTimeMillis();
        Object object = LOCK;
        synchronized (object) {
            if (QUEUE.size() >= cfg.reqMaxQueue) {
                return;
            }
            int mine = 0;
            for (Req r : QUEUE) {
                if (!r.by().equalsIgnoreCase(by)) continue;
                ++mine;
            }
            if (mine >= cfg.reqPerUser) {
                return;
            }
            String key = by.toLowerCase(Locale.ROOT);
            Long last = LAST.get(key);
            if (last != null && now - last < 5000L) {
                return;
            }
            LAST.put(key, now);
            if (LAST.size() > 300) {
                LAST.values().removeIf(v -> now - v > 60000L);
            }
        }
        MusicHub.Track t = MusicHub.resolveTop(query);
        if (t == null) {
            return;
        }
        Object object2 = LOCK;
        synchronized (object2) {
            if (QUEUE.size() >= cfg.reqMaxQueue) {
                return;
            }
            Req c = cur;
            if (c != null && SongRequests.playingRequest() && c.track().id().equals(t.id())) {
                return;
            }
            for (Req r : QUEUE) {
                if (!r.track().id().equals(t.id())) continue;
                return;
            }
            QUEUE.addLast(new Req(by, t));
            view = List.copyOf(QUEUE);
        }
        SongRequests.kick();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean advance() {
        Object object = LOCK;
        synchronized (object) {
            if (QUEUE.isEmpty() || !MusicHub.enabled()) {
                return false;
            }
            SongRequests.startNext();
            return true;
        }
    }

    private static void startNext() {
        Req r = QUEUE.pollFirst();
        if (r == null) {
            return;
        }
        cur = r;
        curSince = System.currentTimeMillis();
        view = List.copyOf(QUEUE);
        MusicHub.playSingle(r.track());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void kick() {
        GlassConfig cfg = GlassConfig.get();
        Object object = LOCK;
        synchronized (object) {
            boolean filler;
            if (QUEUE.isEmpty() || !MusicHub.enabled()) {
                return;
            }
            long since = System.currentTimeMillis() - curSince;
            StreamData.NowPlaying np = StreamHud.DATA.song;
            boolean idle = np == null && !MusicHub.busy() && since > 3000L;
            boolean bl = filler = np != null && LocalPlayer.active() && cfg.reqInterrupt && since > 15000L && !SongRequests.playingRequest();
            if (idle || filler) {
                SongRequests.startNext();
            }
        }
    }

    private static boolean playingRequest() {
        Req c = cur;
        StreamData.NowPlaying np = StreamHud.DATA.song;
        if (c == null || np == null) {
            return false;
        }
        MusicHub.Track t = c.track();
        String title = t.title();
        if (title == null || !title.equals(np.title())) {
            return false;
        }
        String provider = t.provider();
        String source = np.source();
        boolean providerOk = provider == null || provider.isBlank() || source == null || source.isBlank()
            || provider.equalsIgnoreCase(source)
            || ("YOUTUBE".equalsIgnoreCase(provider) && ("YouTube".equalsIgnoreCase(source) || "YT Music".equalsIgnoreCase(source)));
        if (!providerOk) {
            return false;
        }
        String id = t.id();
        String appId = np.appId();
        // appId is normally an app/process id ("glassui-local", "chrome.exe"), not a video id;
        // only treat it as request identity when it actually has a track-id shape.
        boolean idComparable = id != null && !id.isBlank() && appId != null
            && (appId.matches("[A-Za-z0-9_-]{11}") || appId.matches("[A-Za-z0-9]{22}"));
        return !idComparable || id.equalsIgnoreCase(appId);
    }

    static {
        EX = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "GlassUI-Requests");
            t.setDaemon(true);
            return t;
        });
        EX.scheduleWithFixedDelay(() -> {
            try {
                SongRequests.kick();
            }
            catch (Throwable t) {
                GlassUiClient.LOGGER.debug("Request check failed: {}", (Object)t.toString());
            }
        }, 3L, 3L, TimeUnit.SECONDS);
    }

    public record Req(String by, MusicHub.Track track) {
    }
}

