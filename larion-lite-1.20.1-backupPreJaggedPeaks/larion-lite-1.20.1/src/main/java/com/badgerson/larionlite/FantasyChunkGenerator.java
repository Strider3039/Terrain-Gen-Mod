package com.badgerson.larionlite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Block;

/**
 * Column heightmap + domain warp; optional 3D cave noise. Terrain params merge {@link LarionLiteConfig} + world JSON.
 */
public final class FantasyChunkGenerator extends ChunkGenerator {

    public static final Codec<FantasyChunkGenerator> CODEC = RecordCodecBuilder.create(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(FantasyChunkGenerator::noiseSettingsHolder),
            FantasyTerrainConfig.CODEC.optionalFieldOf("terrain", FantasyTerrainConfig.DEFAULT).forGetter(FantasyChunkGenerator::terrain)
    ).apply(i, i.stable(FantasyChunkGenerator::new)));

    private final Holder<NoiseGeneratorSettings> noiseSettingsHolder;
    private final FantasyTerrainConfig terrain;

    private volatile NoiseBundle noises;
    private volatile RandomState cachedNoiseState;

    public FantasyChunkGenerator(BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> noiseSettingsHolder,
            FantasyTerrainConfig terrain) {
        super(biomeSource);
        this.noiseSettingsHolder = noiseSettingsHolder;
        this.terrain = terrain;
    }

    public Holder<NoiseGeneratorSettings> noiseSettingsHolder() {
        return noiseSettingsHolder;
    }

    public FantasyTerrainConfig terrain() {
        return terrain;
    }

    private FantasyTerrainConfig effectiveTerrain() {
        return LarionLiteConfig.resolve(terrain);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState,
            BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk,
            GenerationStep.Carving step) {
        // Vanilla carvers need NoiseBasedChunkGenerator + NoiseChunk. Caves: 3D noise in fillFromNoise.
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        ensureNoises(random);
        FantasyTerrainConfig t = effectiveTerrain();
        int sea = getSeaLevel();
        BlockState water = noiseSettingsHolder.value().defaultFluid();
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        Heightmap floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surfaceWg = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        int baseX = chunk.getPos().getMinBlockX();
        int baseZ = chunk.getPos().getMinBlockZ();
        int[] surf = new int[256];
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                surf[lz << 4 | lx] = surfaceY(baseX + lx, baseZ + lz, random);
            }
        }
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                int wx = baseX + lx;
                int wz = baseZ + lz;
                int surface = surf[lz << 4 | lx];
                mut.set(wx, surface, wz);
                int qy = QuartPos.fromBlock(Mth.clamp(surface, chunk.getMinBuildHeight(), chunk.getMaxBuildHeight() - 1));
                Holder<Biome> biome = chunk.getNoiseBiome(QuartPos.fromBlock(wx), qy, QuartPos.fromBlock(wz));

                if (surface < sea) {
                    for (int y = surface; y < sea; y++) {
                        mut.setY(y);
                        chunk.setBlockState(mut, water, false);
                        floor.update(lx, y, lz, water);
                        surfaceWg.update(lx, y, lz, water);
                    }
                    mut.setY(surface - 1);
                    BlockState seabed = seabedFor(biome);
                    chunk.setBlockState(mut, seabed, false);
                    floor.update(lx, surface - 1, lz, seabed);
                } else {
                    mut.setY(surface - 1);
                    BlockState top = topBlockFor(biome, mut, chunk);
                    chunk.setBlockState(mut, top, false);
                    floor.update(lx, surface - 1, lz, top);
                    surfaceWg.update(lx, surface - 1, lz, top);
                    int dirtDepth = biome.is(Biomes.DESERT) || biome.is(BiomeTags.IS_BEACH) ? 0 : 3;
                    Block under = biome.is(Biomes.DESERT) ? Blocks.SAND : Blocks.DIRT;
                    Block stoneBlock = noiseSettingsHolder.value().defaultBlock().getBlock();
                    for (int d = 1; d <= dirtDepth && surface - 1 - d >= chunk.getMinBuildHeight(); d++) {
                        mut.setY(surface - 1 - d);
                        if (chunk.getBlockState(mut).getBlock() == stoneBlock) {
                            chunk.setBlockState(mut, under.defaultBlockState(), false);
                        }
                    }
                    if (biome.value().coldEnoughToSnow(mut.set(wx, surface, wz))) {
                        mut.setY(surface);
                        if (chunk.getBlockState(mut).isAir()) {
                            chunk.setBlockState(mut, Blocks.SNOW.defaultBlockState(), false);
                        }
                    }
                }
            }
        }
        if (t.screeEnabled()) {
            applyScree(chunk, surf, sea, baseX, baseZ, t.screeMinDelta());
        }
    }

    /** Gravel on steep in-chunk faces only (no cross-chunk lookups — fast). */
    private void applyScree(ChunkAccess chunk, int[] surf, int sea, int baseX, int baseZ, int minDelta) {
        Block stoneBlock = noiseSettingsHolder.value().defaultBlock().getBlock();
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int lz = 1; lz < 15; lz++) {
            for (int lx = 1; lx < 15; lx++) {
                int s0 = surf[lz << 4 | lx];
                if (s0 < sea + 2) {
                    continue;
                }
                int maxD = 0;
                maxD = Math.max(maxD, Math.abs(s0 - surf[lz << 4 | (lx - 1)]));
                maxD = Math.max(maxD, Math.abs(s0 - surf[lz << 4 | (lx + 1)]));
                maxD = Math.max(maxD, Math.abs(s0 - surf[(lz - 1) << 4 | lx]));
                maxD = Math.max(maxD, Math.abs(s0 - surf[(lz + 1) << 4 | lx]));
                if (maxD < minDelta) {
                    continue;
                }
                int wx = baseX + lx;
                int wz = baseZ + lz;
                int qy = QuartPos.fromBlock(Mth.clamp(s0, chunk.getMinBuildHeight(), chunk.getMaxBuildHeight() - 1));
                Holder<Biome> biome = chunk.getNoiseBiome(QuartPos.fromBlock(wx), qy, QuartPos.fromBlock(wz));
                if (biome.is(Biomes.DESERT) || biome.is(BiomeTags.IS_BEACH) || biome.is(BiomeTags.IS_BADLANDS)
                        || biome.is(BiomeTags.IS_RIVER)) {
                    continue;
                }
                mut.set(wx, s0 - 1, wz);
                BlockState cur = chunk.getBlockState(mut);
                if (cur.getBlock() == Blocks.GRASS_BLOCK || cur.getBlock() == Blocks.DIRT) {
                    chunk.setBlockState(mut, Blocks.GRAVEL.defaultBlockState(), false);
                } else if (cur.getBlock() == stoneBlock && maxD >= minDelta + 2) {
                    chunk.setBlockState(mut, Blocks.STONE.defaultBlockState(), false);
                }
            }
        }
    }

    private static BlockState seabedFor(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_DEEP_OCEAN)) {
            return Blocks.GRAVEL.defaultBlockState();
        }
        if (biome.is(BiomeTags.IS_OCEAN)) {
            return Blocks.SAND.defaultBlockState();
        }
        return Blocks.SAND.defaultBlockState();
    }

    private static BlockState topBlockFor(Holder<Biome> biome, BlockPos.MutableBlockPos surfaceColumn, ChunkAccess chunk) {
        if (biome.is(BiomeTags.IS_NETHER) || biome.is(BiomeTags.IS_END)) {
            return chunk.getBlockState(surfaceColumn);
        }
        if (biome.is(BiomeTags.IS_RIVER)) {
            return Blocks.GRAVEL.defaultBlockState();
        }
        if (biome.is(BiomeTags.IS_BEACH) || biome.is(Biomes.DESERT)) {
            return Blocks.SAND.defaultBlockState();
        }
        if (biome.is(BiomeTags.IS_BADLANDS)) {
            return Blocks.RED_SAND.defaultBlockState();
        }
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        if (noiseSettingsHolder.value().disableMobGeneration()) {
            return;
        }
        ChunkPos chunkpos = region.getCenter();
        Holder<Biome> holder = region.getBiome(chunkpos.getWorldPosition().atY(region.getMaxBuildHeight() - 1));
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenrandom.setDecorationSeed(region.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(region, holder, chunkpos, worldgenrandom);
    }

    @Override
    public int getSeaLevel() {
        return noiseSettingsHolder.value().seaLevel();
    }

    @Override
    public int getMinY() {
        return noiseSettingsHolder.value().noiseSettings().minY();
    }

    @Override
    public int getGenDepth() {
        return noiseSettingsHolder.value().noiseSettings().height();
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        return surfaceY(x, z, randomState);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int surface = surfaceY(x, z, randomState);
        int min = heightAccessor.getMinBuildHeight();
        int h = heightAccessor.getHeight();
        BlockState[] states = new BlockState[h];
        BlockState stone = noiseSettingsHolder.value().defaultBlock();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < h; i++) {
            int y = min + i;
            states[i] = y < surface ? stone : air;
        }
        return new NoiseColumn(min, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState randomState, BlockPos pos) {
        FantasyTerrainConfig t = effectiveTerrain();
        lines.add("Larion Lite fantasy | caves=" + t.cavesEnabled());
        lines.add("Surface Y: " + surfaceY(pos.getX(), pos.getZ(), randomState));
    }

    private void ensureNoises(RandomState state) {
        if (cachedNoiseState == state && noises != null) {
            return;
        }
        synchronized (this) {
            if (cachedNoiseState == state && noises != null) {
                return;
            }
            noises = NoiseBundle.create(state);
            cachedNoiseState = state;
        }
    }

    private int surfaceY(int worldX, int worldZ, RandomState randomState) {
        ensureNoises(randomState);
        FantasyTerrainConfig t = effectiveTerrain();
        double warpX = noises.warpX(worldX, worldZ, t.warpSampleScale(), t.warpBlockStrength());
        double warpZ = noises.warpZ(worldX, worldZ, t.warpSampleScale(), t.warpBlockStrength());
        double wx = worldX + warpX;
        double wz = worldZ + warpZ;
        double continent = noises.continent(wx, wz, t.continentScale());
        double absC = Math.abs(continent);
        double smAbs = smoothstep(0.18, 0.82, absC);
        double detailMul = 1.0 - t.detailContinentBlend() * 0.65 * smAbs;
        double hvMult = 1.0 + t.macroVerticalContrast()
                * ((0.64 + 0.71 * smoothstep(0.20, 0.93, absC)) - 1.0);
        int hvEff = Math.max(24, Mth.floor(t.heightVariation() * hvMult));

        double ridge = noises.ridge(worldX, worldZ, t.ridgeScale()) * t.ridgeBlockAmplitude();
        double ridgeFine = t.ridgeFineAmplitude() > 1e-6
                ? noises.ridgeFine(worldX, worldZ, t.ridgeFineScale()) * t.ridgeFineAmplitude()
                : 0.0;
        double detail = noises.detail(worldX, worldZ, t.detailScale()) * t.detailAmplitude() * detailMul;

        int sea = getSeaLevel();
        int y = sea + t.baseHeightOffset() + Mth.floor(continent * hvEff + ridge + ridgeFine + detail);

        if (t.valleyDepth() > 0 || t.channelDepth() > 0) {
            double lowlandMask = 1.0 - smoothstep(0.28, 0.90, absC);
            if (t.valleyDepth() > 0) {
                double vn = (noises.valley(worldX, worldZ, t.valleyScale()) + 1.0) * 0.5;
                y -= Mth.floor(t.valleyDepth() * vn * vn * lowlandMask);
            }
            if (t.channelDepth() > 0) {
                double ch = noises.channelTrough(worldX, worldZ, t.channelScale());
                y -= Mth.floor(t.channelDepth() * ch * ch * lowlandMask);
            }
        }

        int min = getMinY() + 2;
        int max = getMinY() + noiseSettingsHolder.value().noiseSettings().height() - 2;
        return Mth.clamp(y, min, max);
    }

    private static double smoothstep(double edge0, double edge1, double x) {
        if (edge0 >= edge1) {
            return x >= edge1 ? 1.0 : 0.0;
        }
        double t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState,
            StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(() -> {
            ensureNoises(randomState);
            FantasyTerrainConfig t = effectiveTerrain();
            BlockState stone = noiseSettingsHolder.value().defaultBlock();
            BlockState air = Blocks.AIR.defaultBlockState();
            int minY = chunk.getMinBuildHeight();
            int maxY = chunk.getMaxBuildHeight();
            int baseX = chunk.getPos().getMinBlockX();
            int baseZ = chunk.getPos().getMinBlockZ();
            Heightmap floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
            Heightmap surfaceWg = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

            int[] heights = new int[256];
            for (int lz = 0; lz < 16; lz++) {
                for (int lx = 0; lx < 16; lx++) {
                    int wx = baseX + lx;
                    int wz = baseZ + lz;
                    int surface = surfaceY(wx, wz, randomState);
                    heights[lz << 4 | lx] = surface;
                    for (int y = minY; y < maxY; y++) {
                        BlockState b = y < surface ? stone : air;
                        chunk.setBlockState(new BlockPos(lx, y, lz), b, false);
                        if (!b.isAir()) {
                            floor.update(lx, y, lz, b);
                        }
                        if (y >= surface) {
                            surfaceWg.update(lx, y, lz, b);
                        }
                    }
                }
            }

            if (t.cavesEnabled()) {
                double wormS = t.caveNoiseScale();
                double wormTh = t.caveThreshold();
                double roomS = t.caveChamberScale();
                double roomTh = t.caveChamberThreshold();
                int pad = t.caveSurfacePadding();
                int minCarve = minY + 6;
                Block stoneBlock = stone.getBlock();
                for (int lz = 0; lz < 16; lz++) {
                    for (int lx = 0; lx < 16; lx++) {
                        int wx = baseX + lx;
                        int wz = baseZ + lz;
                        int surface = heights[lz << 4 | lx];
                        int yMax = surface - pad;
                        if (yMax <= minCarve) {
                            continue;
                        }
                        double peak = Mth.clamp((surface - getSeaLevel()) / 95.0, 0.0, 1.0);
                        double pr = t.cavePeakReduction();
                        double wormEff = wormTh + peak * pr;
                        double roomEff = roomTh + peak * pr * 0.55;
                        for (int y = minCarve; y < yMax; y++) {
                            boolean open = noises.cave(wx, y, wz, wormS) > wormEff
                                    || noises.caveChamber(wx, y, wz, roomS) > roomEff;
                            if (open) {
                                BlockPos p = new BlockPos(lx, y, lz);
                                if (chunk.getBlockState(p).getBlock() == stoneBlock) {
                                    chunk.setBlockState(p, air, false);
                                }
                            }
                        }
                    }
                }
            }

            return chunk;
        }, executor);
    }

    private static final class NoiseBundle {
        private final NormalNoise continent;
        private final NormalNoise warpA;
        private final NormalNoise warpB;
        private final NormalNoise ridge;
        private final NormalNoise ridgeFine;
        private final NormalNoise detail2d;
        private final NormalNoise valley2d;
        private final NormalNoise channel2d;
        private final NormalNoise cave3d;
        private final NormalNoise caveChamber3d;

        private NoiseBundle(NormalNoise continent, NormalNoise warpA, NormalNoise warpB, NormalNoise ridge,
                NormalNoise ridgeFine, NormalNoise detail2d, NormalNoise valley2d, NormalNoise channel2d,
                NormalNoise cave3d, NormalNoise caveChamber3d) {
            this.continent = continent;
            this.warpA = warpA;
            this.warpB = warpB;
            this.ridge = ridge;
            this.ridgeFine = ridgeFine;
            this.detail2d = detail2d;
            this.valley2d = valley2d;
            this.channel2d = channel2d;
            this.cave3d = cave3d;
            this.caveChamber3d = caveChamber3d;
        }

        static NoiseBundle create(RandomState state) {
            ResourceLocation ns = new ResourceLocation(LarionLiteMod.MOD_ID, "fantasy");
            RandomSource r0 = state.getOrCreateRandomFactory(ns).fromHashOf(new ResourceLocation(LarionLiteMod.MOD_ID, "continent"));
            RandomSource r1 = state.getOrCreateRandomFactory(ns).fromHashOf(new ResourceLocation(LarionLiteMod.MOD_ID, "warp_x"));
            RandomSource r2 = state.getOrCreateRandomFactory(ns).fromHashOf(new ResourceLocation(LarionLiteMod.MOD_ID, "warp_z"));
            RandomSource r3 = state.getOrCreateRandomFactory(ns).fromHashOf(new ResourceLocation(LarionLiteMod.MOD_ID, "ridge"));
            RandomSource r4 = state.getOrCreateRandomFactory(ns).fromHashOf(new ResourceLocation(LarionLiteMod.MOD_ID, "detail"));
            RandomSource r5 = state.getOrCreateRandomFactory(ns).fromHashOf(new ResourceLocation(LarionLiteMod.MOD_ID, "cave_3d"));
            RandomSource r6 = state.getOrCreateRandomFactory(ns).fromHashOf(new ResourceLocation(LarionLiteMod.MOD_ID, "cave_chamber"));
            RandomSource r7 = state.getOrCreateRandomFactory(ns).fromHashOf(new ResourceLocation(LarionLiteMod.MOD_ID, "ridge_fine"));
            RandomSource r8 = state.getOrCreateRandomFactory(ns).fromHashOf(new ResourceLocation(LarionLiteMod.MOD_ID, "valley"));
            RandomSource r9 = state.getOrCreateRandomFactory(ns).fromHashOf(new ResourceLocation(LarionLiteMod.MOD_ID, "channel"));
            return new NoiseBundle(
                    NormalNoise.create(r0, new NormalNoise.NoiseParameters(-8, 1.0, 0.65, 0.35, 0.2)),
                    NormalNoise.create(r1, new NormalNoise.NoiseParameters(-7, 1.0, 0.5)),
                    NormalNoise.create(r2, new NormalNoise.NoiseParameters(-7, 1.0, 0.5)),
                    NormalNoise.create(r3, new NormalNoise.NoiseParameters(-6, 1.0, 0.4, 0.2)),
                    NormalNoise.create(r7, new NormalNoise.NoiseParameters(-4, 1.0, 0.5, 0.35, 0.2)),
                    NormalNoise.create(r4, new NormalNoise.NoiseParameters(-5, 1.0, 0.45, 0.25)),
                    NormalNoise.create(r8, new NormalNoise.NoiseParameters(-7, 1.0, 0.55, 0.3)),
                    NormalNoise.create(r9, new NormalNoise.NoiseParameters(-6, 1.0, 0.5, 0.35)),
                    NormalNoise.create(r5, new NormalNoise.NoiseParameters(-5, 1.0, 0.55, 0.3, 0.15)),
                    NormalNoise.create(r6, new NormalNoise.NoiseParameters(-4, 1.0, 0.5, 0.35, 0.2, 0.1)));
        }

        double continent(double x, double z, double scale) {
            return continent.getValue(x * scale, 0, z * scale);
        }

        double warpX(int x, int z, double scale, double strength) {
            return warpA.getValue(x * scale, 0, z * scale) * strength;
        }

        double warpZ(int x, int z, double scale, double strength) {
            return warpB.getValue(x * scale + 17.3, 0, z * scale + 31.7) * strength;
        }

        double ridge(int x, int z, double scale) {
            double v = ridge.getValue(x * scale, 0, z * scale);
            return v * v * Math.signum(v);
        }

        double detail(int wx, int wz, double scale) {
            return detail2d.getValue(wx * scale, 0, wz * scale);
        }

        double ridgeFine(int x, int z, double scale) {
            double v = ridgeFine.getValue(x * scale, 0, z * scale);
            return v * v * Math.signum(v);
        }

        double valley(int x, int z, double scale) {
            return valley2d.getValue(x * scale, 0, z * scale);
        }

        /** Elongated lows (paired sample → troughs read as shallow channels). */
        double channelTrough(int x, int z, double scale) {
            double sx = x * scale;
            double sz = z * scale;
            double a = channel2d.getValue(sx, 0, sz);
            double b = channel2d.getValue(sx + 2.15, 0, sz + 1.03);
            double c = channel2d.getValue(sx - 1.02, 0, sz + 1.88);
            double d = Math.min(Math.abs(a - b), Math.abs(a - c));
            return Mth.clamp((float) (1.0 - d * 2.4), 0.0f, 1.0f);
        }

        double cave(int wx, int y, int wz, double scale) {
            return cave3d.getValue(wx * scale, y * scale, wz * scale);
        }

        double caveChamber(int wx, int y, int wz, double scale) {
            return caveChamber3d.getValue(wx * scale, y * scale, wz * scale);
        }
    }
}
