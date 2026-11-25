# GlassUI 1.3.18 — Bug Inventory

> Status of audit: **IN PROGRESS** — findings from source analysis and subagents.
> No fixes have been applied. No builds have been run.

## Classification Guide

- **Severity:** CRITICAL / HIGH / MEDIUM / LOW / INFORMATIONAL
- **Status:** CONFIRMED / SUSPECTED / NEEDS MORE INVESTIGATION / NOT A BUG / FIX APPROVED / FIXED / VERIFIED BY SOURCE REVIEW / BLOCKED
- **UI Impact:** Yes (with description) / No

---

## A. Initialization & Entrypoints

| ID | Category | File/Location | Description | Evidence | Severity | Proposed Fix | UI Impact | Status |
|---|---|---|---|---|---|---|---|---|
| INIT-01 | Initialization | `GlassUiClient.java` L88-103 | Keybindings are implemented via raw `GLFW.glfwGetKey()` polling in the client tick, not via Fabric `KeyBinding` registration. Keys are configurable only via `glassui.json`, not via Minecraft Controls screen as documented in the original README. | `GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), cfg.menuKey)` etc. | LOW | INFORMATIONAL only — preserve behavior; do not change unless user requests. | No | INFORMATIONAL |
| INIT-02 | Initialization | `GlassModMenu.java`, `fabric.mod.json` | ModMenu entrypoint declared but no `modmenu` dependency in `fabric.mod.json`. Fabric handles optional entrypoints gracefully, so not a bug. | entrypoint `com.glassui.GlassModMenu` present; no modmenu in `depends` | INFORMATIONAL | No change needed. | No | NOT A BUG |

---

## B. GUI / UI Logic

| ID | Category | File/Location | Description | Evidence | Severity | Proposed Fix | UI Impact | Status |
|---|---|---|---|---|---|---|---|---|
| MUSIC-01 | State feedback | `MusicHub.java` L182-211 | Status text "Starting..." is set synchronously before work is queued on single-thread executor. If another search is running, UI shows misleading status. | `set("Starting...", true)` then `EX.execute(...)` | LOW | Set status inside executor task. | Yes — misleading status text | CONFIRMED |
| GUI-01 | UI / dead branch | `StreamHudConfigScreen.java` L343-360, L379-387; `MusicScreen.java` L216-226, L432, L488 | Spotify provider UI is unreachable. Both `setProvider()` guards reject any value except `"YOUTUBE"` (`if (... \|\| !"YOUTUBE".equals(p)) return;`), and CFG-01 force-resets `musicProvider` to `"YOUTUBE"`. The Spotify Client ID field, login/logout chips, and "Log in" flow never display. | `if (p.equals(cfg.musicProvider) \|\| !"YOUTUBE".equals(p)) return;` | MEDIUM | **NEEDS USER DECISION:** Either allow `"SPOTIFY"` in `setProvider` + fix CFG-01 (enables an existing but hidden platform — may conflict with "no new platform integrations"), or leave disabled. Do NOT silently change. | Yes — Spotify setup/login flow unreachable | CONFIRMED (logic), BLOCKED pending user decision |
| GUI-02 | UI interaction | `GlassAlertScreen.java` L49-52 vs L70-88 | Quit-confirmation "Yes"/"No" buttons are hit-tested at fixed untransformed coordinates but rendered inside a scale matrix (`pop.pos` 0.84→1.0). For ~200 ms during the open animation the visible buttons do not match their clickable rectangles. | `m.scale(this.pop.pos * ...)` then `this.no.method_25394(ctx, mx, my, dt)` | LOW | Render buttons after `m.popMatrix()` in untransformed space, or delay click handling until animation completes. | Yes — transient misaligned clicks | CONFIRMED |
| GUI-03 | UI rendering | `MusicScreen.java` L552-563 | Player-bar status text color computed from `MusicHub.status().isEmpty()` instead of the displayed `st` (which may be a `LocalPlayer.statusLine()` fallback). Fallback lines appear dimmed instead of highlighted. | `int col = ... : (MusicHub.status().isEmpty() ? dim : highlight);` | LOW | Derive color from the actual displayed `st` (or a `fallback` flag). | Yes — cosmetic color of fallback status | CONFIRMED |

