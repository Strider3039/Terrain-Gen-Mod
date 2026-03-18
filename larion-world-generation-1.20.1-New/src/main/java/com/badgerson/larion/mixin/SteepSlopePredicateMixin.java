package com.badgerson.larion.mixin;

import com.badgerson.larion.Constants;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SurfaceRules.Context.SteepMaterialCondition.class)
public class SteepSlopePredicateMixin extends SurfaceRules.LazyXZCondition {

    private static final int STEEP_THRESHOLD = 3;

    protected SteepSlopePredicateMixin(SurfaceRules.Context materialRuleContext) {
        super(materialRuleContext);
    }

    @Override
    protected boolean compute() {
        SurfaceRulesContextAccessor contextAccessor = (SurfaceRulesContextAccessor) (Object) this.context;
        int x = contextAccessor.larion$getBlockX() & 15;
        int z = contextAccessor.larion$getBlockZ() & 15;
        ChunkAccess chunk = contextAccessor.larion$getChunk();
        int here = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        int south = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, Math.max(z - 1, 0));
        if (here - south > STEEP_THRESHOLD) return true;
        int north = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, Math.min(z + 1, Constants.CHUNK_SECTION_MAX));
        if (here - north > STEEP_THRESHOLD) return true;
        int west = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, Math.max(x - 1, 0), z);
        if (here - west > STEEP_THRESHOLD) return true;
        int east = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, Math.min(x + 1, Constants.CHUNK_SECTION_MAX), z);
        return here - east > STEEP_THRESHOLD;
    }
}
