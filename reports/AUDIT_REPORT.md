# GlassUI 1.3.18 — Complete JAR Audit Report

## 1. JAR Information

| Property | Value |
|---|---|
| File | `glassui-1.3.18.jar` |
| Size | 1,392,310 bytes (~1.33 MB) |
| SHA-256 | `899341cbf3fc009a4ebfc687bc31347ebed38aa62d84a02d44eb863569a1f022` |
| Format | Java archive (ZIP), Fabric mod JAR |
| Class file version | 65.0 (Java 21) |
| Total ZIP entries | 136 |
| Extracted files | 126 |
| Class files | 112 |
| Resource files | 14 |
| Decompiled sources | 50 (CFR 0.152, inner classes merged) |

## 2. Manifest Analysis

```
Manifest-Version: 1.0
Fabric-Jar-Type: classes
Fabric-Loom-Mixin-Remap-Type: static
Fabric-Gradle-Version: 9.9.1
Fabric-Loom-Version: 1.14.10
Fabric-Mixin-Compile-Extensions-Version: 0.6.0
Fabric-Minecraft-Version: 1.21.11
Fabric-Tiny-Remapper-Version: 0.12.2
Fabric-Loader-Version: 0.18.4
Fabric-Mixin-Version: 0.17.0+mixin.0.8.7
Fabric-Mixin-Group: net.fabricmc
Fabric-Mapping-Namespace: intermediary
```

**Important:** The manifest records **build-time** tooling versions. The user's target is **Fabric Loader 0.19.5** (newer than the 0.18.4 recorded here). The `fabric.mod.json` dependency range `>=0.18.1` permits 0.19.5 at runtime. No code-level incompatibility is evident from metadata alone.

## 3. Fabric Metadata (`fabric.mod.json`)

| Field | Value | Assessment |
|---|---|---|
| schemaVersion | 1 | Valid |
| id | `glassui` | Valid |
| version | `1.3.18` | Valid |
| name | GlassUI | Valid |
| environment | `client` | Correct (client-only mod) |
| entrypoints.client | `com.glassui.GlassUiClient` | Present |
| entrypoints.modmenu | `com.glassui.GlassModMenu` | Present (optional ModMenu integration) |
| mixins | `glassui.mixins.json` | Present |
| depends.fabricloader | `>=0.18.1` | Compatible with 0.19.5 |
| depends.minecraft | `~1.21.11` | Exact 1.21.x target — compatible |
| depends.java | `>=21` | Compatible with Java 21 |
| depends.fabric-api | `*` | Any version — very permissive |
| license | `MIT` | Declared |
| authors | `["You"]` | Placeholder author name |

**Observations:**
- No explicit `modmenu` dependency declared. ModMenu entrypoint is optional and Fabric handles its absence gracefully.
- `fabric-api: "*"` is overly permissive; specific API features used (`HudElementRegistry`, `ClientLifecycleEvents`, `ClientTickEvents`) require a reasonably recent Fabric API version. A minimum version should be declared, but this is a **recommendation**, not a confirmed runtime bug.
- Author field is literal placeholder `"You"` — inherited from original, preserved as-is.

## 4. Mixin Configuration

```json
{
  "required": true,
  "package": "com.glassui.mixin",
  "compatibilityLevel": "JAVA_21",
  "client": [
    "ClickableWidgetSoundMixin",
    "PressableWidgetMixin",
    "SliderWidgetMixin",
    "TitleScreenMixin"
  ],
  "injectors": { "defaultRequire": 1 }
}
```

Four mixin classes, all client-side. `defaultRequire: 1` makes injections strict (a missing injection point fails mod loading). Two of the four injections explicitly override with `require=0` (ClickableWidgetSoundMixin, SliderWidgetMixin), while PressableWidgetMixin and TitleScreenMixin rely on the strict default.

## 5. Package Inventory

| Package | Class files | Purpose |
|---|---|---|
| `com.glassui` | 4 | Entry points, configuration |
| `com.glassui.hud` | 44 | HUD rendering, stream data, TikTok/YouTube clients, media detection |
| `com.glassui.music` | 17 | Music hub, local player (yt-dlp/ffmpeg), lyrics, suggestions |
| `com.glassui.mixin` | 4 | Widget/title-screen mixins |
| `com.glassui.ui` | 43 | Screens, widgets, rendering helpers |

## 6. Resource Inventory

All resources are under `assets/glassui/font/`:
- 5 font definition JSON files (dejavu_mono, dejavu_sans, dejavu_serif, inter, poppins)
- 5 font binary files (4 TTF + 1 OTF, total ~2.2 MB)
- 1 FONTS-LICENSE.txt

**No textures, no sound assets, no additional libraries/shaded dependencies.** Font license file mentions SIL OFL 1.1 and DejaVu free license.

