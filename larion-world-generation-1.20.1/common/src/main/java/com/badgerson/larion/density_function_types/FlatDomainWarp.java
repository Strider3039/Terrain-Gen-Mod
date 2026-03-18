package com.badgerson.larion.density_function_types;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

/**
 * Warps the sampling domain in X/Z so the input is evaluated at (blockX + warpX, blockY, blockZ + warpZ).
 * On 1.20.1 we use a wrapper around the engine's context (not SinglePointContext) so the input tree
 * still sees a context that delegates to the real provider; only blockX/blockZ are overridden with warped values.
 * This allows batched input.fillArray() to work without breaking vanilla nodes that rely on context identity/state.
 */
public final class FlatDomainWarp implements DensityFunction {
	private final DensityFunction input;
	private final DensityFunction warpX;
	private final DensityFunction warpZ;

	private static final MapCodec<FlatDomainWarp> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance
			.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(FlatDomainWarp::input),
					DensityFunction.HOLDER_HELPER_CODEC.fieldOf("warp_x").forGetter(FlatDomainWarp::warpX),
					DensityFunction.HOLDER_HELPER_CODEC.fieldOf("warp_z").forGetter(FlatDomainWarp::warpZ))
			.apply(instance, (FlatDomainWarp::new)));
	public static final KeyDispatchDataCodec<FlatDomainWarp> CODEC = DensityFunctions.makeCodec(MAP_CODEC);

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
		return input.compute(new SinglePointContext(
				context.blockX() + (int) this.warpX.compute(context),
				context.blockY(),
				context.blockZ() + (int) this.warpZ.compute(context)));
	}

	@Override
	public void fillArray(double[] densities, ContextProvider provider) {
		final int len = densities.length;
		double[] warpXValues = new double[len];
		double[] warpZValues = new double[len];
		this.warpX.fillArray(warpXValues, provider);
		this.warpZ.fillArray(warpZValues, provider);
		// Use a warped provider that wraps the engine's context (not replaces it) so the input
		// tree can batch and vanilla nodes still see a context that delegates to the real provider.
		ContextProvider warpedProvider = new WarpedContextProvider(provider, warpXValues, warpZValues);
		this.input.fillArray(densities, warpedProvider);
	}

	/**
	 * Wraps the engine's FunctionContext and overrides only blockX/blockZ with warped values.
	 * This preserves the real context so nodes that rely on context type or internal state still work.
	 */
	private static final class WarpedFunctionContext implements FunctionContext {
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

	/** Provides warped contexts by wrapping the engine's context; enables batched input.fillArray. */
	private static final class WarpedContextProvider implements ContextProvider {
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
		return visitor.apply(
				new FlatDomainWarp(this.input.mapAll(visitor), this.warpX.mapAll(visitor), this.warpZ.mapAll(visitor)));
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
