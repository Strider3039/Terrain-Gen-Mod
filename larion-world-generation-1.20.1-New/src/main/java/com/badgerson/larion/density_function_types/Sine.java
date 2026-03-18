package com.badgerson.larion.density_function_types;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

/** sin(argument). Uses PureTransformer batched fillArray. */
public final class Sine implements DensityFunctions.PureTransformer {
    private final DensityFunction df;

    private static final MapCodec<Sine> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(Sine::df)
            ).apply(instance, Sine::new));
    public static final KeyDispatchDataCodec<Sine> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    public Sine(DensityFunction df) {
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
        return Math.sin(density);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return new Sine(this.df.mapAll(visitor));
    }

    @Override
    public double minValue() {
        return -1;
    }

    @Override
    public double maxValue() {
        return 1;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
