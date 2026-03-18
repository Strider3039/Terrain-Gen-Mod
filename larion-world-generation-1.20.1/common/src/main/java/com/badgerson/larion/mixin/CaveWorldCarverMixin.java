package com.badgerson.larion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.badgerson.larion.Constants;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;

@Mixin(CaveWorldCarver.class)
public class CaveWorldCarverMixin {
  @Inject(method = "getCaveBound", at = @At("HEAD"), cancellable = true)
  private void getCaveBoundOverride(CallbackInfoReturnable<Integer> ci) {
    // Vanilla value: 15. Larion uses a larger bound for deeper cave generation.
    ci.setReturnValue(Constants.CAVE_BOUND_OVERRIDE);
  }
}
