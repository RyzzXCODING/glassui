# GlassUI 1.3.18 — Second-Pass Audit & Compile-Blocker Repair Report

> Triggered by the master prompt's Phase 1–3 requirement to re-review every source file before build.
> **No build, compile, remap, package, or JAR generation was performed.** Verification is source inspection only.
> The user explicitly approved fixing the errors found: *"fix the error there then confirm what has been done"*.

## 1. Scope and Coverage

| Category | Files | Coverage |
|---|---|---|
| Core (`com/glassui`) | 3 | 100% read |
| UI (`com/glassui/ui`) | 20 | 100% read |
| HUD (`com/glassui/hud`) | 16 | 100% read |
| Music (`com/glassui/music`) | 7 | 100% read |
| Mixins (`com/glassui/mixin`) | 4 | 100% read |
| Gradle / wrapper / metadata / resources | all | 100% read |
| **Total Java files** | **50** | **100%** |

Four independent review agents covered disjoint file groups; all findings were cross-checked by direct source reads before any change.

## 2. Compile-Blocking Reconstruction Errors (FOUND → FIXED)

These were decompiler artifacts left by CFR 0.152 that would make `javac` fail. The earlier "source-only validation" checked brackets and imports but **did not type-check**, so it missed them.

| ID | File:line (original) | Error | Fix applied | Verification |
|---|---|---|---|---|
| BP-01 | `music/Lyrics.java:123` | `List<Object> found` assigned `List<Line>` from `parse()` | Changed to `List<Line>` | Source read |
| BP-02 | `music/Lyrics.java:124,164` | `Object err` assigned to `String error` | Changed to `String err` | Source read |
| BP-03 | `music/LocalPlayer.java:96,125` | `ArrayList<Object> c` iterated as `String` | Changed to `ArrayList<String>` | Source read |
| BP-04 | `music/LocalPlayer.java:130,150` | `ArrayList<Object> y` iterated as `String` | Changed to `ArrayList<String>` | Source read |
| BP-05 | `music/LocalPlayer.java:565,624` | `Object error` passed to `onEnd(..., String)` | Changed to `String error` | Source read |
| BP-06 | `hud/StreamHudRenderer.java:404-540` | `frame()` emitted as pseudocode: undeclared locals `sw, sh, m, wc, e, sz, r, lx, ly, mc, mx, my`; invalid tokens `** break;` ×9 and `** default:`; stray labels `lbl40..lbl77` | Rewrote method with explicit typed declarations and a normal switch; preserved draw order, `chatMaxH`, `lyrFit`, dock placement, local hover coordinates, and `try/finally { m.popMatrix(); }`. Added a null guard for `class_310.method_1551()` consistent with other render paths. | Source read + pseudocode-token grep clean |
| BP-07 | `hud/ChatSound.java:51-67` | `float pitch` declared inside switch cases, used at line 67 out of scope | Declared `float pitch = 1.7f` before the switch; cases now assign (same values 1.5/1.35/1.7) | Source read |
| BP-08 | `hud/TikTokClient.java:89-142` | Anonymous listener declared and self-assigned a `this$0` field (`this.this$0 = this$0`), leaving the outer reference null / colliding with javac's synthetic field | Removed the bogus field; all 8 uses changed to the valid `TikTokClient.this.` qualifier | Source read + grep clean |
| BP-09 | `hud/StreamHudRenderer.java:693-704` | `IconShapes.Shape rowIcon` assigned only in one ternary branch → "might not have been initialized"; also NPE/wrong glyph for most rows | Removed `rowIcon`; the computed `shape` is now drawn in all branches (identical glyph choices for each condition) | Source read |
| BP-10 | `ui/StreamHudConfigScreen.java:568-574` | `col` and `st` assigned only in ternary false branches → definite-assignment error and wrong status text/color when status is non-empty | Declared `st`/`col` directly from the same conditions; removed two unused decompiler locals (`string`, `n`). Rendered text/color values are unchanged from the intended branches. | Source read |

**Post-fix static checks:** 50/50 files bracket-balanced; no `** break`/`** default`/`lblNN` tokens; no `this$0`; no `<Object>`/`Object error` remnants; all fix sites re-read.

## 2b. Build Configuration Blockers (FOUND → FIXED, user-approved)

The final audit agent checked Gradle files, mixins and resources and found two hard build blockers plus version/config inconsistencies. All were verified independently and fixed:

