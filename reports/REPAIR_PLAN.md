# GlassUI 1.3.18 — Proposed Repair Plan

> **STATUS: AWAITING USER APPROVAL — NO FIXES APPLIED**
>
> This plan groups confirmed and high-confidence suspected bugs by category.
> Only bugs explicitly approved by the user will be fixed.
> UI appearance, themes, layouts, and all unrelated behavior will be preserved.

---

## Group 1: YT-DLP & Process Handling (highest priority)

### RP-01 — Bug ID: YTDLP-01 (HIGH, SUSPECTED)
- **Root cause:** `resolveViaYtdlp()` waits 90s for process exit but then calls `out.join()` / `err.join()` with no timeout. If stdout/stderr fds stay open after process exit, the audio thread blocks forever.
- **Affected files:** `com/glassui/music/LocalPlayer.java`
- **Proposed changes:** Replace `CompletableFuture.supplyAsync` + unbounded `join()` with `get(5, TimeUnit.SECONDS)` and `TimeoutException` handling; on timeout call `p.destroyForcibly()` and throw `IOException`. Consider `redirectErrorStream(true)` with a single reader.
- **Risk:** Low — changes only the error/timeout path; normal yt-dlp resolution unchanged.
- **UI changes necessary:** No.
- **Dependency changes:** No.
- **Expected behavior:** yt-dlp resolution always terminates; playback advances or reports error.
- **Source verification:** Inspect modified `resolveViaYtdlp()`; confirm bounded waits and cleanup in `finally`.

### RP-02 — Bug ID: YTDLP-02 (MEDIUM, CONFIRMED behavior)
- **Root cause:** Two blocking stream readers use the common ForkJoinPool without an explicit executor; futures abandoned on timeout.
- **Affected files:** `com/glassui/music/LocalPlayer.java`
- **Proposed changes:** Use a dedicated bounded executor (or existing `EX`), always join/cancel futures in `finally`.
- **Risk:** Low.
- **UI changes:** No.
- **Expected behavior:** No common-pool starvation; no abandoned reader threads.
- **Source verification:** Inspect executor usage and finally blocks.

### RP-03 — Bug ID: YTDLP-03 (HIGH, CONFIRMED behavior)
- **Root cause:** Auto-downloaded executables executed without integrity check; `HttpClient.followRedirects(ALWAYS)` allows HTTPS→HTTP downgrade.
- **Affected files:** `com/glassui/music/LocalPlayer.java`
- **Proposed changes:** Pin download URLs to specific versions; verify SHA-256 against a known checksum before executing. Replace `Redirect.ALWAYS` with scheme-checked redirects (reject non-HTTPS final URIs). 
- **Risk:** Medium — requires knowing correct checksums; may need user input for pinned versions. **This may require user decision on which versions/checksums to use.**
- **UI changes:** No.
- **Dependency changes:** No.
- **Expected behavior:** Only verified binaries are executed; no plaintext downgrade.
- **Source verification:** Inspect `download()` and `DL` initialization.

### RP-04 — Bug ID: RES-01 (MEDIUM, SUSPECTED)
- **Root cause:** ffmpeg not destroyed if `waitFor(5s)` returns false; orphaned process reference lost on resume.
- **Affected files:** `com/glassui/music/LocalPlayer.java`
- **Proposed changes:** `if (!p.waitFor(5, SECONDS)) p.destroyForcibly();` before returning; always join stderr reader in `finally`.
- **Risk:** Low.
- **UI changes:** No.
- **Expected behavior:** No orphaned ffmpeg processes.
- **Source verification:** Inspect `playStream()` cleanup path.

---

## Group 2: Threading

### RP-05 — Bug ID: THREAD-01 (MEDIUM, CONFIRMED blocking)
- **Root cause:** `probe()` runs on single-thread `EX`; each `works()` waits up to 20s × ~13 candidates; media commands on same executor freeze.
- **Affected files:** `com/glassui/music/LocalPlayer.java`
- **Proposed changes:** Reduce per-probe timeout to 2–5s; add lightweight file-existence check before spawning; optionally use a separate executor for probing.
- **Risk:** Low — may reduce detection of slow-starting binaries; user-configured paths still honored.
- **UI changes:** No.
- **Expected behavior:** Controls remain responsive during probing.
- **Source verification:** Inspect `probe()`/`works()` timeout and executor assignment.

### RP-06 — Bug ID: THREAD-02 (LOW-MEDIUM, CONFIRMED)
- **Root cause:** Network I/O performed while holding `MusicHub.class` monitor during token refresh.
- **Affected files:** `com/glassui/music/MusicHub.java`
- **Proposed changes:** Invalidate `access` inside short synchronized block; retry `api()` outside the lock.
- **Risk:** Low.
- **UI changes:** No.
- **Expected behavior:** No lock contention during HTTP calls.
- **Source verification:** Inspect `apiRetry()`.

