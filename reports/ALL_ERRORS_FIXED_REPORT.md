# GlassUI 1.3.18 — All Confirmed Errors Fixed (Phase 3 Final Repair)

> User request: **"fix all errors and then confirm to me once they have been fixed"**.
> This report closes every confirmed finding from `SECOND_PASS_AUDIT.md` and the earlier audits.
> **No build, compile, remap, package, JAR generation, commit-time verification, GitHub Release, or in-game test was performed.** All statements below are based on source inspection and read-only Maven/bytecode checks.

## 1. Scope

29 files changed: 28 Java sources across `com/glassui`, `ui/`, `hud/`, `music/`, `mixin/`, plus `fabric.mod.json`.
Four disjoint fix agents were used; every edit was independently re-read and cross-checked by the coordinator. No new features, colors, layouts, animations, keybindings, settings, or names were introduced.

## 2. Fixes applied

### Core / config / UI (11 fixes)

| ID | File | Fix |
|---|---|---|
| L2 | `GlassConfig.java:110` | `load()` installs a fresh default instance before parsing, so a failed reload cannot leave stale in-memory values. |
| L3 | `ui/GlassAlertScreen.java:42` | `onConfirm` defaults to a no-op when null. Close behavior unchanged. |
| L3 | `ui/DockMenu.java:34,51-54,97,110` | One-time warning (via `GlassUiClient.LOGGER`) on Mods reflection failure; items start inactive until the same `now >= startAt` visibility gate used for drawing, so invisible items cannot be clicked. |
| L6 | `GlassUiClient.java:85-101,110-121` | Menu/music/cassette and arrow-key pressed states are sampled every tick regardless of screen state; toggles now require not-pressed → pressed. Keybindings and cooldowns unchanged. |
| M-1 | `ui/StreamHudConfigScreen.java:578-579` | Null guards for `NowPlaying.title()` / `artist()` before `Fx.trim` / `isBlank`. |
| M-2 | `ui/StyleScreen.java:116`, `ui/MusicScreen.java:490`, `ui/GlassButtonRenderer.java:29-44`, `ui/DockMenu.java:82-84,93-95,116-118,135` | Interactive widgets render after `popMatrix()` so hitboxes match drawn bounds; DockMenu hit bounds follow the animated transform (`method_55444(w,h,x,y)` verified against 1.21.11 bytecode). |
| M-3 | `ui/MusicScreen.java:263-269,411,420-424` | All widget mouse input, Enter search and volume drag are ignored while `closing`; latched drag is cleared. |
| L1 | `ui/HudEditorScreen.java:88-100,133-138` | Decorative info/hint bars are excluded from drag hit testing; hint geometry is shared between hit test and drawing. |

### HUD (7 fixes)

| ID | File | Fix |
|---|---|---|
| F5 | `hud/StreamHudRenderer.java:216-224` | Editor rect uses the same scale-aware `chatMaxH` clamp as live rendering; added client null guard. |
| F6 | `hud/StreamHud.java:14,83-90` | `stop()` skips the bounded `awaitTermination` only on the client/render thread (`method_18854`, verified inherited from `class_1255`); other threads keep the join. Producer flags and `shutdownNow()` are unchanged. |
| F7 | `hud/MediaControl.java:63-67`, `hud/MediaDetector.java:43,61,108,130` | Process stdin pipes closed on all wait/destroy/reader paths. |
| F8 | `hud/MediaDetector.java:33-70,104-107,125-128` | Volatile `stopped` gate; `stop()` and process creation share a synchronized critical section; a process started just before stop is destroyed immediately. |
| F9 | `hud/StreamHudRenderer.java:181-192,421-423,535-538` | `volDrag` cleared via `endVolDrag()` when the HUD is hidden, editor is open, all widgets are off, island is off, or the client is unavailable. |
| F10 | `hud/TikTokClient.java:109-111,151-158`, `hud/YouTubeClient.java:176-183` | Raw upstream payload logging replaced with length-only logging; `shorten()` redacts URLs and `key/token` values before setting HUD status. Exception logging remains sanitized. |
| F11 | `hud/GlassHotbar.java:31`, `hud/CombatFx.java:116`, `hud/StreamHudRenderer.java:181,209-211,217-220,535-538,1038,1570-1572` | Null guards for `class_310.method_1551()` before every previously unguarded dereference. |

### Music / process (12 fixes)

