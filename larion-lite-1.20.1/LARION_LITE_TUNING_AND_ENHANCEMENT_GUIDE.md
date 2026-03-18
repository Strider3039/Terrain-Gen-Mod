# Larion Lite — Terrain tuning & world enhancement guide

Actionable advice for your **existing column-heightmap** system. **No terrain engine replacement.** Ordered by impact.

**Implementation roadmap (phased tasks, code vs pack):** see [LARION_LITE_ENHANCEMENT_IMPLEMENTATION_PLAN.md](LARION_LITE_ENHANCEMENT_IMPLEMENTATION_PLAN.md).

---

## 1. Terrain parameter optimization (primary)

All effects are **multiplicative/interacting**. A change reads differently depending on other knobs.

### `height_variation` (continent vertical range)

| Range | Effect |
|-------|--------|
| **72–90** | Gentler continents; good if structures clip on peaks. |
| **95–115** | **Sweet spot** for “big hills” without constant world ceiling. |
| **118–140** | Very dramatic; more land hits `min_y+2` / `max-2` clamp → **plateaus and flattening** at extremes — can feel *less* varied if overused. |

**Why:** Continent noise is ~[-1,1]. `floor(continent * height_variation)` is the main mass of elevation. Higher = bigger drops between “ocean side” and “highland” — **dramatic silhouettes** at biome/ocean boundaries.

**Anti-repetition:** If the world feels samey, **don’t** only raise this — pair with **stronger warp** so the *pattern* of high/low shifts (see below).

---

### `warp_block_strength`

| Range | Effect |
|-------|--------|
| **280–400** | Subtle fantasy coasts; still readable geography. |
| **440–580** | **Strong identity** — peninsulas, bays, “wrong-way” hills. |
| **620–780** | Extreme; can create **local repetition** (similar warp blobs) if `warp_sample_scale` is too large. |

**Why:** Warp moves the sample point for **continent** noise. Large strength = **non-local** height: neighbors can differ a lot → **cliff-like transitions** in height over short horizontal distance (illusion of escarpments without 3D geometry).

---

### `warp_sample_scale`

| Range | Effect |
|-------|--------|
| **0.022–0.030** | Large, smooth warp blobs — **smoother**, more repetitive macro shape. |
| **0.032–0.042** | **Balanced** — varied fantasy coastlines. |
| **0.044–0.058** | Finer warp — **busier** terrain, less “giant smooth domes.” |

**Why:** Higher scale = faster-varying warp field. **Lower `warp_sample_scale` + high `warp_block_strength`** = huge distorted regions (can feel samey). **Higher scale + moderate strength** = more **broken-up** silhouettes.

**Recommended combo for drama + less repetition:**  
`warp_sample_scale` **0.036–0.044**, `warp_block_strength` **500–620**.

---

### `ridge_block_amplitude`

| Range | Effect |
|-------|--------|
| **18–28** | Rolling hills; ridges read as texture. |
| **32–48** | **Clear ridgelines** — good for silhouettes against sky. |
| **52–68** | Harsh; can dominate continent + feel “washboard” if `ridge_scale` is wrong. |

**Why:** Ridge uses **unwarped** (x,z) — adds **directional** structure warp doesn’t give. That breaks pure radial symmetry from warp+continent.

---

### `ridge_scale`

| Range | Effect |
|-------|--------|
| **0.038–0.048** | Wide ridges — **layered** look at distance (long shadows on shader). |
| **0.050–0.062** | **Default-adjacent**; good detail at 100–200 block view. |
| **0.064–0.080** | Tight ripples; can look noisy next to smooth warp if amplitude high. |

**Why:** Smaller scale = higher frequency ridges. **Pair wide ridges (low scale number → wait, in your code scale multiplies xz: larger scale value = higher frequency)** — in Larion Lite, `ridge(worldX * ridge_scale)` so **larger `ridge_scale` = finer ridges**.  

Correcting:

- **Lower `ridge_scale` (e.g. 0.040–0.048)** → **broader** ridge hills (slower change in XZ) → **layered plateaus** feel.  
- **Higher `ridge_scale` (0.055–0.070)** → **tighter** ripples.

**For dramatic silhouettes:** **lower ridge_scale + medium-high amplitude (36–44)**.

---

### `detail_amplitude` & `detail_scale`

| | |
|--|--|
| **detail_amplitude 10–14** | Subtle local bumpiness; **structure-safe**. |
| **detail_amplitude 16–22** | **Strong** micro-relief; exploration feels less flat on foot. |
| **detail_amplitude 24–32** | Very choppy; **steep “fake cliffs”** between adjacent columns (1-block steps) — can look blocky; test with shaders. |

