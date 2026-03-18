# Implementation Plan: Larion 1.20.6+ Features on 1.20.1

This document describes how we plan to reimplement Larion’s 1.20.6+ feature set cleanly on the 1.20.1 Java engine. It is grounded in the [Architecture Report](LARION_ARCHITECTURE_REPORT.md) and [Developer Goals Report](LARION_DEVELOPER_GOALS_REPORT.md), and in what works best with the 1.20.1 worldgen pipeline.

---

## 1. Objective

- **Goal:** Deliver the same *features and feel* as Larion 1.20.6+ (fantasy terrain, tall world, deep caves, slope/cliff surface rules, datapack-driven worldgen) on **1.20.1**, with **clean, maintainable code** and **acceptable performance**.
- **Scope:** One codebase targeting 1.20.1 (Forge first; Fabric/NeoForge as in current setup). No requirement to share binary with 1.20.6+; we can diverge implementation where the 1.20.1 engine demands it.

---

## 2. 1.20.1 engine realities

What we must work with on 1.20.1:

| Reality | Implication |
|--------|-------------|
| **Slower than 1.20.6** | The same density tree and noise_router run slower on 1.20.1. We cannot “upgrade” the engine; we can only reduce work per chunk or change *how* we produce terrain. |
| **ContextProvider / FunctionContext semantics** | The engine’s provider supplies a context (often from NoiseChunk). Replacing it with a bare `SinglePointContext(x,y,z)` breaks nodes that rely on context type or identity (e.g. ocean-only bug). **We must wrap the engine context** (e.g. `WarpedFunctionContext`) and override only position when we need warped sampling. |
| **fillArray vs compute** | Batched `fillArray` reduces per-sample overhead. Custom density types should implement `fillArray` where it makes sense (e.g. Division: batch both args then one loop; FlatDomainWarp: batch warp then `input.fillArray(warpedProvider)`). Default `provider.fillAllDirectly(this)` is the slow path on 1.20.1. |
| **Vanilla pipeline is fixed** | NoiseBasedChunkGenerator, NoiseChunk, noise_router, and the density → block fill flow are what we have. We either optimize inside this pipeline or introduce an alternative path (e.g. custom ChunkGenerator) and accept less datapack compatibility. |
| **JSON + registries** | Worldgen is data-driven. Keeping `minecraft:overworld/*` redirects and `larion:overworld/*` roots preserves compatibility with datapacks and keeps tuning in JSON rather than hardcode. |

These constraints drive the plan below.

---

## 3. Feature parity with 1.20.6+ (what we reimplement)

Aligned with the [Developer Goals Report](LARION_DEVELOPER_GOALS_REPORT.md):

