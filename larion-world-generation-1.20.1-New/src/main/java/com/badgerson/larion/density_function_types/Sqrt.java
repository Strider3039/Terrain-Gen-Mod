package com.badgerson.larion.density_function_types;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

/** sqrt(argument); input ≤ 0 → 0 (NaN-safe). Uses PureTransformer batched fillArray. */
public final class Sqrt implements DensityFunctions.PureTransformer {
    private final DensityFunction df;

    private static final MapCodec<Sqrt> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(Sqrt::df)
            ).apply(instance, Sqrt::new));
    public static final KeyDispatchDataCodec<Sqrt> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    public Sqrt(DensityFunction df) {
        this.df = df;
    }

    public DensityFunction df() {
        return this.df;
    }

    @Override
    public DensityFunction input() {
        return this.df;
    }

    @Override
    public double transform(double density) {
        if (density <= 0) {
            return 0;
        }
        return Math.sqrt(density);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new Sqrt(this.df.mapAll(visitor));
    }

    @Override
    public double minValue() {
        double min = this.df.minValue();
        return min <= 0 ? 0 : Math.sqrt(min);
    }

    @Override
    public double maxValue() {
        double max = this.df.maxValue();
        return max <= 0 ? 0 : Math.sqrt(max);
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
