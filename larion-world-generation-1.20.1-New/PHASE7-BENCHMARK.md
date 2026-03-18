# Phase 7: Benchmark and decision

## How to run the benchmark

1. Start a world (vanilla overworld or a dimension that uses Larion worldgen when you have it wired).
2. Go to an **ungenerated area** (e.g. fly far from spawn, or create a new world and run the command immediately) so that `getChunk` actually generates chunks instead of loading from disk.
3. Run:
   - **`/larion benchmark`** — generates 11×11 = 121 chunks (radius 5).
   - **`/larion benchmark <radius>`** — e.g. `/larion benchmark 4` → 9×9 = 81 chunks; radius 1–12.
4. Read the result: *"Generated N chunks in T ms (X chunks/s)"*.

**Tip:** For generation-only timing, avoid areas that are already generated; otherwise the command mostly measures chunk load time, not worldgen cost.

## How to decide

- **If performance is acceptable** (smooth enough for your target hardware and play style): close the plan; keep the primary pipeline (density tree + mixins) and maintain only.
- **If it’s too costly** (e.g. very low chunks/s, visible stutter when exploring): follow **[DEVELOPMENT_MAP_PERFORMANCE_FIRST.md](../DEVELOPMENT_MAP_PERFORMANCE_FIRST.md)** and consider the performance-first fantasy chunk generator (custom generator, no density tree).

Optional: compare subjectively with Minecraft 1.20.6 on the same machine to see how much of the cost is engine vs Larion.
