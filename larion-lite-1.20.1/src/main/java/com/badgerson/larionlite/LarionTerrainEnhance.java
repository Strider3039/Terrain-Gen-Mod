package com.badgerson.larionlite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Packed terrain extras (codec group size limit — elevation layers nested). */
public final class LarionTerrainEnhance {

    static final LarionTerrainEnhance DEFAULT = new LarionTerrainEnhance(
            1.0, 0.78, 3.8, 0.064, 11, 0.0092, 6, 0.0138, true, 5, 0.12,
            LarionElevationLayers.DEFAULT);

    static final Codec<LarionTerrainEnhance> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("detail_continent_blend", 1.0).forGetter(LarionTerrainEnhance::detailContinentBlend),
            Codec.DOUBLE.optionalFieldOf("macro_vertical_contrast", 0.78).forGetter(LarionTerrainEnhance::macroVerticalContrast),
            Codec.DOUBLE.optionalFieldOf("ridge_fine_amplitude", 3.8).forGetter(LarionTerrainEnhance::ridgeFineAmplitude),
            Codec.DOUBLE.optionalFieldOf("ridge_fine_scale", 0.064).forGetter(LarionTerrainEnhance::ridgeFineScale),
            Codec.INT.optionalFieldOf("valley_depth", 11).forGetter(LarionTerrainEnhance::valleyDepth),
            Codec.DOUBLE.optionalFieldOf("valley_scale", 0.0092).forGetter(LarionTerrainEnhance::valleyScale),
            Codec.INT.optionalFieldOf("channel_depth", 6).forGetter(LarionTerrainEnhance::channelDepth),
            Codec.DOUBLE.optionalFieldOf("channel_scale", 0.0138).forGetter(LarionTerrainEnhance::channelScale),
            Codec.BOOL.optionalFieldOf("scree_enabled", true).forGetter(LarionTerrainEnhance::screeEnabled),
            Codec.INT.optionalFieldOf("scree_min_delta", 5).forGetter(LarionTerrainEnhance::screeMinDelta),
            Codec.DOUBLE.optionalFieldOf("cave_peak_reduction", 0.12).forGetter(LarionTerrainEnhance::cavePeakReduction),
            LarionElevationLayers.CODEC.optionalFieldOf("elevation_layers", LarionElevationLayers.DEFAULT)
                    .forGetter(LarionTerrainEnhance::elevationLayers)
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
    private final LarionElevationLayers elevationLayers;

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
            double cavePeakReduction,
            LarionElevationLayers elevationLayers) {
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
        this.elevationLayers = elevationLayers != null ? elevationLayers : LarionElevationLayers.DEFAULT;
    }

    LarionElevationLayers elevationLayers() {
        return elevationLayers;
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

    int plateauMidBlocks() {
        return elevationLayers.plateauMidBlocks();
    }

    int plateauHighExtra() {
        return elevationLayers.plateauHighExtra();
    }

    double plateauScale() {
        return elevationLayers.plateauScale();
    }

    double peakBoostBlocks() {
        return elevationLayers.peakBoostBlocks();
    }

    double hillAmplitude() {
        return elevationLayers.hillAmplitude();
    }

    double hillScale() {
        return elevationLayers.hillScale();
    }

    int deepCanalDepth() {
        return elevationLayers.deepCanalDepth();
    }

    double deepCanalScale() {
        return elevationLayers.deepCanalScale();
    }
}