## 7. Feature Inventory (observed from source)

- Liquid glass UI rendering (custom widget rendering, spring animations)
- iOS-style dock menu on title screen (replaces vanilla buttons)
- Independent HUD elements: stream HUD, damage numbers, hotbar replacement, hidden vanilla bars
- TikTok live integration (EULER WebSocket / TIKFINITY local WebSocket) with chat, gifts, follows
- YouTube Live integration via YouTube Data API v3 (stats + live chat polling)
- Dynamic Island display
- In-game music search (YouTube, Spotify OAuth PKCE flow)
- Local playback via ffmpeg + yt-dlp fallback
- Music requests system with per-user limits
- Lyrics display (LRCLIB + Netease Cloud Music APIs)
- Suggestions (Google suggestqueries)
- Media control (OS media keys interception)
- Combo tracker, top-tap tracker, chat sounds, combat FX
- Configuration GUI with theme/font selection, HUD editor

## 8. Dependencies (observed from bytecode references)

- Fabric API (client lifecycle events, HUD registry, attacks callback)
- Fabric Loader API (`FabricLoader.getInstance().getConfigDir()`)
- Gson (JSON serialization — from Minecraft's bundled Gson)
- Minecraft classes (mapped to intermediary names in JAR: `class_XXX`)
- java.net.http.HttpClient (JDK 11+)
- javax.sound.sampled (JDK)
- org.lwjgl.glfw.GLFW (bundled with Minecraft)
- SLF4J (bundled with Minecraft)
- ModMenu API (optional, compile-time only)
- Spongepowered Mixin (bundled with Fabric Loader)

**No bundled third-party runtime libraries** — all dependencies are provided by Minecraft/Fabric/JDK.

## 9. Reconstruction Methodology & Limitations

- Source was decompiled from bytecode using **CFR 0.152**. Decompilation is lossy:
  - Some `try/finally` blocks are reconstructed with warnings ("Removed try catching itself").
  - Comments, original variable names (where obfuscated), and javadoc are lost.
  - Lambda expressions are reconstructed but may differ syntactically from original.
  - Inner classes are merged into outer class files.
- The reconstructed source is **not** the original source. It is functionally equivalent bytecode-derived code.
- Line numbers in reports refer to the **decompiled** `.java` files, not original sources.
- Some method names remain as intermediary mappings (`method_XXXXX`, `field_XXXXX`) because the JAR was built with official Mojang mappings but compiled against intermediary-named Minecraft classes.

## 10. Licensing / Authorization

- Mod declares MIT license.
- Bundled fonts have separate licenses (SIL OFL 1.1 / DejaVu free license) — documented in FONTS-LICENSE.txt.
- The user provided the JAR and authorized reconstruction, audit, and repair.
- GitHub push will be performed using the existing `gh` auth session.
- No credentials, tokens, or secrets are hardcoded in the source (user-supplied API keys are stored in config).

## 11. Summary of Findings by Category

Detailed findings are in `BUG_INVENTORY.md`. Summary:

| Category | Confirmed | Suspected | Informational | Needs Decision |
|---|---|---|---|---|
| Initialization | 0 | 0 | 2 | 0 |
| GUI / UI Logic | 3 | 0 | 0 | 1 (GUI-01) |
| HUD / Stream | 12 | 1 | 1 | 0 |
| Music / Platforms | 3 | 1 | 0 | 0 |
| YT-DLP / Process | 3 | 4 | 0 | 1 (YTDLP-03) |
| Threading | 5 | 0 | 0 | 0 |
| Resources | 2 | 0 | 1 | 0 |
| Mixins | 3 | 0 | 1 | 0 |
| Configuration | 2 | 0 | 0 | 0 |
| Dependencies | 0 | 1 | 2 | 0 |
| Security | 1 | 1 | 0 | 0 |

> **Note:** The final analysis agent (initialization/mixins) reported MIX-01..04, THREAD-05 and DEP-03 after the repair approval gate. These are **documented but NOT fixed** and require a separate user decision. See `BUG_INVENTORY.md` section H and `REPAIR_PLAN.md` Group 7.

## 12. Unknowns / Not Determinable from Static Analysis

- Actual runtime behavior of yt-dlp on the user's machine (OS, installed binaries, network).
- Whether Fabric API version installed by the user supports all `HudElementRegistry` methods used.
- Whether ModMenu is installed (entrypoint optional).
- Exact behavior of `UiSound.click()` audio (sound is generated programmatically).
- Network API availability / rate limits for YouTube Data API, LRCLIB, Netease, Google Suggest.

---

*This report was generated by static analysis of the supplied JAR. No build, compilation, or runtime execution was performed.*
