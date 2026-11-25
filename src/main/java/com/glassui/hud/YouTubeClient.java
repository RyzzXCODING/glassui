/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassUiClient;
import com.glassui.hud.StreamData;
import com.glassui.music.SongRequests;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class YouTubeClient {
    private static final String BASE = "https://www.googleapis.com/youtube/v3/";
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
    private final StreamData d;
    private final ScheduledExecutorService ex;
    private final String key;
    private final String vid;
    private final int statsSec;
    private final int chatSec;
    private volatile boolean stopped;
    private volatile boolean chatStarted;
    private volatile boolean firstChat = true;
    private volatile String chatId;
    private volatile String pageToken;

    YouTubeClient(StreamData d, ScheduledExecutorService ex, String key, String vid, int statsSec, int chatSec) {
        this.d = d;
        this.ex = ex;
        this.key = key;
        this.vid = vid;
        this.statsSec = Math.max(10, statsSec);
        this.chatSec = Math.max(5, chatSec);
    }

    void start() {
        this.ex.execute(this::stats);
    }

    void stop() {
        this.stopped = true;
        this.http.close();
    }

    private JsonObject get(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10L)).GET().build();
        HttpResponse<String> res = this.http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (res.statusCode() != 200) {
            String msg = "HTTP " + res.statusCode();
            try {
                JsonObject err = JsonParser.parseString((String)res.body()).getAsJsonObject();
                if (err.has("error") && err.getAsJsonObject("error").has("message")) {
                    msg = err.getAsJsonObject("error").get("message").getAsString();
                }
            }
            catch (Exception ignored) {
                // keep the status-based message when the error body is not the expected JSON
            }
            throw new IOException(msg);
        }
        return JsonParser.parseString((String)res.body()).getAsJsonObject();
    }

    private static long num(JsonObject o, String k) {
        try {
            return o != null && o.has(k) ? Long.parseLong(o.get(k).getAsString()) : -1L;
        }
        catch (Exception e) {
            return -1L;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void stats() {
        if (this.stopped) {
            return;
        }
        try {
            JsonObject j = this.get("https://www.googleapis.com/youtube/v3/videos?part=statistics,liveStreamingDetails,snippet&id=" + this.vid + "&key=" + this.key);
            JsonArray items = j.getAsJsonArray("items");
            if (items == null || items.isEmpty()) {
                this.d.status = "Video not found";
                return;
            }
            JsonObject v = items.get(0).getAsJsonObject();
            JsonObject st = v.has("statistics") ? v.getAsJsonObject("statistics") : null;
            JsonObject ls = v.has("liveStreamingDetails") ? v.getAsJsonObject("liveStreamingDetails") : null;
            this.d.views = YouTubeClient.num(st, "viewCount");
            this.d.likes = YouTubeClient.num(st, "likeCount");
            this.d.watching = YouTubeClient.num(ls, "concurrentViewers");
            boolean bl = this.d.live = ls != null && ls.has("actualStartTime") && !ls.has("actualEndTime");
            if (v.has("snippet")) {
                this.d.title = v.getAsJsonObject("snippet").get("title").getAsString();
            }
            this.d.status = "";
            if (ls != null && ls.has("activeLiveChatId") && !this.chatStarted) {
                this.chatId = ls.get("activeLiveChatId").getAsString();
                this.chatStarted = true;
                this.ex.execute(this::chat);
            }
        }
        catch (Exception e) {
            this.d.status = YouTubeClient.shorten(e.getMessage());
            GlassUiClient.LOGGER.warn("YouTube stats failed: {}", (Object)e.getClass().getSimpleName());
        }
        finally {
            if (!this.stopped) {
                this.ex.schedule(this::stats, (long)this.statsSec, TimeUnit.SECONDS);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void chat() {
        if (this.stopped) {
            return;
        }
        long delay = (long)this.chatSec * 1000L;
        try {
            int from;
            String url = "https://www.googleapis.com/youtube/v3/liveChat/messages?part=snippet,authorDetails&maxResults=200&liveChatId=" + URLEncoder.encode(this.chatId, StandardCharsets.UTF_8) + (String)(this.pageToken != null ? "&pageToken=" + URLEncoder.encode(this.pageToken, StandardCharsets.UTF_8) : "") + "&key=" + this.key;
            JsonObject j = this.get(url);
            String string = this.pageToken = j.has("nextPageToken") ? j.get("nextPageToken").getAsString() : this.pageToken;
            if (j.has("pollingIntervalMillis")) {
                delay = Math.max(delay, j.get("pollingIntervalMillis").getAsLong());
            }
            JsonArray items = j.getAsJsonArray("items");
            for (int i = from = this.firstChat ? Math.max(0, items.size() - 4) : 0; i < items.size(); ++i) {
                JsonObject it = items.get(i).getAsJsonObject();
                JsonObject sn = it.getAsJsonObject("snippet");
                if (!sn.has("displayMessage")) continue;
                String author = it.getAsJsonObject("authorDetails").get("displayName").getAsString();
                String msg = sn.get("displayMessage").getAsString();
                this.d.chat.addLast(new StreamData.ChatLine(author, msg));
                if (this.firstChat) continue;
                ++this.d.chatCount;
                SongRequests.onChat(author, msg);
            }
            this.firstChat = false;
            while (this.d.chat.size() > 6) {
                this.d.chat.pollFirst();
            }
        }
        catch (Exception e) {
            String m;
            String string = m = e.getMessage() == null ? "" : e.getMessage();
            if (m.toLowerCase().contains("ended") || m.toLowerCase().contains("disabled")) {
                this.d.status = "Chat ended";
                return;
            }
            this.d.status = YouTubeClient.shorten(m);
            delay = 30000L;
        }
        finally {
            if (!this.stopped && !"Chat ended".equals(this.d.status)) {
                this.ex.schedule(this::chat, delay, TimeUnit.MILLISECONDS);
            }
        }
    }

    private static String shorten(String s) {
        if (s == null) {
            return "Error";
        }
        return s.length() > 40 ? s.substring(0, 40) : s;
    }
}

