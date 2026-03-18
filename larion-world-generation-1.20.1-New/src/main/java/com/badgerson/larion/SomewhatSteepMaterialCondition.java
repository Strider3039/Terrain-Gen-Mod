package com.badgerson.larion;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public enum SomewhatSteepMaterialCondition implements SurfaceRules.ConditionSource {
    INSTANCE;

    static final KeyDispatchDataCodec<SomewhatSteepMaterialCondition> CODEC =
            KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.Condition apply(SurfaceRules.Context context) {
        // Mixin adds MaterialRuleContextExtensions to Context; if not applied (e.g. classloader edge case), fall back
        if (MaterialRuleContextExtensions.class.isInstance(context)) {
            return ((MaterialRuleContextExtensions) (Object) context).getSomewhatSteepSlopePredicate();
        }
        return NEVER;
    }

    /** Fallback when mixin is not applied to this Context (avoids ClassCastException during chunk gen). */
    private static final SurfaceRules.Condition NEVER = new SurfaceRules.Condition() {
        @Override
        public boolean test() {
            return false;
        }
    };
}
