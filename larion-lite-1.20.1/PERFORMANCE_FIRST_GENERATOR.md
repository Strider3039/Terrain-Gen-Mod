# Performance-first fantasy generator (Larion Lite)

## When to use

Use this path when the **full Larion / vanilla density pipeline** (1.20.1 `NoiseBasedChunkGenerator` + density tree) is **too slow**. This mod replaces **overworld terrain shape** only; biomes stay multi-noise overworld.

## What stays vanilla-ish

- **Biome placement** — `minecraft:overworld` multi_noise preset.
- **Structures & features** — run after base terrain (strongholds, ores, trees, etc.).
- **Nether / End** — unchanged in the bundled world preset.

## What we replace

| Piece | Vanilla / Larion | Larion Lite |
|-------|------------------|-------------|
| Land height | 3D density + noise router | **Column heightmap**: continent + warp + ridge + **detail** + **macro contrast** (effective height range varies by \|continent\|) + **fine ridge** + **valley/channel** subtract (still one Y per XZ) |
| Underground shape | Carvers + aquifers | **Worm + chamber** 3D noises — **stricter thresholds on peaks** (fewer carved blocks, better FPS) |
| Surface | Full surface rules | **Biome-tag tops** + optional **scree** (gravel on steep in-chunk faces) |

## Cost (land)

Per column, roughly **9× NormalNoise 2D/3D samples** for surface (warp×2, continent, ridge, ridge_fine, detail, valley, channel×2) when valleys/channels enabled; **ridge_fine** skipped if amplitude 0; **valley/channel** block skipped if both depths 0. Still **O(1) per column** — no density tree.

**Caves:** 3D loop only from `minY` band to `surface - padding`. **Higher worm/chamber thresholds on tall surface** → less air carved → fewer `setBlockState` calls on mountains.

## Config

`config/larion_lite-common.toml`:

- **`terrain_preset`** — default **`dramatic_explorer`**; **`structure_friendly`** for structure-heavy packs (raise `cave_surface_padding`).
- Cave/detail overrides as before; chamber threshold **≥ 0.12**.

Datapack: optional **`larion_lite_enhance`** inside `terrain` for macro/valley/scree (see README).

## Benchmark

```
/larion_lite benchmark 8
```

## Limits

- No vanilla **carved** caves without a full `NoiseChunk`.
- Cave pockets may **not align perfectly** at chunk borders.
- `getBaseColumn` ignores caves (solid column to surface).

See also: [DEVELOPMENT_MAP_PERFORMANCE_FIRST.md](../DEVELOPMENT_MAP_PERFORMANCE_FIRST.md).
