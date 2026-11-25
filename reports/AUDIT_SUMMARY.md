# GlassUI 1.3.18 — Audit Summary (for user review)

## Status

**AUDIT COMPLETE. APPROVED FIXES APPLIED. NO BUILDS RUN.**

## What was done

1. Downloaded and inspected `glassui-1.3.18.jar` (SHA-256: `899341cbf3fc009a4ebfc687bc31347ebed38aa62d84a02d44eb863569a1f022`).
2. Extracted and catalogued all 112 class files + 14 resources.
3. Decompiled all classes with CFR 0.152 → 50 editable Java source files.
4. Reconstructed an editable source project skeleton with Gradle build config (NOT built).
5. Ran 4 parallel deep-analysis agents across: initialization/mixins, music/yt-dlp, HUD/stream, UI/config.
6. Compiled findings into structured reports.
7. After explicit user approval, applied all fixes in `REPAIR_PLAN.md` Groups 1–6 (28 bug IDs) and performed source-only static validation. No build was run.

## Reports generated

| File | Contents |
|---|---|
| `AUDIT_REPORT.md` | JAR metadata, manifest, Fabric metadata, mixin config, package/resource inventory, feature inventory, limitations |
| `BUG_INVENTORY.md` | Every discovered issue with ID, category, location, evidence, severity, status, proposed fix, UI impact |
| `REPAIR_PLAN.md` | Grouped repair proposals with root cause, affected files, risk, UI impact, verification method |
| `SOURCE_REVIEW.md` | All 50 source files listed with purpose, feature, status, review status |
| `BUILD_PREPARATION_REPORT.md` | Target versions, compatibility, structure, build instructions (user builds) |
| `FIX_REPORT.md` | Placeholder — to be filled after approval |
| `SOURCE_VALIDATION_REPORT.md` | Planned checks — to be filled after fixes |
| `GITHUB_PUSH_REPORT.md` | Planned push checklist — to be filled after push |

## Key findings at a glance

### Confirmed bugs requiring repair (grouped by priority)

**HIGH severity:**
- YTDLP-03: Auto-downloaded ffmpeg/yt-dlp executables executed without integrity verification; HTTP redirects allow HTTPS→HTTP downgrade. *(Needs user decision on pinned versions/checksums.)*

**MEDIUM severity:**
- STREAM-01: `StreamData.reset()` leaves stale `song`.
- HUD-01: Marquee float precision causes jumping text.
- HUD-02: One render exception permanently disables HUD.
- HUD-03: Chat sounds break after reconnect (`seen` not reset).
- HUD-04: TapTracker 2000-user cap not enforced.
- THREAD-03: Stream `stop()` doesn't await background writers → stale data.
- THREAD-04: Media command processes can hang forever.
- RES-04: Linux `playerctl` process not killed on stop.
- CFG-01: `musicProvider` forcibly reset to `"YOUTUBE"` on every load.
- YTDLP-02: Blocking stream readers on common ForkJoinPool.
- THREAD-01: `probe()` blocks media controls for up to 20s × N candidates.
- YTDLP-05 / RES-01 / YTDLP-01: Suspected process-handling hangs/leaks.
- GUI-01: Spotify UI unreachable. *(BLOCKED — needs user decision.)*

**LOW severity (confirmed):**
- GUI-02, GUI-03, STREAM-02, HUD-05, HUD-06, HUD-07, RES-02, RES-03, CFG-02, MUSIC-01, SONGREQ-01, THREAD-02.

### Explicitly NOT bugs / do not change

- INIT-01: GLFW key polling (design choice).
- INIT-02: Optional ModMenu entrypoint (handled by Fabric).
- DEP-01: Loader 0.19.5 compatible with `>=0.18.1`.
- RES-01(font): Font resources correct.
- All "NOT A BUG" items verified against bytecode by subagents.

## Critical build-preparation issue

The decompiled source uses **intermediary-mapped Minecraft names** (`class_XXX`, `method_XXX`) because the original JAR was built with intermediary mappings. The proposed `build.gradle.kts` uses `loom.officialMojangMappings()`, which provides Mojang names — the source will NOT compile without remapping. Options:
1. Switch Loom to intermediary mappings (not recommended for development).
2. Remap decompiled source to Mojang names (requires mapping data).

**This must be resolved before the user builds.** No build was attempted.

## Approval gate

The required question was asked and the user answered **"Ya, fix semua bug yang disetujui"**. Repairs in Groups 1–6 were applied.

**Still requiring a separate decision (NOT fixed):**
- GUI-01 (Spotify UI unreachable) — would enable a currently-disabled platform.
- YTDLP-03 / SEC-02 (binary download integrity + HTTPS→HTTP downgrade) — needs pinned versions/checksums.
- MIX-01 (HIGH), MIX-02, MIX-03, THREAD-05, DEP-03/04 — found by the final agent after approval; see `REPAIR_PLAN.md` Group 7.

**Push gate:** no appropriate GlassUI source remote exists locally (`mods` holds only the JAR; `MusicallyMods` is a different mod). A target repository must be chosen before any push.
