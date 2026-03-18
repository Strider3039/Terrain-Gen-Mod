package com.badgerson.larionlite;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Presets: terrain identity + plateaus/valleys/channels/hills/peaks; caves/detail still overridden by larion_lite-common.toml when merged.
 */
public final class FantasyTerrainPresets {

    /** Layered exploration: mid/high plateaus, low valleys, deep canals, hills, peak boost. */
    private static final FantasyTerrainConfig DRAMATIC_EXPLORER = FantasyTerrainConfig.DEFAULT;

    /** Gentler slopes, milder plateaus/canals for heavy structures. */
    private static final FantasyTerrainConfig STRUCTURE_FRIENDLY = new FantasyTerrainConfig(
            0.012, 0.033, 410.0, 0.054, 28.0, 11, 90,
            false, 0.054, 0.36, 17,
            0.096, 12.0, 0.029, 0.19,
            0.42, 0.22, 0.0, 0.076, 5, 0.010, 3, 0.014, true, 6, 0.05,
            6, 8, 0.0052, 4.0, 10.0, 0.06, 6, 0.032);

    private static final FantasyTerrainConfig BALANCED = new FantasyTerrainConfig(
            0.0105, 0.036, 520.0, 0.051, 40.0, 14, 104,
            false, 0.054, 0.36, 12,
            0.092, 17.0, 0.029, 0.19,
            0.88, 0.58, 8.0, 0.078, 10, 0.011, 5, 0.016, true, 5, 0.10,
            10, 12, 0.0048, 8.0, 14.0, 0.057, 10, 0.033);

    private static final Map<String, FantasyTerrainConfig> PRESETS = Map.ofEntries(
            Map.entry("default", FantasyTerrainConfig.DEFAULT),
            Map.entry("dramatic_explorer", DRAMATIC_EXPLORER),
            Map.entry("structure_friendly", STRUCTURE_FRIENDLY),
            Map.entry("modpack_balanced", BALANCED),
            Map.entry("flat_fantasy", new FantasyTerrainConfig(
                    0.018, 0.025, 300.0, 0.042, 22.0, 6, 58,
                    false, 0.054, 0.36, 12, 0.1, 12.0, 0.029, 0.19,
                    0.5, 0.35, 0.0, 0.076, 3, 0.012, 1, 0.018, false, 5, 0.06,
                    4, 5, 0.006, 2.0, 8.0, 0.065, 4, 0.028)),
            Map.entry("tall_ridges", new FantasyTerrainConfig(
                    0.0098, 0.041, 560.0, 0.046, 52.0, 16, 124,
                    false, 0.054, 0.36, 12, 0.088, 18.0, 0.029, 0.19,
                    0.92, 0.78, 13.0, 0.072, 12, 0.010, 6, 0.015, true, 5, 0.11,
                    12, 18, 0.0042, 16.0, 14.0, 0.054, 16, 0.036)),
            Map.entry("dramatic_warp", new FantasyTerrainConfig(
                    0.0088, 0.033, 720.0, 0.052, 36.0, 10, 118,
                    false, 0.054, 0.36, 12, 0.09, 20.0, 0.029, 0.19,
                    1.0, 0.88, 11.0, 0.078, 15, 0.010, 9, 0.017, true, 4, 0.10,
                    16, 18, 0.0044, 14.0, 18.0, 0.052, 16, 0.031)),
            Map.entry("mellow", new FantasyTerrainConfig(
                    0.015, 0.028, 360.0, 0.058, 20.0, 8, 64,
                    false, 0.054, 0.36, 12, 0.1, 10.0, 0.029, 0.19,
                    0.4, 0.28, 4.0, 0.085, 4, 0.013, 0, 0.016, true, 6, 0.08,
                    5, 6, 0.0055, 3.0, 9.0, 0.062, 3, 0.026)),
            Map.entry("cave_rich", new FantasyTerrainConfig(
                    0.011, 0.035, 500.0, 0.054, 36.0, 12, 102,
                    true, 0.05, 0.32, 14, 0.09, 16.0, 0.026, 0.16,
                    0.85, 0.52, 7.0, 0.077, 11, 0.011, 6, 0.016, true, 5, 0.14,
                    12, 14, 0.0047, 10.0, 15.0, 0.055, 12, 0.033)));

    private FantasyTerrainPresets() {
    }

    public static Optional<FantasyTerrainConfig> byName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(PRESETS.get(name.trim().toLowerCase(Locale.ROOT)));
    }
}