---

## C. HUD & Stream Systems

| ID | Category | File/Location | Description | Evidence | Severity | Proposed Fix | UI Impact | Status |
|---|---|---|---|---|---|---|---|---|
| STREAM-01 | State / lifecycle | `StreamData.java` L20, L22-33 | `reset()` never clears `song`. After reconnect/disconnect the Now Playing widget shows the previous track; `MusicToggle.toggle()` takes the wrong branch. | `reset()` clears views/chat/status but not `song` | MEDIUM | Add `song = null;` in `reset()`. | Yes — stale Now Playing, wrong button behavior | CONFIRMED |
| STREAM-02 | Error handling | `YouTubeClient.java` L55-66 | Non-JSON error body (HTML 502/503) is parsed as JSON before status check → misleading parse error instead of HTTP status. | `JsonParser.parseString(body)` before `statusCode()!=200` | LOW | Check status first; parse error body defensively. | Yes — misleading status text | CONFIRMED |
| STREAM-03 | Lifecycle | `TikTokClient.java` L84-145 | `closed.await()` has no timeout; a half-open WebSocket never reconnects and stays "Connecting...". | `closed.await();` | LOW | `closed.await(60, SECONDS)`; abort + reconnect on timeout. | Yes — stuck "Connecting..." | SUSPECTED |
| HUD-01 | Rendering | `StreamHudRenderer.java` L1762-1776 | Marquee uses `(float)System.currentTimeMillis()` (~1.7e12) → float ULP ≈131 s → jumping text instead of smooth scroll. | `float off = (float)System.currentTimeMillis() * 0.04f % span;` | MEDIUM | `(float)(System.currentTimeMillis() % span) * 0.04f` | Yes — jittering island/song/cassette text | CONFIRMED |
| HUD-02 | Lifecycle | `StreamHudRenderer.java` L163-174 | First render `Throwable` sets `warned=true` permanently; all later draws abort → HUD invisible until restart. | `if (warned) break; warned = true;` | MEDIUM | Keep drawing each frame; rate-limit logging. | Yes — HUD blank after one transient error | CONFIRMED |
| HUD-03 | State | `ChatSound.java` L15, L21-37 | `DATA.reset()` zeroes chatCount but `ChatSound.seen` retains old value → chat ping silent after reconnect until new count exceeds old. | `boolean fresh = c > seen;` with stale `seen` | MEDIUM | Add `ChatSound.reset()` called from `StreamData.reset()`. | Yes — no chat ping after reconnect | CONFIRMED |
| HUD-04 | Memory | `TapTracker.java` L26-39, L105-117 | 2000-user cap not enforced when all users are recent: `prune()` removes nothing but `MAP.put()` still grows. | `if (MAP.size()>=2000) prune(); MAP.put(key,t);` with time-only prune | MEDIUM | Force-evict lowest-total entries after time prune. | No (long-session memory growth) | CONFIRMED |
| HUD-05 | GUI logic | `IslandSearch.java` L75-79, L103-107 | Backspace removes one UTF-16 unit from emoji appended via `appendCodePoint` → unpaired surrogate in search text. | `TEXT.appendCodePoint(cp)` / `TEXT.setLength(len-1)` | LOW | Delete one full code point via `Character.charCount`/`codePointBefore`. | Yes — garbled search text | CONFIRMED |
| HUD-06 | GUI logic | `StreamHudRenderer.java` L683-686 | Search panel trims query from index 0 (front), showing the tail instead of the beginning of long queries. | `t = t.substring(1)` in width loop | LOW | Trim from end (or use `Fx.trim`). | Yes — confusing long-query display | CONFIRMED |
| HUD-07 | Cache | `StreamHudRenderer.java` L1335-1341, L1823-1829, L1622-1626 | `WRAPS` and `nameTxt` caches not invalidated when font changes at runtime → stale widths/truncation. | Cache key lacks font index; `StyleScreen` changes font live | LOW | Include font in cache key or clear on font change. | Yes — stale chat/name widths | CONFIRMED |
| THREAD-03 | Threading | `StreamHud.java` L32-53, L67-84 | `stop()` sets flags + `shutdownNow()` but does not await termination; in-flight callbacks write stale data into `DATA` after reset. | No `awaitTermination` / no generation token | MEDIUM | Await termination with short timeout or use generation token. | Yes — stale stats/chat after reconnect | CONFIRMED |
| RES-03 | Resource leak | `IslandSearch.java` L55-87 | GLFW key/char callbacks installed once and never freed; `hooked` never reset on window change. | `glfwSetKeyCallback` / `glfwSetCharCallback`, no free path | LOW | Restore/free callbacks on close or window change. | No direct impact | CONFIRMED |
| RES-04 | Resource leak | `MediaDetector.java` L20, L29-34, L64-99 | Linux `playerctl` process uses local `p`; `stop()` only destroys `this.proc` (Windows). Detector thread not interrupted. | `this.proc` assigned only in `windows()` | MEDIUM | Assign `this.proc = p`; keep thread ref and interrupt in `stop()`. | No direct UI | CONFIRMED |
| RES-05 | Resource leak | `TikTokClient.java` L20, `YouTubeClient.java` L25 | `HttpClient` instances never closed on stop/disconnect (Java 21). | No `close()` call in `stop()` | INFORMATIONAL | Call `close()` in `stop()` or use shared client. | No | CONFIRMED |

