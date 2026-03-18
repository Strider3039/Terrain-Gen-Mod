# Larion Lite — Enhancement implementation plan

Derived from [LARION_LITE_TUNING_AND_ENHANCEMENT_GUIDE.md](LARION_LITE_TUNING_AND_ENHANCEMENT_GUIDE.md).  
**Scope:** Build on the existing heightmap generator — **no replacement** of core pipeline.

---

## How to use this document

| Track | Who | Artifact |
|-------|-----|----------|
| **A — Config & presets** | Mod author / pack author | `FantasyTerrainConfig`, TOML defaults, named presets |
| **B — Generator code** | Mod author | `FantasyChunkGenerator`, `FantasyTerrainConfig`, codecs |
| **C — Modpack playbook** | Pack author | Mod list, configs, datapacks (outside jar) |
| **D — QA & performance** | Both | Benchmarks, limits doc |

---

## Phase A — Terrain parameter productization (PRIMARY)

**Goal:** Ship **guide-aligned** defaults and **named presets** so players/packs get dramatic fantasy terrain without manual tuning.

| ID | Task | Actions | Done when |
|----|------|---------|-----------|
| A1 | **Baseline preset** `dramatic_explorer` | Encode guide §1 “dramatic fantasy starter” into `FantasyTerrainPresets` (height_variation 108–118, warp 540/0.040, ridge 42/0.052, detail 20/0.093, base_offset 14–16). Document in README. | Preset selectable via `terrain_preset`; new world shows visibly stronger silhouettes vs old default |
| A2 | **Safe preset** `structure_friendly` | Gentler: height_variation 88–95, warp_strength 380–440, detail_amplitude 12–14, cave_surface_padding 16–18. | Structures/villages clip less on peaks |
| A3 | **Default TOML alignment** | Set `LarionLiteConfig` default ranges to **match** A1 OR keep current defaults but add comment block pointing to presets | First-run `larion_lite-common.toml` comments list presets + link to guide |
| A4 | **Preset matrix doc** | Table: preset name → use case (exploration / servers / modpack heavy structures). Add to `LARION_LITE_TUNING_AND_ENHANCEMENT_GUIDE.md` appendix or README. | Pack authors pick preset in one line |

**Dependencies:** None.  
**Risk:** Existing worlds keep old level.dat terrain — document “new preset affects **new chunks** / new worlds.”

---

## Phase B — Illusion of complexity (code, LOW incremental cost)

**Goal:** Heightmap-only improvements from guide §2B + §8.

| ID | Task | Actions | Done when |
|----|------|---------|-----------|
| B1 | **Continent-weighted detail** | In `surfaceY`: `detailEff = detail_noise * detail_amplitude * f(|continent|)` with e.g. `f(c) = 0.35 + 0.65 * smoothstep(0.2, 0.85, c)` so **lowlands** get more micro-relief, **peaks** stay smoother. Add optional `detail_continent_blend` (0–1) in config + codec default 1.0 = on. | Visually richer valleys; peaks less noisy |
| B2 | **Optional second ridge octave** | One extra 2D noise at **higher frequency**, amplitude **8–14** blocks (configurable), **unwarped** — breaks ridge periodicity. Field: `ridge_fine_amplitude`, `ridge_fine_scale`. **Cost:** +1 NormalNoise, +1 sample/column. | Less “washboard” on long hikes |
| B3 | **Surface polish (cheap)** | Extend `buildSurface`: thin **gravel/stone strip** on steep vertical neighbor columns (height delta ≥ N in 1 block) — **optional** flag `steep_slope_scree` — 2–4 block wide. Uses only chunk heightmap diff. | Cliff feet read more natural |

**Dependencies:** B1 before B2 recommended (avoid stacking too much high-freq).  
**Performance:** B1–B2 stay **O(columns)**; B3 O(256×4) neighbor checks — negligible.

---

## Phase C — Rivers & valleys (code, MEDIUM)

**Goal:** Guide §3 — natural **linear lows** without full erosion.

