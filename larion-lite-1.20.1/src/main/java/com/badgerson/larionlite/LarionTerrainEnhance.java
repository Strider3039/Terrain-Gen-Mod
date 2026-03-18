package com.badgerson.larionlite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Packed terrain extras (codec group size limit). */
public final class LarionTerrainEnhance {

    static final LarionTerrainEnhance DEFAULT = new LarionTerrainEnhance(
            1.0, 0.78, 10.0, 0.076, 9, 0.011, 5, 0.016, true, 5, 0.12);

    static final Codec<LarionTerrainEnhance> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("detail_continent_blend", 1.0).forGetter(LarionTerrainEnhance::detailContinentBlend),
            Codec.DOUBLE.optionalFieldOf("macro_vertical_contrast", 0.78).forGetter(LarionTerrainEnhance::macroVerticalContrast),
            Codec.DOUBLE.optionalFieldOf("ridge_fine_amplitude", 10.0).forGetter(LarionTerrainEnhance::ridgeFineAmplitude),
            Codec.DOUBLE.optionalFieldOf("ridge_fine_scale", 0.076).forGetter(LarionTerrainEnhance::ridgeFineScale),
            Codec.INT.optionalFieldOf("valley_depth", 9).forGetter(LarionTerrainEnhance::valleyDepth),
            Codec.DOUBLE.optionalFieldOf("valley_scale", 0.011).forGetter(LarionTerrainEnhance::valleyScale),
            Codec.INT.optionalFieldOf("channel_depth", 5).forGetter(LarionTerrainEnhance::channelDepth),
            Codec.DOUBLE.optionalFieldOf("channel_scale", 0.016).forGetter(LarionTerrainEnhance::channelScale),
            Codec.BOOL.optionalFieldOf("scree_enabled", true).forGetter(LarionTerrainEnhance::screeEnabled),
            Codec.INT.optionalFieldOf("scree_min_delta", 5).forGetter(LarionTerrainEnhance::screeMinDelta),
            Codec.DOUBLE.optionalFieldOf("cave_peak_reduction", 0.12).forGetter(LarionTerrainEnhance::cavePeakReduction)
    ).apply(i, LarionTerrainEnhance::new));

    private final double detailContinentBlend;
    private final double macroVerticalContrast;
    private final double ridgeFineAmplitude;
    private final double ridgeFineScale;
    private final int valleyDepth;
    private final double valleyScale;
    private final int channelDepth;
    private final double channelScale;
    private final boolean screeEnabled;
    private final int screeMinDelta;
    private final double cavePeakReduction;

    LarionTerrainEnhance(
            double detailContinentBlend,
            double macroVerticalContrast,
            double ridgeFineAmplitude,
            double ridgeFineScale,
            int valleyDepth,
            double valleyScale,
            int channelDepth,
            double channelScale,
            boolean screeEnabled,
            int screeMinDelta,
            double cavePeakReduction) {
        this.detailContinentBlend = detailContinentBlend;
        this.macroVerticalContrast = macroVerticalContrast;
        this.ridgeFineAmplitude = ridgeFineAmplitude;
        this.ridgeFineScale = ridgeFineScale;
        this.valleyDepth = valleyDepth;
        this.valleyScale = valleyScale;
        this.channelDepth = channelDepth;
        this.channelScale = channelScale;
        this.screeEnabled = screeEnabled;
        this.screeMinDelta = screeMinDelta;
        this.cavePeakReduction = cavePeakReduction;
    }

    double detailContinentBlend() {
        return detailContinentBlend;
    }

    double macroVerticalContrast() {
        return macroVerticalContrast;
    }

    double ridgeFineAmplitude() {
        return ridgeFineAmplitude;
    }

    double ridgeFineScale() {
        return ridgeFineScale;
    }

    int valleyDepth() {
        return valleyDepth;
    }

    double valleyScale() {
        return valleyScale;
    }

    int channelDepth() {
        return channelDepth;
    }

    double channelScale() {
        return channelScale;
    }

    boolean screeEnabled() {
        return screeEnabled;
    }

    int screeMinDelta() {
        return screeMinDelta;
    }

    double cavePeakReduction() {
        return cavePeakReduction;
    }
}
