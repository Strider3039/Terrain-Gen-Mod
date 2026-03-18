# Larion Lite — How the Mod Works (Technical Overview)

**Location:** `larion-lite-1.20.1/` (Forge **1.20.1**)  
**Mod ID:** `larion_lite`  
This document describes **exactly** what the mod changes in the world, **which Minecraft/Forge APIs it uses**, and **what “tools” exist** for players and pack makers.

---

## 1. What the mod changes (and what it does not)

| Changed | Not changed |
|--------|-------------|
| **Overworld terrain** when you pick the Larion world preset | Nether / End (still vanilla `minecraft:noise`) |
| **Shape of land** (height per column) | **Which biomes spawn** (still vanilla `multi_noise` overworld preset) |
| **Stone / air column fill** under that height | Vanilla **carver step** (ravines etc. are not run the same way as vanilla noise worlds) |
| **Top few blocks** (water, grass/sand, dirt, snow, scree) | Biome **features** (trees, ores) — still normal datapack/vanilla |

The mod **does not** add blocks, items, entities, or new biomes. It **replaces only the overworld chunk generator** with a custom implementation that computes **one surface height per (x, z)** and builds columns from that.

---

## 2. How it attaches to the game

1. **Registry**  
   On load, the mod registers a chunk generator codec under  
   **`larion_lite:fantasy`**  
   (`LarionLiteMod` → `DeferredRegister` on `Registries.CHUNK_GENERATOR`).

2. **World preset (datapack)**  
   **`larion-lite-1.20.1/src/main/resources/data/larion_lite/world_preset/performance_overworld.json`** defines a world type whose **overworld** uses:
   - `"type": "larion_lite:fantasy"`
   - `"biome_source": { "type": "minecraft:multi_noise", "preset": "minecraft:overworld" }`
   - `"settings": "minecraft:overworld"` (sea level, min Y, default stone/fluid, etc.)

3. **Player choice**  
   New world → **World type → Larion Lite (Performance overworld)** uses that preset.

---

## 3. Core class: `FantasyChunkGenerator`

This class **extends** `net.minecraft.world.level.chunk.ChunkGenerator`. Minecraft calls it to:

| Method / behavior | Role |
|-------------------|------|
| **`getBaseHeight` / `getBaseColumn` / heightmaps** | All use **`surfaceY(x, z)`** — same height formula everywhere (structures, maps, etc.). |
| **`fillFromNoise`** | For each column in the chunk: fill **below surface** with **default block** (stone), **above** with **air**. Optionally carve **caves** with 3D noise. |
| **`buildSurface`** | After columns exist: **sea water**, **biome surface block** (grass, sand…), **dirt under grass**, **snow**, **gravel/scree** on steep steps. |
| **`applyCarvers`** | **Empty** — vanilla carvers expect `NoiseBasedChunkGenerator`; Larion does not wire that path. |
| **`codec()`** | Serializes the generator for world save / datapack (biome source + noise settings + optional `terrain` JSON). |

So the **only** custom “world modification” for terrain is: **compute `surfaceY`**, then **fill**, then **surface pass**.

---

## 4. Tools the code uses (APIs & libraries)

| Tool | Use in Larion Lite |
|------|-------------------|
| **`NormalNoise`** (`net.minecraft.world.level.levelgen.synth.NormalNoise`) | Every height/cave sample: **continent, warp, ridge, ridge fine, detail, valley, channel, plateau, hill, deep canal, 3D cave worms/chambers**. Created with **`NormalNoise.NoiseParameters`** (octaves, amplitudes). |
| **`RandomState`** | World-consistent RNG factories: `getOrCreateRandomFactory(ResourceLocation).fromHashOf(...)` so the same world seed always produces the same noise streams. |
| **`ChunkGenerator` + `Codec`** (Mojang **DataFixerUpper** / `com.mojang.serialization`) | Save/load generator settings; optional **`terrain`** object on the generator JSON. |
| **`ForgeConfigSpec`** | **`config/larion_lite-common.toml`** — preset name, cave toggles, **detail** scale/amplitude (merged at runtime). |
| **`RecordCodecBuilder`** | **`FantasyTerrainConfig`**, **`LarionTerrainEnhance`**, **`LarionElevationLayers`** — datapack-editable numbers. |
| **`WorldgenRandom` / `LegacyRandomSource`** | Mob spawn hook only (vanilla parity). |
| **Brigadier** | **`/larion_lite benchmark`** command registration. |

There is **no** TerraBlender, no structure API, no mixin-based terrain — **only** this generator + config + preset JSON.

---

## 5. Height formula (`surfaceY`) — order of operations

