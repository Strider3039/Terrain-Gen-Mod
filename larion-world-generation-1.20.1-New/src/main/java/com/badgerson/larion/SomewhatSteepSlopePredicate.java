package com.badgerson.larion;

import com.badgerson.larion.mixin.SurfaceRulesContextAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.SurfaceRules;

/**
 * Condition: true when height difference to a neighbor is > 1 block (somewhat steep).
 */
public class SomewhatSteepSlopePredicate extends SurfaceRules.LazyXZCondition {

    private static final int SOMEWHAT_STEEP_THRESHOLD = 1;

    public SomewhatSteepSlopePredicate(SurfaceRules.Context context) {
        super(context);
    }

    @Override
    protected boolean compute() {
        SurfaceRulesContextAccessor contextAccessor = (SurfaceRulesContextAccessor) (Object) this.context;
        int x = contextAccessor.larion$getBlockX() & 15;
        int z = contextAccessor.larion$getBlockZ() & 15;
        ChunkAccess chunk = contextAccessor.larion$getChunk();
        int here = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        int south = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, Math.max(z - 1, 0));
        if (here - south > SOMEWHAT_STEEP_THRESHOLD) return true;
        int north = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, Math.min(z + 1, Constants.CHUNK_SECTION_MAX));
        if (here - north > SOMEWHAT_STEEP_THRESHOLD) return true;
        int west = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, Math.max(x - 1, 0), z);
        if (here - west > SOMEWHAT_STEEP_THRESHOLD) return true;
        int east = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, Math.min(x + 1, Constants.CHUNK_SECTION_MAX), z);
        return here - east > SOMEWHAT_STEEP_THRESHOLD;
    }
}
