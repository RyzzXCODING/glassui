/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.hud.LiveAlerts;
import com.glassui.hud.StreamData;
import com.glassui.hud.TapTracker;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Locale;
import java.util.Map;

final class TikTokEvents {
    private static String lastFollowUser = "";
    private static long lastFollowAt;
    private static final String[] NAME_KEYS;

    private TikTokEvents() {
    }

    static boolean handle(String json, StreamData d) {
        JsonElement root;
        try {
            root = JsonParser.parseString((String)json);
        }
        catch (Exception e) {
            return false;
        }
        return TikTokEvents.walk(root, d, null, 0);
    }

    private static boolean walk(JsonElement el, StreamData d, String inheritedType, int depth) {
        if (el == null || depth > 4) {
            return false;
        }
        boolean any = false;
        if (el.isJsonArray()) {
            for (JsonElement e : (JsonArray)el) {
                any |= TikTokEvents.walk(e, d, inheritedType, depth + 1);
            }
            return any;
        }
        if (!el.isJsonObject()) {
            return false;
        }
        JsonObject o = el.getAsJsonObject();
        for (String k : new String[]{"messages", "events", "batch"}) {
            if (!o.has(k) || !o.get(k).isJsonArray()) continue;
            return TikTokEvents.walk(o.get(k), d, inheritedType, depth + 1);
        }
        String type = TikTokEvents.typeOf(o);
        if (type == null) {
            type = inheritedType;
        }
        JsonObject data = TikTokEvents.payload(o);
        if (type == null) {
            type = TikTokEvents.guess(data);
        }
        if (type == null) {
            return false;
        }
        String t = type.toLowerCase(Locale.ROOT);
        if (t.contains("error") || t.contains("fail")) {
            String m = TikTokEvents.str(data, "message", "error", "reason");
            d.status = m == null ? "Error" : TikTokEvents.trim(m);
            d.live = false;
            return true;
        }
        if (t.contains("end") && (t.contains("live") || t.contains("stream") || t.contains("control"))) {
            d.live = false;
            d.linkState = "Live ended";
            return true;
        }
        if (t.contains("chat") || t.equals("comment")) {
            String text = TikTokEvents.str(data, "comment", "content", "text", "message");
            if (text == null || text.isBlank()) {
                return false;
            }
            String who = TikTokEvents.name(data);
            d.addChat(who, text);
            TikTokEvents.markLive(d);
            return true;
        }
        if (t.contains("like")) {
            long total;
            String tapper = TikTokEvents.nameOrNull(data);
            if (tapper != null) {
                long taps = TikTokEvents.num(data, "likeCount", "like_count", "count");
                TapTracker.record(tapper, taps > 0L ? taps : 1L);
            }
            if ((total = TikTokEvents.num(data, "totalLikeCount", "total_like_count", "totalLikes", "likes")) >= 0L) {
                d.likes = total;
            } else {
                long add = TikTokEvents.num(data, "likeCount", "like_count", "count");
                if (add > 0L) {
                    d.likes = Math.max(0L, d.likes) + add;
                }
            }
            TikTokEvents.markLive(d);
            return true;
        }
        if (t.contains("roomuser") || t.contains("viewer") || t.contains("roominfo") || t.contains("room_user")) {
            long total;
            long v = TikTokEvents.num(data, "viewerCount", "viewer_count", "totalUser", "total_user", "userCount", "viewers");
            if (v >= 0L) {
                d.watching = v;
                d.views = v;
            }
            if ((total = TikTokEvents.num(data, "totalLikeCount", "likeCount")) >= 0L && t.contains("roominfo")) {
                d.likes = total;
            }
            TikTokEvents.markLive(d);
            return true;
        }
        if (t.contains("gift")) {
            TikTokEvents.gift(data, d);
            TikTokEvents.markLive(d);
            return true;
        }
        if (TikTokEvents.isFollow(t, o, data)) {
            String who = TikTokEvents.name(data);
            if (!TikTokEvents.dupFollow(who)) {
                LiveAlerts.push(new LiveAlerts.Alert(LiveAlerts.Kind.FOLLOW, who, "", 0, 0L, System.currentTimeMillis()));
            }
            TikTokEvents.markLive(d);
            return true;
        }
        if (t.contains("connect") || t.contains("roomstart") || t.contains("member") || t.contains("share")) {
            TikTokEvents.markLive(d);
            return true;
        }
        return false;
    }

    private static String typeOf(JsonObject o) {
        String m;
        StringBuilder sb = new StringBuilder();
        for (String k : new String[]{"event", "eventName", "event_name", "type", "eventType", "messageType", "method", "name"}) {
            JsonElement e = o.get(k);
            if (e == null || !e.isJsonPrimitive() || e.getAsString().isEmpty()) continue;
            sb.append(e.getAsString()).append(' ');
        }
        if (o.has("common") && o.get("common").isJsonObject() && (m = TikTokEvents.str(o.getAsJsonObject("common"), "method")) != null) {
            sb.append(m).append(' ');
        }
        return sb.length() == 0 ? null : sb.toString().trim();
    }

    private static JsonObject payload(JsonObject o) {
        for (String k : new String[]{"data", "payload", "body"}) {
            if (!o.has(k) || !o.get(k).isJsonObject()) continue;
            return o.getAsJsonObject(k);
        }
        return o;
    }

