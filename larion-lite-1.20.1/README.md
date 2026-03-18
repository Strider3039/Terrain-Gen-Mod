# Larion Lite 1.20.1 — **0.1.0** (CurseForge parity)

This source tree matches **[larion_lite-0.1.0](https://www.curseforge.com)** on Forge **1.20.1**: same generator, presets, and defaults. See **[CURSEFORGE_0.1.0.md](CURSEFORGE_0.1.0.md)**.

## Play

1. Forge **1.20.1**, add the built jar to `mods`.
2. **Create world → More world options → World type → Larion Lite (Performance overworld)**.

## Config reset (match 0.1.0 feel)

If terrain/caves changed: copy **`config-defaults/larion_lite-common-0.1.0.toml`** → **`config/larion_lite-common.toml`** (game closed).

| Key | 0.1.0 default |
|-----|----------------|
| `terrain_preset` | `dramatic_explorer` |
| Caves / detail | see defaults file |

Advanced terrain still comes from presets or datapack **`larion_lite_enhance`** (see [PERFORMANCE_FIRST_GENERATOR.md](PERFORMANCE_FIRST_GENERATOR.md)).

## Commands

```
/larion_lite benchmark [radius]
```

## Build

```bat
gradlew.bat build
```
