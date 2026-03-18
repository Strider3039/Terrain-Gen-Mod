package com.badgerson.larion.density_function_types;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Dividend / divisor; divide-by-zero → 0. Batches both arguments then one divide loop (IMPLEMENTATION_PLAN §4).
 */
public final class Division implements DensityFunction {
    private final DensityFunction argument1;
    private final DensityFunction argument2;

    private static final MapCodec<Division> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument1").forGetter(Division::argument1),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument2").forGetter(Division::argument2)
            ).apply(instance, Division::new));
    public static final KeyDispatchDataCodec<Division> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    public Division(DensityFunction argument1, DensityFunction argument2) {
        this.argument1 = argument1;
        this.argument2 = argument2;
    }

    public DensityFunction argument1() {
        return this.argument1;
    }

    public DensityFunction argument2() {
        return this.argument2;
    }

    @Override
    public double compute(FunctionContext context) {
        double divisorValue = this.argument2.compute(context);
        if (divisorValue == 0) {
            return 0.0;
        }
        return this.argument1.compute(context) / divisorValue;
    }

    /** Batch both args, then one divide loop; 0 where divisor is 0. */
    @Override
    public void fillArray(double[] densities, ContextProvider provider) {
        final int len = densities.length;
        double[] dividendValues = new double[len];
        double[] divisorValues = new double[len];
        this.argument1.fillArray(dividendValues, provider);
        this.argument2.fillArray(divisorValues, provider);
        for (int i = 0; i < len; i++) {
            double num = dividendValues[i];
            double den = divisorValues[i];
            densities[i] = den == 0.0 ? 0.0 : num / den;
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new Division(this.argument1.mapAll(visitor), this.argument2.mapAll(visitor)));
    }

    @Override
    public double minValue() {
        if (this.argument2.minValue() == 0) return 0;
        return this.argument1.minValue() / this.argument2.minValue();
    }

    @Override
    public double maxValue() {
        if (this.argument2.maxValue() == 0) return 0;
        return this.argument1.maxValue() / this.argument2.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