    private static boolean isFollow(String t, JsonObject root, JsonObject data) {
        String s;
        if (t.contains("unfollow")) {
            return false;
        }
        if (t.contains("follow")) {
            return true;
        }
        if (!t.contains("social") && !t.contains("relation")) {
            return false;
        }
        StringBuilder sig = new StringBuilder();
        TikTokEvents.collect(root, sig, 0);
        if (data != root) {
            TikTokEvents.collect(data, sig, 0);
        }
        if ((s = sig.toString()).contains("unfollow")) {
            return false;
        }
        if (s.contains("follow")) {
            return true;
        }
        if (s.contains("share")) {
            return false;
        }
        long action = TikTokEvents.num(data, "action");
        long shareType = TikTokEvents.num(data, "shareType", "share_type");
        return action == 1L && shareType <= 0L;
    }

    private static void collect(JsonObject o, StringBuilder out, int depth) {
        if (o == null || depth > 3) {
            return;
        }
        for (Map.Entry en : o.entrySet()) {
            JsonElement v = (JsonElement)en.getValue();
            String k = ((String)en.getKey()).toLowerCase(Locale.ROOT);
            if (v.isJsonPrimitive()) {
                if (!k.equals("displaytype") && !k.equals("display_type") && !k.equals("label") && !k.equals("key") && !k.equals("action") && !k.equals("type") && !k.equals("eventtype") && !k.equals("defaultpattern") && !k.equals("default_pattern") && !k.equals("displaytext") && !k.equals("text")) continue;
                out.append(v.getAsString().toLowerCase(Locale.ROOT)).append(' ');
                continue;
            }
            if (!v.isJsonObject() || k.equals("user") || k.equals("touser") || k.equals("to_user")) continue;
            TikTokEvents.collect(v.getAsJsonObject(), out, depth + 1);
        }
    }

    private static synchronized boolean dupFollow(String who) {
        long now = System.currentTimeMillis();
        boolean dup = who.equals(lastFollowUser) && now - lastFollowAt < 5000L;
        lastFollowUser = who;
        lastFollowAt = now;
        return dup;
    }

    private static void gift(JsonObject data, StreamData d) {
        boolean hasEnd;
        JsonElement end;
        long each;
        String name;
        JsonObject det = null;
        for (String k : new String[]{"giftDetails", "gift_details", "gift", "giftInfo"}) {
            if (!data.has(k) || !data.get(k).isJsonObject()) continue;
            det = data.getAsJsonObject(k);
            break;
        }
        if ((name = TikTokEvents.str(data, "giftName", "gift_name")) == null && det != null) {
            name = TikTokEvents.str(det, "giftName", "gift_name", "name");
        }
        if (name == null) {
            name = "a gift";
        }
        if ((each = TikTokEvents.num(data, "diamondCount", "diamond_count")) < 0L && det != null) {
            each = TikTokEvents.num(det, "diamondCount", "diamond_count");
        }
        int count = (int)Math.max(1L, Math.min(9999L, TikTokEvents.num(data, "repeatCount", "repeat_count", "comboCount")));
        long type = TikTokEvents.num(data, "giftType", "gift_type");
        if (type < 0L && det != null) {
            type = TikTokEvents.num(det, "giftType", "gift_type", "type");
        }
        if ((end = data.get("repeatEnd")) == null) {
            end = data.get("repeat_end");
        }
        boolean bl = hasEnd = end != null && end.isJsonPrimitive();
        if (type == 1L && hasEnd && !"true".equalsIgnoreCase(end.getAsString()) && !"1".equals(end.getAsString())) {
            return;
        }
        long diamonds = each < 0L ? 0L : each * (long)count;
        LiveAlerts.push(new LiveAlerts.Alert(LiveAlerts.Kind.GIFT, TikTokEvents.name(data), TikTokEvents.trim(name), count, diamonds, System.currentTimeMillis()));
    }

    private static void markLive(StreamData d) {
        d.live = true;
        d.status = "";
        d.linkState = "LIVE";
    }

    private static String guess(JsonObject data) {
        if (data.has("comment")) {
            return "chat";
        }
        if (data.has("totalLikeCount") || data.has("likeCount")) {
            return "like";
        }
        if (data.has("viewerCount") || data.has("totalUser")) {
            return "roomUser";
        }
        return null;
    }

    private static String name(JsonObject data) {
        String n = TikTokEvents.nameOrNull(data);
        return n != null ? n : "viewer";
    }

    private static String nameOrNull(JsonObject data) {
        String n = TikTokEvents.str(data, NAME_KEYS);
        if (n != null) {
            return n;
        }
        for (String k : new String[]{"user", "userInfo", "user_info", "sender", "fromUser", "from_user", "author"}) {
            String u;
            if (!data.has(k) || !data.get(k).isJsonObject() || (u = TikTokEvents.str(data.getAsJsonObject(k), NAME_KEYS)) == null) continue;
            return u;
        }
        return TikTokEvents.str(data, "user");
    }

    private static String str(JsonObject o, String ... keys) {
        for (String k : keys) {
            String s;
            JsonElement e = o.get(k);
            if (e == null || !e.isJsonPrimitive() || (s = e.getAsString()).isEmpty()) continue;
            return s;
        }
        return null;
    }

    private static long num(JsonObject o, String ... keys) {
        for (String k : keys) {
            JsonElement e = o.get(k);
            if (e == null || !e.isJsonPrimitive()) continue;
            try {
                return (long)Double.parseDouble(e.getAsString());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return -1L;
    }

    private static String trim(String s) {
        return s.length() > 48 ? s.substring(0, 48) : s;
    }

    static {
        NAME_KEYS = new String[]{"nickname", "nickName", "nick_name", "uniqueId", "unique_id", "username", "userName", "displayName", "display_name", "displayId", "profileName"};
    }
}

