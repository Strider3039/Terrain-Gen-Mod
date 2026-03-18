# Larion Lite — Modpack evaluation brief (current build)

This document describes **what Larion Lite is today**, **how** overworld generation works, and **whether it fits your modpack**. It reflects the **enhanced** terrain system (macro contrast, valleys, scree, peak-aware caves), not the earliest Lite prototype.

---

## 1. What Larion Lite is (current state)

| | |
|--|--|
| **Platform** | **Minecraft 1.20.1**, **Forge** (47.x range) |
| **Mod ID** | `larion_lite` |
| **Purpose** | A **fast fantasy overworld** that **does not** run vanilla’s **noise router + density-function** pipeline for **land shape**. Terrain is still a **single surface Y per column** (structure-friendly, no overhangs). |
| **Look & feel (2025-era build)** | **Strong regional read**: some areas read as **broader basins / floodplains**, others as **sharper highlands** (macro vertical contrast). **Valleys and shallow channel-like lows** (2D noise, not true rivers). **Scree** (gravel/stone) on steep **in-chunk** faces. **Richer lowlands** via continent-weighted detail; optional **second ridge octave** for less repetitive ridgelines. |
| **Relationship to “Larion”** | Full Larion uses a **heavy density tree**. Larion Lite targets **orders of magnitude less math** on the solid terrain pass while keeping a **cinematic, warped** overworld identity. |

Players **opt in** via the bundled world preset (**Larion Lite / Performance overworld**). Default **Normal** world type is unchanged.

---

## 2. What dimensions it touches

| Dimension | Generator |
|-----------|-----------|
| **Overworld** | **Custom** — `larion_lite:fantasy` (`FantasyChunkGenerator`) |
| **Nether** | Vanilla noise + nether preset |
| **End** | Vanilla noise + end preset |

Overworld still uses **`minecraft:overworld`** dimension type and **noise settings** (sea level, default stone/fluid, structure context). **Biomes** = **`multi_noise`**, preset **`minecraft:overworld`**. Structures, ores, trees, and features run **after** Larion’s fill + surface.

---

## 3. End-to-end generation order (Overworld)

1. **Biomes** — Same as vanilla overworld multi_noise; biome mods that target overworld still apply.

2. **`fillFromNoise` (async)**  
   - For each column: compute **`Y_surface`** (full formula in §4).  
   - Stone below, air at/above surface; heightmaps updated.  
   - **Optional caves:** 3D worm + chamber noise; thresholds **rise on tall terrain** (`cave_peak_reduction`) so **peaks get fewer carved voxels** (performance + less “Swiss cheese” mountains).

3. **Vanilla carvers** — **`applyCarvers` is empty** (no cheese/spaghetti/noodles without `NoiseChunk`).

4. **`buildSurface`**  
   - First pass: cache **256** surface heights per chunk (same formula as fill — thread-safe).  
   - Water fill, seabed, grass/sand/gravel by **biome tags**, dirt under grass, snow where cold.  
   - **Scree pass (optional):** on **inner 14×14** columns only, if neighbor height delta ≥ threshold, replace grass/dirt top with **gravel** (or stone on harsher steps). **No cross-chunk height lookups** — fast and deterministic per chunk.

5. **Everything else** — Structures, configured features, etc., as usual.

---

## 4. Surface height formula (current)

Still **one Y per (x, z)**. Uses **Mojang `NormalNoise`**, seeded via **`RandomState`**.

**Warp** — Two 2D samples at `(x,z)` → block offsets → **warped** sample point `(x', z')`.

**Continent** — Sampled at **(x', z')**; value ~[-1, 1]. Let **|c|** = magnitude.

**Macro vertical contrast (B4)** — Effective continent scale per column:

```text
height_variation_eff ≈ height_variation × lerp(1.0, 0.64…1.35, |c|)  (strength from preset: macro_vertical_contrast)
```

So **mid-continent** can read **calmer**, **extreme continent** **taller** — fantasy **basin vs massif** contrast without a new dimension.

**Ridge (main)** — Unwarped `(x,z)`, shaped (square × sign) × `ridge_block_amplitude`.

**Ridge fine (B2)** — Optional second, higher-frequency ridge octave (amplitude **0** disables extra sample).

**Detail (B1)** — Unwarped 2D noise × `detail_amplitude` × **continent blend**: **more** detail in **low-|c|** areas, **smoother** on peaks.

**Valley (C1)** — Low-frequency 2D noise → subtract up to **`valley_depth`** blocks, masked to **low/mid |c|** so mountain tops are not punched down uniformly.

**Channel (C2)** — Paired samples on channel noise → **trough-like** extra depression up to **`channel_depth`**, same lowland mask.

**Combine & clamp**

```text
Y_raw = sea_level + base_height_offset
      + floor(continent × height_variation_eff + ridge + ridge_fine + detail_weighted)
      - valley_term - channel_term
Y_surface = clamp(Y_raw, min_y+2, max_build-2)
```

**Rough cost per land column:** on the order of **~9×** cheap 2D noise samples when valleys/channels enabled (warp×2, continent, ridge, ridge_fine, detail, valley, channel×2); **ridge_fine** skipped if amplitude 0; **valley/channel block** skipped if both depths 0. Still **O(1) per column** — no density tree.

