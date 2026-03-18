package com.badgerson.larionlite;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(LarionLiteMod.MOD_ID)
public final class LarionLiteMod {

    public static final String MOD_ID = "larion_lite";

    private static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MOD_ID);

    public static final RegistryObject<Codec<? extends ChunkGenerator>> FANTASY =
            CHUNK_GENERATORS.register("fantasy", () -> FantasyChunkGenerator.CODEC);

    @SuppressWarnings("removal")
    public LarionLiteMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LarionLiteConfig.SPEC, LarionLiteMod.MOD_ID + "-common.toml");
        CHUNK_GENERATORS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