---

## Group 3: Configuration

### RP-07 — Bug ID: CFG-01 (MEDIUM, CONFIRMED)
- **Root cause:** `GlassConfig.load()` forcibly sets `musicProvider = "YOUTUBE"` on every load, discarding saved value.
- **Affected files:** `com/glassui/GlassConfig.java`
- **Proposed changes:** Remove forced assignment; add null-only fallback (`if (musicProvider == null) musicProvider = "YOUTUBE";`).
- **Risk:** Very low.
- **UI changes:** No.
- **Expected behavior:** User's saved `musicProvider` is respected.
- **Source verification:** Inspect `load()` validation section.

### RP-08 — Bug ID: CFG-02 (LOW, CONFIRMED)
- **Root cause:** `save()` uses plain `Files.writeString()` — crash during write corrupts config.
- **Affected files:** `com/glassui/GlassConfig.java`
- **Proposed changes:** Write to `${path}.tmp`, then `Files.move(tmp, path, ATOMIC_MOVE, REPLACE_EXISTING)` with fallback.
- **Risk:** Low.
- **UI changes:** No.
- **Expected behavior:** Atomic config writes; no partial config corruption.
- **Source verification:** Inspect `save()`.

---

## Group 4: Music / State / Lyrics

### RP-09 — Bug ID: MUSIC-01 (LOW, CONFIRMED)
- **Root cause:** "Starting..." status set before work is queued; misleading during concurrent searches.
- **Affected files:** `com/glassui/music/MusicHub.java`
- **Proposed changes:** Move status update inside executor task.
- **Risk:** Very low.
- **UI changes:** No (status text timing only).
- **Source verification:** Inspect `play()`/`playSingle()`.

### RP-10 — Bug ID: SONGREQ-01 (LOW, CONFIRMED logic)
- **Root cause:** `playingRequest()` compares title only; same-title tracks misidentified.
- **Affected files:** `com/glassui/music/SongRequests.java`
- **Proposed changes:** Compare provider + id when available; title as fallback.
- **Risk:** Low — requires checking `NowPlaying` carries id; if not, preserve existing behavior with a note.
- **UI changes:** No.
- **Source verification:** Inspect `playingRequest()`.

### RP-11 — Bug ID: LYRICS-01 (LOW, SUSPECTED)
- **Root cause:** `estSec += Math.min(0.25, delta)` causes lyric drift at low frame rates.
- **Affected files:** `com/glassui/music/Lyrics.java`
- **Proposed changes:** Cap only abnormal gaps (>2s); otherwise use full delta.
- **Risk:** Low.
- **UI changes:** No (timing accuracy only).
- **Source verification:** Inspect `positionSec()`.

---

## Group 5: HUD / Stream Systems

### RP-13a — Bug IDs: GUI-02, GUI-03 (LOW, CONFIRMED)
- **Root causes:** Alert buttons hit-tested under scale transform mismatch; player-bar color from stale status.
- **Affected files:** `GlassAlertScreen.java`, `MusicScreen.java`
- **Proposed changes:** Render alert buttons in untransformed space (or delay clicks until animation ends); derive status color from displayed text.
- **Risk:** Low — localized rendering changes, no visual redesign.
- **UI changes:** No (corrects transient misalignment/color; preserves design).
- **Source verification:** Inspect `method_25394()` and `drawPlayerBar()`.

### RP-13 — Bug ID: STREAM-01 (MEDIUM, CONFIRMED)
- **Root cause:** `StreamData.reset()` omits `song = null` → stale Now Playing after reconnect/disconnect.
- **Affected files:** `com/glassui/hud/StreamData.java`
- **Proposed changes:** Add `song = null;` in `reset()`.
- **Risk:** Very low.
- **UI changes:** No (removes stale data only).
- **Expected behavior:** Now Playing clears on disconnect/reconnect; media buttons use correct branch.
- **Source verification:** Inspect `reset()`.

### RP-14 — Bug ID: HUD-01 (MEDIUM, CONFIRMED)
- **Root cause:** `(float)System.currentTimeMillis()` precision loss causes marquee jumps.
- **Affected files:** `com/glassui/hud/StreamHudRenderer.java`
- **Proposed changes:** Compute modulo in `long` domain before casting to float.
- **Risk:** Very low.
- **UI changes:** No (fixes jitter, preserves scroll speed/direction).
- **Source verification:** Inspect `marquee()`.

