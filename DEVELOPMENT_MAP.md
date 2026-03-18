# Larion 1.20.1-New: Step-by-Step Development Map

This map turns the [Implementation Plan](IMPLEMENTATION_PLAN.md) into ordered, actionable steps for building the Larion mod in **larion-world-generation-1.20.1-New**. Each step has a clear deliverable and dependency order. Reference: [LARION_ARCHITECTURE_REPORT.md](LARION_ARCHITECTURE_REPORT.md).

---

## Limitations (from Implementation Plan §7 and §4)

The development map **must** respect these; do not add steps that violate them:

| Constraint | What it means for this map |
|------------|----------------------------|
| **Wrap context only** | FlatDomainWarp uses **WarpedContextProvider** + **WarpedFunctionContext** (override only blockX/blockZ). **No** bare `SinglePointContext` in the batched path (causes ocean-only bug). |
| **No context replacement** | We do not replace the engine’s `FunctionContext` with a new context type; we only wrap it. |
| **Keep density tree** | Do not change the density tree structure or remove `flat_domain_warp` to “simplify” terrain. |
| **No thread-local/pooled buffers** | Do not add thread-local or pooled buffers unless there is a measured need and documented reentrancy/thread-safety story. Phase 6 only tightens existing code. |
| **Single pipeline only** | No second pipeline (e.g. custom ChunkGenerator) until Phase 7 shows the primary pipeline is insufficient; then see IMPLEMENTATION_PLAN §5. |

Batching and correctness rules from the plan: **FlatDomainWarp** = batch warp X/Z then `input.fillArray(warpedProvider)`; **Division** = batch both args then one divide loop; **Sqrt** = NaN-safe min/max (input ≤ 0 → 0); **Division** = divide-by-zero → 0. Constants (cave bound 40, chunk section max) in one place.

---

## Phase 0: Build and config (already done)

- [x] Forge 1.20.1 MDK project with `gradlew.bat` and working build.
- [x] Mod identity in `gradle.properties`: `mod_id=larion_new`, `mod_group_id=com.badgerson.larion`.
- [x] Minimal `LarionMod` entry point; `settings.gradle` valid (pluginManagement first, rootProject.name last).

---

## Phase 1: Foundation for worldgen

### 1.1 Mixin setup

- [x] **Add Mixin dependency and plugin**  
  In `build.gradle`: apply MixinGradle (or Forge’s mixin config) so mixins are processed. Ensure `refmap` is generated.
- [x] **Create mixin config**  
  `src/main/resources/larion_new.mixins.json`: package `com.badgerson.larion.mixin`, list the five mixins (to be implemented in §3), client + server where relevant.
- **Deliverable:** `./gradlew build` runs with mixin plugin; refmap path correct in config.

### 1.2 Access transformer

- [x] **Create Forge access transformer**  
  `src/main/resources/META-INF/accesstransformer.cfg` with:
  - `public net.minecraft.world.level.levelgen.DensityFunctions makeCodec(Lcom/mojang/serialization/MapCodec;)Lnet/minecraft/util/KeyDispatchDataCodec;`
  - `public net.minecraft.world.level.levelgen.DensityFunctions NOISE_VALUE_CODEC`
  - `public net.minecraft.world.level.levelgen.DensityFunctions$PureTransformer`
  - SurfaceRules: `LazyXZCondition`, `Condition`, `Context`, `Context$SteepMaterialCondition`; `Context` fields: `blockX`, `blockZ`, `chunk`.
- [x] **Enable AT in build**  
  In `build.gradle` minecraft block: `accessTransformer = file('src/main/resources/META-INF/accesstransformer.cfg')`.
- **Deliverable:** Code can call `DensityFunctions.makeCodec(MAP_CODEC)` and SurfaceRules internals without compile errors.

### 1.3 Constants

- [x] **Add shared constants**  
  Class (e.g. `Constants` or in `LarionMod`): `MOD_ID`, `CAVE_BOUND_OVERRIDE = 40`, `CHUNK_SECTION_MAX = 15` (for mixins and predicates).
- **Deliverable:** Single place for mod id and worldgen constants; used later by mixins and density code.

---

## Phase 2: Registration and density function types

### 2.1 Registry registration

- [x] **Register density function types**  
  In `LarionMod`: `DeferredRegister` for `Registries.DENSITY_FUNCTION_TYPE` (namespace `larion`); register 7 types: `larion:div`, `larion:sqrt`, `larion:signum`, `larion:sine`, `larion:x`, `larion:z`, `larion:flat_domain_warp` (each mapping to its codec).
