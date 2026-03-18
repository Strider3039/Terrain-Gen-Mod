package com.badgerson.larion;

import com.badgerson.larion.command.LarionCommands;
import com.badgerson.larion.density_function_types.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(Constants.MOD_ID)
public class LarionMod {

    private static final DeferredRegister<Codec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, "larion");

    public static final RegistryObject<Codec<? extends DensityFunction>> DIV =
            DENSITY_FUNCTION_TYPES.register("div", () -> Division.CODEC.codec());
    public static final RegistryObject<Codec<? extends DensityFunction>> SQRT =
            DENSITY_FUNCTION_TYPES.register("sqrt", () -> Sqrt.CODEC.codec());
    public static final RegistryObject<Codec<? extends DensityFunction>> SIGNUM =
            DENSITY_FUNCTION_TYPES.register("signum", () -> Signum.CODEC.codec());
    public static final RegistryObject<Codec<? extends DensityFunction>> SINE =
            DENSITY_FUNCTION_TYPES.register("sine", () -> Sine.CODEC.codec());
    public static final RegistryObject<Codec<? extends DensityFunction>> X_COORD =
            DENSITY_FUNCTION_TYPES.register("x", () -> XCoord.CODEC.codec());
    public static final RegistryObject<Codec<? extends DensityFunction>> Z_COORD =
            DENSITY_FUNCTION_TYPES.register("z", () -> ZCoord.CODEC.codec());
    public static final RegistryObject<Codec<? extends DensityFunction>> FLAT_DOMAIN_WARP =
            DENSITY_FUNCTION_TYPES.register("flat_domain_warp", () -> FlatDomainWarp.CODEC.codec());

    private static final DeferredRegister<Codec<? extends SurfaceRules.ConditionSource>> MATERIAL_CONDITIONS =
            DeferredRegister.create(Registries.MATERIAL_CONDITION, "larion");

    public static final RegistryObject<Codec<? extends SurfaceRules.ConditionSource>> SOMEWHAT_STEEP =
            MATERIAL_CONDITIONS.register("somewhat_steep", () -> SomewhatSteepMaterialCondition.CODEC.codec());

    @SuppressWarnings("removal")
    public LarionMod() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DENSITY_FUNCTION_TYPES.register(modBus);
        MATERIAL_CONDITIONS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(LarionMod::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        LarionCommands.register(event.getDispatcher());
    }
}