---

## D. Music Hub & Platforms

| ID | Category | File/Location | Description | Evidence | Severity | Proposed Fix | UI Impact | Status |
|---|---|---|---|---|---|---|---|---|
| MUSIC-01 | State feedback | `MusicHub.java` L182-211 | See section B. | — | LOW | — | Yes | CONFIRMED |
| SONGREQ-01 | Request tracking | `SongRequests.java` L184-188 | `playingRequest()` identifies the active request by title equality only. Two different tracks with the same title are misidentified. | `c.track().title().equals(np.title())` | LOW | Compare provider + id when available. | Yes — incorrect request state | CONFIRMED (logic), SUSPECTED (impact) |
| LYRICS-01 | Lyrics timing | `Lyrics.java` L88-101 | Estimated playback position clamps per-frame delta to 0.25s, causing lyric drift at low frame rates. | `estSec += Math.min(0.25, delta)` | LOW | Cap only abnormal gaps (>2s) or use absolute time anchor. | Yes — lyric sync drift | SUSPECTED |
| THREAD-02 | Threading | `MusicHub.java` L543-552 | Network HTTP request performed while holding `MusicHub.class` monitor during 401 token refresh. Other threads block for up to 10s. | `synchronized (MusicHub.class) { access=null; return api(...); }` | LOW-MEDIUM | Invalidate token in short synchronized block, retry outside. | Possible brief state lag | CONFIRMED |
| THREAD-03 | Threading | `StreamHud.java` L32-53, L67-84 | See section C — in-flight callbacks write stale data after reset. | — | MEDIUM | — | Yes | CONFIRMED |
| THREAD-04 | Threading | `MediaControl.java` L30-31, L43-62 | Each media command spawns a daemon thread calling `p.waitFor()` with no timeout. PowerShell uses infinite `$t.Wait(-1)`. Repeated clicks accumulate blocked threads/processes. | `p.waitFor();` | MEDIUM | `p.waitFor(10, SECONDS)` + `destroyForcibly()` on timeout. | Yes — buttons appear unresponsive | CONFIRMED |

---

## E. YT-DLP & Process Handling