- [x] **Register material condition**  
  `DeferredRegister` for `Registries.MATERIAL_CONDITION` (namespace `larion`); register `larion:somewhat_steep` (implemented in §4).
- **Deliverable:** Mod loads and registries contain Larion types; JSON can reference `larion:...` once data is in place.

### 2.2 Simple density types (no warp)

Implement in `com.badgerson.larion.density_function_types`; each has `CODEC = DensityFunctions.makeCodec(MAP_CODEC)` (after AT) and implements `DensityFunction`.

- [x] **XCoord** – `blockX` from context, clamped to world bounds; `KeyDispatchDataCodec.of(MapCodec.unit(...))`.
- [x] **ZCoord** – same for `blockZ`.
- [x] **Signum** – one argument; extends `PureTransformer`; signum(argument).
- [x] **Sine** – one argument; extends `PureTransformer`; sin(argument).
- [x] **Sqrt** – one argument; extends `PureTransformer`; sqrt(argument); **minValue/maxValue**: if input ≤ 0 then 0 (NaN-safe).
- [x] **Division** – two arguments (dividend, divisor). **compute:** divide, 0 if divisor == 0. **fillArray:** batch both args into two temp arrays, one divide loop (0 where divisor == 0). Use `final int len = densities.length`.
- **Deliverable:** All six types compile, serialize with codec; Division uses batched fillArray.

### 2.3 FlatDomainWarp (warped sampling)

- [x] **WarpedFunctionContext** – Wraps `DensityFunction.FunctionContext`; overrides only `blockX()` and `blockZ()` with warped values; delegates rest to inner context.
- [x] **WarpedContextProvider** – Implements `ContextProvider`; wraps engine provider; `forIndex(i)` returns `WarpedFunctionContext` with warp X/Z from pre-filled arrays.
- [x] **FlatDomainWarp** – Fields: `input`, `warpX`, `warpZ` (all DensityFunction). **fillArray:** batch warp X/Z, then `input.fillArray(densities, warpedProvider)`. **compute:** use `WarpedFunctionContext` (no SinglePointContext). **CODEC:** `DensityFunctions.makeCodec(MAP_CODEC)`.
- **Deliverable:** No use of bare `SinglePointContext` in batched path; terrain shape correct when used as root of a density tree (no ocean-only bug).

---

## Phase 3: Mixins

Implement in `com.badgerson.larion.mixin`; all listed in `larion_new.mixins.json`.

- [x] **NoiseChunkGeneratorMixin** – Target: `NoiseBasedChunkGenerator`. Inject into `createFluidPicker`; lava level = `noiseSettings.minY() + 10`; water from settings.
- [x] **CaveWorldCarverMixin** – Target: `CaveWorldCarver`. Inject into `getCaveBound()`; return `Constants.CAVE_BOUND_OVERRIDE` (20; less vertical carving = faster gen).
- [x] **SurfaceRulesContextAccessor** – Interface on `SurfaceRules.Context`: accessors `larion$getBlockX`, `larion$getBlockZ`, `larion$getChunk`.
- [x] **SteepSlopePredicateMixin** – Target: `SurfaceRules.Context.SteepMaterialCondition`. Replace compute with WORLD_SURFACE_WG at (x,z) vs 4 neighbors; steep if difference > 3.
- [x] **MaterialRuleContextMixin** – Target: `SurfaceRules.Context`. Implements `MaterialRuleContextExtensions`; constructor tail sets `somewhatSteepSlopePredicate` = new `SomewhatSteepSlopePredicate(context)`.
- **Deliverable:** Lava at minY+10, deeper caves, steep/somewhat steep surface behavior; no runtime errors from missing accessors.

---

## Phase 4: Surface rules (somewhat steep)

- [x] **SomewhatSteepSlopePredicate** – Implements slope check: “height difference to a neighbor > 1 block” (uses SurfaceRulesContextAccessor for chunk/position). Used by material condition.
- [x] **SomewhatSteepMaterialCondition** – Implements `SurfaceRules.ConditionSource`; codec for `larion:somewhat_steep`; apply() returns predicate from context via `MaterialRuleContextExtensions`.
- [x] **MaterialRuleContextExtensions** – Interface to get `somewhatSteepSlopePredicate` from context (implemented by MaterialRuleContextMixin).
- **Deliverable:** Surface rules JSON can use `larion:somewhat_steep`; cliff/steep placement matches design.

---

## Phase 5: Worldgen data

Copy/adapt from **larion-world-generation-1.20.1** `common/src/main/resources/data`. Namespace: keep `larion_new` for mod id; worldgen can stay `larion` for density/noise IDs if desired (or align with mod id—see plan).

### 5.1 Dimension and noise_settings

