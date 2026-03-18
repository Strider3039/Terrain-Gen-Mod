package com.badgerson.larionlite;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Presets: terrain identity + valley/channel/macro; caves/detail still overridden by larion_lite-common.toml when merged.
 */
public final class FantasyTerrainPresets {

    /** Phase A1 — cinematic exploration (also matches {@link FantasyTerrainConfig#DEFAULT}). */
    private static final FantasyTerrainConfig DRAMATIC_EXPLORER = new FantasyTerrainConfig(
            0.0105, 0.039, 550.0, 0.052, 42.0, 16, 112,
            false, 0.054, 0.36, 12,
            0.095, 20.0, 0.029, 0.19,
            1.0, 0.78, 10.0, 0.076, 9, 0.011, 5, 0.016, true, 5, 0.12);

    /** Phase A2 — gentler slopes, less micro-relief, deeper padding for structure mods. */
    private static final FantasyTerrainConfig STRUCTURE_FRIENDLY = new FantasyTerrainConfig(
            0.012, 0.033, 410.0, 0.054, 28.0, 11, 90,
            false, 0.054, 0.36, 17,
            0.096, 12.0, 0.029, 0.19,
            0.42, 0.22, 0.0, 0.076, 3, 0.010, 2, 0.014, true, 6, 0.05);

    private static final FantasyTerrainConfig BALANCED = new FantasyTerrainConfig(
            0.0105, 0.036, 520.0, 0.051, 40.0, 14, 104,
            false, 0.054, 0.36, 12,
            0.092, 17.0, 0.029, 0.19,
            0.88, 0.58, 8.0, 0.078, 6, 0.011, 3, 0.016, true, 5, 0.10);

    private static final Map<String, FantasyTerrainConfig> PRESETS = Map.ofEntries(
            Map.entry("default", FantasyTerrainConfig.DEFAULT),
            Map.entry("dramatic_explorer", DRAMATIC_EXPLORER),
            Map.entry("structure_friendly", STRUCTURE_FRIENDLY),
            Map.entry("modpack_balanced", BALANCED),
            Map.entry("flat_fantasy", new FantasyTerrainConfig(
                    0.018, 0.025, 300.0, 0.042, 22.0, 6, 58,
                    false, 0.054, 0.36, 12, 0.1, 12.0, 0.029, 0.19,
                    0.5, 0.35, 0.0, 0.076, 2, 0.012, 1, 0.018, false, 5, 0.06)),
            Map.entry("tall_ridges", new FantasyTerrainConfig(
                    0.0098, 0.041, 560.0, 0.046, 52.0, 16, 118,
                    false, 0.054, 0.36, 12, 0.088, 18.0, 0.029, 0.19,
                    0.92, 0.72, 11.0, 0.074, 8, 0.010, 4, 0.015, true, 5, 0.11)),
            Map.entry("dramatic_warp", new FantasyTerrainConfig(
                    0.0088, 0.033, 720.0, 0.052, 36.0, 10, 112,
                    false, 0.054, 0.36, 12, 0.09, 20.0, 0.029, 0.19,
                    1.0, 0.85, 9.0, 0.080, 11, 0.010, 6, 0.017, true, 4, 0.10)),
            Map.entry("mellow", new FantasyTerrainConfig(
                    0.015, 0.028, 360.0, 0.058, 20.0, 8, 64,
                    false, 0.054, 0.36, 12, 0.1, 10.0, 0.029, 0.19,
                    0.4, 0.28, 4.0, 0.085, 2, 0.013, 0, 0.016, true, 6, 0.08)),
            Map.entry("cave_rich", new FantasyTerrainConfig(
                    0.011, 0.035, 500.0, 0.054, 36.0, 12, 98,
                    true, 0.05, 0.32, 14, 0.09, 16.0, 0.026, 0.16,
                    0.85, 0.52, 7.0, 0.077, 7, 0.011, 3, 0.016, true, 5, 0.14)));

    private FantasyTerrainPresets() {
    }

    public static Optional<FantasyTerrainConfig> byName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(PRESETS.get(name.trim().toLowerCase(Locale.ROOT)));
    }
}
