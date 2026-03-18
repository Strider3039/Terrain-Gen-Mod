package com.badgerson.larionlite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Plateaus, hills, peak boost, deep canals — nested under larion_lite_enhance (keeps codec group ≤16). */
public final class LarionElevationLayers {

    static final LarionElevationLayers DEFAULT = new LarionElevationLayers(
            14, 16, 0.0046, 16.5, 16.0, 0.056, 14, 0.034);

    static final Codec<LarionElevationLayers> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("plateau_mid_blocks", 14).forGetter(LarionElevationLayers::plateauMidBlocks),
            Codec.INT.optionalFieldOf("plateau_high_extra", 16).forGetter(LarionElevationLayers::plateauHighExtra),
            Codec.DOUBLE.optionalFieldOf("plateau_scale", 0.0046).forGetter(LarionElevationLayers::plateauScale),
            Codec.DOUBLE.optionalFieldOf("peak_boost_blocks", 16.5).forGetter(LarionElevationLayers::peakBoostBlocks),
            Codec.DOUBLE.optionalFieldOf("hill_amplitude", 16.0).forGetter(LarionElevationLayers::hillAmplitude),
            Codec.DOUBLE.optionalFieldOf("hill_scale", 0.056).forGetter(LarionElevationLayers::hillScale),
            Codec.INT.optionalFieldOf("deep_canal_depth", 14).forGetter(LarionElevationLayers::deepCanalDepth),
            Codec.DOUBLE.optionalFieldOf("deep_canal_scale", 0.034).forGetter(LarionElevationLayers::deepCanalScale)
    ).apply(i, LarionElevationLayers::new));

    private final int plateauMidBlocks;
    private final int plateauHighExtra;
    private final double plateauScale;
    private final double peakBoostBlocks;
    private final double hillAmplitude;
    private final double hillScale;
    private final int deepCanalDepth;
    private final double deepCanalScale;

    LarionElevationLayers(
            int plateauMidBlocks,
            int plateauHighExtra,
            double plateauScale,
            double peakBoostBlocks,
            double hillAmplitude,
            double hillScale,
            int deepCanalDepth,
            double deepCanalScale) {
        this.plateauMidBlocks = Math.max(0, plateauMidBlocks);
        this.plateauHighExtra = Math.max(0, plateauHighExtra);
        this.plateauScale = Math.max(1e-6, plateauScale);
        this.peakBoostBlocks = Math.max(0.0, peakBoostBlocks);
        this.hillAmplitude = Math.max(0.0, hillAmplitude);
        this.hillScale = Math.max(1e-6, hillScale);
        this.deepCanalDepth = Math.max(0, deepCanalDepth);
        this.deepCanalScale = Math.max(1e-6, deepCanalScale);
    }

    int plateauMidBlocks() {
        return plateauMidBlocks;
    }

    int plateauHighExtra() {
        return plateauHighExtra;
    }

    double plateauScale() {
        return plateauScale;
    }

    double peakBoostBlocks() {
        return peakBoostBlocks;
    }

    double hillAmplitude() {
        return hillAmplitude;
    }

    double hillScale() {
        return hillScale;
    }

    int deepCanalDepth() {
        return deepCanalDepth;
    }

    double deepCanalScale() {
        return deepCanalScale;
    }
}
