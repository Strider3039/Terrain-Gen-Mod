package com.badgerson.larionlite;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Merges with world/datapack terrain. Safe for modpacks: still one surface Y per column; tune here without recompile.
 */
public final class LarionLiteConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> TERRAIN_PRESET;
    public static final ForgeConfigSpec.BooleanValue CAVES_ENABLED;
    public static final ForgeConfigSpec.DoubleValue CAVE_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue CAVE_NOISE_SCALE;
    public static final ForgeConfigSpec.IntValue CAVE_SURFACE_PADDING;
    public static final ForgeConfigSpec.DoubleValue DETAIL_SCALE;
    public static final ForgeConfigSpec.DoubleValue DETAIL_AMPLITUDE;
    public static final ForgeConfigSpec.DoubleValue CAVE_CHAMBER_SCALE;
    public static final ForgeConfigSpec.DoubleValue CAVE_CHAMBER_THRESHOLD;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("Larion Lite — performance overworld (column heightmap; O(1) terrain samples per column).")
                .comment("Presets: dramatic_explorer (default), structure_friendly, modpack_balanced, flat_fantasy, tall_ridges, dramatic_warp, mellow, cave_rich. Empty = world JSON terrain only.")
                .push("terrain");
        TERRAIN_PRESET = b
                .comment("dramatic_explorer = macro basins/peaks + valleys + scree. structure_friendly = gentler for heavy structures.")
                .define("terrain_preset", "dramatic_explorer");
        CAVES_ENABLED = b.define("caves_enabled", true);
        CAVE_THRESHOLD = b
                .comment("Worm tunnels: lower = more connections (try 0.30–0.42).")
                .defineInRange("cave_threshold", 0.36, 0.12, 0.8);
        CAVE_NOISE_SCALE = b
                .comment("Worm frequency: lower = wider tunnels.")
                .defineInRange("cave_noise_scale", 0.054, 0.025, 0.12);
        CAVE_SURFACE_PADDING = b
                .comment("Solid band under surface for grass/structures.")
                .defineInRange("cave_surface_padding", 12, 4, 28);
        DETAIL_SCALE = b
                .comment("Small-scale surface hills (2D, per column).")
                .defineInRange("detail_scale", 0.095, 0.04, 0.2);
        DETAIL_AMPLITUDE = b
                .comment("Avoid >34 (blocky). dramatic_explorer preset ~20.")
                .defineInRange("detail_amplitude", 20.0, 0.0, 36.0);
        CAVE_CHAMBER_SCALE = b
                .comment("Large cave rooms: lower scale = bigger chambers.")
                .defineInRange("cave_chamber_scale", 0.029, 0.014, 0.055);
        CAVE_CHAMBER_THRESHOLD = b
                .comment("Avoid <0.12 (Swiss cheese). Try 0.14–0.26.")
                .defineInRange("cave_chamber_threshold", 0.19, 0.12, 0.45);
        b.pop();
        SPEC = b.build();
    }

    private LarionLiteConfig() {
    }

    public static FantasyTerrainConfig resolve(FantasyTerrainConfig fromWorldGenerator) {
        FantasyTerrainConfig base = FantasyTerrainPresets.byName(TERRAIN_PRESET.get())
                .orElse(fromWorldGenerator);
        return new FantasyTerrainConfig(
                base.continentScale(),
                base.warpSampleScale(),
                base.warpBlockStrength(),
                base.ridgeScale(),
                base.ridgeBlockAmplitude(),
                base.baseHeightOffset(),
                base.heightVariation(),
                CAVES_ENABLED.get(),
                CAVE_NOISE_SCALE.get(),
                CAVE_THRESHOLD.get(),
                CAVE_SURFACE_PADDING.get(),
                DETAIL_SCALE.get(),
                DETAIL_AMPLITUDE.get(),
                CAVE_CHAMBER_SCALE.get(),
                CAVE_CHAMBER_THRESHOLD.get(),
                base.detailContinentBlend(),
                base.macroVerticalContrast(),
                base.ridgeFineAmplitude(),
                base.ridgeFineScale(),
                base.valleyDepth(),
                base.valleyScale(),
                base.channelDepth(),
                base.channelScale(),
                base.screeEnabled(),
                base.screeMinDelta(),
                base.cavePeakReduction());
    }
}