| ID | File | Fix |
|---|---|---|
| N1 | `music/MusicHub.java:76-79,373-400,807-811` | First-play `LocalPlayer.setup()` runs on a dedicated daemon `SETUP_EX`; `youtubePlay()` returns an internal sentinel so `doPlay()` does not overwrite the "Setting up…" status. |
| N2 | `music/MusicHub.java:433,439-466` | OAuth callback accepts up to 32 connections × 64 request lines (10 s/socket, 180 s overall), skipping favicon/other requests; PKCE state check unchanged. |
| N3 | `music/SongRequests.java:214-221` | Local playback identity compares title (existing), source (existing) and artist (new) because `appId` is the fixed `glassui-local`. External-app id logic unchanged. |
| N4 | `music/LocalPlayer.java:637-640` | `InterruptedException` now calls `onEnd()` via an `"playback interrupted"` outcome and restores the interrupt flag instead of returning early. |
| N5 | `music/LocalPlayer.java:365,407-417,477,497-499,612,619,626,646,801` | `safeError()` strips URLs, query strings and long opaque tokens; raw yt-dlp/ffmpeg stderr is never logged; UI status capped at 60 chars. |
| N6 | `music/Lyrics.java:128-152,187-207` | 15 s total deadline per `load()`; skips remaining LRCLIB candidates and gates NetEase lookups; gen guards preserved. |
| N7 | `music/SongRequests.java:28,113-121` | Rate-limit map prunes entries older than 60 s, then removes oldest until back to `MAX_LAST = 300`. |
| N8 | `music/Suggest.java:116,156,160,175`, `music/MusicHub.java:191,439,485`, `music/LocalPlayer.java:673` | Dead constants reused in place of literals; values unchanged. |
| N10 | `music/YouTubeStream.java:74-110` | Progressive `formatStreams` tried before HLS when adaptive formats yield no audio. |
| N11 | `music/LocalPlayer.java:18,171-173,186-209` | Pure-Java PATH/PATHEXT executability check for bare-name commands; 3 s timeout retained. |
| N12 | `music/Launcher.java:13` | `target == null` guard. |
| THREAD-05 | `music/MusicHub.java:77-79,714-729,812-817` | `commitVolume()` coalesces `GlassConfig.save()` onto a daemon scheduler with 150 ms debounce; no disk I/O on the client tick. |

### Mixins / metadata (5 fixes)

| ID | File | Fix |
|---|---|---|
| MIX-01 | `ui/UiSound.java:16-37`, `mixin/ClickableWidgetSoundMixin.java:17-24` | `pop(...)`, `click()` return `boolean` (true only when a sound actually played); vanilla click sound is cancelled only on true. |
| MIX-02 | `mixin/PressableWidgetMixin.java:16,23-24,32,50` | `@Shadow method_76256(ctx)` reproduces vanilla cursor handling before cancel; shadow target verified in `class_339` bytecode. |
| MIX-03 | `mixin/SliderWidgetMixin.java:9-11,28-31,56-67` | Slider click sound inject on `class_357.method_25354` with the same boolean contract; slider cursor reproduced via `field_62464` and `class_11876.field_62455/62457`. Verified `class_357` truly overrides the otherwise-empty vanilla method, so the inject is required and correct. |
| A-10 | `mixin/ClickableWidgetSoundMixin.java:17`, `mixin/SliderWidgetMixin.java:31` | `require=0` removed; `defaultRequire=1` now applies so injection failures fail loudly. |
| DEP-03/04 | `fabric.mod.json:17,19` | `minecraft` pinned to exact `"1.21.11"`; `fabric-api` minimum set to `">=0.141.6+1.21.11"`. |

### Intentionally left unchanged (documented, not errors)

- **A-11:** `build.gradle.kts` `from("LICENSE")` with no LICENSE file. Gradle skips the missing source; adding license text is a legal/content decision requiring approval. Bundled font license text remains under resources.
- **A-14:** `ProcessResources.expand(version)` is ineffective because `fabric.mod.json` deliberately holds the literal version `1.3.18`. Harmless; preserves original metadata byte-for-byte.
- **N9:** Hardcoded YouTube client versions need external version data, not a source fix.

## 3. Verification performed (all read-only; NO build)

1. All 50 Java files re-read across four independent agents plus coordinator cross-checks.
2. Cross-file contracts verified: `UiSound.click()/pop()` boolean API matches both mixins; `method_55444(w,h,x,y)` argument order confirmed from 1.21.11 bytecode; `method_76256`, `field_62464`, `field_62455/62457`, `method_18854`, `method_25402(class_11909,boolean)` all confirmed against intermediary mappings and client bytecode.
3. `method_55444` argument order: bytecode shows first two params set width/height (`field_22758`/`field_22759`), last two set x/y — the DockMenu fix is correct.
4. Structural checks: 50/50 Java files bracket-balanced; no `** break`/`** default`/`lblNN`/`this$0`/`<Object>` CFR remnants.
5. All new imports confirmed present; no unimported class references in changed lines.
6. Gradle/build config from the previous phase remains: Kotlin DSL quotes fixed, ModMenu repository added, ModMenu 17.0.1 (`>=1.21.11 <26`), intermediary mappings, `archives_base_name=glassui`.

## 4. Residual risks (require a build or in-game test to resolve)

- **No compilation has been run.** All fixes are source-verified only. Type correctness, mixin refmap generation, and runtime behavior remain unproven.
- MIX-02's `@Shadow` for inherited `method_76256` and MIX-03's shadowed private `field_62464` depend on Mixin superclass resolution; a build/runtime test is needed.
- N5/F10 redaction is heuristic (short opaque tokens without URL/key patterns could survive).
- N3 cannot distinguish two local YouTube uploads with identical title+artist+source without a `StreamData` change (out of scope).
- N6 worst case slightly exceeds 15 s if one HTTP call is already in flight.
- F6 intentionally skips the join on the client thread; a rare in-flight callback could still write to `DATA` just after reset.
- N1/THREAD-05: async setup serializes on one thread; 150 ms save debounce leaves a small crash window.

## 5. Status

**All confirmed errors are fixed in the working tree.** The source is ready for the build approval gate, but **NOT yet build-verified**. No JAR exists, no release was created, and no in-game test was performed.
