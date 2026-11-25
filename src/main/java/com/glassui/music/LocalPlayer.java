/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.music;

import com.glassui.GlassConfig;
import com.glassui.GlassUiClient;
import com.glassui.hud.MediaControl;
import com.glassui.hud.StreamData;
import com.glassui.hud.StreamHud;
import com.glassui.music.MusicHub;
import com.glassui.music.SongRequests;
import com.glassui.music.YouTubeStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import net.fabricmc.loader.api.FabricLoader;

public final class LocalPlayer {
    public static final String APP_ID = "glassui-local";
    private static final boolean WIN = System.getProperty("os.name", "").toLowerCase().contains("win");
    private static final AudioFormat FMT = new AudioFormat(44100.0f, 16, 2, true, false);
    private static final ExecutorService EX = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "GlassUI-Player");
        t.setDaemon(true);
        return t;
    });
    private static final ExecutorService READ_EX = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "GlassUI-yt-dlp-io");
        t.setDaemon(true);
        return t;
    });
    private static volatile boolean probed;
    private static volatile String ffmpeg;
    private static volatile String ytdlp;
    private static volatile Session cur;
    private static List<MusicHub.Track> queue;
    private static int qi;
    private static final String FFMPEG_HINT;
    private static volatile boolean settingUp;
    private static volatile String setupStatus;
    private static final HttpClient DL;
    private static final Object YT_LOCK;

    private LocalPlayer() {
    }

    public static void probeAsync() {
        if (!probed) {
            EX.execute(LocalPlayer::probe);
        }
    }

    public static synchronized void probe() {
        if (probed) {
            return;
        }
        GlassConfig cfg = GlassConfig.get();
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("glassui");
        String exe = WIN ? ".exe" : "";
        ArrayList<String> c = new ArrayList<String>();
        if (!cfg.ffmpegPath.isBlank()) {
            c.add(cfg.ffmpegPath.trim());
        }
        c.add(dir.resolve("ffmpeg" + exe).toString());
        c.add(dir.resolve("ffmpeg").resolve("bin").resolve("ffmpeg" + exe).toString());
        c.add("ffmpeg");
        if (WIN) {
            String lad = System.getenv("LOCALAPPDATA");
            String string = System.getenv("USERPROFILE");
            String string2 = System.getenv("ProgramData");
            String pf = System.getenv("ProgramFiles");
            if (lad != null) {
                c.add((String)lad + "\\Microsoft\\WinGet\\Links\\ffmpeg.exe");
            }
            if (string != null) {
                c.add(string + "\\scoop\\shims\\ffmpeg.exe");
            }
            if (string2 != null) {
                c.add(string2 + "\\chocolatey\\bin\\ffmpeg.exe");
            }
            if (pf != null) {
                c.add(pf + "\\ffmpeg\\bin\\ffmpeg.exe");
            }
        } else {
            c.add("/usr/local/bin/ffmpeg");
            c.add("/opt/homebrew/bin/ffmpeg");
            c.add("/usr/bin/ffmpeg");
        }
        for (String string : c) {
            if (!LocalPlayer.works(string)) continue;
            ffmpeg = string;
            break;
        }
        ArrayList<String> y = new ArrayList<String>();
        if (!cfg.ytdlpPath.isBlank()) {
            y.add(cfg.ytdlpPath.trim());
        }
        y.add(dir.resolve("yt-dlp" + exe).toString());
        y.add("yt-dlp");
        if (WIN) {
            String string = System.getenv("LOCALAPPDATA");
            String string3 = System.getenv("USERPROFILE");
            if (string != null) {
                y.add(string + "\\Microsoft\\WinGet\\Links\\yt-dlp.exe");
            }
            if (string3 != null) {
                y.add(string3 + "\\scoop\\shims\\yt-dlp.exe");
            }
        } else {
            y.add("/usr/local/bin/yt-dlp");
            y.add("/opt/homebrew/bin/yt-dlp");
            y.add("/usr/bin/yt-dlp");
        }
        for (String string : y) {
            if (!LocalPlayer.works(string, "--version")) continue;
            ytdlp = string;
            break;
        }
        probed = true;
        GlassUiClient.LOGGER.info("Background player: ffmpeg={} yt-dlp(fallback)={}", (Object)ffmpeg, (Object)ytdlp);
    }

    private static boolean works(String cmd) {
        return LocalPlayer.works(cmd, "-version");
    }

    private static boolean works(String cmd, String flag) {
        try {
            if (cmd.indexOf('/') >= 0 || cmd.indexOf('\\') >= 0) {
                Path exe = Path.of(cmd);
                if (!Files.isExecutable(exe)) {
                    return false;
                }
            }
            Process p = new ProcessBuilder(cmd, flag).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            if (!p.waitFor(3L, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static boolean ready() {
        return GlassConfig.get().localPlayer && ffmpeg != null;
    }

    public static String statusLine() {
        if (!GlassConfig.get().localPlayer) {
            return "Background player is off.";
        }
        if (settingUp && !setupStatus.isEmpty()) {
            return setupStatus;
        }
        if (!probed) {
            return "Checking for ffmpeg...";
        }
        if (ffmpeg == null) {
            return "ffmpeg not found (auto-setup on first play). " + FFMPEG_HINT;
        }
        return "Background player ready (ffmpeg).";
    }

    public static boolean settingUp() {
        return settingUp;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static synchronized boolean setup() {
        LocalPlayer.probe();
        GlassConfig cfg = GlassConfig.get();
        if (!cfg.localPlayer) {
            return false;
        }
        if (LocalPlayer.ready()) {
            return true;
        }
        if (!cfg.autoSetupPlayer || !WIN) {
            return false;
        }
        settingUp = true;
        try {
            Path dir = FabricLoader.getInstance().getConfigDir().resolve("glassui");
            Files.createDirectories(dir, new FileAttribute[0]);
            setupStatus = "Downloading ffmpeg...";
            MusicHub.status(setupStatus);
            Path zip = dir.resolve("ffmpeg-download.zip");
            LocalPlayer.download("https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-lgpl.zip", zip);
            setupStatus = "Unpacking ffmpeg...";
            MusicHub.status(setupStatus);
            Path out = dir.resolve("ffmpeg.exe");
            boolean found = false;
            try (ZipInputStream z = new ZipInputStream(Files.newInputStream(zip, new OpenOption[0]));){
                ZipEntry e;
                while ((e = z.getNextEntry()) != null) {
                    if (e.isDirectory() || !e.getName().endsWith("/bin/ffmpeg.exe")) continue;
                    Files.copy(z, out, StandardCopyOption.REPLACE_EXISTING);
                    found = true;
                    break;
                }
            }
            finally {
                Files.deleteIfExists(zip);
            }
            if (!found) {
                throw new IOException("ffmpeg.exe not found in archive");
            }
            if (LocalPlayer.works(out.toString())) {
                ffmpeg = out.toString();
            } else {
                GlassUiClient.LOGGER.warn("Downloaded ffmpeg does not run: {}", (Object)out);
            }
        }
        catch (Exception e) {
            GlassUiClient.LOGGER.warn("Player setup failed: {}", (Object)e.toString());
        }
        finally {
            settingUp = false;
            setupStatus = "";
        }
        return LocalPlayer.ready();
    }

    public static void setupAsync() {
        EX.execute(() -> {
            LocalPlayer.setup();
            MusicHub.status(LocalPlayer.statusLine());
        });
    }

    private static void download(String url, Path target) throws IOException, InterruptedException {
        Path tmp = target.resolveSibling(String.valueOf(target.getFileName()) + ".part");
        HttpResponse<Path> r = DL.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(10L)).header("User-Agent", "GlassUI").GET().build(), HttpResponse.BodyHandlers.ofFile(tmp));
        if (r.statusCode() != 200) {
            Files.deleteIfExists(tmp);
            throw new IOException("HTTP " + r.statusCode() + " for " + url);
        }
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String ensureYtdlp() {
        Object object = YT_LOCK;
        synchronized (object) {
            if (ytdlp != null) {
                return ytdlp;
            }
            if (!WIN || !GlassConfig.get().autoSetupPlayer) {
                return null;
            }
            try {
                Path dir = FabricLoader.getInstance().getConfigDir().resolve("glassui");
                Files.createDirectories(dir, new FileAttribute[0]);
                MusicHub.status("Downloading yt-dlp...");
                Path target = dir.resolve("yt-dlp.exe");
                LocalPlayer.download("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe", target);
                if (LocalPlayer.works(target.toString(), "--version")) {
                    ytdlp = target.toString();
                }
            }
            catch (Exception e) {
                GlassUiClient.LOGGER.warn("yt-dlp download failed: {}", (Object)e.toString());
            }
            return ytdlp;
        }
    }

    private static YouTubeStream.Stream resolveViaYtdlp(String videoId, String exe) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(exe, "--no-playlist", "--no-warnings", "-f", "bestaudio/best", "-j", "https://www.youtube.com/watch?v=" + videoId).start();
        CompletableFuture<String> err = CompletableFuture.supplyAsync(() -> {
            try {
                return new String(p.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            catch (IOException e) {
                return "";
            }
        }, READ_EX);
        CompletableFuture<String> out = CompletableFuture.supplyAsync(() -> {
            try {
                return new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            catch (IOException e) {
                return "";
            }
        }, READ_EX);
        try {
            if (!p.waitFor(90L, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new IOException("yt-dlp timed out");
            }
            String o = LocalPlayer.readFuture(out).trim();
            if (p.exitValue() != 0 || o.isEmpty()) {
                String e = LocalPlayer.readFuture(err).trim();
                throw new IOException("yt-dlp: " + (String)(e.isEmpty() ? "exit " + p.exitValue() : e.substring(Math.max(0, e.length() - 200))));
            }
            JsonObject j = JsonParser.parseString((String)o.lines().findFirst().orElse("{}")).getAsJsonObject();
            if (!j.has("url")) {
                throw new IOException("yt-dlp: no url");
            }
            String ua = "Mozilla/5.0";
            LinkedHashMap<String, String> h = new LinkedHashMap<String, String>();
            if (j.has("http_headers") && j.get("http_headers").isJsonObject()) {
                for (Map.Entry en : j.getAsJsonObject("http_headers").entrySet()) {
                    if (!((JsonElement)en.getValue()).isJsonPrimitive()) continue;
                    if (((String)en.getKey()).equalsIgnoreCase("User-Agent")) {
                        ua = ((JsonElement)en.getValue()).getAsString();
                        continue;
                    }
                    h.put((String)en.getKey(), ((JsonElement)en.getValue()).getAsString());
                }
            }
            long dur = j.has("duration") && j.get("duration").isJsonPrimitive() ? (long)(j.get("duration").getAsDouble() * 1000.0) : 0L;
            return new YouTubeStream.Stream(j.get("url").getAsString(), ua, "", 0L, h, dur);
        }
        finally {
            out.cancel(true);
            err.cancel(true);
            if (p.isAlive()) {
                p.destroyForcibly();
            }
        }
    }

    private static String readFuture(CompletableFuture<String> f) throws IOException, InterruptedException {
        try {
            return f.get(5L, TimeUnit.SECONDS);
        }
        catch (ExecutionException e) {
            return "";
        }
        catch (TimeoutException e) {
            throw new IOException("yt-dlp output read timed out");
        }
    }

    public static boolean active() {
        Session s = cur;
        return s != null && s.alive;
    }

    static synchronized void play(List<MusicHub.Track> q, int i) throws MusicHub.UserError {
        LocalPlayer.probe();
        if (!LocalPlayer.ready()) {
            throw new MusicHub.UserError(LocalPlayer.statusLine());
        }
        queue = List.copyOf(q);
        LocalPlayer.startAt(Math.max(0, Math.min(i, queue.size() - 1)));
    }

    public static double positionSec() {
        Session s = cur;
        if (s == null) {
            return 0.0;
        }
        SourceDataLine l = s.line;
        return l == null ? 0.0 : (double)l.getMicrosecondPosition() / 1000000.0;
    }

    private static void startAt(int i) throws MusicHub.UserError {
        Session s;
        MusicHub.Track t = queue.get(i);
        if (!t.id().matches("[A-Za-z0-9_-]{11}")) {
            throw new MusicHub.UserError("Bad video id");
        }
        LocalPlayer.stopSession();
        qi = i;
        cur = s = new Session(t, ffmpeg);
        LocalPlayer.publish(s);
        Thread th = new Thread(s::run, "GlassUI-Audio");
        th.setDaemon(true);
        th.start();
    }

    private static void stopSession() {
        Session s = cur;
        cur = null;
        if (s != null) {
            s.cancel();
        }
    }

    public static synchronized void stop() {
        LocalPlayer.stopSession();
        LocalPlayer.clearSong();
    }

    private static synchronized void onEnd(Session s, boolean ok, String error) {
        if (cur != s) {
            return;
        }
        cur = null;
        if (SongRequests.advance()) {
            if (!ok) {
                GlassUiClient.LOGGER.warn("Playback failed, moving on to next request: {}", (Object)error);
            }
            return;
        }
        if (ok && GlassConfig.get().shuffle) {
            MusicHub.playRandom();
            return;
        }
        if (ok && qi + 1 < queue.size()) {
            try {
                LocalPlayer.startAt(qi + 1);
            }
            catch (MusicHub.UserError e) {
                LocalPlayer.clearSong();
                MusicHub.status(e.getMessage());
            }
            return;
        }
        LocalPlayer.clearSong();
        if (!ok) {
            GlassUiClient.LOGGER.warn("Playback failed: {}", (Object)error);
            MusicHub.status("Playback failed: " + (error == null ? "unknown" : (error.length() > 60 ? error.substring(0, 60) : error)));
        }
    }

    private static void publish(Session s) {
        StreamHud.DATA.song = new StreamData.NowPlaying(s.track.title(), s.track.artist(), "YouTube", !s.paused, APP_ID);
    }

    private static void clearSong() {
        StreamData.NowPlaying np = StreamHud.DATA.song;
        if (np != null && APP_ID.equals(np.appId())) {
            StreamHud.DATA.song = null;
        }
    }

    public static void command(MediaControl.Cmd c) {
        EX.execute(() -> {
            Class<LocalPlayer> clazz = LocalPlayer.class;
            synchronized (LocalPlayer.class) {
                Session s = cur;
                if (s == null || !s.alive) {
                    // ** MonitorExit[var1_1] (shouldn't be in output)
                    return;
                }
                try {
                    switch (c) {
                        case TOGGLE: {
                            s.paused = !s.paused;
                            LocalPlayer.publish(s);
                            break;
                        }
                        case NEXT: {
                            if (SongRequests.advance()) break;
                            if (GlassConfig.get().shuffle) {
                                MusicHub.playRandom();
                                break;
                            }
                            if (qi + 1 >= queue.size()) break;
                            LocalPlayer.startAt(qi + 1);
                            break;
                        }
                        case PREV: {
                            LocalPlayer.startAt(Math.max(0, qi - 1));
                        }
                    }
                }
                catch (MusicHub.UserError e) {
                    MusicHub.status(e.getMessage());
                }
                return;
            }
        });
    }

    public static void setVolume(int v) {
        GlassConfig.get().musicVolume = Math.max(0, Math.min(100, v));
    }

    static {
        queue = List.of();
        FFMPEG_HINT = WIN ? "Put ffmpeg.exe in config/glassui/ or run: winget install ffmpeg" : "Install it with your package manager (apt/dnf/pacman install ffmpeg, brew install ffmpeg).";
        setupStatus = "";
        DL = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).connectTimeout(Duration.ofSeconds(15L)).build();
        YT_LOCK = new Object();
    }

    private static final class Session {
        final MusicHub.Track track;
        final String ffmpegCmd;
        volatile boolean paused;
        volatile boolean cancelled;
        volatile boolean alive = true;
        private volatile Process proc;
        private volatile SourceDataLine line;
        private volatile long decoded;
        private static final double BYTES_PER_SEC = 176400.0;

        Session(MusicHub.Track t, String ffmpegCmd) {
            this.track = t;
            this.ffmpegCmd = ffmpegCmd;
        }

        void cancel() {
            SourceDataLine l;
            this.cancelled = true;
            Process p = this.proc;
            if (p != null) {
                p.destroyForcibly();
            }
            if ((l = this.line) != null) {
                try {
                    l.stop();
                    l.flush();
                    l.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void run() {
            boolean ok;
            Outcome o = null;
            String error = null;
            try {
                try {
                    o = this.playWithResume(YouTubeStream.resolve(this.track.id()), () -> YouTubeStream.resolve(this.track.id()));
                }
                catch (IOException e) {
                    error = e.getMessage();
                }
                if (!(this.cancelled || o != null && (o.ok() || o.started()))) {
                    String y;
                    if (o != null) {
                        error = o.error();
                    }
                    GlassUiClient.LOGGER.info("Own resolver failed ({}), trying yt-dlp for the link", error);
                    String string = y = ytdlp != null ? ytdlp : LocalPlayer.ensureYtdlp();
                    if (y != null) {
                        try {
                            o = this.playWithResume(LocalPlayer.resolveViaYtdlp(this.track.id(), y), () -> LocalPlayer.resolveViaYtdlp(this.track.id(), y));
                        }
                        catch (IOException e) {
                            error = e.getMessage();
                            o = null;
                        }
                    } else {
                        error = (String)(error == null ? "" : (String)error + " | ") + "yt-dlp not available";
                    }
                }
            }
            catch (IllegalArgumentException | LineUnavailableException e) {
                o = new Outcome(false, false, "no audio output device");
            }
            catch (InterruptedException e) {
                return;
            }
            catch (Exception e) {
                block33: {
                    if (!this.cancelled) break block33;
                    return;
                }
                o = new Outcome(false, false, e.getMessage() == null ? e.toString() : e.getMessage());
            }
            finally {
                SourceDataLine l;
                this.alive = false;
                Process p = this.proc;
                if (p != null) {
                    p.destroyForcibly();
                }
                if ((l = this.line) != null) {
                    try {
                        l.close();
                    }
                    catch (Exception exception) {}
                }
            }
            if (this.cancelled) {
                return;
            }
            boolean bl = ok = o != null && o.ok();
            LocalPlayer.onEnd(this, ok, ok ? null : (o != null && o.error() != null ? o.error() : error));
        }

        private Outcome playWithResume(YouTubeStream.Stream st, Callable<YouTubeStream.Stream> again) throws Exception {
            double want;
            double got;
            Outcome o = this.playStream(st, 0.0);
            for (int tries = 0; tries < 4 && !this.cancelled && o.ok() && st.durationMs() > 0L && !((got = (double)this.decoded / 176400.0) >= (want = (double)st.durationMs() / 1000.0) - 5.0); ++tries) {
                YouTubeStream.Stream next;
                GlassUiClient.LOGGER.info("Stream ended early ({}s of {}s), resuming", (Object)Math.round(got), (Object)Math.round(want));
                try {
                    next = again.call();
                }
                catch (IOException e) {
                    break;
                }
                if (this.cancelled) break;
                st = next;
                Outcome r = this.playStream(st, got);
                if (!r.ok()) {
                    o = new Outcome(true, true, null);
                    break;
                }
                o = r;
            }
            return o;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private Outcome playStream(YouTubeStream.Stream st, double startSec) throws Exception {
            Process p;
            if (this.cancelled) {
                return new Outcome(false, true, "cancelled");
            }
            StringBuilder hdr = new StringBuilder();
            st.headers().forEach((k, v) -> {
                if (!k.equalsIgnoreCase("Range")) {
                    hdr.append((String)k).append(": ").append((String)v).append("\r\n");
                }
            });
            ArrayList<String> cmd = new ArrayList<String>(List.of(this.ffmpegCmd, "-hide_banner", "-loglevel", "error", "-nostdin", "-user_agent", st.userAgent()));
            if (hdr.length() > 0) {
                cmd.add("-headers");
                cmd.add(hdr.toString());
            }
            cmd.addAll(List.of("-reconnect", "1", "-reconnect_streamed", "1", "-reconnect_at_eof", "1", "-reconnect_delay_max", "5"));
            if (startSec > 1.0) {
                cmd.add("-ss");
                cmd.add(String.format(Locale.ROOT, "%.2f", startSec));
            }
            cmd.addAll(List.of("-i", st.url(), "-vn", "-ac", "2", "-ar", "44100", "-f", "s16le", "pipe:1"));
            this.proc = p = new ProcessBuilder(cmd).start();
            if (this.cancelled) {
                p.destroyForcibly();
                return new Outcome(false, true, "cancelled");
            }
            ArrayDeque tail = new ArrayDeque();
            Thread er = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8));){
                    String l;
                    while ((l = r.readLine()) != null) {
                        Deque deque = tail;
                        synchronized (deque) {
                            if (tail.size() >= 3) {
                                tail.pollFirst();
                            }
                            tail.addLast(l);
                        }
                    }
                    return;
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }, "GlassUI-ffmpeg-err");
            er.setDaemon(true);
            er.start();
            SourceDataLine sl = this.line;
            if (sl == null) {
                sl = AudioSystem.getSourceDataLine(FMT);
                sl.open(FMT, 88200);
                this.line = sl;
            }
            if (this.cancelled) {
                return new Outcome(false, true, "cancelled");
            }
            sl.start();
            byte[] buf = new byte[4096];
            long total = 0L;
            boolean lineStopped = false;
            try (InputStream in = p.getInputStream();){
                while (!this.cancelled) {
                    int n = in.readNBytes(buf, 0, buf.length);
                    if (n <= 0) {
                        break;
                    }
                    if ((n &= 0xFFFFFFFC) == 0) {
                        break;
                    }
                    total += (long)n;
                    this.decoded += (long)n;
                    while (this.paused && !this.cancelled) {
                        if (!lineStopped) {
                            sl.stop();
                            lineStopped = true;
                        }
                        Thread.sleep(40L);
                    }
                    if (this.cancelled) {
                        break;
                    }
                    if (lineStopped) {
                        sl.start();
                        lineStopped = false;
                    }
                    Session.applyVolume(buf, n);
                    sl.write(buf, 0, n);
                }
            }
            if (this.cancelled) {
                return new Outcome(false, true, "cancelled");
            }
            sl.drain();
            if (!p.waitFor(5L, TimeUnit.SECONDS)) {
                p.destroyForcibly();
            }
            boolean ok = total > 0L;
            String error = null;
            if (!ok) {
                er.join(500L);
                ArrayDeque arrayDeque = tail;
                synchronized (arrayDeque) {
                    error = String.join((CharSequence)" | ", tail);
                }
                if (error.isEmpty()) {
                    error = "no audio decoded";
                }
            }
            return new Outcome(ok, total > 0L, error);
        }

        private static void applyVolume(byte[] b, int n) {
            int v = GlassConfig.get().musicVolume;
            if (v >= 100) {
                return;
            }
            float g = (float)v / 100.0f * ((float)v / 100.0f);
            int i = 0;
            while (i + 1 < n) {
                int s = b[i] & 0xFF | b[i + 1] << 8;
                s = Math.round((float)s * g);
                b[i] = (byte)s;
                b[i + 1] = (byte)(s >> 8);
                i += 2;
            }
        }

        private record Outcome(boolean ok, boolean started, String error) {
        }
    }
}

