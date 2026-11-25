/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.music;

import com.glassui.GlassConfig;
import com.glassui.GlassUiClient;
import com.glassui.hud.StreamData;
import com.glassui.hud.StreamHud;
import com.glassui.music.Launcher;
import com.glassui.music.LocalPlayer;
import com.glassui.music.Suggest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MusicHub {
    public static final int SPOTIFY_PORT = 8898;
    public static final String SPOTIFY_REDIRECT = "http://127.0.0.1:8898/callback";
    private static final Pattern YT_ID = Pattern.compile("[A-Za-z0-9_-]{11}");
    private static final Pattern SP_ID = Pattern.compile("[A-Za-z0-9]{22}");
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
    private static final ExecutorService EX = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "GlassUI-Music");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicInteger VERSION = new AtomicInteger();
    private static volatile List<Track> results = List.of();
    private static volatile boolean showResults;
    private static volatile String status;
    private static volatile boolean busy;
    private static String access;
    private static long accessExp;
    private static final Deque<String> RECENT_RANDOM;
    private static final Random RND;
    private static final Pattern YT_VID;
    private static final Pattern YT_TITLE;
    private static final Pattern YT_OWNER;
    private static final ScheduledExecutorService VOL_EX;
    private static final AtomicBoolean VOL_PENDING;
    private static final ExecutorService SETUP_EX;
    private static final ScheduledExecutorService SAVE_EX;
    private static final AtomicBoolean SAVE_PENDING;
    private static final String ASYNC_PLAYBACK = "<async-playback-pending>";

    private MusicHub() {
    }

    public static List<Track> results() {
        return results;
    }

    public static List<Track> listing() {
        List<Track> r = results;
        return showResults && !r.isEmpty() ? r : Suggest.list();
    }

    public static boolean listingIsSuggestions() {
        return !showResults || results.isEmpty();
    }

    public static void typing(String text) {
        showResults = false;
        Suggest.typing(text);
    }

    static void status(String st) {
        MusicHub.set(st, false);
    }

    public static String status() {
        return status;
    }

    public static boolean busy() {
        return busy;
    }

    public static int version() {
        return VERSION.get();
    }

    public static boolean spotifyLoggedIn() {
        return !GlassConfig.get().spotifyRefresh.isBlank();
    }

    private static void set(String st, boolean b) {
        status = st;
        busy = b;
        VERSION.incrementAndGet();
    }

    public static boolean enabled() {
        return GlassConfig.get().musicEnabled;
    }

    public static void setEnabled(boolean on) {
        GlassConfig cfg = GlassConfig.get();
        cfg.musicEnabled = on;
        GlassConfig.save();
        if (!on) {
            LocalPlayer.stop();
            MusicHub.set("Music off", false);
        } else {
            MusicHub.set("", false);
        }
    }

    public static boolean muted() {
        return GlassConfig.get().musicVolume <= 0;
    }

    public static void toggleMute() {
        GlassConfig cfg = GlassConfig.get();
        if (cfg.musicVolume > 0) {
            cfg.musicVolBeforeMute = cfg.musicVolume;
            MusicHub.setVolume(0);
        } else {
            MusicHub.setVolume(Math.max(1, cfg.musicVolBeforeMute));
        }
        MusicHub.commitVolume();
    }

    public static void search(String query) {
        String q;
        String string = q = query == null ? "" : query.trim();
        if (q.isEmpty()) {
            return;
        }
        boolean sp = false;
        MusicHub.set("Searching...", true);
        EX.execute(() -> {
            try {
                List<Track> r = MusicHub.youtubeSearch(q);
                results = r;
                showResults = true;
                MusicHub.set(r.isEmpty() ? "No results" : "", false);
            }
            catch (UserError e) {
                results = List.of();
                MusicHub.set(e.getMessage(), false);
            }
            catch (Exception e) {
                GlassUiClient.LOGGER.warn("Music search failed: {}", (Object)e.toString());
                results = List.of();
                MusicHub.set("Search failed (" + MusicHub.clip(String.valueOf(e.getMessage())) + ")", false);
            }
        });
    }

    public static void play(Track t) {
        if (!MusicHub.enabled()) {
            MusicHub.set("Music is off", false);
            return;
        }
        if (Suggest.PROVIDER.equals(t.provider())) {
            MusicHub.playQuery(t.id());
            return;
        }
        List<Track> r = results;
        int idx = r.indexOf(t);
        List<Track> q = idx >= 0 ? r : List.of(t);
        int at = Math.max(0, idx);
        EX.execute(() -> {
            MusicHub.set("Starting " + MusicHub.clip(t.title()) + "...", true);
            MusicHub.doPlay(q, at);
        });
    }

    public static Track resolveTop(String query) throws Exception {
        List<Track> r = MusicHub.youtubeSearch(query);
        return r.isEmpty() ? null : r.get(0);
    }

    public static void playSingle(Track t) {
        if (!MusicHub.enabled()) {
            MusicHub.set("Music is off", false);
            return;
        }
        EX.execute(() -> {
            MusicHub.set("Starting " + MusicHub.clip(t.title()) + "...", true);
            MusicHub.doPlay(List.of(t), 0);
        });
    }

    public static void playQuery(String query) {
        String q;
        if (!MusicHub.enabled()) {
            MusicHub.set("Music is off", false);
            return;
        }
        String string = q = query == null ? "" : query.trim();
        if (q.isEmpty()) {
            return;
        }
        MusicHub.set("Searching " + MusicHub.clip(q) + "...", true);
        EX.execute(() -> MusicHub.queryAndPlay(q));
    }

    private static void queryAndPlay(String q) {
        List<Track> r;
        try {
            r = MusicHub.youtubeSearch(q);
        }
        catch (UserError e) {
            MusicHub.set(e.getMessage(), false);
            return;
        }
        catch (Exception e) {
            GlassUiClient.LOGGER.warn("Music search failed: {}", (Object)e.toString());
            MusicHub.set("Search failed (" + MusicHub.clip(String.valueOf(e.getMessage())) + ")", false);
            return;
        }
        results = r;
        showResults = true;
        if (r.isEmpty()) {
            MusicHub.set("No results", false);
            return;
        }
        MusicHub.doPlay(r, 0);
    }

    public static void playRandom() {
        if (!MusicHub.enabled()) {
            MusicHub.set("Music is off", false);
            return;
        }
        MusicHub.set("Picking a random song...", true);
        EX.execute(() -> {
            ArrayList<Track> pool = new ArrayList<Track>(Suggest.popularList(60));
            pool.addAll(Suggest.recentList());
            ArrayList<String> cand = new ArrayList<String>();
            Deque<String> deque = RECENT_RANDOM;
            synchronized (deque) {
                for (Track t : pool) {
                    if (RECENT_RANDOM.contains(t.id()) || cand.contains(t.id())) continue;
                    cand.add(t.id());
                }
                if (cand.isEmpty()) {
                    RECENT_RANDOM.clear();
                    for (Track t : pool) {
                        if (cand.contains(t.id())) continue;
                        cand.add(t.id());
                    }
                }
            }
            if (cand.isEmpty()) {
                MusicHub.set("No songs to shuffle", false);
                return;
            }
            String pick = (String)cand.get(RND.nextInt(cand.size()));
            Deque<String> deque2 = RECENT_RANDOM;
            synchronized (deque2) {
                RECENT_RANDOM.addLast(pick);
                while (RECENT_RANDOM.size() > 12) {
                    RECENT_RANDOM.pollFirst();
                }
            }
            MusicHub.queryAndPlay(pick);
        });
    }

    private static void doPlay(List<Track> q, int at) {
        Track t = q.get(at);
        String note = null;
        try {
            if ("SPOTIFY".equals(t.provider())) {
                try {
                    MusicHub.spotifyPlay(t);
                }
                catch (UserError e) {
                    note = MusicHub.spotifyMirror(t, e);
                }
            } else {
                note = MusicHub.youtubePlay(q, at);
            }
        }
        catch (UserError e) {
            MusicHub.set(e.getMessage(), false);
            return;
        }
        catch (Exception e) {
            GlassUiClient.LOGGER.warn("Music play failed: {}", (Object)e.toString());
            MusicHub.set("Play failed (" + MusicHub.clip(String.valueOf(e.getMessage())) + ")", false);
            return;
        }
        Suggest.remember(t);
        if (MusicHub.ASYNC_PLAYBACK.equals(note)) {
            return;
        }
        MusicHub.set((String)(note != null ? note : "Playing: " + MusicHub.clip(t.title())), false);
    }

    private static List<Track> youtubeSearch(String q) throws Exception {
        String url = "https://www.youtube.com/results?search_query=" + MusicHub.enc(q) + "&sp=EgIQAQ%3D%3D";
        HttpResponse<String> r = HTTP.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(12L)).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36").header("Accept-Language", "en-US,en;q=0.9").GET().build(), HttpResponse.BodyHandlers.ofString());
        String html = r.body();
        ArrayList<Track> out = new ArrayList<Track>();
        HashSet<String> seen = new HashSet<String>();
        Matcher m = YT_VID.matcher(html);
        ArrayList<int[]> spots = new ArrayList<int[]>();
        ArrayList<String> ids = new ArrayList<String>();
        while (m.find()) {
            spots.add(new int[]{m.start()});
            ids.add(m.group(1));
        }
        for (int i = 0; i < ids.size() && out.size() < 14; ++i) {
            if (!seen.add((String)ids.get(i))) continue;
            int from = ((int[])spots.get(i))[0];
            int to = i + 1 < spots.size() ? ((int[])spots.get(i + 1))[0] : Math.min(html.length(), from + 6000);
            String seg = html.substring(from, Math.min(to, from + 6000));
            Matcher tm = YT_TITLE.matcher(seg);
            Matcher om = YT_OWNER.matcher(seg);
            if (!tm.find()) continue;
            out.add(new Track(MusicHub.jsonText(tm.group(1)), om.find() ? MusicHub.jsonText(om.group(1)) : "", "YOUTUBE", (String)ids.get(i)));
        }
        if (out.isEmpty()) {
            throw new UserError("YouTube search returned nothing (try again)");
        }
        return out;
    }

    private static String jsonText(String raw) {
        try {
            return JsonParser.parseString((String)("\"" + raw + "\"")).getAsString();
        }
        catch (Exception e) {
            return raw;
        }
    }

    private static String youtubePlay(List<Track> q, int at) throws Exception {
        Track t = q.get(at);
        if (!YT_ID.matcher(t.id()).matches()) {
            throw new UserError("Bad video id");
        }
        GlassConfig cfg = GlassConfig.get();
        LocalPlayer.probe();
        if (!LocalPlayer.ready() && cfg.localPlayer) {
            MusicHub.set("Setting up the player (first time only)...", true);
            SETUP_EX.execute(() -> {
                try {
                    LocalPlayer.setup();
                    if (LocalPlayer.ready()) {
                        LocalPlayer.play(q, at);
                        MusicHub.set("Playing: " + MusicHub.clip(t.title()), false);
                        return;
                    }
                    if (cfg.browserFallback) {
                        Launcher.open("https://music.youtube.com/watch?v=" + t.id());
                        MusicHub.set("Opened in browser. " + LocalPlayer.statusLine(), false);
                        return;
                    }
                    MusicHub.set(LocalPlayer.statusLine(), false);
                }
                catch (UserError e) {
                    MusicHub.set(e.getMessage(), false);
                }
                catch (Exception e) {
                    GlassUiClient.LOGGER.warn("Music play failed: {}", (Object)e.toString());
                    MusicHub.set("Play failed (" + MusicHub.clip(String.valueOf(e.getMessage())) + ")", false);
                }
            });
            return MusicHub.ASYNC_PLAYBACK;
        }
        if (LocalPlayer.ready()) {
            LocalPlayer.play(q, at);
            return null;
        }
        if (cfg.browserFallback || !cfg.localPlayer) {
            Launcher.open("https://music.youtube.com/watch?v=" + t.id());
            return "Opened in browser. " + LocalPlayer.statusLine();
        }
        throw new UserError(LocalPlayer.statusLine());
    }

    private static String spotifyMirror(Track t, UserError why) throws Exception {
        GlassConfig cfg = GlassConfig.get();
        if (!cfg.spotifyMirror || !cfg.localPlayer) {
            throw why;
        }
        MusicHub.set("Spotify can't play it here, finding it on YouTube...", true);
        List<Track> r = MusicHub.youtubeSearch((t.title() + " " + t.artist() + " audio").trim());
        String note = MusicHub.youtubePlay(r, 0);
        return note != null ? note : "Playing via YouTube: " + MusicHub.clip(t.title());
    }

    public static void spotifyLogin() {
        GlassConfig cfg = GlassConfig.get();
        String cid = cfg.spotifyClientId.trim();
        if (cid.isEmpty()) {
            MusicHub.set("Paste your Spotify Client ID first", false);
            return;
        }
        MusicHub.set("Waiting for Spotify login in your browser...", true);
        Thread th = new Thread(() -> {
            try {
                String code = null;
                SecureRandom rnd = new SecureRandom();
                String verifier = MusicHub.rand(rnd, 64);
                String state = MusicHub.rand(rnd, 16);
                String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
                String auth = "https://accounts.spotify.com/authorize?client_id=" + MusicHub.enc(cid) + "&response_type=code&redirect_uri=" + MusicHub.enc(SPOTIFY_REDIRECT) + "&code_challenge_method=S256&code_challenge=" + challenge + "&state=" + state + "&scope=" + MusicHub.enc("user-modify-playback-state user-read-playback-state");
                try (ServerSocket ss = new ServerSocket(SPOTIFY_PORT, 1, InetAddress.getByName("127.0.0.1"));){
                    ss.setSoTimeout(180000);
                    long stopAt = System.currentTimeMillis() + 180000L;
                    Launcher.open(auth);
                    int connections = 0;
                    while (code == null && connections < 32 && System.currentTimeMillis() < stopAt) {
                        try (Socket s = ss.accept();){
                            ++connections;
                            s.setSoTimeout(10000);
                            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.US_ASCII));
                            for (int i = 0; i < 64 && code == null; ++i) {
                                String line;
                                try {
                                    line = br.readLine();
                                }
                                catch (SocketTimeoutException e) {
                                    break;
                                }
                                if (line == null) break;
                                code = MusicHub.parseCode(line, state);
                            }
                            String html = "<html><body style='font-family:sans-serif;background:#111;color:#eee;text-align:center;padding-top:80px'>" + (code != null ? "<h2>Spotify connected</h2><p>You can close this tab and go back to Minecraft.</p>" : "<h2>Login failed</h2><p>Go back to Minecraft and try again.</p>") + "</body></html>";
                            byte[] body = html.getBytes(StandardCharsets.UTF_8);
                            s.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: " + body.length + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
                            s.getOutputStream().write(body);
                            s.getOutputStream().flush();
                        }
                    }
                }
                if (code == null) {
                    throw new UserError("Spotify login was cancelled or failed");
                }
                JsonObject t = MusicHub.postForm("https://accounts.spotify.com/api/token", "grant_type=authorization_code&code=" + MusicHub.enc(code) + "&redirect_uri=" + MusicHub.enc(SPOTIFY_REDIRECT) + "&client_id=" + MusicHub.enc(cid) + "&code_verifier=" + MusicHub.enc(verifier));
                if (!t.has("access_token")) {
                    throw new UserError("Spotify: " + MusicHub.clip(MusicHub.str(t, "error_description", MusicHub.str(t, "error", "token error"))));
                }
                MusicHub.storeToken(t);
                MusicHub.set("Spotify connected", false);
            }
            catch (UserError e) {
                MusicHub.set(e.getMessage(), false);
            }
            catch (SocketTimeoutException e) {
                MusicHub.set("Spotify login timed out", false);
            }
            catch (BindException e) {
                MusicHub.set("Port " + SPOTIFY_PORT + " is busy (login already open?)", false);
            }
            catch (Exception e) {
                GlassUiClient.LOGGER.warn("Spotify login failed: {}", (Object)e.toString());
                MusicHub.set("Spotify login failed (" + MusicHub.clip(String.valueOf(e.getMessage())) + ")", false);
            }
        }, "GlassUI-SpotifyLogin");
        th.setDaemon(true);
        th.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void spotifyLogout() {
        GlassConfig cfg = GlassConfig.get();
        cfg.spotifyRefresh = "";
        Class<MusicHub> clazz = MusicHub.class;
        synchronized (MusicHub.class) {
            access = null;
            GlassConfig.save();
            MusicHub.set("Spotify disconnected", false);
            return;
        }
    }

    private static String parseCode(String requestLine, String state) {
        if (requestLine == null) {
            return null;
        }
        String[] p = requestLine.split(" ");
        if (p.length < 2) {
            return null;
        }
        int q = p[1].indexOf(63);
        if (q < 0) {
            return null;
        }
        String code = null;
        String st = null;
        for (String kv : p[1].substring(q + 1).split("&")) {
            int e = kv.indexOf(61);
            if (e < 0) continue;
            String k = kv.substring(0, e);
            String v = URLDecoder.decode(kv.substring(e + 1), StandardCharsets.UTF_8);
            if (k.equals("code")) {
                code = v;
                continue;
            }
            if (!k.equals("state")) continue;
            st = v;
        }
        return state.equals(st) ? code : null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void storeToken(JsonObject t) {
        Class<MusicHub> clazz = MusicHub.class;
        synchronized (MusicHub.class) {
            access = t.get("access_token").getAsString();
            long sec = t.has("expires_in") ? t.get("expires_in").getAsLong() : 3600L;
            accessExp = System.currentTimeMillis() + sec * 1000L;
            if (t.has("refresh_token") && !t.get("refresh_token").isJsonNull()) {
                GlassConfig.get().spotifyRefresh = t.get("refresh_token").getAsString();
                GlassConfig.save();
            }
            return;
        }
    }

    private static synchronized String token() throws Exception {
        if (access != null && System.currentTimeMillis() < accessExp - 30000L) {
            return access;
        }
        GlassConfig cfg = GlassConfig.get();
        if (cfg.spotifyRefresh.isBlank()) {
            throw new UserError("Log in to Spotify first (Music tab)");
        }
        JsonObject t = MusicHub.postForm("https://accounts.spotify.com/api/token", "grant_type=refresh_token&refresh_token=" + MusicHub.enc(cfg.spotifyRefresh) + "&client_id=" + MusicHub.enc(cfg.spotifyClientId.trim()));
        if (!t.has("access_token")) {
            cfg.spotifyRefresh = "";
            GlassConfig.save();
            throw new UserError("Spotify session expired, log in again");
        }
        MusicHub.storeToken(t);
        return access;
    }

    private static HttpResponse<String> api(String method, String path, String json) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("https://api.spotify.com/v1" + path)).timeout(Duration.ofSeconds(10L)).header("Authorization", "Bearer " + MusicHub.token());
        if (json != null) {
            b.header("Content-Type", "application/json");
        }
        b.method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        return HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static HttpResponse<String> apiRetry(String method, String path, String json) throws Exception {
        HttpResponse<String> r = MusicHub.api(method, path, json);
        if (r.statusCode() != 401) return r;
        Class<MusicHub> clazz = MusicHub.class;
        synchronized (MusicHub.class) {
            access = null;
        }
        return MusicHub.api(method, path, json);
    }

    private static List<Track> spotifySearch(String q) throws Exception {
        HttpResponse<String> r = MusicHub.apiRetry("GET", "/search?type=track&limit=10&q=" + MusicHub.enc(q), null);
        if (r.statusCode() != 200) {
            throw new UserError("Spotify search error " + r.statusCode());
        }
        JsonObject o = JsonParser.parseString((String)r.body()).getAsJsonObject();
        ArrayList<Track> out = new ArrayList<Track>();
        if (!o.has("tracks")) {
            return out;
        }
        JsonArray items = o.getAsJsonObject("tracks").getAsJsonArray("items");
        for (JsonElement el : items) {
            JsonObject it;
            String id;
            if (el == null || !el.isJsonObject() || !SP_ID.matcher(id = MusicHub.str(it = el.getAsJsonObject(), "id", "")).matches()) continue;
            String artist = "";
            if (it.has("artists") && it.get("artists").isJsonArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonElement a : it.getAsJsonArray("artists")) {
                    if (!a.isJsonObject()) continue;
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(MusicHub.str(a.getAsJsonObject(), "name", ""));
                }
                artist = sb.toString();
            }
            out.add(new Track(MusicHub.str(it, "name", "?"), artist, "SPOTIFY", id));
        }
        return out;
    }

    private static void spotifyPlay(Track t) throws Exception {
        if (!SP_ID.matcher(t.id()).matches()) {
            throw new UserError("Bad track id");
        }
        String body = "{\"uris\":[\"spotify:track:" + t.id() + "\"]}";
        HttpResponse<String> r = MusicHub.apiRetry("PUT", "/me/player/play", body);
        int sc = r.statusCode();
        if (sc == 404) {
            String dev = MusicHub.firstDevice();
            if (dev == null) {
                MusicHub.set("Opening Spotify...", true);
                Launcher.open("spotify:");
                for (int i = 0; i < 8 && dev == null; ++i) {
                    Thread.sleep(1500L);
                    dev = MusicHub.firstDevice();
                }
            }
            if (dev == null) {
                throw new UserError("No Spotify device found. Start Spotify once, then try again");
            }
            r = MusicHub.apiRetry("PUT", "/me/player/play?device_id=" + MusicHub.enc(dev), body);
            sc = r.statusCode();
        }
        if (sc == 200 || sc == 202 || sc == 204) {
            return;
        }
        if (sc == 403) {
            throw new UserError("Spotify refused: Premium is required to play from here");
        }
        throw new UserError("Spotify play error " + sc);
    }

    private static String firstDevice() throws Exception {
        HttpResponse<String> r = MusicHub.apiRetry("GET", "/me/player/devices", null);
        if (r.statusCode() != 200) {
            return null;
        }
        JsonObject o = JsonParser.parseString((String)r.body()).getAsJsonObject();
        if (!o.has("devices")) {
            return null;
        }
        String first = null;
        for (JsonElement el : o.getAsJsonArray("devices")) {
            JsonObject d = el.getAsJsonObject();
            if (!d.has("id") || d.get("id").isJsonNull()) continue;
            String id = d.get("id").getAsString();
            if (d.has("is_active") && d.get("is_active").getAsBoolean()) {
                return id;
            }
            if (first != null) continue;
            first = id;
        }
        return first;
    }

    public static int volume() {
        return GlassConfig.get().musicVolume;
    }

    public static boolean canVolume() {
        if (LocalPlayer.active()) {
            return true;
        }
        StreamData.NowPlaying np = StreamHud.DATA.song;
        if (np == null) {
            return false;
        }
        if ("Spotify".equals(np.source())) {
            return MusicHub.spotifyLoggedIn();
        }
        return System.getProperty("os.name", "").toLowerCase().contains("linux");
    }

    public static void setVolume(int v) {
        GlassConfig.get().musicVolume = Math.max(0, Math.min(100, v));
        if (VOL_PENDING.compareAndSet(false, true)) {
            VOL_EX.schedule(MusicHub::applyVolume, 120L, TimeUnit.MILLISECONDS);
        }
    }

    public static void commitVolume() {
        if (SAVE_PENDING.compareAndSet(false, true)) {
            SAVE_EX.schedule(() -> {
                SAVE_PENDING.set(false);
                try {
                    GlassConfig.save();
                }
                catch (Exception e) {
                    GlassUiClient.LOGGER.debug("Config save failed: {}", (Object)e.toString());
                }
            }, 150L, TimeUnit.MILLISECONDS);
        }
        if (VOL_PENDING.compareAndSet(false, true)) {
            VOL_EX.execute(MusicHub::applyVolume);
        }
    }

    private static void applyVolume() {
        VOL_PENDING.set(false);
        int v = GlassConfig.get().musicVolume;
        try {
            if (LocalPlayer.active()) {
                LocalPlayer.setVolume(v);
                return;
            }
            StreamData.NowPlaying np = StreamHud.DATA.song;
            if (np == null) {
                return;
            }
            if ("Spotify".equals(np.source())) {
                if (MusicHub.spotifyLoggedIn()) {
                    MusicHub.apiRetry("PUT", "/me/player/volume?volume_percent=" + v, null);
                }
            } else if (System.getProperty("os.name", "").toLowerCase().contains("linux")) {
                String id = np.appId() == null ? "" : np.appId().replaceAll("[^A-Za-z0-9._!\\- ]", "");
                ProcessBuilder pb = id.isEmpty() ? new ProcessBuilder("playerctl", "volume", String.format(Locale.ROOT, "%.2f", (double)v / 100.0)) : new ProcessBuilder("playerctl", "-p", id, "volume", String.format(Locale.ROOT, "%.2f", (double)v / 100.0));
                pb.redirectError(ProcessBuilder.Redirect.DISCARD).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            }
        }
        catch (Exception e) {
            GlassUiClient.LOGGER.debug("Volume change failed: {}", (Object)e.toString());
        }
    }

    private static JsonObject postForm(String url, String form) throws Exception {
        HttpRequest rq = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10L)).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form)).build();
        HttpResponse<String> r = HTTP.send(rq, HttpResponse.BodyHandlers.ofString());
        return JsonParser.parseString((String)r.body()).getAsJsonObject();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String str(JsonObject o, String k) {
        return MusicHub.str(o, k, "");
    }

    private static String str(JsonObject o, String k, String def) {
        JsonElement e = o.get(k);
        return e != null && e.isJsonPrimitive() ? e.getAsString() : def;
    }

    private static String rand(SecureRandom r, int n) {
        String a = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            sb.append(a.charAt(r.nextInt(a.length())));
        }
        return sb.toString();
    }

    private static String unescape(String s) {
        return s.replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">");
    }

    private static String clip(String s) {
        return s.length() > 60 ? s.substring(0, 60) : s;
    }

    static {
        status = "";
        RECENT_RANDOM = new ArrayDeque<String>();
        RND = new Random();
        YT_VID = Pattern.compile("\"videoRenderer\":\\{\"videoId\":\"([A-Za-z0-9_-]{11})\"");
        YT_TITLE = Pattern.compile("\"title\":\\{\"runs\":\\[\\{\"text\":\"((?:[^\"\\\\]|\\\\.)*)\"");
        YT_OWNER = Pattern.compile("\"ownerText\":\\{\"runs\":\\[\\{\"text\":\"((?:[^\"\\\\]|\\\\.)*)\"");
        VOL_EX = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "GlassUI-Volume");
            t.setDaemon(true);
            return t;
        });
        VOL_PENDING = new AtomicBoolean();
        SETUP_EX = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "GlassUI-PlayerSetup");
            t.setDaemon(true);
            return t;
        });
        SAVE_EX = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "GlassUI-ConfigSave");
            t.setDaemon(true);
            return t;
        });
        SAVE_PENDING = new AtomicBoolean();
    }

    public record Track(String title, String artist, String provider, String id) {
    }

    static final class UserError
    extends Exception {
        UserError(String m) {
            super(m);
        }
    }
}

