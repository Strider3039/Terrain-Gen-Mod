package com.badgerson.larionlite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Column-scale terrain + caves. O(1) 2D samples per column; optional valley/channel/scree.
 */
public final class FantasyTerrainConfig {

    /** Baseline: dramatic_explorer-style + macro contrast + valleys (preset can override). */
    public static final FantasyTerrainConfig DEFAULT = new FantasyTerrainConfig(
            0.0105,
            0.039,
            550.0,
            0.052,
            42.0,
            16,
            112,
            false,
            0.054,
            0.36,
            12,
            0.095,
            20.0,
            0.029,
            0.19,
            1.0,
            0.78,
            10.0,
            0.076,
            9,
            0.011,
            5,
            0.016,
            true,
            5,
            0.12);

    public static final Codec<FantasyTerrainConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.fieldOf("continent_scale").forGetter(FantasyTerrainConfig::continentScale),
            Codec.DOUBLE.fieldOf("warp_sample_scale").forGetter(FantasyTerrainConfig::warpSampleScale),
            Codec.DOUBLE.fieldOf("warp_block_strength").forGetter(FantasyTerrainConfig::warpBlockStrength),
            Codec.DOUBLE.fieldOf("ridge_scale").forGetter(FantasyTerrainConfig::ridgeScale),
            Codec.DOUBLE.fieldOf("ridge_block_amplitude").forGetter(FantasyTerrainConfig::ridgeBlockAmplitude),
            Codec.INT.fieldOf("base_height_offset").forGetter(FantasyTerrainConfig::baseHeightOffset),
            Codec.INT.fieldOf("height_variation").forGetter(FantasyTerrainConfig::heightVariation),
            Codec.BOOL.optionalFieldOf("caves_enabled", false).forGetter(FantasyTerrainConfig::cavesEnabled),
            Codec.DOUBLE.optionalFieldOf("cave_noise_scale", 0.054).forGetter(FantasyTerrainConfig::caveNoiseScale),
            Codec.DOUBLE.optionalFieldOf("cave_threshold", 0.36).forGetter(FantasyTerrainConfig::caveThreshold),
            Codec.INT.optionalFieldOf("cave_surface_padding", 12).forGetter(FantasyTerrainConfig::caveSurfacePadding),
            Codec.DOUBLE.optionalFieldOf("detail_scale", 0.095).forGetter(FantasyTerrainConfig::detailScale),
            Codec.DOUBLE.optionalFieldOf("detail_amplitude", 20.0).forGetter(FantasyTerrainConfig::detailAmplitude),
            Codec.DOUBLE.optionalFieldOf("cave_chamber_scale", 0.029).forGetter(FantasyTerrainConfig::caveChamberScale),
            Codec.DOUBLE.optionalFieldOf("cave_chamber_threshold", 0.19).forGetter(FantasyTerrainConfig::caveChamberThreshold),
            LarionTerrainEnhance.CODEC.optionalFieldOf("larion_lite_enhance", LarionTerrainEnhance.DEFAULT)
                    .forGetter(FantasyTerrainConfig::enhanceForCodec)
    ).apply(i, FantasyTerrainConfig::fromCodec));

    private static FantasyTerrainConfig fromCodec(
            double continentScale,
            double warpSampleScale,
            double warpBlockStrength,
            double ridgeScale,
            double ridgeBlockAmplitude,
            int baseHeightOffset,
            int heightVariation,
            boolean cavesEnabled,
            double caveNoiseScale,
            double caveThreshold,
            int caveSurfacePadding,
            double detailScale,
            double detailAmplitude,
            double caveChamberScale,
            double caveChamberThreshold,
            LarionTerrainEnhance e) {
        return new FantasyTerrainConfig(
                continentScale, warpSampleScale, warpBlockStrength, ridgeScale, ridgeBlockAmplitude,
                baseHeightOffset, heightVariation, cavesEnabled, caveNoiseScale, caveThreshold,
                caveSurfacePadding, detailScale, detailAmplitude, caveChamberScale, caveChamberThreshold,
                e.detailContinentBlend(), e.macroVerticalContrast(), e.ridgeFineAmplitude(), e.ridgeFineScale(),
                e.valleyDepth(), e.valleyScale(), e.channelDepth(), e.channelScale(),
                e.screeEnabled(), e.screeMinDelta(), e.cavePeakReduction());
    }

    private LarionTerrainEnhance enhanceForCodec() {
        return new LarionTerrainEnhance(
                detailContinentBlend, macroVerticalContrast, ridgeFineAmplitude, ridgeFineScale,
                valleyDepth, valleyScale, channelDepth, channelScale,
                screeEnabled, screeMinDelta, cavePeakReduction);
    }

    private final double continentScale;
    private final double warpSampleScale;
    private final double warpBlockStrength;
    private final double ridgeScale;
    private final double ridgeBlockAmplitude;
    private final int baseHeightOffset;
    private final int heightVariation;
    private final boolean cavesEnabled;
    private final double caveNoiseScale;
    private final double caveThreshold;
    private final int caveSurfacePadding;
    private final double detailScale;
    private final double detailAmplitude;
    private final double caveChamberScale;
    private final double caveChamberThreshold;
    /** 0 = uniform detail; 1 = more detail in lowlands, smoother peaks. */
    private final double detailContinentBlend;
    /** 0 = flat height_variation; ~0.8 = basins calmer, highlands more dramatic. */
    private final double macroVerticalContrast;
    /** Secondary ridge octave (0 disables extra sample). */
    private final double ridgeFineAmplitude;
    private final double ridgeFineScale;
    /** Blocks subtracted by valley noise (0 = off). */
    private final int valleyDepth;
    private final double valleyScale;
    /** Extra linear lows (0 = off). */
    private final int channelDepth;
    private final double channelScale;
    private final boolean screeEnabled;
    private final int screeMinDelta;
    /** Added to cave thresholds on high terrain (fewer caves on peaks, faster gen). */
    private final double cavePeakReduction;

    public FantasyTerrainConfig(
            double continentScale,
            double warpSampleScale,
            double warpBlockStrength,
            double ridgeScale,
            double ridgeBlockAmplitude,
            int baseHeightOffset,
            int heightVariation,
            boolean cavesEnabled,
            double caveNoiseScale,
            double caveThreshold,
            int caveSurfacePadding,
            double detailScale,
            double detailAmplitude,
            double caveChamberScale,
            double caveChamberThreshold,
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
        this.continentScale = continentScale;
        this.warpSampleScale = warpSampleScale;
        this.warpBlockStrength = warpBlockStrength;
        this.ridgeScale = ridgeScale;
        this.ridgeBlockAmplitude = ridgeBlockAmplitude;
        this.baseHeightOffset = baseHeightOffset;
        this.heightVariation = heightVariation;
        this.cavesEnabled = cavesEnabled;
        this.caveNoiseScale = caveNoiseScale;
        this.caveThreshold = caveThreshold;
        this.caveSurfacePadding = caveSurfacePadding;
        this.detailScale = detailScale;
        this.detailAmplitude = detailAmplitude;
        this.caveChamberScale = caveChamberScale;
        this.caveChamberThreshold = caveChamberThreshold;
        this.detailContinentBlend = MthClamp(detailContinentBlend, 0.0, 1.0);
        this.macroVerticalContrast = MthClamp(macroVerticalContrast, 0.0, 1.0);
        this.ridgeFineAmplitude = Math.max(0.0, ridgeFineAmplitude);
        this.ridgeFineScale = ridgeFineScale;
        this.valleyDepth = Math.max(0, valleyDepth);
        this.valleyScale = valleyScale;
        this.channelDepth = Math.max(0, channelDepth);
        this.channelScale = channelScale;
        this.screeEnabled = screeEnabled;
        this.screeMinDelta = Math.max(2, screeMinDelta);
        this.cavePeakReduction = MthClamp(cavePeakReduction, 0.0, 0.28);
    }

    private static double MthClamp(double v, double a, double b) {
        return v < a ? a : (Math.min(v, b));
    }

    public double continentScale() {
        return continentScale;
    }

    public double warpSampleScale() {
        return warpSampleScale;
    }

    public double warpBlockStrength() {
        return warpBlockStrength;
    }

    public double ridgeScale() {
        return ridgeScale;
    }

    public double ridgeBlockAmplitude() {
        return ridgeBlockAmplitude;
    }

    public int baseHeightOffset() {
        return baseHeightOffset;
    }

    public int heightVariation() {
        return heightVariation;
    }

    public boolean cavesEnabled() {
        return cavesEnabled;
    }

    public double caveNoiseScale() {
        return caveNoiseScale;
    }

    public double caveThreshold() {
        return caveThreshold;
    }

    public int caveSurfacePadding() {
        return caveSurfacePadding;
    }

    public double detailScale() {
        return detailScale;
    }

    public double detailAmplitude() {
        return detailAmplitude;
    }

    public double caveChamberScale() {
        return caveChamberScale;
    }

    public double caveChamberThreshold() {
        return caveChamberThreshold;
    }

    public double detailContinentBlend() {
        return detailContinentBlend;
    }

    public double macroVerticalContrast() {
        return macroVerticalContrast;
    }

    public double ridgeFineAmplitude() {
        return ridgeFineAmplitude;
    }

    public double ridgeFineScale() {
        return ridgeFineScale;
    }

    public int valleyDepth() {
        return valleyDepth;
    }

    public double valleyScale() {
        return valleyScale;
    }

    public int channelDepth() {
        return channelDepth;
    }

    public double channelScale() {
        return channelScale;
    }

    public boolean screeEnabled() {
        return screeEnabled;
    }

    public int screeMinDelta() {
        return screeMinDelta;
    }

    public double cavePeakReduction() {
        return cavePeakReduction;
    }
}