| **detail_scale** | |
| **0.07–0.088** | Broader bumps (fewer pimples per chunk). |
| **0.090–0.11** | **Default-adjacent**; good variety. |
| **0.12–0.16** | Fine grain; with high amplitude → **busy** terrain. |

**Why:** Detail is **unwarped** 2D — it adds **high-frequency** breakup so the heightmap doesn’t look like one smooth function. **That’s your main anti-smoothness lever** without touching caves.

**Cost:** ~1 extra `NormalNoise` sample per column — **negligible** vs cave loop.

---

### One-line “dramatic fantasy” starter (TOML / preset base)

| Parameter | Suggested start |
|-----------|-----------------|
| height_variation | **108–118** |
| warp_block_strength | **540–600** |
| warp_sample_scale | **0.038–0.042** |
| ridge_block_amplitude | **38–46** |
| ridge_scale | **0.048–0.056** |
| detail_amplitude | **18–22** |
| detail_scale | **0.088–0.098** |
| base_height_offset | **12–18** (slightly more land above sea) |

Tune **down** if structures float or sea is too rare; **up** warp_strength before maxing height_variation (avoids ceiling clamp).

---

## 2. Illusion of complexity (heightmap-only)

You cannot have **true overhangs**. You *can* sell **cliffs, layers, drama** with:

### A. Parameter interaction (no code)

1. **Cliff illusion:** **High `warp_block_strength`** + **moderate `warp_sample_scale`** → height changes fast laterally → **steep 1-block “stair” faces** read as cliffs from distance, especially with **lighting + shader ambient occlusion**.

2. **Layered terrain:** **Low `ridge_scale` (broad ridges)** + **moderate continent** → parallel “steps” of elevation → **terrace illusion**. **Detail** at **medium amplitude** roughens each layer.

3. **Silhouettes:** **Raise `ridge_block_amplitude`** on **open biomes** (plains edges) — you can’t do per-biome height in stock Larion Lite, but globally higher ridges + **tree line** from mods = readable skyline.

### B. Surface tricks (already in mod)

- **Snow on cold biomes** breaks flat color.
- **Sand/gravel/beach** at low coastal transition — reads as **shore complexity**.

**Future small code win (optional):** multiply `detail_amplitude` by a smooth function of **|continent|** so **flat lowlands** get *more* detail and **peaks** stay smoother → **valley richness** without 3D.

### C. Biome blending (pack-level)

- Use biome mods that add **edge biomes** (river banks, sparse forest strips) — **color and tree line** change where height is flat → brain reads “complexity.”
- Avoid **single-block biome stripes** in datapacks; they read as noise chaos (see §4).

---

## 3. River & valley (heightmap-only, honest limits)

**Current generator:** no dedicated river carve. Valleys are **where continent+warp+ridge **happen** to sit low**.

### What you can do **without** new code

1. **Bias elevation down near sea:** `base_height_offset` **+8 to +14** with **height_variation 100–110** → more area **near sea level** → **wider floodplains**; rivers from **feature placement** (vanilla/mod river biomes) read naturally in those flats.

2. **Valley “basins”:** **Slightly lower `warp_sample_scale`** (0.032–0.036) with **high warp strength** creates **large enclosed lows** — feels basin-like when water or wetland biomes spawn there.

### What needs a **small code addition** (if you add later)

- **River mask:** 2D noise channel **subtracted** from height only where another noise marks “channel” → linear lows.  
- **Valley carve:** `height -= max(0, valley_noise) * depth` with valley noise **low frequency** along one axis (expensive to fake oriented rivers without 2D ridge of minima).

Until then: **rely on BYG/river biomes + structure mods** that place bridges and docks in **low flat** areas — heightmap will supply the flats.

---

## 4. Biome & terrain synergy (BYG-style packs)

**Larion Lite height does not vary by biome** — BYG biomes sit on **your** hills. Cohesion is **visual + placement**, not height correlation.

| Technique | Why it helps |
|-----------|--------------|
| **Reduce biome size / increase rarity** in BYG config | Smaller patches = **fewer jarring jumps** from “mesa on peak” to “tundra in hole” in one view. |
| **Prefer biome mods that add transition biomes** | Forest→sparse→plains reads **intentional**. |
| **Match temperature/humidity** to similar **vertical bands** indirectly | You can’t tie height to biome in Lite; instead **tune continent scale** so **large regions** share similar elevation → biome noise at similar scale **aligns visually** (both vary over ~800–2000 blocks). |
| **Avoid stacking** two mods that both **shred** overworld biome source | One strong overworld biome layer + datapack tweaks beats **three** independent noise layers. |

