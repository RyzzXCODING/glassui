/*
 * Decompiled with CFR 0.152.
 */
package com.glassui.hud;

import com.glassui.GlassUiClient;
import com.glassui.hud.StreamData;
import com.glassui.hud.StreamHud;
import com.glassui.music.LocalPlayer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public final class MediaControl {
    private static final String PS = "[Console]::OutputEncoding=[System.Text.Encoding]::UTF8\nAdd-Type -AssemblyName System.Runtime.WindowsRuntime\n$asTask=([System.WindowsRuntimeSystemExtensions].GetMethods()|Where-Object{$_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'})[0]\nfunction Await($op,$type){$t=$asTask.MakeGenericMethod($type).Invoke($null,@($op));$t.Wait(-1)|Out-Null;$t.Result}\n[void][Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]\n$mT=[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]\n$mgr=Await ($mT::RequestAsync()) $mT\n$target=$null\nforeach($s in $mgr.GetSessions()){ if($s.SourceAppUserModelId -eq '@@ID@@'){ $target=$s } }\nif($target -eq $null){ $target=$mgr.GetCurrentSession() }\nif($target -ne $null){\n  switch('@@CMD@@'){\n    'next'{ [void](Await ($target.TrySkipNextAsync()) ([bool])) }\n    'prev'{ [void](Await ($target.TrySkipPreviousAsync()) ([bool])) }\n    'toggle'{ [void](Await ($target.TryTogglePlayPauseAsync()) ([bool])) }\n  }\n}\n";

    private MediaControl() {
    }

    public static void send(Cmd cmd) {
        String id;
        if (LocalPlayer.active()) {
            LocalPlayer.command(cmd);
            return;
        }
        StreamData.NowPlaying np = StreamHud.DATA.song;
        String string = id = np == null || np.appId() == null ? "" : np.appId().replaceAll("[^A-Za-z0-9._!\\- ]", "");
        if (np != null && cmd == Cmd.TOGGLE) {
            StreamHud.DATA.song = new StreamData.NowPlaying(np.title(), np.artist(), np.source(), !np.playing(), np.appId());
        }
        Thread t = new Thread(() -> MediaControl.run(cmd, id), "GlassUI-MediaCmd");
        t.setDaemon(true);
        t.start();
    }

    private static void run(Cmd cmd, String id) {
        String os = System.getProperty("os.name", "").toLowerCase();
        String name = switch (cmd.ordinal()) {
            default -> throw new MatchException(null, null);
            case 2 -> "next";
            case 0 -> "prev";
            case 1 -> "toggle";
        };
        try {
            Process p;
            if (os.contains("win")) {
                String script = PS.replace("@@ID@@", id).replace("@@CMD@@", name);
                String enc = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
                p = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-EncodedCommand", enc).redirectError(ProcessBuilder.Redirect.DISCARD).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            } else if (os.contains("linux")) {
                String pc = switch (cmd.ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 2 -> "next";
                    case 0 -> "previous";
                    case 1 -> "play-pause";
                };
                ProcessBuilder pb = id.isEmpty() ? new ProcessBuilder("playerctl", pc) : new ProcessBuilder("playerctl", "-p", id, pc);
                p = pb.redirectError(ProcessBuilder.Redirect.DISCARD).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            } else {
                return;
            }
            if (!p.waitFor(10L, TimeUnit.SECONDS)) {
                p.destroyForcibly();
            }
        }
        catch (Exception e) {
            GlassUiClient.LOGGER.debug("Media command failed: {}", (Object)e.toString());
        }
    }

    public static enum Cmd {
        PREV,
        TOGGLE,
        NEXT;

    }
}

