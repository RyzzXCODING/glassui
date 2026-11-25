# GlassUI 1.3.18 — Source Review Checklist

> All source files are available at `source/src/main/java/com/glassui/`.
> These are **decompiled reconstructions**, not original source. Review statuses reflect audit progress; none are user-approved yet.

## File Inventory

| # | File | Purpose | Feature | Status | Review Status |
|---|---|---|---|---|---|
| 1 | `com/glassui/GlassConfig.java` | Configuration load/save/validation | All settings | Reconstructed | READY FOR REVIEW |
| 2 | `com/glassui/GlassModMenu.java` | ModMenu config screen entrypoint | Mod integration | Reconstructed | READY FOR REVIEW |
| 3 | `com/glassui/GlassUiClient.java` | Main client entrypoint, key handling, HUD registration | Core init | Reconstructed | READY FOR REVIEW |
| 4 | `com/glassui/hud/ChatSound.java` | Chat notification sounds | Chat sounds | Reconstructed | READY FOR REVIEW |
| 5 | `com/glassui/hud/CombatFx.java` | Damage numbers, combat effects, HUD render | Combat FX | Reconstructed | READY FOR REVIEW |
| 6 | `com/glassui/hud/ComboTracker.java` | Combo tracking | Combo display | Reconstructed | READY FOR REVIEW |
| 7 | `com/glassui/hud/GlassHotbar.java` | Hotbar replacement render | Hotbar | Reconstructed | READY FOR REVIEW |
| 8 | `com/glassui/hud/IslandSearch.java` | Dynamic Island search display | Island | Reconstructed | READY FOR REVIEW |
| 9 | `com/glassui/hud/LiveAlerts.java` | Gift/follow pop-up alerts | Stream alerts | Reconstructed | READY FOR REVIEW |
| 10 | `com/glassui/hud/MediaControl.java` | OS media key interception | Media control | Reconstructed | READY FOR REVIEW |
| 11 | `com/glassui/hud/MediaDetector.java` | Detect OS media playback state | Now playing | Reconstructed | READY FOR REVIEW |
| 12 | `com/glassui/hud/MusicToggle.java` | Music playback toggle | Music control | Reconstructed | READY FOR REVIEW |
| 13 | `com/glassui/hud/StreamData.java` | Shared stream/music state DTO | Stream HUD | Reconstructed | READY FOR REVIEW |
| 14 | `com/glassui/hud/StreamHud.java` | Stream HUD lifecycle management | Stream HUD | Reconstructed | READY FOR REVIEW |
| 15 | `com/glassui/hud/StreamHudRenderer.java` | Stream HUD rendering (~65KB class) | Stream HUD | Reconstructed | READY FOR REVIEW |
| 16 | `com/glassui/hud/TapTracker.java` | Top-tap tracking | Tap display | Reconstructed | READY FOR REVIEW |
| 17 | `com/glassui/hud/TikTokClient.java` | TikTok WebSocket client (EULER/TIKFINITY) | TikTok Live | Reconstructed | READY FOR REVIEW |
| 18 | `com/glassui/hud/TikTokEvents.java` | TikTok event parsing | TikTok Live | Reconstructed | READY FOR REVIEW |
| 19 | `com/glassui/hud/YouTubeClient.java` | YouTube Data API v3 stats + live chat | YouTube Live | Reconstructed | READY FOR REVIEW |
| 20 | `com/glassui/mixin/ClickableWidgetSoundMixin.java` | Replace vanilla click sound | UI sound | Reconstructed | READY FOR REVIEW |
| 21 | `com/glassui/mixin/PressableWidgetMixin.java` | Custom button rendering | Glass buttons | Reconstructed | READY FOR REVIEW |
| 22 | `com/glassui/mixin/SliderWidgetMixin.java` | Custom slider rendering | Glass sliders | Reconstructed | READY FOR REVIEW |
| 23 | `com/glassui/mixin/TitleScreenMixin.java` | Replace title-screen buttons with dock | Dock menu | Reconstructed | READY FOR REVIEW |
| 24 | `com/glassui/music/Launcher.java` | Open URLs externally | Link handling | Reconstructed | READY FOR REVIEW |
| 25 | `com/glassui/music/LocalPlayer.java` | ffmpeg + yt-dlp local playback | Local music | Reconstructed | READY FOR REVIEW |
| 26 | `com/glassui/music/Lyrics.java` | LRCLIB + Netease lyrics fetching | Lyrics | Reconstructed | READY FOR REVIEW |
| 27 | `com/glassui/music/MusicHub.java` | Music search, Spotify OAuth, YouTube | Music hub | Reconstructed | READY FOR REVIEW |
| 28 | `com/glassui/music/SongRequests.java` | Viewer song request queue | Requests | Reconstructed | READY FOR REVIEW |
| 29 | `com/glassui/music/Suggest.java` | Google search suggestions | Search | Reconstructed | READY FOR REVIEW |
| 30 | `com/glassui/music/YouTubeStream.java` | YouTube stream URL resolution | Playback | Reconstructed | READY FOR REVIEW |
| 31 | `com/glassui/ui/Anim.java` | Animation helper | UI animations | Reconstructed | READY FOR REVIEW |
| 32 | `com/glassui/ui/CustomGlassWidget.java` | Marker interface for custom widgets | Glass UI | Reconstructed | READY FOR REVIEW |
| 33 | `com/glassui/ui/DockMenu.java` | iOS-style dock menu | Dock menu | Reconstructed | READY FOR REVIEW |
| 34 | `com/glassui/ui/FontStyle.java` | Font style enum | Fonts | Reconstructed | READY FOR REVIEW |
| 35 | `com/glassui/ui/Fx.java` | Font loading, text drawing helpers | Text rendering | Reconstructed | READY FOR REVIEW |
| 36 | `com/glassui/ui/Glass.java` | Glass rendering primitives | Visual style | Reconstructed | READY FOR REVIEW |
| 37 | `com/glassui/ui/GlassAlertScreen.java` | Alert popup screen | Alerts | Reconstructed | READY FOR REVIEW |
| 38 | `com/glassui/ui/GlassButtonRenderer.java` | Button rendering | Glass buttons | Reconstructed | READY FOR REVIEW |
| 39 | `com/glassui/ui/GlassWidgets.java` | Chip/Tab/Toggle widgets | Settings UI | Reconstructed | READY FOR REVIEW |
| 40 | `com/glassui/ui/HudEditorScreen.java` | HUD position editor | HUD editor | Reconstructed | READY FOR REVIEW |
| 41 | `com/glassui/ui/IconShapes.java` | Vector icon shapes | Icons | Reconstructed | READY FOR REVIEW |
| 42 | `com/glassui/ui/Icons.java` | Icon definitions | Icons | Reconstructed | READY FOR REVIEW |
| 43 | `com/glassui/ui/MusicScreen.java` | Music player GUI | Music player | Reconstructed | READY FOR REVIEW |
| 44 | `com/glassui/ui/PlayerHead.java` | Player head rendering | Profile HUD | Reconstructed | READY FOR REVIEW |
| 45 | `com/glassui/ui/Raster.java` | Raster graphics utility | Rendering | Reconstructed | READY FOR REVIEW |
| 46 | `com/glassui/ui/Spring.java` | Spring physics animation | Animations | Reconstructed | READY FOR REVIEW |
| 47 | `com/glassui/ui/StreamHudConfigScreen.java` | Main settings screen | Settings | Reconstructed | READY FOR REVIEW |
| 48 | `com/glassui/ui/StyleScreen.java` | Theme/style settings screen | Settings | Reconstructed | READY FOR REVIEW |
| 49 | `com/glassui/ui/Theme.java` | Theme definitions | Themes | Reconstructed | READY FOR REVIEW |
| 50 | `com/glassui/ui/UiSound.java` | Programmatic UI sounds | UI sounds | Reconstructed | READY FOR REVIEW |

## Review Notes

- All files are accessible at the paths above for user review.
- None are marked `APPROVED BY USER` — awaiting explicit user approval.
- Decompiler artifacts: CFR warnings about removed try-catch blocks, intermediary-mapped Minecraft method names (`method_XXXXX`, `field_XXXXX`).
- No source file is a placeholder — all contain reconstructed logic from the JAR.

## Awaiting User Action

Please review the source files. Mark files as approved or request changes. No fixes will be applied until the explicit approval gate is passed.