| Feature | 1.20.6+ | Our 1.20.1 approach |
|--------|---------|----------------------|
| **Fantasy terrain (warped continents, ridges, vegetation, sloped cheese)** | Same density tree; engine does batching. | Same JSON tree; our custom types (especially **FlatDomainWarp**) implement batching and context wrapping so the tree evaluates correctly and efficiently. |
| **Tall world (640 height, min_y -128)** | Dimension + noise_settings. | Same: dimension types and noise_settings in data; no engine change. |
| **Deeper caves, lava level tied to min_y** | Mixins: fluid picker, cave bound. | Same mixins: **NoiseChunkGeneratorMixin** (lava = minY+10), **CaveWorldCarverMixin** (cave bound 40); flood logic stays in noise_router JSON. |
| **Slope/cliff surface rules** | Steep + “somewhat steep” conditions. | Same: **SteepSlopePredicateMixin**, **SomewhatSteepSlopePredicate**, **MaterialRuleContextMixin**, **SurfaceRulesContextAccessor**; `larion:somewhat_steep` in surface rules. |
| **Datapack compatibility** | Vanilla names + redirects. | Keep minecraft:overworld/* redirects to larion:overworld/*; final_density and router entries point at our roots. |
| **Custom density primitives** | div, sqrt, signum, sine, x, z, flat_domain_warp. | Same set; implementations tuned for 1.20.1 (batching, context wrapping, NaN-safe min/max where relevant). |

We do *not* promise identical performance to 1.20.6; we aim for “good enough” on 1.20.1 with the same features and visuals.

---

## 4. Pipeline strategy: stay in vanilla density pipeline (primary)

**Decision:** Our primary path is to **keep using the vanilla density pipeline** (NoiseBasedChunkGenerator + noise_router + density functions) and make it as efficient and correct as possible on 1.20.1.

**Rationale:**

- Matches the developers’ goal of staying inside vanilla worldgen (see [Developer Goals Report](LARION_DEVELOPER_GOALS_REPORT.md)).
- Preserves full datapack control (JSON density trees, surface rules, carvers).
- We already have a working, correct implementation (WarpedContextProvider + WarpedFunctionContext); the main cost is engine speed, not our code shape.
- An alternative pipeline (e.g. custom ChunkGenerator) would duplicate a lot of behavior (caves, aquifers, biomes, surface rules) and complicate maintenance.

**What “clean reimplementation” means on this path:**

1. **Single, clear strategy for warped sampling**  
   - **FlatDomainWarp** is the only place we warp the domain.  
   - Always use **WarpedContextProvider** + **WarpedFunctionContext** (wrap engine context; override only blockX/blockZ).  
   - No code paths that replace the context with a bare `SinglePointContext` in the batched call (that path is known to break terrain).

2. **Batching everywhere we can**  
   - **FlatDomainWarp:** batch warp X/Z, then `input.fillArray(densities, warpedProvider)`.  
   - **Division:** batch both arguments, then one divide loop.  
   - **Signum, Sine, Sqrt:** keep using vanilla `PureTransformer` default fillArray (already one pass).  
   - **XCoord, ZCoord:** keep as SimpleFunction (no custom fillArray); cost is dominated by context/provider.

3. **Minimal allocations in hot path**  
   - Use `final int len = densities.length` and reuse for allocations and loops (already done).  
   - No thread-local or pooled buffers unless we measure a clear win and document reentrancy/thread safety.

4. **Correctness guards**  
   - **Sqrt:** NaN-safe minValue/maxValue (input ≤ 0 → 0).  
   - **Division:** divide-by-zero → 0 in both compute and fillArray.  
   - Constants (e.g. cave bound, chunk section max) in one place; use in mixins and predicates.

5. **Data and mixins unchanged in spirit**  
   - Same dimension height/min_y, same noise_router layout, same redirects and larion:overworld/* roots.  
   - Same five mixins (fluid picker, cave bound, steep, somewhat steep, context accessor).  
   - Surface rules and cave/aquifer tuning stay in JSON.

This gives us a **clean, single-pipeline** 1.20.1 build that reimplements 1.20.6+ features and is optimized for the 1.20.1 engine without a second pipeline to maintain.

---

## 5. Optional alternative pipeline (only if needed)

If, after all optimizations in §4, performance on 1.20.1 is still unacceptable (e.g. unplayable chunk gen times), we can consider a **parallel alternative**:

- **Custom ChunkGenerator** (e.g. `LarionChunkGenerator`) that does *not* use the full density tree.
- **Idea:** For each column (x,z), compute a small set of values (e.g. 2–4 2D noises for continent/ridge/warp, then “height” and optional cave density) in our own code, then fill stone below that height and run **vanilla biomes, surface rules, and carvers** on the chunk so the rest of the game still works.
- **Pros:** Fewer samples per chunk, no deep density tree, full control over hot loops.  
- **Cons:** Less datapack-driven; terrain shape would be coded or configured in a simpler format (e.g. a small config or formula), not the full JSON density tree. Would require a separate design doc and scope (e.g. “Larion Lite” or “Larion Fast” profile).

**Plan:** Treat this as **out of scope** until we have evidence that the primary pipeline cannot meet our performance bar. No implementation work here until we decide to open that track.

---

## 6. Implementation phases (primary pipeline)

### Phase 1: Stabilize and document (current state)

- [x] FlatDomainWarp: WarpedContextProvider + WarpedFunctionContext only (no SinglePointContext in batched path).
- [x] Division: batched fillArray with two temp arrays and one divide loop.
- [x] Sqrt: NaN-safe min/max; constants (cave bound, chunk section max) used in mixins and predicates.
- [x] Architecture and Developer Goals reports written.
- **Deliverable:** Clear baseline: one pipeline, correct terrain, known hot path and data flow.

### Phase 2: Small, safe optimizations (no behavior change)

- [ ] Review all custom density types for redundant work (e.g. repeated .length, extra allocations). Apply only where obviously safe.
- [ ] Ensure all density types that support it implement fillArray (no accidental fallback to fillAllDirectly in hot paths).
- [ ] Optionally: add short comments in key classes (FlatDomainWarp, Division) pointing to this plan and the “wrap context, batch where possible” rule.
- **Deliverable:** Same behavior, slightly lower overhead; codebase remains easy to reason about.

### Phase 3: Measure and decide

- [ ] Run simple benchmarks (e.g. time-to-generate N chunks in a fresh world) on 1.20.1 with the mod, and compare subjectively to 1.20.6 if possible.
- [ ] If performance is acceptable: close out plan; maintain primary pipeline only.
- [ ] If not: document the gap (e.g. “chunk gen 2× slower than 1.20.6”) and decide whether to (a) try further micro-optimizations (e.g. reduced noise usage in JSON, profiling-driven changes) or (b) open the alternative-pipeline track (§5) with a separate design.

### Phase 4 (only if alternative pipeline is opened)

- [ ] Design doc for custom generator: height formula, warp model, how biomes/surface/carvers are retained.
- [ ] Implement and test behind a config or dimension type so users can choose “density pipeline” vs “fast pipeline” if we ever ship both.

---

## 7. What we do *not* do (by design)

- **Do not** change the density tree structure or remove flat_domain_warp to “simplify” terrain; that has already been tried and broke terrain (ocean-only, flat world).
- **Do not** replace the engine’s FunctionContext with a new context type in the batched path; we only wrap it (WarpedFunctionContext).
- **Do not** add thread-local or pooled buffers without a clear need and documented reentrancy/thread-safety story.
- **Do not** introduce a second pipeline (custom generator) until we have data showing the primary pipeline is insufficient.

---

## 8. Success criteria

- **Correctness:** New 1.20.1 worlds show the same *kind* of terrain as 1.20.6+ (continents, ridges, warped coastlines, tall mountains, deep caves, correct surface on cliffs). No ocean-only or flat-world regressions.
- **Performance:** Chunk generation on 1.20.1 with the mod is “playable” (subjective; we document and revisit in Phase 3).
- **Maintainability:** One clear pipeline (density-based), one set of rules (wrap context, batch where possible), and two reports (architecture, goals) so future changes stay consistent with the original goals and 1.20.1 engine realities.

---

## 9. References

- [LARION_ARCHITECTURE_REPORT.md](LARION_ARCHITECTURE_REPORT.md) – How the mod is built and which processes run (registration, mixins, worldgen data, density tree, execution path).
- [LARION_DEVELOPER_GOALS_REPORT.md](LARION_DEVELOPER_GOALS_REPORT.md) – Inferred goals (fantasy terrain, tall world, deep caves, slopes, pipeline compatibility, multi-loader, performance) and the code/data that backs them.

This implementation plan is the single place we record *how* we reimplement 1.20.6+ on 1.20.1 and *why* we choose the current pipeline and constraints.
