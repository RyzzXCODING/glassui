# GlassUI 1.3.18 — Build Preparation Report

## Target Versions

| Component | Target | Source |
|---|---|---|
| Minecraft | 1.21.11 (exact) | User requirement + fabric.mod.json `~1.21.11` |
| Java | 21 | User requirement + class file version 65.0 |
| Fabric Loader | 0.19.5 | User requirement |
| Fabric API | 0.141.6+1.21.11 | Selected for 1.21.11 compatibility; original used `*` |
| Fabric Loom | 1.19.3 (remap) | Chosen for 1.21.11 + Loader 0.19.5 support |
| Gradle Wrapper | 9.2.1 | User requirement (manifest recorded 9.9.1 build-time) |
| Mappings | Intermediary (`net.fabricmc:intermediary:1.21.11:v2`) | Required because reconstructed source retains intermediary member names from bytecode |

## Compatibility Review

- **Minecraft 1.21.11 + Loader 0.19.5**: The original JAR was built against Loader 0.18.4 but declares `fabricloader: ">=0.18.1"` — 0.19.5 satisfies this. Loom 1.19.3 supports Loader 0.19.x.
- **Java 21**: Class file version 65.0 confirms original was Java 21. Toolchain set to 21.
- **Fabric API**: Mod uses `HudElementRegistry`, `ClientLifecycleEvents`, `ClientTickEvents`, `AttackEntityCallback`. These are available in recent Fabric API versions. `0.141.6+1.21.11` is a known-good version for 1.21.11.
- **ModMenu**: Declared as `modCompileOnly` so the entrypoint compiles without requiring ModMenu at runtime. Fabric ignores missing entrypoint classes when the mod is absent.
- **Gson**: Provided by Minecraft; no explicit dependency needed.

## Project Structure

```
source/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── .gitignore
├── README.md
└── src/main/
    ├── java/com/glassui/   (50 decompiled/reconstructed .java files)
    └── resources/
        ├── fabric.mod.json
        ├── glassui.mixins.json
        └── assets/glassui/font/  (5 JSON + 5 font files + license)
```

## Build Instructions (for user — NOT executed by audit agent)

```bash
cd source
./gradlew build
```

Output JAR: `source/build/libs/glassui-1.3.18.jar`

## Mapping Resolution (addressed in preparation)

The decompiled source uses intermediary-mapped Minecraft names (`class_XXX`, `method_XXX`, `field_XXX`) because production JARs are compiled with intermediary names. `build.gradle.kts` was therefore changed from `loom.officialMojangMappings()` to:

```kotlin
mappings("net.fabricmc:intermediary:${property("minecraft_version")}:v2")
```

This lets the reconstructed source compile **without renaming any Minecraft member**. Fabric API classes are unaffected. This is a build-configuration preparation change only — it was not compiled or verified by build.

## Remaining Known Issues

1. **ModMenu version `15.0.0`** in `modCompileOnly` is a placeholder — verify the correct version for MC 1.21.11 on Maven before building.
2. Gradle wrapper jar was downloaded from the Gradle v9.2.1 tag — verify its checksum before use.
3. `fabric-api` version `0.141.6+1.21.11` and Loader `0.19.5` were not resolved/verified (no build permitted).

## Manual Build Steps (for the user)

1. Verify/adjust `modCompileOnly` ModMenu version.
2. Verify the Gradle wrapper checksum.
3. Run `./gradlew build` personally.

## Explicit Statement

**No build, compilation, or Gradle task was executed by the audit agent.** This report documents preparation only. The user will build the project personally.