| ID | Category | File/Location | Description | Evidence | Severity | Proposed Fix | UI Impact | Status |
|---|---|---|---|---|---|---|---|---|
| YTDLP-01 | yt-dlp / hang | `LocalPlayer.java` L298-324 | 90s timeout covers only `p.waitFor()`. `out.join()` and `err.join()` are unbounded. If child inherits stdout/stderr fds, audio thread blocks permanently → no auto-advance. | `String o = out.join().trim();` (no timeout) | HIGH | Use `get(5, SECONDS)` with TimeoutException handling; or `redirectErrorStream(true)` with single timed reader. | Yes — song stuck, status frozen | SUSPECTED (requires fd inheritance) |
| YTDLP-02 | yt-dlp / threading | `LocalPlayer.java` L300-315 | Two blocking `readAllBytes()` futures use common ForkJoinPool without explicit executor; concurrent resolves can starve the pool. Futures abandoned on timeout. | `CompletableFuture.supplyAsync(...)` without executor | MEDIUM | Dedicated bounded executor; always cancel/join in `finally`. | Indirect — latency under concurrency | CONFIRMED (behavior), SUSPECTED (starvation) |
| YTDLP-03 | Security | `LocalPlayer.java` L216, L286, L259-267, L486 | Auto-downloaded ffmpeg/yt-dlp executables have no checksum/signature verification. `HttpClient` uses `followRedirects(ALWAYS)` — HTTPS→HTTP downgrade possible, executing substituted binaries. | `download(".../latest/download/...")` + `works(target)` + `Redirect.ALWAYS` | HIGH | Pin versions + verify SHA-256; restrict redirect schemes. | No direct UI | CONFIRMED (no verification), SUSPECTED (exploitability) |
| YTDLP-04 | yt-dlp / state | `LocalPlayer.java` L548-552, L562-564 | `resolveViaYtdlp()` throws `InterruptedException`; outer catch `return`s before `onEnd()`, leaving stale `cur` and `StreamHud.DATA.song`. | `catch (InterruptedException e) { return; }` | LOW | Catch interrupt, restore flag, still call `onEnd`/`clearSong`. | Yes (rare) — stale Now Playing | SUSPECTED |
| YTDLP-05 | ffmpeg / hang | `LocalPlayer.java` L637, L684 | ffmpeg uses `-reconnect_at_eof 1` with no max retries; `readNBytes()` blocks indefinitely on stalled stream. | `in.readNBytes(...)` with no timeout | MEDIUM | Add playback watchdog or cap reconnect attempts. | Yes — frozen playback | SUSPECTED |
| RES-01 | Resource leak | `LocalPlayer.java` L643, L715 | ffmpeg process not destroyed if `waitFor(5s)` returns false; `this.proc` overwritten on resume → orphaned process. | `p.waitFor(5, SECONDS);` no destroy on false | MEDIUM | `if (!p.waitFor(...)) p.destroyForcibly();` | Indirect — stray audio/resources | SUSPECTED |
| RES-02 | Threading | `LocalPlayer.java` L649-668, L719 | stderr reader thread joined only on failure path; on success left running. Minor (daemon). | `er.join(500L)` only when `!ok` | LOW | Always join in `finally`. | No | CONFIRMED |

---

## F. Threading

| ID | Category | File/Location | Description | Evidence | Severity | Proposed Fix | UI Impact | Status |
|---|---|---|---|---|---|---|---|---|
| THREAD-01 | Threading | `LocalPlayer.java` L56-60, L76-164 | `probe()` runs on single-thread `EX`, each `works()` waits up to 20s per candidate (~13 candidates). Play/pause/next commands also use `EX` → controls freeze during probing. | `waitFor(20L, SECONDS)` × ~13 candidates on single executor | MEDIUM | Reduce timeout to 2-5s; separate probe executor; pre-check file existence. | Yes — laggy media buttons | CONFIRMED (blocking), SUSPECTED (magnitude) |
| THREAD-02 | Threading | `MusicHub.java` L543-552 | See section D. | — | LOW-MEDIUM | — | Possible lag | CONFIRMED |

---

## G. Resources

