/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.music;

import com.glassui.GlassUiClient;

final class Launcher {
    private Launcher() {
    }

    static void open(String target) {
        if (target == null || (!target.startsWith("https://") && !target.startsWith("spotify:"))) {
            return;
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            ProcessBuilder pb = os.contains("win") ? new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", target) : (os.contains("mac") ? new ProcessBuilder("open", target) : new ProcessBuilder("xdg-open", target));
            pb.redirectError(ProcessBuilder.Redirect.DISCARD).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        }
        catch (Exception e) {
            GlassUiClient.LOGGER.debug("Open failed: {}", (Object)e.toString());
        }
    }
}

