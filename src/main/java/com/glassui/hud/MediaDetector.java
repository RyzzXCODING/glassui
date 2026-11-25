/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassConfig;
import com.glassui.GlassUiClient;
import com.glassui.hud.StreamData;
import com.glassui.music.LocalPlayer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public final class MediaDetector {
    private volatile boolean stopped;
    private Process proc;
    private Thread thread;
    private static final String PS = "[Console]::OutputEncoding=[System.Text.Encoding]::UTF8\ntry{(Get-Process -Id $PID).PriorityClass='BelowNormal'}catch{}\nAdd-Type -AssemblyName System.Runtime.WindowsRuntime\n$asTask=([System.WindowsRuntimeSystemExtensions].GetMethods()|Where-Object{$_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'})[0]\nfunction Await($op,$type){$t=$asTask.MakeGenericMethod($type).Invoke($null,@($op));$t.Wait(-1)|Out-Null;$t.Result}\n[void][Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]\n$mT=[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]\n$pT=[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties]\n$mgr=Await ($mT::RequestAsync()) $mT\nwhile($true){\n  try{\n    foreach($s in $mgr.GetSessions()){\n      $p=Await ($s.TryGetMediaPropertiesAsync()) $pT\n      $st=$s.GetPlaybackInfo().PlaybackStatus\n      [Console]::Out.WriteLine(\"S`t$($s.SourceAppUserModelId)`t$st`t$($p.Title)`t$($p.Artist)`t$($p.AlbumTitle)\")\n    }\n  }catch{}\n  [Console]::Out.WriteLine(\"END\"); [Console]::Out.Flush()\n  Start-Sleep -Milliseconds 2000\n}\n";

    public void start(StreamData d) {
        Thread t = new Thread(() -> this.run(d), "GlassUI-Media");
        t.setDaemon(true);
        this.thread = t;
        t.start();
    }

    public void stop() {
        this.stopped = true;
        Thread t = this.thread;
        if (t != null) {
            t.interrupt();
        }
        if (this.proc != null) {
            this.proc.destroyForcibly();
        }
    }

    private void run(StreamData d) {
        String os = System.getProperty("os.name", "").toLowerCase();
        while (!this.stopped) {
            block7: {
                try {
                    if (os.contains("win")) {
                        this.windows(d);
                        break block7;
                    }
                    if (os.contains("linux")) {
                        this.linux(d);
                        break block7;
                    }
                    return;
                }
                catch (Exception e) {
                    GlassUiClient.LOGGER.debug("Media detector: {}", (Object)e.toString());
                }
            }
            try {
                Thread.sleep(5000L);
            }
            catch (InterruptedException ie) {
                return;
            }
        }
    }

    private void windows(StreamData d) throws Exception {
        String enc = Base64.getEncoder().encodeToString(PS.getBytes(StandardCharsets.UTF_16LE));
        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-EncodedCommand", enc);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        this.proc = pb.start();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(this.proc.getInputStream(), StandardCharsets.UTF_8));){
            String line;
            ArrayList<String[]> batch = new ArrayList<String[]>();
            while (!this.stopped && (line = r.readLine()) != null) {
                if ((line = line.replace("\ufeff", "")).equals("END")) {
                    MediaDetector.publish(d, batch);
                    batch.clear();
                    continue;
                }
                if (!line.startsWith("S\t")) continue;
                batch.add(line.split("\t", -1));
            }
        }
    }

    private void linux(StreamData d) throws Exception {
        while (!this.stopped) {
            Process p = new ProcessBuilder("playerctl", "-a", "metadata", "--format", "{{playerName}}\t{{status}}\t{{title}}\t{{artist}}\t{{album}}").redirectError(ProcessBuilder.Redirect.DISCARD).start();
            this.proc = p;
            ArrayList<String[]> batch = new ArrayList<String[]>();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));){
                String line;
                while (!this.stopped && (line = r.readLine()) != null) {
                    String[] a = line.split("\t", -1);
                    if (a.length < 5) continue;
                    batch.add(new String[]{"S", a[0], a[1], a[2], a[3], a[4]});
                }
            }
            MediaDetector.publish(d, batch);
            try {
                Thread.sleep(2000L);
            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static boolean isBrowser(String id) {
        for (String b : new String[]{"chrome", "msedge", "edge", "firefox", "brave", "opera", "vivaldi", "zen", "chromium"}) {
            if (!id.contains(b)) continue;
            return true;
        }
        return false;
    }

    private static void publish(StreamData d, List<String[]> batch) {
        if (LocalPlayer.active()) {
            return;
        }
        String want = GlassConfig.get().musicApp;
        boolean auto = want == null || want.equals("AUTO");
        StreamData.NowPlaying best = null;
        int bestRank = -1;
        for (String[] s : batch) {
            int rank;
            String src;
            if (s.length < 6) continue;
            String rawId = s[1];
            String id = rawId.toLowerCase();
            String title = s[3];
            String artist = s[4];
            String album = s[5];
            if (title.isBlank()) continue;
            boolean playing = s[2].equalsIgnoreCase("Playing");
            if (id.contains("spotify")) {
                src = "Spotify";
                rank = 3;
            } else if (id.contains("youtubemusic") || id.contains("youtube-music") || id.contains("youtube_music") || id.contains("pear")) {
                src = "YT Music";
                rank = 3;
            } else if (MediaDetector.isBrowser(id)) {
                if (!auto && (want.equals("YT Music") || want.equals("YouTube"))) {
                    src = want;
                    rank = 2;
                } else if (!album.isBlank()) {
                    src = "YT Music";
                    rank = 2;
                } else {
                    src = "YouTube";
                    rank = 1;
                }
            } else {
                src = id.replaceAll("!.*", "").replace(".exe", "");
                rank = 0;
            }
            if (!auto && !src.equals(want)) continue;
            if (playing) {
                rank += 10;
            }
            if (rank <= bestRank) continue;
            bestRank = rank;
            best = new StreamData.NowPlaying(title, artist, src, playing, rawId);
        }
        if (!Objects.equals(d.song, best)) {
            d.song = best;
        }
    }
}