| ID | Category | File/Location | Description | Evidence | Severity | Proposed Fix | UI Impact | Status |
|---|---|---|---|---|---|---|---|---|
| RES-03 | Resources | `IslandSearch.java` L55-87 | See section C — GLFW callbacks never freed. | — | LOW | — | No | CONFIRMED |
| RES-04 | Resources | `MediaDetector.java` L20, L29-34, L64-99 | See section C — Linux playerctl not killed. | — | MEDIUM | — | No direct UI | CONFIRMED |
| RES-05 | Resources | `TikTokClient.java` L20, `YouTubeClient.java` L25 | See section C — HTTP clients never closed. | — | INFORMATIONAL | — | No | CONFIRMED |
| INFO-RES | Resources | `assets/glassui/font/*.json`, `FONTS-LICENSE.txt` | Font definitions verified correct: identifiers resolve, binaries present, licenses documented. | Each JSON has `ttf` provider + `minecraft:default` fallback | INFORMATIONAL | None. | No | NOT A BUG |

---

## H. Mixins & Initialization (final agent — NOT FIXED, pending approval)

| ID | Category | File/Location | Description | Evidence | Severity | Proposed Fix | UI Impact | Status |
|---|---|---|---|---|---|---|---|---|
| MIX-01 | Mixins / audio | `ClickableWidgetSoundMixin.java` L17-24; `UiSound.java` L16-35 | Custom click sound is cancelled unconditionally when `uiSound=true`, but `UiSound.pop()` returns silently on the main menu (no player entity), volume 0, or inside a 35 ms debounce. Vanilla sound is suppressed with no replacement → silent clicks. | `ci.cancel()` unconditional after `UiSound.click()`; `if (mc == null || mc.field_1724 == null) return;` | HIGH | Make `UiSound.pop()` return a boolean indicating whether a sound played; only `ci.cancel()` when true (or remove the player requirement). | Yes — no click feedback on menus | CONFIRMED — NOT FIXED (pending approval) |
| MIX-02 | Mixins / cursor | `PressableWidgetMixin.java` L22-46; `SliderWidgetMixin.java` L25-50 | `renderWidget` HEAD injections cancel vanilla rendering but never reproduce vanilla cursor handling (`handleCursor` / `requestCursor`). Hovered glass buttons/sliders never change the mouse pointer. | `ci.cancel()` with no cursor call | LOW | Call `self.handleCursor(ctx)` / `ctx.requestCursor(...)` before cancel, respecting active/hover state. | Yes — no pointer cursor | CONFIRMED — NOT FIXED (pending approval) |
| MIX-03 | Mixins / audio | `ClickableWidgetSoundMixin.java` L15 | Only `class_339` (AbstractWidget) is targeted; `class_357` (AbstractSliderButton) overrides `playDownSound`, so sliders keep the vanilla click sound. | `@Mixin(class_339.class)` only | LOW | Add a second mixin for `class_357` (or target the shared static sound helper) if uniform audio is intended. | Yes — inconsistent audio | CONFIRMED — NOT FIXED (pending approval) |
| MIX-04 | Mixins / diagnostics | `ClickableWidgetSoundMixin.java` L17; `SliderWidgetMixin.java` L25 | `require=0` on injections silently disables them on mapping mismatch, overriding `defaultRequire:1`. Likely intentional version tolerance, but gives no diagnostic. | `@Inject(..., require=0)` | INFORMATIONAL | Log a one-time warning or remove `require=0`. | Indirect | CONFIRMED (behavior, likely intentional) — NOT FIXED |
| THREAD-05 | Threading / config I/O | `GlassUiClient.java` L123-126; `MusicHub.java` commitVolume | `commitVolume()` calls `GlassConfig.save()` synchronously from `END_CLIENT_TICK` on volume-key release → blocking disk I/O on the render thread. | `if (volChanged) MusicHub.commitVolume();` | LOW | Schedule the save off-thread or debounce/coalesce writes. | Yes — possible brief stutter | CONFIRMED — NOT FIXED (pending approval) |
| DEP-03 | Dependencies | `fabric.mod.json` L19; `GlassUiClient.java` L21-22 | `"fabric-api": "*"` permits an aggregate API build lacking `HudElementRegistry` / `VanillaHudElements`, causing `NoClassDefFoundError` at entrypoint. Real-world exposure limited by `minecraft ~1.21.11`. | Wildcard dependency vs direct HUD API usage | MEDIUM (SUSPECTED) | Pin a minimum Fabric API version containing the HUD element registry. | Yes if triggered — mod fails to init | SUSPECTED — NOT FIXED (pending approval) |
| DEP-04 | Dependencies | `fabric.mod.json` L17; `glassui.mixins.json` L2, L11-13 | `minecraft: ~1.21.11` allows later 1.21.x patches, but all four mixins are hard-required and depend on exact intermediary IDs; an upstream rename would crash at startup. No break on 1.21.11 (verified). | Hard-required mixins + exact IDs | LOW (SUSPECTED) | Narrow the Minecraft constraint or use `require=0` + soft-failing cosmetic mixins. | Yes only on untested future version | SUSPECTED — NOT FIXED (pending approval) |

