/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.hud.TapTracker;
import com.glassui.music.SongRequests;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class StreamData {
    public volatile long views = -1L;
    public volatile long likes = -1L;
    public volatile long watching = -1L;
    public volatile long chatCount = 0L;
    public volatile boolean live;
    public volatile String title = "";
    public volatile String status = "";
    public volatile String linkState = "";
    public final ConcurrentLinkedDeque<ChatLine> chat = new ConcurrentLinkedDeque();
    public volatile NowPlaying song;

    public void reset() {
        this.watching = -1L;
        this.likes = -1L;
        this.views = -1L;
        this.chatCount = 0L;
        this.live = false;
        this.title = "";
        this.status = "";
        this.linkState = "";
        this.song = null;
        this.chat.clear();
        TapTracker.clear();
        ChatSound.reset();
    }

    public void addChat(String author, String text) {
        this.chat.addLast(new ChatLine(author, text));
        while (this.chat.size() > 6) {
            this.chat.pollFirst();
        }
        ++this.chatCount;
        SongRequests.onChat(author, text);
    }

    public record ChatLine(String author, String text, long at) {
        public ChatLine(String author, String text) {
            this(author, text, System.nanoTime());
        }
    }

    public record NowPlaying(String title, String artist, String source, boolean playing, String appId) {
    }
}