### RP-15 — Bug ID: HUD-02 (MEDIUM, CONFIRMED)
- **Root cause:** `warned` latch permanently suppresses all HUD drawing after first render exception.
- **Affected files:** `com/glassui/hud/StreamHudRenderer.java`
- **Proposed changes:** Keep calling `draw()` each frame; rate-limit error logging instead of latching.
- **Risk:** Low — repeated exceptions will log more (bounded by rate limit).
- **UI changes:** No (restores intended rendering).
- **Source verification:** Inspect `render()`.

### RP-16 — Bug ID: HUD-03 (MEDIUM, CONFIRMED)
- **Root cause:** `ChatSound.seen` not reset when `StreamData.reset()` zeroes chatCount → no chat ping after reconnect.
- **Affected files:** `com/glassui/hud/ChatSound.java`, `com/glassui/hud/StreamData.java`
- **Proposed changes:** Add `ChatSound.reset()` setting `seen=0`; call from `StreamData.reset()`.
- **Risk:** Very low.
- **UI changes:** No (audible behavior restored).
- **Source verification:** Inspect reset paths.

### RP-17 — Bug ID: THREAD-03 (MEDIUM, CONFIRMED)
- **Root cause:** `StreamHud.stop()` doesn't await background writers; stale callbacks write into freshly reset `DATA`.
- **Affected files:** `com/glassui/hud/StreamHud.java`
- **Proposed changes:** Await executor termination with short timeout (e.g. 2s) before reset, or use a generation token ignored by stale callbacks.
- **Risk:** Medium — await adds brief delay on disconnect; generation token requires touching several callback classes (larger scope). **Prefer await approach for minimal change.**
- **UI changes:** No.
- **Source verification:** Inspect `stop()`/`start()`.

### RP-18 — Bug ID: THREAD-04 (MEDIUM, CONFIRMED)
- **Root cause:** `MediaControl.run()` uses `p.waitFor()` with no timeout → blocked threads/process accumulation.
- **Affected files:** `com/glassui/hud/MediaControl.java`
- **Proposed changes:** `p.waitFor(10, SECONDS)` + `destroyForcibly()` on timeout.
- **Risk:** Very low.
- **UI changes:** No (buttons stop hanging).
- **Source verification:** Inspect `run()`.

### RP-19 — Bug ID: RES-04 (MEDIUM, CONFIRMED)
- **Root cause:** Linux `playerctl` process not referenced by `this.proc` → `stop()` can't kill it; thread not interrupted.
- **Affected files:** `com/glassui/hud/MediaDetector.java`
- **Proposed changes:** Assign `this.proc = p` in `linux()`; keep worker thread reference and interrupt in `stop()`.
- **Risk:** Low.
- **UI changes:** No.
- **Source verification:** Inspect `linux()`/`stop()`.

### RP-20 — Bug ID: STREAM-02 (LOW, CONFIRMED)
- **Root cause:** YouTube error bodies parsed as JSON before HTTP status check → misleading parse errors.
- **Affected files:** `com/glassui/hud/YouTubeClient.java`
- **Proposed changes:** Check status code first; parse error body in try/catch with `"HTTP <code>"` fallback.
- **Risk:** Very low.
- **UI changes:** No (status text accuracy only).
- **Source verification:** Inspect `get()`.

### RP-21 — Bug IDs: HUD-04, HUD-05, HUD-06, HUD-07 (LOW, CONFIRMED)
- **Root causes:** Tap cap not force-enforced; backspace splits surrogate pairs; search trims from front; caches not invalidated on font change.
- **Affected files:** `TapTracker.java`, `IslandSearch.java`, `StreamHudRenderer.java`
- **Proposed changes:** Force-evict low-total entries at cap; delete full code point on backspace; trim query from end; include font in cache keys or clear on change.
- **Risk:** Low — localized logic changes.
- **UI changes:** No (corrects existing behavior; no visual redesign).
- **Source verification:** Inspect each location.

### RP-22 — Bug IDs: STREAM-03, RES-03, RES-05 (LOW / INFORMATIONAL)
- **Root causes:** TikTok `closed.await()` no timeout; GLFW callbacks never freed; HTTP clients never closed.
- **Affected files:** `TikTokClient.java`, `IslandSearch.java`, `YouTubeClient.java`
- **Proposed changes:** Bounded `await()`; free/restore callbacks on close; `close()` clients on stop.
- **Risk:** Low–medium (GLFW callback lifecycle needs care to avoid breaking chat input).
- **UI changes:** No.
- **Source verification:** Inspect each location.

---

## Group 6: Security

