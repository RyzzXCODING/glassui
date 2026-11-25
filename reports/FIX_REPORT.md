# GlassUI 1.3.18 — Fix Report

> Fixes applied after explicit user approval of `REPAIR_PLAN.md` Groups 1–6.
> **No build, compilation, remap, packaging, commit, or push was performed.**
> All listed bugs were verified by static source inspection only.

## Applied Fixes

| Bug ID | Root Cause | Fix Implemented | Files Modified | Why Necessary | UI Impact | Source Validation | Remaining Risks |
|---|---|---|---|---|---|---|---|
| YTDLP-01 | `out.join()`/`err.join()` had no timeout; reader fds left open could block the audio thread forever | Added `readFuture()` using `get(5, SECONDS)` with `TimeoutException`→`IOException`; all waits are bounded | `music/LocalPlayer.java` | Prevents permanent playback hangs | No (timing/error path only) | Verified `readFuture` present and no `.join()` remains in resolver | A 5 s slow reader now reports an error instead of waiting |
| YTDLP-02 | Two blocking readers used the common ForkJoinPool and were abandoned on timeout | Added dedicated 2-thread daemon `READ_EX`; both futures are created with it and cancelled in `finally` | `music/LocalPlayer.java` | Avoids common-pool starvation and leaked reader tasks | No | Verified `READ_EX` and `finally { out.cancel(true); err.cancel(true); ... }` | Cancelling a completed future is harmless; in-flight readers are interrupted on error |
| RES-01 | ffmpeg process not destroyed when `waitFor(5s)` timed out | `if (!p.waitFor(5, SECONDS)) p.destroyForcibly();` | `music/LocalPlayer.java` | Prevents orphaned ffmpeg processes on resume | No | Verified at end of `playStream()` | None known |
| THREAD-01 (probe) | `probe()` blocked the single-thread player executor with up to 20 s × 13 candidate probes | Explicit paths pre-checked with `Files.isExecutable`; per-candidate timeout reduced to 3 s | `music/LocalPlayer.java` | Keeps media controls responsive during probing | No | Verified `works()` change | Very slow-starting binaries at explicit paths may be skipped if not executable-flagged |
| THREAD-02 | HTTP retry executed while holding the `MusicHub.class` monitor | Synchronized block now only clears `access`; retry call moved outside the monitor | `music/MusicHub.java` | Removes up to 10 s lock contention | No (brief state lag only) | Verified retry structure and single-retry semantics | Concurrent token refresh is benign; `token()` itself still performs I/O under lock (pre-existing, out of scope) |
| CFG-01 | `musicProvider` force-reset to `"YOUTUBE"` on every load | Replaced with a null/blank-only fallback | `GlassConfig.java` | Saved provider value is now respected | No (restores intended setting) | Verified guarded assignment | Spotify UI (GUI-01) remains unreachable — separate decision |
| CFG-02 | `save()` used in-place `Files.writeString()` | Writes to `*.tmp` then `Files.move(..., ATOMIC_MOVE, REPLACE_EXISTING)` with non-atomic fallback | `GlassConfig.java` | Prevents truncated/corrupt config after interruption | No | Verified temp path + both move branches | A stale `.tmp` file may remain after a crash |
| MUSIC-01 | `"Starting..."` status set before work was queued | Status update moved inside the executor task in `play()`/`playSingle()` | `music/MusicHub.java` | Status now reflects the task actually running | Yes — accurate status text timing | Verified both lambdas | `playQuery()`/`playRandom()` retain their original pattern (outside approved scope) |
| SONGREQ-01 | Active request identified by title alone | Added provider agreement and id-shape agreement, with title-only fallback | `music/SongRequests.java` | Same-title tracks are no longer misidentified across providers | No | Verified `playingRequest()` | Two same-title tracks from the same external app remain indistinguishable |
| LYRICS-01 | Every frame delta capped at 0.25 s → lyric drift at low FPS | Cap only abnormal gaps > 2 s; otherwise add the full delta | `music/Lyrics.java` | Removes normal playback drift | No (timing accuracy only) | Verified `delta > 2.0 ? 0.25 : delta` | A single >2 s stall still advances only 0.25 s |
| SEC-01 | Exception messages containing key-bearing URLs were logged | Logs now report `e.getClass().getSimpleName()` instead of raw messages; request URLs unchanged | `hud/YouTubeClient.java`, `hud/TikTokClient.java` | Prevents key leakage into logs | No | Verified all `LOGGER` exception calls | `d.status` still derives from exception text (UI state, not a log) |
| GUI-02 | Alert buttons hit-tested untransformed but rendered inside a scale matrix | Moved button rendering after `m.popMatrix()` | `ui/GlassAlertScreen.java` | Clickable rects now match drawn rects | Yes — buttons no longer scale during the ~200 ms animation (intentional trade-off) | Verified render call placement | None known |
| GUI-03 | Status color computed from a fresh `MusicHub.status()` instead of displayed `st` | Color now derives from `((String)st).isEmpty()` | `ui/MusicScreen.java` | Fallback statuses are no longer wrongly dimmed | Yes — fallback lines now use the highlight color | Verified color expression | None known |
| STREAM-01 | `StreamData.reset()` never cleared `song` | Added `song = null;` | `hud/StreamData.java` | Prevents stale Now Playing and wrong media-button branch | Yes — stale track cleared on disconnect | Verified `reset()` | Subtitle caches are unchanged (not identified as stale) |
| HUD-01 | `(float)System.currentTimeMillis()` lost precision → marquee jumps | Modulo computed in `long` domain before casting | `hud/StreamHudRenderer.java` | Restores smooth 40 px/s scroll | Yes — smoother marquee text | Verified `marquee()` | None known |
| HUD-02 | First render exception latched `warned=true`, permanently blanking the HUD | Removed latch; each frame retries `draw()`; errors rate-limited to one per 10 s | `hud/StreamHudRenderer.java` | A transient error no longer disables the HUD forever | Yes — HUD recovers automatically | Verified `render()` and `lastWarnMs` | Repeated failures log every 10 s (bounded) |
| HUD-03 | `ChatSound.seen` survived `chatCount` reset | Added `ChatSound.reset()` and called it from `StreamData.reset()` | `hud/ChatSound.java`, `hud/StreamData.java` | Restores chat pings after reconnect | Yes (audible) | Verified reset path | None known |
| THREAD-03 | `StreamHud.stop()` did not await background writers before `DATA.reset()` | `awaitTermination(2, SECONDS)` after all producers stop, with interrupt re-assertion | `hud/StreamHud.java` | Prevents stale callbacks writing into fresh state | Yes — stale stats/chat no longer appear | Verified `stop()` | `synchronized stop()` may hold the lock up to 2 s |
| THREAD-04 | Media command threads used unbounded `p.waitFor()` | `p.waitFor(10, SECONDS)` + `destroyForcibly()` on timeout | `hud/MediaControl.java` | Prevents accumulated blocked threads/processes | Yes — buttons stop hanging | Verified `run()` | PowerShell script still contains `$t.Wait(-1)`, but the Java side now bounds and kills it |
| RES-04 | Linux `playerctl` process stored in a local, so `stop()` could not kill it; worker thread not interrupted | Assign `this.proc = p` in `linux()`; keep thread reference and interrupt it in `stop()`; handle sleep interruption without extra delay | `hud/MediaDetector.java` | No stray process/thread after disconnect | No | Verified `start()`/`stop()`/`linux()` | Restoring `stopped` on a reused detector requires a fresh instance (as `StreamHud` already does) |
| STREAM-02 | YouTube error bodies parsed as JSON before HTTP status check | Status checked first; error body parsed defensively with `"HTTP <code>"` fallback | `hud/YouTubeClient.java` | Accurate status messages on HTML/empty errors | Yes — status text accuracy | Verified `get()` | None known |
| HUD-04 | 2000-user cap not enforced when all entries were recent | Added `evictToCap()` force-evicting lowest-total (then oldest) entries | `hud/TapTracker.java` | Prevents unbounded map growth | No (long-session memory) | Verified `record()` and `evictToCap()` | Active users may be dropped when the cap is reached |
| HUD-05 | Backspace removed one UTF-16 unit from an emoji code point | Delete one full code point via `Character.charCount`/`codePointBefore` | `hud/IslandSearch.java` | No unpaired surrogates in the search query | Yes — correct emoji deletion | Verified `onKey()` case 259 | None known |
| HUD-06 | Search display trimmed from the front, showing the query tail | Trim from the end instead | `hud/StreamHudRenderer.java` | Long queries display from the beginning | Yes — readable long queries | Verified `searchPanel()` | No ellipsis indicator was added (preserves original styling) |
| HUD-07 | `WRAPS`/`nameTxt` caches ignored runtime font changes | Font index now tracked; caches cleared/recomputed when it changes | `hud/StreamHudRenderer.java` | Chat bubble widths and names use the current font | Yes — correct widths after font change | Verified `wrapFont`/`nameFont` | None known |
| STREAM-03 | `closed.await()` unbounded on a half-open WebSocket | Bounded `await(60, SECONDS)` + `ws.abort()` on timeout | `hud/TikTokClient.java` | Stuck "Connecting..." sessions now reconnect | Yes — link state recovers | Verified `session()` | A late callback may fire on the abandoned socket (harmless) |
| RES-03 | GLFW callbacks installed once and never restored/freed on window change | Track window handle and saved callbacks; restore/free on window change, re-hook new window | `hud/IslandSearch.java` | No stale callbacks on a replaced GLFW window | No direct | Verified `hook()` | If the old window was destroyed, restore is skipped safely; third-party callbacks are left intact |
| RES-05 | YouTube `HttpClient` never closed | `http.close()` in `YouTubeClient.stop()` | `hud/YouTubeClient.java` | Releases selector/connection resources | No | Verified `stop()` | TikTok's `CLIENT` is `static final` and reused across restarts, so it was intentionally **not** closed; `close()` may briefly wait for in-flight requests (bounded by their 10 s timeout) |

