# Larion Lite — Enhancement Implementation Plan (Refined)

# Phase A — Terrain Parameter Productization (PRIMARY)

**Goal:** Ship strong defaults and presets so terrain feels “epic” immediately.

### A1 — Baseline preset `dramatic_explorer`

* height_variation: **108–120**
* warp_strength: **520–580**
* warp_scale: **0.035–0.045**
* ridge_amplitude: **38–46**
* ridge_scale: **0.045–0.060**
* detail_amplitude: **18–24**
* detail_scale: **0.085–0.110**
* base_offset: **14–18**

**Done when:**

* Immediate large silhouettes
* No flat-feeling regions

---

### A2 — Safe preset `structure_friendly`

* height_variation: **85–95**
* warp_strength: **380–440**
* detail_amplitude: **10–14**
* cave_surface_padding: **16–18**

---

### A3 — Default TOML alignment

* Default = dramatic OR clearly documented presets
* Comments explain tradeoffs

---

### A4 — Preset matrix doc

| Preset             | Use Case              |
| ------------------ | --------------------- |
| dramatic_explorer  | cinematic exploration |
| structure_friendly | heavy structure packs |

---

# Phase B — Terrain Identity & Illusion of Complexity (CORE)

**Goal:** Improve perceived terrain richness without increasing cost.

---

### B1 — Continent-weighted detail

```java
detailEff = detail * amplitude * (0.35 + 0.65 * smoothstep(...))
```

* More detail in lowlands
* Cleaner peaks

---

### B2 — Secondary ridge octave

* amplitude: **8–14**
* higher frequency
* unwarped

**Effect:**

* breaks repetition
* adds realism at distance

---

### B3 — Surface polish (scree + cliffs)

* detect steep neighbor deltas
* add gravel/stone transitions

---

### 🔥 B4 — Elevation bias zones (NEW — HIGH IMPACT)

**Purpose:** Add macro-scale variation (THIS WAS MISSING)

```java
height_variation_eff = height_variation * (0.7 + 0.6 * smoothstep(continent))
```

**Result:**

* some regions → massive mountains
* some → flatter basins

👉 This creates **true fantasy scale contrast**

---

# Phase C — Rivers & Valleys (MEDIUM)

---

### C1 — Valley depression field

* subtract 4–16 blocks based on low-frequency noise
* only apply in mid/low continents

---

### C2 — Channel mask (improved)

Current:

* trough via noise differences

### 🔥 Upgrade:

Add **directional bias**

Options:

* gradient-based direction
* paired noise vector field

**Goal:**

* rivers feel like they “flow”
* not random cuts

---

### C3 — Integration

* consistent with:

  * getBaseHeight
  * sea level
  * structures

---

### 🔥 C4 — Cave distribution bias (NEW)

* increase caves in valleys
* reduce caves in peaks

**Effect:**

* reinforces terrain readability
* improves exploration feel

---

# Phase D — Biome–Terrain Synergy (OPTIONAL)

---

### D1 — Pack-only config

* biome size tuning
* smoother transitions
* avoid noise chaos

---

### D2 — Optional climate height bias

* ±2–4 blocks max
* based on temperature/humidity

**Default: OFF**

---

# Phase E — Environmental Density (CRITICAL FOR VISUALS)

> ⚠️ This is NOT optional for a fantasy pack

---

### 🔥 E1 — Required mod pairing (UPGRADED)

Minimum:

* Oh The Trees You’ll Grow
* optional undergrowth mod

---

### E2 — Density rules

* tree density: **≤1.2x vanilla baseline**
* biome-specific variation
* avoid overclutter

---

### E3 — Terrain filling

* rocks
* bushes
* ground clutter

**Goal:**

* eliminate “empty terrain syndrome”

---

# Phase F — Performance & Consistency

---

### F1 — Benchmark budget

* define chunks/sec target
* test with:

  * dramatic preset
  * caves enabled

---

### F2 — Config guardrails

Warn against:

* cave_chamber_threshold < 0.12
* detail_amplitude > 34

---

### 🔥 F3 — Consistency enforcement (NEW)

Add limits to prevent:

* overly flat regions
* dead zones

Examples:

* minimum height variation floor
* minimum warp influence

---

### F4 — Shader appendix

* Complementary / Fantasy Reimagined
* fog + AO tuning

---

# Recommended Implementation Order

| Order | Phase   | Why                    |
| ----- | ------- | ---------------------- |
| 1     | A       | Immediate impact       |
| 2     | B1 + B4 | biggest visual upgrade |
| 3     | C1      | valleys                |
| 4     | F1–F2   | safety                 |
| 5     | B2–B3   | polish                 |
| 6     | C2      | river shaping          |
| 7     | C4      | cave integration       |
| 8     | D2      | optional               |
| 9     | E       | pack completion        |

---

# Milestones

| Milestone | Includes  | Result                  |
| --------- | --------- | ----------------------- |
| M1        | A         | usable presets          |
| M2        | + B1 + B4 | strong terrain identity |
| M3        | + C1      | valley systems          |
| M4        | + C2 + B2 | advanced terrain detail |
| M5        | + E + C4  | full fantasy world      |

---

# Key Design Principles (Summary)

* Prioritize **macro variation first**, then detail
* Use **illusion over simulation**
* Maintain **O(columns) performance**
* Ensure **consistent visual quality**, not rare peaks
* Let **trees + shaders amplify terrain**, not replace it

---

# Final Outcome

This system will produce:

* consistent cinematic landscapes
* strong regional identity (mountains vs basins)
* natural-feeling valleys and traversal paths
* high performance for multiplayer

---

**End Goal:**

> A terrain system that *feels handcrafted*, not randomly generated
