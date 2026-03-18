package com.badgerson.larion.mixin;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SurfaceRules.Context.class)
public interface SurfaceRulesContextAccessor {

    @Accessor(value = "blockX", remap = false)
    int larion$getBlockX();

    @Accessor(value = "blockZ", remap = false)
    int larion$getBlockZ();

    @Accessor(value = "chunk", remap = false)
    ChunkAccess larion$getChunk();
}
