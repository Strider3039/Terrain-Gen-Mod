package com.badgerson.larion.density_function_types;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Warps the sampling domain in X/Z so the input is evaluated at (blockX + warpX, blockY, blockZ + warpZ).
 * Uses WarpedContextProvider + WarpedFunctionContext only (wrap engine context; override only blockX/blockZ).
 * No SinglePointContext in the batched path — see IMPLEMENTATION_PLAN.
 */
public final class FlatDomainWarp implements DensityFunction {
    private final DensityFunction input;
    private final DensityFunction warpX;
    private final DensityFunction warpZ;

    private static final MapCodec<FlatDomainWarp> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(FlatDomainWarp::input),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("warp_x").forGetter(FlatDomainWarp::warpX),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("warp_z").forGetter(FlatDomainWarp::warpZ)
            ).apply(instance, FlatDomainWarp::new));
    public static final KeyDispatchDataCodec<FlatDomainWarp> CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    public FlatDomainWarp(DensityFunction input, DensityFunction warpX, DensityFunction warpZ) {
        this.input = input;
        this.warpX = warpX;
        this.warpZ = warpZ;
    }

    public DensityFunction input() {
        return this.input;
    }

    public DensityFunction warpX() {
        return this.warpX;
    }

    public DensityFunction warpZ() {
        return this.warpZ;
    }

    @Override
    public double compute(FunctionContext context) {
        int wx = (int) this.warpX.compute(context);
        int wz = (int) this.warpZ.compute(context);
        FunctionContext warpedContext = new WarpedFunctionContext(context, wx, wz);
        return input.compute(warpedContext);
    }

    @Override
    public void fillArray(double[] densities, ContextProvider provider) {
        final int len = densities.length;
        double[] warpXValues = new double[len];
        double[] warpZValues = new double[len];
        this.warpX.fillArray(warpXValues, provider);
        this.warpZ.fillArray(warpZValues, provider);
        ContextProvider warpedProvider = new WarpedContextProvider(provider, warpXValues, warpZValues);
        this.input.fillArray(densities, warpedProvider);
    }

    /** Wraps the engine's FunctionContext; overrides only blockX/blockZ with warped values. */
    static final class WarpedFunctionContext implements FunctionContext {
        private final FunctionContext delegate;
        private final int warpX;
        private final int warpZ;

        WarpedFunctionContext(FunctionContext delegate, int warpX, int warpZ) {
            this.delegate = delegate;
            this.warpX = warpX;
            this.warpZ = warpZ;
        }

        @Override
        public int blockX() {
            return delegate.blockX() + warpX;
        }

        @Override
        public int blockY() {
            return delegate.blockY();
        }

        @Override
        public int blockZ() {
            return delegate.blockZ() + warpZ;
        }
    }

    /** Provides warped contexts by wrapping the engine's provider; enables batched input.fillArray. */
    static final class WarpedContextProvider implements ContextProvider {
        private final ContextProvider provider;
        private final double[] warpX;
        private final double[] warpZ;

        WarpedContextProvider(ContextProvider provider, double[] warpX, double[] warpZ) {
            this.provider = provider;
            this.warpX = warpX;
            this.warpZ = warpZ;
        }

        @Override
        public FunctionContext forIndex(int index) {
            FunctionContext ctx = provider.forIndex(index);
            return new WarpedFunctionContext(ctx, (int) warpX[index], (int) warpZ[index]);
        }

        @Override
        public void fillAllDirectly(double[] densities, DensityFunction function) {
            final int len = densities.length;
            for (int i = 0; i < len; i++) {
                densities[i] = function.compute(forIndex(i));
            }
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new FlatDomainWarp(
                this.input.mapAll(visitor),
                this.warpX.mapAll(visitor),
                this.warpZ.mapAll(visitor)));
    }

    @Override
    public double minValue() {
        return this.input.minValue();
    }

    @Override
    public double maxValue() {
        return this.input.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
