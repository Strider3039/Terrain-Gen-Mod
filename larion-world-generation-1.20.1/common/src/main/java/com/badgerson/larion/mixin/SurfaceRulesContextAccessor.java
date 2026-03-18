package com.badgerson.larion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.SurfaceRules;

@Mixin(SurfaceRules.Context.class)
public interface SurfaceRulesContextAccessor {
    @Accessor("blockX")
    int larion$getBlockX();

    @Accessor("blockZ")
    int larion$getBlockZ();

    @Accessor("chunk")
    ChunkAccess larion$getChunk();
}
