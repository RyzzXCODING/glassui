/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.music;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

final class YouTubeStream {
    private static final List<Client> CLIENTS = List.of(new Client("ANDROID_VR", "28", "1.60.19", "com.google.android.apps.youtube.vr.oculus/1.60.19 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip", "{\"clientName\":\"ANDROID_VR\",\"clientVersion\":\"1.60.19\",\"deviceMake\":\"Oculus\",\"deviceModel\":\"Quest 3\",\"osName\":\"Android\",\"osVersion\":\"12L\",\"androidSdkVersion\":32,\"hl\":\"en\",\"gl\":\"US\"}"), new Client("IOS", "5", "20.10.4", "com.google.ios.youtube/20.10.4 (iPhone16,2; U; CPU iOS 18_3_2 like Mac OS X;)", "{\"clientName\":\"IOS\",\"clientVersion\":\"20.10.4\",\"deviceMake\":\"Apple\",\"deviceModel\":\"iPhone16,2\",\"osName\":\"iPhone\",\"osVersion\":\"18.3.2.22D82\",\"hl\":\"en\",\"gl\":\"US\"}"));
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();

    private YouTubeStream() {
    }

    static Stream resolve(String videoId) throws IOException, InterruptedException {
        String lastReason = "no playable audio found";
        for (Client c : CLIENTS) {
            try {
                Stream s = YouTubeStream.fromClient(c, videoId);
                if (s == null) continue;
                return s;
            }
            catch (IOException e) {
                lastReason = e.getMessage() == null ? e.toString() : e.getMessage();
            }
        }
        throw new IOException(lastReason);
    }

    private static Stream fromClient(Client c, String id) throws IOException, InterruptedException {
        JsonObject sd;
        JsonObject ps;
        String st;
        JsonObject o;
        String body = "{\"context\":{\"client\":" + c.clientJson() + "},\"videoId\":\"" + id + "\",\"contentCheckOk\":true,\"racyCheckOk\":true}";
        HttpRequest rq = HttpRequest.newBuilder(URI.create("https://youtubei.googleapis.com/youtubei/v1/player?prettyPrint=false")).timeout(Duration.ofSeconds(15L)).header("Content-Type", "application/json").header("User-Agent", c.ua()).header("X-YouTube-Client-Name", c.id()).header("X-YouTube-Client-Version", c.version()).POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> r = HTTP.send(rq, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) {
            throw new IOException(c.name() + " HTTP " + r.statusCode());
        }
        try {
            o = JsonParser.parseString((String)r.body()).getAsJsonObject();
        }
        catch (Exception e) {
            throw new IOException(c.name() + ": bad response");
        }
        if (o.has("playabilityStatus") && o.get("playabilityStatus").isJsonObject() && !"OK".equals(st = YouTubeStream.str(ps = o.getAsJsonObject("playabilityStatus"), "status"))) {
            String why = YouTubeStream.str(ps, "reason");
            throw new IOException(c.name() + ": " + (why.isEmpty() ? st : why));
        }
        if (!o.has("streamingData") || !o.get("streamingData").isJsonObject()) {
            throw new IOException(c.name() + ": no streams");
        }
        long durMs = 0L;
        if (o.has("videoDetails") && o.get("videoDetails").isJsonObject()) {
            try {
                durMs = Long.parseLong(YouTubeStream.str(o.getAsJsonObject("videoDetails"), "lengthSeconds")) * 1000L;
            }
            catch (Exception why) {
                // empty catch block
            }
        }
        JsonArray fmts = (sd = o.getAsJsonObject("streamingData")).has("adaptiveFormats") && sd.get("adaptiveFormats").isJsonArray() ? sd.getAsJsonArray("adaptiveFormats") : new JsonArray();
        Stream best = YouTubeStream.bestAudio(fmts, c, durMs, false);
        if (best == null && sd.has("formatStreams") && sd.get("formatStreams").isJsonArray()) {
            best = YouTubeStream.bestAudio(sd.getAsJsonArray("formatStreams"), c, durMs, true);
        }
        if (best != null) {
            return best;
        }
        String hls = YouTubeStream.str(sd, "hlsManifestUrl");
        if (!hls.isEmpty()) {
            return new Stream(hls, c.ua(), "application/x-mpegURL", 0L, Map.of(), durMs);
        }
        throw new IOException(c.name() + ": no audio format");
    }

    private static Stream bestAudio(JsonArray fmts, Client c, long durMs, boolean progressive) {
        Stream best = null;
        int bestScore = Integer.MIN_VALUE;
        for (JsonElement el : fmts) {
            JsonObject at;
            if (!el.isJsonObject()) continue;
            JsonObject f = el.getAsJsonObject();
            String mime = YouTubeStream.str(f, "mimeType");
            String url = YouTubeStream.str(f, "url");
            if (url.isEmpty()) continue;
            if (progressive ? !mime.startsWith("video/") : !mime.startsWith("audio/")) continue;
            long br = f.has("bitrate") && f.get("bitrate").isJsonPrimitive() ? f.get("bitrate").getAsLong() : 0L;
            int score = (int)Math.min(br / 1000L, 100000L);
            if (!progressive && f.has("audioTrack") && f.get("audioTrack").isJsonObject() && (at = f.getAsJsonObject("audioTrack")).has("audioIsDefault") && at.get("audioIsDefault").getAsBoolean()) {
                score += 1000000;
            }
            if (score <= bestScore) continue;
            bestScore = score;
            best = new Stream(url, c.ua(), mime, br, progressive ? Map.<String, String>of() : Map.of("Range", "bytes=0-"), durMs);
        }
        return best;
    }

    private static String str(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e != null && e.isJsonPrimitive() ? e.getAsString() : "";
    }

    private record Client(String name, String id, String version, String ua, String clientJson) {
    }

    record Stream(String url, String userAgent, String mime, long bitrate, Map<String, String> headers, long durationMs) {
    }
}

