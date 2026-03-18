package com.badgerson.larion.mixin;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

    @Inject(method = "createFluidPicker", at = @At("HEAD"), cancellable = true, remap = false)
    private static void createFluidPicker(NoiseGeneratorSettings settings,
            CallbackInfoReturnable<Aquifer.FluidPicker> ci) {
        // Lava sea level follows world minY + 10 so deeper caves aren't flooded (vanilla uses -54).
        int lavaSeaLevel = settings.noiseSettings().minY() + 10;
        Aquifer.FluidStatus lavaFluidStatus = new Aquifer.FluidStatus(lavaSeaLevel, Blocks.LAVA.defaultBlockState());
        int waterSeaLevel = settings.seaLevel();
        Aquifer.FluidStatus defaultFluidStatus = new Aquifer.FluidStatus(waterSeaLevel, settings.defaultFluid());
        ci.setReturnValue((x, y, z) -> y < Math.min(lavaSeaLevel, waterSeaLevel) ? lavaFluidStatus : defaultFluidStatus);
    }
}
