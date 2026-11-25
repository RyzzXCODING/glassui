/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassUiClient;
import com.glassui.hud.StreamData;
import com.glassui.hud.TikTokEvents;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

final class TikTokClient {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private final StreamData d;
    private final String user;
    private final String mode;
    private final String key;
    private volatile boolean stopped;
    private volatile WebSocket ws;
    private Thread thread;
    private volatile boolean gotEvent;

    TikTokClient(StreamData d, String user, String mode, String key) {
        this.d = d;
        this.user = user;
        this.mode = mode;
        this.key = key;
    }

    void start() {
        this.thread = new Thread(this::loop, "GlassUI-TikTok");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    void stop() {
        this.stopped = true;
        WebSocket w = this.ws;
        if (w != null) {
            w.abort();
        }
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }

    private void loop() {
        int wait = 5;
        while (!this.stopped) {
            this.gotEvent = false;
            try {
                this.d.linkState = "Connecting...";
                this.session();
            }
            catch (InterruptedException ie) {
                return;
            }
            catch (Exception e) {
                this.d.status = TikTokClient.shorten(e.getMessage() == null ? e.toString() : e.getMessage());
                GlassUiClient.LOGGER.warn("TikTok connect failed: {}", (Object)e.getClass().getSimpleName());
            }
            if (this.stopped) {
                return;
            }
            this.d.live = false;
            this.d.linkState = "Waiting for live (@" + this.user + ")";
            wait = this.gotEvent ? 5 : Math.min(30, wait + 5);
            try {
                Thread.sleep((long)wait * 1000L);
            }
            catch (InterruptedException ie) {
                return;
            }
        }
    }

    private void session() throws Exception {
        URI uri = "TIKFINITY".equals(this.mode) ? URI.create("ws://localhost:21213/") : URI.create("wss://ws.eulerstream.com?uniqueId=" + TikTokClient.enc(this.user) + "&apiKey=" + TikTokClient.enc(this.key));
        final CountDownLatch closed = new CountDownLatch(1);
        final StringBuilder buf = new StringBuilder();
        WebSocket.Listener l = new WebSocket.Listener(){
            @Override
            public void onOpen(WebSocket w) {
                TikTokClient.this.d.status = "";
                TikTokClient.this.d.linkState = "TIKFINITY".equals(TikTokClient.this.mode) ? "Connected to TikFinity" : "Connected - waiting for @" + TikTokClient.this.user;
                w.request(1L);
            }

            @Override
            public CompletionStage<?> onText(WebSocket w, CharSequence data, boolean last) {
                buf.append(data);
                if (last) {
                    String s = buf.toString();
                    buf.setLength(0);
                    try {
                        if (TikTokEvents.handle(s, TikTokClient.this.d)) {
                            TikTokClient.this.gotEvent = true;
                        } else {
                            String clip = s.length() > 300 ? s.substring(0, 300) : s;
                            String low = clip.toLowerCase();
                            if (low.contains("social") || low.contains("follow")) {
                                GlassUiClient.LOGGER.info("TikTok unhandled social/follow event ({} chars)", (Object)Integer.valueOf(clip.length()));
                            } else {
                                GlassUiClient.LOGGER.debug("TikTok unhandled ({} chars)", (Object)Integer.valueOf(clip.length()));
                            }
                        }
                    }
                    catch (Exception e) {
                        GlassUiClient.LOGGER.debug("TikTok parse error: {}", (Object)e.getClass().getSimpleName());
                    }
                }
                w.request(1L);
                return null;
            }

            @Override
            public CompletionStage<?> onClose(WebSocket w, int code, String reason) {
                if (reason != null && !reason.isBlank()) {
                    TikTokClient.this.d.status = TikTokClient.shorten(reason);
                }
                closed.countDown();
                return null;
            }

            @Override
            public void onError(WebSocket w, Throwable t) {
                TikTokClient.this.d.status = TikTokClient.shorten(String.valueOf(t.getMessage()));
                closed.countDown();
            }
        };
        this.ws = CLIENT.newWebSocketBuilder().connectTimeout(Duration.ofSeconds(10L)).buildAsync(uri, l).get(15L, TimeUnit.SECONDS);
        if (!closed.await(60L, TimeUnit.SECONDS)) {
            WebSocket w = this.ws;
            if (w != null) {
                w.abort();
            }
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String shorten(String s) {
        if (s == null) {
            return "Error";
        }
        s = s.replace('\n', ' ').replaceAll("(?i)(https?|wss?)://\\S+", "[url]");
        s = s.replaceAll("(?i)(api[_-]?key|key|token|access[_-]?token)\\s*[:=]\\s*[\"']?[^&\\s\"',}]+", "$1=[redacted]");
        return s.length() > 48 ? s.substring(0, 48) : s;
    }
}

