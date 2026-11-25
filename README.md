# GlassUI

Liquid glass UI, iOS-style dock menu, spring pop-ups, and a Stream HUD for Minecraft — with TikTok/YouTube live overlays, live chat, gift/follow alerts, now playing, and in-game music search.

## Version contract

| Component | Version |
|---|---|
| Minecraft | 1.21.11 (exact) |
| Java | 21 |
| Fabric Loader | 0.19.5 |
| Fabric API | 0.141.6+1.21.11 |
| Gradle Wrapper | 9.2.1 (recommended) |
| Fabric Loom | 1.19.3+ (remap variant) |

## Source provenance

This repository contains the GlassUI Fabric mod source for Minecraft 1.21.11 (Fabric Loader 0.19.5, Java 21).

## Build configuration

Because this source was reconstructed from bytecode, Minecraft members retain their
intermediary names (`class_XXX`, `method_XXX`, `field_XXX`). `build.gradle.kts` therefore
uses intermediary mappings (`net.fabricmc:intermediary:<minecraft_version>:v2`) instead of
official Mojang mappings, so no source renaming is required.

## Audit & repair reports

The `reports/` directory contains the full audit, bug inventory, repair plan, fix report,
source review, source validation, and build-preparation notes. A number of findings were
deliberately **not** fixed (they require a separate decision); see `reports/REPAIR_PLAN.md`
Group 7 and `reports/FIX_REPORT.md`.

## Build

```
./gradlew build
```

The produced JAR will be in `build/libs/`. Place it in `mods/` alongside Fabric API.

**Note:** Build with `./gradlew build` (Java 21).

## Features

- Liquid glass button/widget rendering with spring animations
- iOS-style dock menu replacing vanilla title-screen buttons
- Stream HUD with Dynamic Island, live chat, gift/follow pop-ups, now playing
- TikTok Live (EULER WebSocket / TIKFINITY local) integration
- YouTube Live via Data API v3 (user-supplied API key)
- In-game music search with local ffmpeg playback and yt-dlp fallback
- Lyrics (LRCLIB + Netease), song requests, media key controls
- HUD editor and extensive configuration GUI

## Configuration

Runtime config is written to `config/glassui.json` on first launch. Sensitive values (YouTube API key, TikTok key, Spotify credentials) are stored there in plaintext — do not share your config file.

## License

MIT (as declared in the original `fabric.mod.json`). Bundled fonts carry their own licenses — see `src/main/resources/assets/glassui/font/FONTS-LICENSE.txt`.