---

## I. Configuration

| ID | Category | File/Location | Description | Evidence | Severity | Proposed Fix | UI Impact | Status |
|---|---|---|---|---|---|---|---|---|
| CFG-01 | Config | `GlassConfig.java` L154 | `musicProvider` is forcibly reset to `"YOUTUBE"` on every `load()`, discarding any saved user value. | `GlassConfig.inst.musicProvider = "YOUTUBE";` | MEDIUM | Remove the forced assignment or add null-only fallback. | Yes — user setting ignored | CONFIRMED |
| CFG-02 | Config | `GlassConfig.java` L205-212 | `save()` uses plain `Files.writeString()` without temp-file + atomic move. Crash during write can corrupt config. | `Files.writeString(path, ...)` | LOW | Write to temp file then `Files.move(... ATOMIC_MOVE)`. | No (data loss risk) | CONFIRMED |

---

## J. Dependencies & Compatibility

| ID | Category | File/Location | Description | Evidence | Severity | Proposed Fix | UI Impact | Status |
|---|---|---|---|---|---|---|---|---|
| DEP-01 | Compatibility | `fabric.mod.json`, manifest | Manifest records Loader 0.18.4 (build-time); target is Loader 0.19.5. `>=0.18.1` range permits it. No code incompatibility evident. | `Fabric-Loader-Version: 0.18.4` vs `depends: >=0.18.1` | INFORMATIONAL | No change required — runtime-compatible. | No | NOT A BUG |
| DEP-02 | Dependencies | `fabric.mod.json` | `fabric-api: "*"` is overly permissive; mod uses `HudElementRegistry` which requires a recent Fabric API. | `"fabric-api": "*"` | LOW | Consider `>=0.100.0` or similar minimum. | No | INFORMATIONAL |

---

## K. Security & Stability

| ID | Category | File/Location | Description | Evidence | Severity | Proposed Fix | UI Impact | Status |
|---|---|---|---|---|---|---|---|---|
| SEC-01 | Security | `YouTubeClient.java` L86, L130, L111 | YouTube API key is included in request URLs. Exception messages logged at L111 may contain the URL (including key) on connection errors. | `...&key=` + key in URL; `LOGGER.warn("...", e.getMessage())` | LOW-MEDIUM | Sanitize log messages; avoid logging full exception messages that may contain URLs. | No | SUSPECTED |
| SEC-02 | Security | `LocalPlayer.java` L216, L286 | Executables downloaded over network and executed without verification (see YTDLP-03). | — | HIGH | See YTDLP-03. | No | CONFIRMED |

---

## Confirmed Safe (explicitly checked)

- **No shell injection**: All `ProcessBuilder` calls use argument arrays, never shell strings.
- **Video ID validation**: `[A-Za-z0-9_-]{11}` before process arguments.
- **No Zip-Slip**: ffmpeg extraction writes to fixed path.
- **PKCE Spotify auth**: Random state + verifier, localhost-only callback.
- **Class file version**: 65.0 = Java 21, matches target.
- **Author placeholder `"You"`**: Declares MIT license; fonts have separate OFL license.

---

*Inventory finalized after all four analysis agents reported. Approved fixes from `REPAIR_PLAN.md` Groups 1–6 were applied; items marked "NOT FIXED (pending approval)" in section H were discovered after the approval gate and remain unfixed.*
