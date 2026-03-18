# Larion Lite 1.20.1 (performance-first)

Forge mod: **fast fantasy overworld** — column heightmap + warp, **macro basins/peaks**, **valleys + channels**, optional **scree**, optional **3D cave noise**, biome-tag surface. Still **O(1) 2D noise per land column** (plus 3D cave loop only below surface).

## Play

1. Forge **1.20.1**, add the built jar to `mods`.
2. **Create world → More world options → World type → Larion Lite (Performance overworld)**.

## Config (`config/larion_lite-common.toml`)

| Key | Notes |
|-----|--------|
| **`terrain_preset`** | Default **`dramatic_explorer`** — strong silhouettes, valleys, scree. **`structure_friendly`** — gentler slopes, less detail, set **`cave_surface_padding`** to **16–18** for heavy structures. Also: `modpack_balanced`, `flat_fantasy`, `tall_ridges`, `dramatic_warp`, `mellow`, `cave_rich`. **Empty** = use world/datapack `terrain` JSON only. |
| **Caves** | `cave_chamber_threshold` minimum **0.12** (guardrail). Peaks use **higher effective thresholds** → fewer cave voxels carved on tall terrain (**faster**, less floating cutouts). |
| **Detail** | `detail_amplitude` capped **36** in UI; **>34** looks blocky. |

Advanced terrain (macro contrast, valleys, ridge octave, scree) lives in **presets** or datapack block **`larion_lite_enhance`** inside `terrain`:

```json
"terrain": {
  "continent_scale": 0.0105,
  ...
  "larion_lite_enhance": {
    "macro_vertical_contrast": 0.78,
    "valley_depth": 9,
    "channel_depth": 5,
    "scree_enabled": true,
    "cave_peak_reduction": 0.12
  }
}
```

## Commands

```
/larion_lite benchmark [radius]
```

## Docs

- **[PERFORMANCE_FIRST_GENERATOR.md](PERFORMANCE_FIRST_GENERATOR.md)** — design + perf notes.
- **[Larion_Lite_Enhancement_Plan.md](Larion_Lite_Enhancement_Plan.md)** — roadmap alignment.

## Build

```bat
gradlew.bat build
```