**Jarring transitions:** Often **color** not height. Use **leaf carpet / fallen trees / short grass strips** on biome edges (feature mods or datapack).

---

## 5. Environmental density (high impact, low cost)

| Approach | Cost | Note |
|----------|------|------|
| **Increase tree/feature density in biome datapacks** | Low at gen; **moderate** FPS if insane — cap ~1.2× vanilla per biome. | Reads **scale** — forest hides heightmap simplicity. |
| **Undergrowth mods** (ferns, flowers, short grass packs) | Very low | Fills **foot-level** emptiness. |
| **Wilder Wild / similar** (if on Forge 1.20.1) | Check compat | Bushes, fallen logs **break flat ground**. |
| **Geophilic, Terrablender-friendly foliage** | Low | Surface variation without new blocks. |
| **Avoid** global “more structures everywhere” + **max trees** — chunk feature stage spikes. |

**Rule:** Prefer **many small features** over **few huge trees** for FPS — **perceived density** goes up with ground cover.

---

## 6. Structure integration (no terrain code change)

| Goal | Action |
|------|--------|
| **More to explore** | Add **YUNG’s** suites, **Explorify**, **When Dungeons Arise**, **Towns and Towers** — **jigsaw** structures use **surface height**; your **column heightmap** is **continuous** → they **sit on top**; steep **1-block steps** may clip bases on worst slopes. |
| **Fit slopes** | Choose structure mods with **terrain matching** / **foundation** behavior (many YUNG packs adjust). |
| **Reduce floating** | Your **`cave_surface_padding`** already protects topsoil — raise to **14–18** if basements break through. |
| **Villages on hills** | **TerraBlender** doesn’t fix placement — use **structure mods** that **flatten** small pads OR accept **terraced** villages (often looks good). |

**Frequency:** Datapack `spacing` / `separation` for vanilla structures; mod configs for modded. **Double dungeon frequency** is usually cheaper than **double villages**.

---

## 7. Visual amplification (shaders & mods)

| Tool | Impact | FPS |
|------|--------|-----|
| **Complementary / Photon / BSL** with **strong AO** | **Cliff edges and column steps** read as depth | Medium |
| **Distant Horizons** (if supported) | **Scale** — mountains read huge | Configurable LOD cost |
| **Fog:** raise **start distance** slightly + **denser horizon fog** | Hides **repetitive mid-distance** height pattern | Low |
| **Entity / block light tweak** | Valleys darker = **layering** | Low |

**Minimal FPS:** **Iris + short render distance + good AO** beats 32 chunks flat.

---

## 8. Performance safeguards

### What **does** cost time

| Factor | Impact |
|--------|--------|
| **Cave band height** | `(surface - padding - minY) × 256 columns × 2` 3D samples. **Taller worlds + high surface** = more blocks. |
| **Very low `cave_threshold`** | More **air** → slightly more **light updates / later features** — minor. |
| **Extreme `height_variation`** | **No extra land cost** — height is still **one column evaluation**. **Cave cost rises** with taller columns. |

### Safe limits (practical)

| Parameter | Avoid |
|-----------|--------|
| `detail_amplitude` | **> 36** — blocky chaos; marginal visual gain. |
| `cave_chamber_threshold` | **< 0.10** — Swiss cheese; feature spam underground. |
| `cave_surface_padding` | **< 6** — surface collapse + structure issues. |
| Simultaneous | **max height_variation + max warp + max caves** on **low-end server** — benchmark `/larion_lite benchmark 8` before ship. |

### What **does not** hurt chunk gen much

- **`height_variation`, warp, ridge, detail`** — all **O(1) per column** for land shape.

---

## Priority summary

1. **Tune warp + ridge + detail** (§1) for drama without ceiling clamp.  
2. **Dense ground cover + edge biomes** (§4–5) for “handcrafted” feel.  
3. **Shader AO + fog** (§7) for cliffs and scale.  
4. **Structure packs + padding** (§6) for exploration.  
5. **Cave thresholds** — only after land feels right; watch §8.

---

*Aligned with Larion Lite `FantasyTerrainConfig` + `larion_lite-common.toml`. Heightmap limits acknowledged; river carve = future optional feature.*