## Second-Pass Compile-Blocker Repairs (user-approved)

A mandatory second-pass review found 10 decompiler artifacts that would have prevented compilation. These were fixed after the user's explicit approval *"fix the error there then confirm what has been done"*:

| ID | File | Change | UI / behavior impact |
|---|---|---|---|
| BP-01/02 | `music/Lyrics.java` | `List<Object>`→`List<Line>`, `Object err`→`String err` | None — type correction only |
| BP-03/04/05 | `music/LocalPlayer.java` | `ArrayList<Object>`→`ArrayList<String>` (×2), `Object error`→`String error` | None — type correction only |
| BP-06 | `hud/StreamHudRenderer.java` | `frame()` rewritten from CFR pseudocode with typed locals and a normal switch; draw order, matrix pop, hover coordinates preserved; added null guard for `MinecraftClient` | None intended; restores compilability and removes NPE risk |
| BP-07 | `hud/ChatSound.java` | `pitch` hoisted out of switch cases (same 1.5/1.35/1.7 values) | None — scope correction only |
| BP-08 | `hud/TikTokClient.java` | Removed bogus self-assigned `this$0` field; 8 uses now `TikTokClient.this.` | Restores functional WebSocket callbacks (previously null outer instance) |
| BP-09 | `hud/StreamHudRenderer.java` | Uninitialized `rowIcon` removed; computed `shape` drawn in all branches | Fixes null-icon NPE / wrong glyph in search rows |
| BP-10 | `ui/StreamHudConfigScreen.java` | `col`/`st` definitely assigned; two unused decompiler locals removed | Fixes compile error and wrong status text/color |

Full detail: `reports/SECOND_PASS_AUDIT.md`. **No build was run; all of these need build verification.**

## Deliberately Not Fixed

- **GUI-01** (Spotify UI unreachable) — enabling it would expose a currently-disabled platform; requires user decision.
- **YTDLP-03 / SEC-02** (unverified downloaded executables, HTTPS→HTTP redirects) — requires user decision on pinned versions and checksums.
- **YTDLP-04, YTDLP-05, RES-02** — left as documented in `REPAIR_PLAN.md`.
- **INIT-01, INIT-02, DEP-01, DEP-02** — informational / design choices.
- **MIX-01..04, THREAD-05, DEP-03, DEP-04** (final agent findings) — discovered after the approval gate; see `REPAIR_PLAN.md` Group 7. **NOT fixed.**

## Confirmation

- No UI redesign, layout, color, theme, feature, keybinding, or platform changes were made.
- No Minecraft member names were renamed.
- No build, compile, remap, assemble, package, commit, upload, or push was performed.
- No JAR or build artifact exists in the source tree.