### RP-12 — Bug ID: SEC-01 (LOW-MEDIUM, SUSPECTED)
- **Root cause:** API keys included in URLs; exception messages with URLs may be logged.
- **Affected files:** `com/glassui/hud/YouTubeClient.java`, `com/glassui/hud/TikTokClient.java`
- **Proposed changes:** Sanitize log messages; avoid logging raw exception messages that may contain URLs with keys. Do not change URLs sent to APIs.
- **Risk:** Low.
- **UI changes:** No.
- **Source verification:** Inspect logging in `stats()`, `chat()`, `loop()`.

---

## Group 7: Newly Discovered — REQUIRES SEPARATE APPROVAL (NOT APPLIED)

> These findings arrived from the final analysis agent **after** the user's repair approval.
> They are documented but were **not** fixed. Do not implement without explicit user approval.

### RP-23 — MIX-01 (HIGH, CONFIRMED)
- **Root cause:** `ClickableWidgetSoundMixin` calls `ci.cancel()` whenever `uiSound=true`, even when `UiSound.click()` was a no-op (no player on menus, volume 0, debounce).
- **Affected files:** `ClickableWidgetSoundMixin.java`, `UiSound.java`
- **Proposed change:** Have `UiSound.pop()` report whether a sound played; cancel the vanilla sound only then (or allow menu clicks by removing the player requirement).
- **Risk:** Medium — changes audible feedback; must verify menu, in-game and rapid-click behavior.
- **UI changes:** No (audio only).
- **Expected behavior:** Every custom-ui click has audible feedback; no silent double-suppression.
- **Source verification:** Inspect `pop()` return values and the mixin callback.

### RP-24 — MIX-02 / MIX-03 (LOW, CONFIRMED)
- **Root cause:** Overridden `renderWidget` skips vanilla cursor handling; sliders bypass the click-sound mixin.
- **Affected files:** `PressableWidgetMixin.java`, `SliderWidgetMixin.java`, `ClickableWidgetSoundMixin.java`
- **Proposed change:** Reproduce cursor handling in both render mixins; optionally add a `class_357` click-sound mixin.
- **Risk:** Low–medium (cursor states need care).
- **UI changes:** No visual redesign; restores expected cursor/audio.
- **Source verification:** Inspect render callbacks and new cursor calls.

### RP-25 — THREAD-05 (LOW, CONFIRMED)
- **Root cause:** `MusicHub.commitVolume()` performs `GlassConfig.save()` (disk I/O) on the client tick thread on volume-key release.
- **Affected files:** `GlassUiClient.java`, `MusicHub.java`, `GlassConfig.java`
- **Proposed change:** Off-thread/debounced save (only after the key release, coalescing rapid changes).
- **Risk:** Low — must preserve config contents.
- **UI changes:** No (removes potential stutter).
- **Source verification:** Inspect scheduling and saved JSON.

### RP-26 — MIX-04 / DEP-03 / DEP-04 (INFORMATIONAL / SUSPECTED)
- **Root cause:** `require=0` silent injection failures; permissive `fabric-api: "*"`; permissive `minecraft ~1.21.11` with hard-required mixins.
- **Affected files:** mixin sources, `fabric.mod.json`, `glassui.mixins.json`
- **Proposed change:** Diagnostics for `require=0`; pin minimum Fabric API; narrow Minecraft range or soften cosmetic mixins.
- **Risk:** Medium — dependency metadata changes affect resolution; user decision required.
- **UI changes:** No.
- **Source verification:** Inspect metadata and mixin annotations.

---

## Items Explicitly NOT Planned (do not fix without user request)

- INIT-01 (GLFW key polling instead of KeyBinding) — design choice, preserve behavior.
- INIT-02 (ModMenu optional) — not a bug.
- GUI-01 (Spotify UI unreachable) — **BLOCKED pending user decision**: fixing would enable a currently-disabled platform, which may conflict with "no new platform integrations". Do NOT silently change.
- DEP-01 (Loader 0.18.4 build vs 0.19.5 target) — runtime-compatible.
- DEP-02 (`fabric-api: "*"`) — informational; changing is optional.
- RES-02 (stderr reader not joined on success) — minor, daemon thread.
- YTDLP-04 (InterruptedException skips onEnd) — rare, low impact.
- YTDLP-05 (ffmpeg no overall timeout) — suspected, requires design decision.
- Any UI/layout/theme/color changes — strictly forbidden.
- Any new features, platform integrations, or refactoring — strictly forbidden.

---

## Approval Gate

**The user must explicitly approve this plan before any fixes are applied.**
Example approval: "Silakan fix," "Mulai perbaikan," "Saya izinkan," or "Fix semua bug yang sudah ditemukan."

Questions or requests to explain findings are NOT approval.