Full parameter set splits between **core `terrain` fields** and optional **`larion_lite_enhance`** (see §7).

---

## 5. Cave system (current)

When **`caves_enabled`** (TOML):

- Band from ~`min_y+6` to **`Y_surface - cave_surface_padding`**.  
- Per stone block: carve if **worm > worm_eff** OR **chamber > chamber_eff**.  
- **Peak bias:** `worm_eff` / `chamber_eff` **increase** on **high** surface (configurable **`cave_peak_reduction`** from preset / JSON). **Fewer air blocks on mountains** → less work and cleaner silhouettes.

No vanilla aquifer-driven water in carved voids; fluids can still come from features.

---

## 6. What Larion Lite does **not** implement

| Gap | Implication |
|-----|-------------|
| **NoiseBasedChunkGenerator** overworld | No vanilla aquifer/cheese/spaghetti/noodles **carvers** on overworld. |
| **Full surface rules** | Tag-based tops + scree, not full JSON `surface_rule` trees. |
| **`getBaseColumn` / structure probes** | Solid stone to surface **ignoring caves** in that API. |
| **True rivers / 3D overhangs** | Channels are **height dips** only; no carved river geometry. |
| **Perfect chunk-border cave continuity** | Possible small seams at borders. |

---

## 7. Configuration layers (current)

**Layer 1 — `config/larion_lite-common.toml`**  
- **`terrain_preset`** — Default **`dramatic_explorer`**. Also **`structure_friendly`** (gentler, good with heavy structure packs — raise **`cave_surface_padding`** to **16–18** in TOML), **`modpack_balanced`**, **`flat_fantasy`**, **`tall_ridges`**, **`dramatic_warp`**, **`mellow`**, **`cave_rich`**.  
- **Empty** `terrain_preset` → shape + enhance come from **world / datapack** `terrain` only.  
- TOML **always overrides** (when merged): **caves on/off**, all **cave** numerics, **`detail_scale` / `detail_amplitude`**, **`cave_surface_padding`**.  
- **Macro, valleys, channels, scree, ridge_fine, cave_peak_reduction** come from the **chosen preset** or from datapack **`larion_lite_enhance`** — not separate TOML keys today.

**Layer 2 — Datapack `terrain` JSON**  
Core fields (`continent_scale`, `height_variation`, …) plus optional nested object:

```json
"larion_lite_enhance": {
  "detail_continent_blend": 1.0,
  "macro_vertical_contrast": 0.78,
  "ridge_fine_amplitude": 10.0,
  "ridge_fine_scale": 0.076,
  "valley_depth": 9,
  "valley_scale": 0.011,
  "channel_depth": 5,
  "channel_scale": 0.016,
  "scree_enabled": true,
  "scree_min_delta": 5,
  "cave_peak_reduction": 0.12
}
```

Omitted → defaults inside codec (matches **dramatic_explorer-style** baseline).

**Layer 3 — Commands**  
`/larion_lite benchmark [radius]` — rough chunk generation timing (test on **fresh** terrain).

**Guardrails** — `cave_chamber_threshold` minimum **0.12** in config spec; very high **`detail_amplitude`** discouraged (>34 blocky).

---

## 8. Modpack fit checklist

**Works well with**

- Overworld biome mods (multi_noise).  
- Structure mods using surface heightmaps; **`structure_friendly`** + higher padding if bases clip.  
- Ore/underground **placed features**.  
- Packs that need **strong fantasy terrain without** full custom density cost.

**Watch out for**

- Mods hard-depending on **vanilla cave topology** or **aquifers**.  
- Mods assuming **air** in `getBaseColumn` below surface.  
- Mods expecting **exact** vanilla surface materials.  
- **cave_chamber_threshold** too low → huge voids and possible lag.

**Server** — Config is **common**; seed + preset + JSON determine terrain.

---

## 9. Summary one-liner

**Larion Lite** replaces **only Overworld land shape** with a **fast column heightmap**: **warp + macro contrast + ridges + continent-weighted detail + optional fine ridge + valley/channel lows + scree**, **optional peak-aware 3D caves**, **vanilla overworld biomes**, **normal Nether/End**, **no vanilla overworld carvers**, **TOML + presets** (`dramatic_explorer` default).

---

## 10. File reference

| Area | File |
|------|--------|
| Generator | `FantasyChunkGenerator.java` |
| Core terrain + codec glue | `FantasyTerrainConfig.java` |
| Enhance block codec | `LarionTerrainEnhance.java` |
| Presets | `FantasyTerrainPresets.java` |
| Forge TOML merge | `LarionLiteConfig.java` |
| World preset | `data/larion_lite/world_preset/performance_overworld.json` |
| Registration / commands | `LarionLiteMod.java`, `LarionLiteCommands.java` |

---

*Aligned with `larion-lite-1.20.1/` as of the enhanced terrain build. For tuning ranges see `LARION_LITE_TUNING_AND_ENHANCEMENT_GUIDE.md`.*
