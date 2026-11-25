/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassConfig;
import com.glassui.hud.MediaDetector;
import com.glassui.hud.StreamData;
import com.glassui.hud.TikTokClient;
import com.glassui.hud.YouTubeClient;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class StreamHud {
    public static final StreamData DATA = new StreamData();
    private static ScheduledExecutorService ex;
    private static YouTubeClient yt;
    private static TikTokClient tt;
    private static MediaDetector media;

    private StreamHud() {
    }

    public static synchronized void restart() {
        StreamHud.start(true);
    }

    public static synchronized void startup() {
        StreamHud.start(GlassConfig.get().autoConnect);
    }

    private static void start(boolean connect) {
        StreamHud.stop();
        GlassConfig cfg = GlassConfig.get();
        DATA.reset();
        if (connect && cfg.isLinkReady()) {
            if (cfg.isTikTok()) {
                StreamHud.DATA.linkState = "Connecting...";
                tt = new TikTokClient(DATA, cfg.tiktokName(), cfg.tiktokMode, cfg.tiktokKey.trim());
                tt.start();
            } else {
                ex = Executors.newScheduledThreadPool(2, r -> {
                    Thread t = new Thread(r, "GlassUI-YT");
                    t.setDaemon(true);
                    return t;
                });
                yt = new YouTubeClient(DATA, ex, cfg.apiKey.trim(), cfg.videoId(), cfg.statsPollSec, cfg.chatPollSec);
                yt.start();
            }
        }
        media = new MediaDetector();
        media.start(DATA);
    }

    public static boolean isConnecting() {
        return tt != null || yt != null;
    }

    public static synchronized void disconnect() {
        StreamHud.stop();
        DATA.reset();
        StreamHud.DATA.linkState = "Disconnected";
        media = new MediaDetector();
        media.start(DATA);
    }

    public static synchronized void stop() {
        if (yt != null) {
            yt.stop();
        }
        if (tt != null) {
            tt.stop();
        }
        if (ex != null) {
            ex.shutdownNow();
        }
        if (media != null) {
            media.stop();
        }
        if (ex != null) {
            try {
                ex.awaitTermination(2L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        yt = null;
        tt = null;
        ex = null;
        media = null;
    }
}