| ID | Location | Problem | Fix | Verification |
|---|---|---|---|---|
| A-01 | `settings.gradle.kts:5-6` | Groovy single-quoted strings (`name = 'Fabric'`, `url = '...'`) are invalid Kotlin DSL `Char` literals → settings evaluation fails | Changed to double-quoted strings and `uri(...)` | File re-read; no single quotes remain in any `.kts` |
| A-02 | `build.gradle.kts:15-18,31` | ModMenu artifact is not on Maven Central or Fabric Maven; repository missing | Added `https://maven.terraformersmc.com/releases/` to `repositories` | Artifact POM/JAR verified at that URL (HTTP 200) |
| A-03 | `gradle.properties` / `build.gradle.kts` | ModMenu `15.0.0` targets MC `~1.21.6`, not 1.21.11 | Added `modmenu_version=17.0.1` (its metadata declares `>=1.21.11 <26`) and referenced it from the build script | Downloaded JAR metadata inspected; `com/terraformersmc/modmenu/api/ModMenuApi.class` present |
| A-12 | `gradle.properties:18` | Stale comment claiming official Mojang mappings | Corrected to intermediary mappings | File re-read |
| A-13 | `build.gradle.kts` | `archives_base_name=glassui` was never applied; output would be `GlassUI-1.3.18.jar` | Added `base { archivesName.set(providers.gradleProperty("archives_base_name")) }` | File re-read; no build run |
| A-11 | `build.gradle.kts:53-56` | `from("LICENSE")` but no LICENSE file | **Left unchanged** — Gradle skips a missing source; adding license text would be a new content decision requiring approval | Documented, not fixed |
| A-14 | `build.gradle.kts:45-51` | `expand(version)` ineffective (literal version in `fabric.mod.json`) | **Left unchanged** — harmless at 1.3.18; documented | Documented, not fixed |

Mixin targets, mixin config, all five fonts and all Fabric metadata were verified as plausible/identical to the original JAR. `require=0` on two injectors (A-10) is documented, not changed.

## 3. Previous Bug-Fix Verification (all 28 approved IDs)

Every approved fix from `REPAIR_PLAN.md` Groups 1–6 was re-read by independent agents.

- **FIXED AND VERIFIED BY SOURCE REVIEW:** CFG-01, CFG-02, GUI-02, GUI-03, STREAM-01/02/03, HUD-01/02/03/04/05/06/07, THREAD-01/02/03/04, RES-01/03/04/05, SEC-01, YTDLP-01/02, MUSIC-01, SONGREQ-01, LYRICS-01.
- **FIXED BUT NEEDS BUILD VERIFICATION:** all of the above — no compile has been run, so none has build confirmation.
- No regression was introduced by the approved fixes. BP-01..BP-10 were pre-existing decompiler artifacts, not regressions.

## 4. New Issues Found — NOT FIXED (require separate approval)

### Runtime / logic (not build blockers)

