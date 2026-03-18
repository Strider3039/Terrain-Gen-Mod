package com.badgerson.larion.density_function_types;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** blockZ from context, clamped to world bounds. Custom fillArray avoids per-cell SimpleFunction overhead. */
public final class ZCoord implements DensityFunction.SimpleFunction {

    public static final KeyDispatchDataCodec<ZCoord> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(new ZCoord()));

    public ZCoord() {}

    @Override
    public void fillArray(double[] densities, ContextProvider provider) {
        final double min = minValue();
        final double max = maxValue();
        final int len = densities.length;
        for (int i = 0; i < len; i++) {
            double v = provider.forIndex(i).blockZ();
            densities[i] = v < min ? min : (v > max ? max : v);
        }
    }

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        return Math.min(Math.max(context.blockZ(), minValue()), maxValue());
    }

    @Override
    public double minValue() {
        return -30_000_000;
    }

    @Override
    public double maxValue() {
        return 30_000_000;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
