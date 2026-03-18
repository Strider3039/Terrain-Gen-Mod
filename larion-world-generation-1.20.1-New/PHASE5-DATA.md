# Phase 5: Worldgen data

Dimension types and Minecraft overworld **redirects** are already in `src/main/resources/data/`.

To get **noise_settings**, **Larion density roots**, and **Larion noises** (required for terrain to generate), copy the full worldgen data from the original Larion 1.20.1 project:

```powershell
# From this folder (larion-world-generation-1.20.1-New):
.\copy-worldgen-data.ps1
```

This copies from `../larion-world-generation-1.20.1/common/src/main/resources/data/` into `./src/main/resources/data/`, including:

- `minecraft/worldgen/noise_settings/overworld.json`
- `minecraft/worldgen/density_function/overworld/caves/*` (if any)
- `minecraft/worldgen/configured_carver/*` (if any)
- `larion/worldgen/density_function/overworld/*` (final_density, continents, vegetation, ridges, sloped_cheese, terrain, caves, temperature, etc.)
- `larion/worldgen/noise/*` (terrain, vegetation, ridges, temperature, continents, etc.)

After running the script, build and run the game; new worlds should use Larion terrain (tall world, warped continents, deep caves, slope surface rules).