| ID | Severity | File:line | Issue |
|---|---|---|---|
| N1 | MEDIUM | `music/MusicHub.java:366-370`, `music/LocalPlayer.java:272-280` | First-play `LocalPlayer.setup()` (HTTP download, up to 10 min timeout) runs synchronously on the single Music Hub executor, stalling all search/play UI operations. |
| N2 | MEDIUM | `music/MusicHub.java:412-417` | Spotify OAuth callback reads only the first HTTP request line; a `/favicon.ico` request first aborts login. |
| N3 | MEDIUM | `music/SongRequests.java:203-209` | Id check is inert for local playback (`appId` = `glassui-local`, 13 chars); same-title YouTube videos can still collide. |
| N4 | MEDIUM | `music/LocalPlayer.java:596-598` | `InterruptedException` path skips `onEnd()`, leaving `cur`/`DATA.song` stale and controls dead. |
| M-1 | MEDIUM | `ui/StreamHudConfigScreen.java:576-583` | `NowPlaying.title()`/`artist()` can be null (see `MusicScreen.java:542-544` guards); NPE on tab 3. |
| M-2 | MEDIUM | `ui/StyleScreen.java:103-119`, `ui/MusicScreen.java:443-483`, `ui/GlassButtonRenderer.java:39-56`, `ui/DockMenu.java:81-120` | During open/close/hover scaling animations, drawn widgets and click hitboxes disagree (GUI-02 was fixed only for `GlassAlertScreen`). |
| M-3 | MEDIUM | `ui/MusicScreen.java:245-260,394-397,478-481` | Close is deferred to `render()`; widgets remain clickable during the closing animation. |
| F4 | HIGH | `hud/StreamHudRenderer.java:693-704` | Was null/wrong icon — **addressed by BP-09** (same fix). |
| F5 | MEDIUM | `hud/StreamHudRenderer.java:214,308,415` | Editor chat bounding box uses `chatMaxH = 1.0E9f` vs live clamped height; outline/hit region can be too tall. |
| F6 | MEDIUM | `hud/StreamHud.java:68-92` | `synchronized stop()` awaits up to 2 s on the client thread (frame hitch). |
| F7 | LOW | `hud/MediaControl.java:49,58`; `hud/MediaDetector.java:74,92` | Process stdin pipes are not closed. |
| F8 | LOW | `hud/MediaDetector.java:37-39,92-93` | `stop()` creation race can miss a just-started process. |
| F9 | LOW | `hud/StreamHudRenderer.java:633,562,574,662-668` | `volDrag` can remain latched if the island is hidden mid-drag. |
| F10 | LOW | `hud/TikTokClient.java:66,114,116`; `hud/YouTubeClient.java:116` | Raw upstream payload / exception-derived text reaches HUD status. Exception *logging* is sanitized (SEC-01 holds). |
| F11 | LOW | `hud/GlassHotbar.java:30-31`, `hud/CombatFx.java:115-116`, `hud/StreamHudRenderer.java:179-181,205-212,1589-1590` | Unguarded `class_310.method_1551()` dereferences outside `render()`'s catch. |
| N5 | MEDIUM/LOW | `music/LocalPlayer.java:337,437,457,756-759` | Raw process stderr (yt-dlp/ffmpeg) logged verbatim; can contain signed short-lived media URLs. No API keys/tokens logged. |
| N6–N12 | LOW | `music/Lyrics.java`, `SongRequests.java`, `Suggest.java`, `LocalPlayer.java`, `YouTubeStream.java`, `Launcher.java` | Lyrics worker lag, rate-limit map bound, dead constants, pinned YouTube client versions, no progressive-format candidate, PATH pre-check gap, `Launcher.open()` null guard. |
| L1–L6 | LOW | `ui/HudEditorScreen.java`, `GlassConfig.java`, `GlassAlertScreen.java`, `DockMenu.java`, `GlassUiClient.java` | Decorative-bar overlap in editor, stale config on reload failure, `onConfirm` not guarded/no auto-close, silent Mods-tile reflection failure, unreachable Spotify UI (GUI-01), held hotkey can reopen screen. |

**Explicitly out of scope / already awaiting decision:** GUI-01, YTDLP-03/SEC-02 (binary integrity), MIX-01..04, THREAD-05, DEP-03/04, and config/documentation inconsistencies (`gradle.properties:18` stale "Mojang mappings" comment; `build.gradle.kts:53-57` `LICENSE` file absent; `archives_base_name` unused → output JAR would be `GlassUI-1.3.18.jar`).

## 5. Dependency / Compatibility Verification (static, no build)

| Item | Result |
|---|---|
| Minecraft 1.21.11 | Maven artifact URL resolves (HTTP 200) |
| Fabric Loader 0.19.5 | Maven artifact URL resolves (HTTP 200) |
| Fabric API 0.141.6+1.21.11 | Maven POM resolves (HTTP 200) |
| Loom 1.19.3 plugin marker + `fabric-loom` | Both resolve via Fabric maven (HTTP 200); `settings.gradle.kts` includes Fabric maven in `pluginManagement` |
| ModMenu 15.0.0 | Maven POM resolves (HTTP 200) |
| Gradle 9.2.1 wrapper distribution | URL resolves (HTTP 200) |
| Intermediary mappings `1.21.11:v2` | Maven artifact URL resolves (HTTP 200); matches decompiled member names |
| Java 21 features | No post-21 syntax found |
| Mixin config | 4 client mixins, all classes present, unchanged from original JAR |
| Fabric metadata / fonts | Byte-identical to original JAR |

**Caveat:** URL resolution is not dependency resolution or compilation. No build verification exists.

## 6. Readiness Status

**NOT READY FOR BUILD** — until build verification is explicitly approved and performed. Source-level blockers BP-01..BP-10 are fixed, but:

1. No compilation has been run; the type fixes and the Gradle syntax fix need build confirmation.
2. Unfixed HIGH/MEDIUM runtime issues remain (N1–N4, M-1–M-3, F5/F6, N5, A-04 MIX-01) and require a user decision.
3. Two low-impact documentation items remain (A-11 missing LICENSE file, A-14 ineffective version expansion).

No in-game test was performed. No JAR was produced or uploaded. No release was created.
