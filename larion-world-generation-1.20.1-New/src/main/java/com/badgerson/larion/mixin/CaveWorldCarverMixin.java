package com.badgerson.larion.mixin;

import com.badgerson.larion.Constants;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CaveWorldCarver.class)
public class CaveWorldCarverMixin {

    @Inject(method = "getCaveBound", at = @At("HEAD"), cancellable = true, remap = false)
    private void getCaveBoundOverride(CallbackInfoReturnable<Integer> ci) {
        ci.setReturnValue(Constants.CAVE_BOUND_OVERRIDE);
    }
}
