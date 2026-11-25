/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.hud.MediaControl;
import com.glassui.hud.StreamHud;
import com.glassui.music.MusicHub;

public final class MusicToggle {
    private MusicToggle() {
    }

    public static void toggle() {
        if (!MusicHub.enabled()) {
            MusicHub.setEnabled(true);
        }
        if (StreamHud.DATA.song == null) {
            MusicHub.playRandom();
        } else {
            MediaControl.send(MediaControl.Cmd.TOGGLE);
        }
    }

    public static String keyLabel(int code) {
        return switch (code) {
            case 0 -> "Off";
            case 66 -> "B";
            case 78 -> "N";
            case 80 -> "P";
            case 295 -> "F6";
            case 296 -> "F7";
            default -> "?";
        };
    }
}