| ID | Task | Actions | Done when |
|----|------|---------|-----------|
| C1 | **Valley depression field** | 2D low-frequency noise `valley(x,z)` in [0,1]; subtract `valley_depth * smooth(valley)` from surface Y (valley_depth 4–16 blocks, config). Carve only where valley > threshold **and** continent below median-ish — avoids sinking whole map. | Wider **basin** feel; river biomes sit lower |
| C2 | **Channel mask (v1)** | Second 2D noise designed as **elongated** features: sample at `(x,z)` and `(x,z)+perp_offset` min/max to fake **valley axis** OR use `abs(noise_a - noise_b)` for **trough lines**. Subtract **channel_depth** along trough. Tune for **~3–8 block** drops, width ~20–40 blocks. | Visible **dry washes** / river beds when paired with water features |
| C3 | **Integration** | Document interaction with **sea level** and **cave_surface_padding**; ensure `getBaseHeight` uses same formula. | Structures see depressed channels |

**Dependencies:** C1 before C2.  
**Performance:** +1–2 2D samples per column — **still O(columns)**.

---

## Phase D — Biome–terrain synergy (OPTIONAL code + pack)

**Goal:** Guide §4 — more **intentional** feel.

| ID | Task | Actions | Done when |
|----|------|---------|-----------|
| D1 | **Pack-only** | Publish **recommended BYG/TerraBlender** settings (biome size, transition biomes). Checklist in modpack doc. | No code |
| D2 | **Optional height bias from climate sampler** | If performance acceptable: in `surfaceY`, add `+ floor((temperature - 0.5) * temp_height_bias)` and/or humidity term **small** (±4 blocks max) using `randomState.sampler()` at column — **must not** break structure stability (keep bias small). Feature-flag in config default **off**. | Cold reads slightly higher when enabled; subtle cohesion |

**Dependencies:** D2 requires careful testing with **/locate** and structure mods.

---

## Phase E — Environmental density & structures (PACK + light doc)

**Goal:** Guide §5–6 — **no core terrain change**.

| ID | Task | Actions | Done when |
|----|------|---------|-----------|
| E1 | **Modpack template** | Curated list: undergrowth mod, ≤1.2× tree density rule, YUNG/Explorify spacing notes, `cave_surface_padding` 14–18 for dungeon mods. | `MODPACK_TEMPLATE.md` in repo |
| E2 | **In-game tip** | Optional `/larion_lite tips` or book — links preset + padding for structure-heavy packs | Nice-to-have |

---

## Phase F — Visual & performance (QA)

**Goal:** Guide §7–8.

| ID | Task | Actions | Done when |
|----|------|---------|-----------|
| F1 | **Benchmark budget** | Document target: e.g. “≥ X chunks/s on mid PC with `dramatic_explorer` + default caves” using `/larion_lite benchmark 8`. Record in PERFORMANCE_FIRST or CI note. | Regression visible if cave threshold abused |
| F2 | **Guardrails in config UI comments** | Already partially done — extend with **hard warnings** for `cave_chamber_threshold < 0.12`, `detail_amplitude > 34`. | TOML self-documents risk |
| F3 | **Shader appendix** | Link Complementary/Photon AO + fog tips (pack author doc). | Copy-paste for modpack page |

---

## Recommended implementation order

| Order | Phase | Rationale |
|-------|-------|-----------|
| 1 | **A** | Immediate player value; zero risk |
| 2 | **B1** | High visual ROI, one formula |
| 3 | **C1** | Valleys without full rivers |
| 4 | **F1–F2** | Before shipping C2/D2 |
| 5 | **B2, B3** | Polish |
| 6 | **C2** | Rivers/washes — hardest to tune |
| 7 | **D2** | Only if pack needs biome-height cohesion |
| 8 | **E** | Parallel pack work |

---

## Milestone summary

| Milestone | Includes | Pack-facing outcome |
|-----------|----------|---------------------|
| **M1** | A complete | “Pick a preset, play” |
| **M2** | A + B1 + F2 | Richer lowlands, documented limits |
| **M3** | + C1 | Valley basins, better river biome fit |
| **M4** | + C2 + B2 | Linear channels + detail ridges |
| **M5** | E1 + D optional | Full modpack story |

---

## Traceability to tuning guide

| Guide § | Plan phases |
|---------|-------------|
| §1 Parameter ranges | **A** (presets), **F2** (guardrails) |
| §2 Illusion of complexity | **B1–B3** |
| §3 Rivers & valleys | **C1–C3** |
| §4 Biome synergy | **D1–D2** |
| §5 Environmental density | **E1** |
| §6 Structures | **E1**, **A2** preset |
| §7 Visual | **F3** |
| §8 Performance | **F1–F2** |

---

*Maintainer: update checkboxes as tasks land. Source guide: `LARION_LITE_TUNING_AND_ENHANCEMENT_GUIDE.md`.*