- [x] **Dimension types** – `overworld.json` / `overworld_caves.json`: **height 576, min_y -64** (shallower than -128/640 for faster chunk gen; surface still to ~y511).
- [x] **Noise settings** – `noise_settings/overworld.json` + `rworld.json`: same bounds; bottom density gradients aligned (-64..-40). After `copy-phase5-data.ps1`, re-apply bounds if source overwrote them.

### 5.2 Minecraft overworld redirects

- [x] **Redirect density functions** – Under `data/minecraft/worldgen/density_function/overworld/`: continents, depth, erosion, ridges, sloped_cheese, jaggedness (all point to `larion:overworld/...`); caves/spaghetti_2d.json and caves/noodle.json (vanilla cave density) present.

### 5.3 Larion density and noise roots

- [x] **Larion density + noise** – `data/larion/worldgen/density_function/overworld/` (full tree) and `data/larion/worldgen/noise/` (continents, terrain, vegetation, ridges, caves, erosion, temperature, etc.). To refresh or set up from scratch, run **PowerShell:** `.\copy-phase5-data.ps1` from **larion-world-generation-1.20.1-New** (requires `../larion-world-generation-1.20.1` for noise + optional minecraft worldgen copy).
- **Deliverable:** Larion terrain with warped coastlines; **performance preset**: -64 floor, narrower cave carver bound, X/Z coord batched `fillArray`. Deeper world: restore min_y -128, height 640, `CAVE_BOUND_OVERRIDE` 40, and matching JSON gradients.

### 5.4 Carvers (optional)

- [ ] **Configured carvers** – Included in the data copy if present in source. Cave bound is already overridden by mixin.

---

## Phase 6: Implementation plan Phase 2 (small optimizations)

- [x] Review all density types for redundant work (e.g. repeated `.length`, extra allocations); tighten only where obviously safe.
- [x] Ensure every custom type that can implement `fillArray` does so (no accidental fallback to fillAllDirectly in hot paths).
- [x] Add short comments in FlatDomainWarp and Division referencing IMPLEMENTATION_PLAN (wrap context, batch where possible).
- **Deliverable:** Same behavior, slightly lower overhead; code remains easy to reason about.

---

## Phase 7: Implementation plan Phase 3 (measure and decide)

- [x] **Benchmark command** – `/larion benchmark [radius]` forces generation of N chunks (radius 1–12, default 5 → 11×11 = 121 chunks), times it, and reports ms and chunks/s. Registered on Forge `RegisterCommandsEvent`; see [PHASE7-BENCHMARK.md](larion-world-generation-1.20.1-New/PHASE7-BENCHMARK.md).
- [ ] Compare subjectively with 1.20.6 if available (optional).
- [x] **If acceptable:** Close plan; maintain primary pipeline only. *(Decision: performance acceptable.)*
- [x] **Performance-first track (optional mod):** **[larion-lite-1.20.1](larion-lite-1.20.1/)** — world preset + `FantasyChunkGenerator`; see [DEVELOPMENT_MAP_PERFORMANCE_FIRST.md](DEVELOPMENT_MAP_PERFORMANCE_FIRST.md).

---

## Dependency summary

| Phase | Depends on |
|-------|------------|
| 1.1 Mixin | — |
| 1.2 AT | — |
| 1.3 Constants | — |
| 2.1 Registration | 1.2, 1.3 |
| 2.2 Simple density types | 1.2, 2.1 |
| 2.3 FlatDomainWarp | 1.2, 2.1 |
| 3 Mixins | 1.1, 1.2, 1.3 |
| 4 Surface rules | 1.2, 3 (context accessor + MaterialRuleContextMixin) |
| 5 Worldgen data | 2.x, 3, 4 (registries and mixins must exist; data references them) |
| 6 Optimizations | 2.x, 5 (working terrain) |
| 7 Measure | 6 (or 5 if skipping 6) |

---

## Reference

- **Implementation Plan:** [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) – pipeline strategy, what we do and don’t do, phases.
- **Architecture:** [LARION_ARCHITECTURE_REPORT.md](LARION_ARCHITECTURE_REPORT.md) – layout, registration, mixins, data flow, density tree, file paths.
- **Goals:** [LARION_DEVELOPER_GOALS_REPORT.md](LARION_DEVELOPER_GOALS_REPORT.md) – why each feature exists and where it’s implemented.
- **If primary pipeline is too costly:** [DEVELOPMENT_MAP_PERFORMANCE_FIRST.md](DEVELOPMENT_MAP_PERFORMANCE_FIRST.md) – performance-first fantasy chunk generator (custom generator, no density tree).