For each world **(x, z)**:

1. **Domain warp** — two 2D noise samples offset position for **continent**, **hill**, **plateau** (breaks grid alignment).
2. **Continent noise** — main **macro** elevation driver (−1…1 scaled by `height_variation` and modifiers).
3. **Masks** — `plainsCalm`, `highRelief`, **`sereneValley`** (low `|continent|`) to **soften** detail/ridges/hills in valleys.
4. **Ridge + ridge fine** — shaped with **power ~1.42–1.48** on |noise| (smoother than raw squares).
5. **Detail** — high-frequency 2D bump; **reduced** on mountains and plains/valleys.
6. **Hills** — gentle rolling on warped coords.
7. **Peak lift** — extra height in **mountain zones**; partly **continent-driven**, partly ridge accent (broad summits).
8. **Macro term** — stretch, basin/peak curves, mid-band tweak, **massif boost** on high land.
9. **Base Y** — `sea_level + base_height_offset + floor(sum of above)`.
10. **Plateaus** — optional block lifts from plateau noise (masked).
11. **Valleys / channels / deep canals** — subtract depth in **lowland** mask (gentler exponents).
12. **Clamp** to world min/max build height.

---

## 6. Caves (`fillFromNoise`, when enabled)

If **`caves_enabled`** (config) is true:

- For each column, from a safe minimum Y up to **surface − padding**, sample **two 3D noises** (worm + chamber).
- Where density exceeds **thresholds**, **stone → air**.
- Thresholds **slightly relax** under tall surface (**cave_peak_reduction**) so mountain bases stay more solid.

This is **not** the same as vanilla noodle/cheese carvers; it is **custom 3D noise carving** only.

---

## 7. Surface pass (`buildSurface`)

- Below sea: **fluid** to sea level, **seabed** by biome tag.
- At/above sea: **top block** from biome (grass, sand, gravel in rivers…), **dirt column** under grass where applicable, **snow** if cold.
- **Scree:** if enabled, steep **in-chunk** height steps get **gravel** (and some stone faces) — no cross-chunk lookups.

---

## 8. Configuration “surfaces” (what you can tune)

| Surface | What it controls |
|---------|------------------|
| **`config/larion_lite-common.toml`** | **`terrain_preset`** (built-in profile name), **caves** (on/off, scales, thresholds, padding), **detail_scale**, **detail_amplitude**. |
| **World generator JSON** `terrain` | Full **`FantasyTerrainConfig`**: continent, warp, ridge sizes, **`larion_lite_enhance`** (valleys, channels, scree, **elevation_layers**: plateaus, hills, peak boost, deep canals). |
| **Preset names** | e.g. `dramatic_explorer`, `structure_friendly`, `flat_fantasy`, `tall_ridges`, … — each is a full **`FantasyTerrainConfig`** default in code. |

Runtime rule: **preset (or world JSON) supplies most numbers**; **TOML overrides caves + detail** on top of that merged config (`LarionLiteConfig.resolve`).

---

## 9. Command-line / in-game “tools”

| Command | Function |
|---------|----------|
| **`/larion_lite benchmark`** | Forces generation of an **11×11 chunk** area around the player (default). |
| **`/larion_lite benchmark <radius>`** | **radius** 1–12 → **(2×radius+1)²** chunks; prints **ms** and **chunks/s**. |

Useful for comparing settings or hardware; not required for gameplay.

---

## 10. Source layout (quick map)

```
larion-lite-1.20.1/
  src/main/java/com/badgerson/larionlite/
    LarionLiteMod.java          — mod entry, codec registration, TOML
    FantasyChunkGenerator.java — height, fill, surface, NoiseBundle
    FantasyTerrainConfig.java  — main terrain parameters + codec
    LarionTerrainEnhance.java  — enhance block + codec
    LarionElevationLayers.java — plateau/hill/peak/canal numbers
    FantasyTerrainPresets.java — named presets
    LarionLiteConfig.java      — TOML merge
    LarionLiteCommands.java    — benchmark command
  src/main/resources/data/larion_lite/world_preset/
    performance_overworld.json — Larion overworld preset
```

---

## 11. Summary one-liner

**Larion Lite** registers **`larion_lite:fantasy`**, a **`ChunkGenerator`** that builds the overworld from **2D + 3D `NormalNoise`**, **`smoothstep` masks**, and **config/datapack numbers**, outputting **column heights** then **vanilla-style surface blocks**, with **optional 3D cave carving** and a **benchmark command** — leaving **biomes, features, and other dimensions** to vanilla (or other mods).

---

*Accurate for the codebase under `larion-lite-1.20.1/`; update this file if generation behavior changes.*
