# Larion Lite 1.20.1 — 0.1.0 (CurseForge parity)

Forge **1.20.1**, same generator/presets/defaults as **larion_lite-0.1.0** on CurseForge.

## Play

1. Add the built jar to `mods`.
2. **Create world → More world options → World type → Larion Lite (Performance overworld)**.

## Config

Reset to shipped defaults: copy **`config-defaults/larion_lite-common-0.1.0.toml`** → **`config/larion_lite-common.toml`** (game closed).  
`terrain_preset` default is **`dramatic_explorer`** (layered elevation: **mid/high plateaus**, **deeper valleys**, **shallow + deep river channels**, **rolling hills**, **ridge peak boost**). Tune in datapack under `terrain` → `larion_lite_enhance` → **`elevation_layers`** (`plateau_mid_blocks`, `plateau_high_extra`, `deep_canal_depth`, `hill_amplitude`, `peak_boost_blocks`, etc.).

## Commands

`/larion_lite benchmark [radius]`

## Build

```bat
gradlew.bat build
```
