package com.badgerson.larionlite;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Phase 7: chunk generation benchmark (fantasy overworld only meaningful when that generator is active).
 */
@Mod.EventBusSubscriber(modid = LarionLiteMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LarionLiteCommands {

    private static final int DEFAULT_RADIUS = 5;
    private static final int MIN_RADIUS = 1;
    private static final int MAX_RADIUS = 12;

    private LarionLiteCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("larion_lite")
                .then(Commands.literal("benchmark")
                        .executes(ctx -> runBenchmark(ctx, DEFAULT_RADIUS))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(MIN_RADIUS, MAX_RADIUS))
                                .executes(ctx -> runBenchmark(ctx, IntegerArgumentType.getInteger(ctx, "radius"))))));
    }

    private static int runBenchmark(CommandContext<CommandSourceStack> ctx, int radius) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getLevel() instanceof ServerLevel)) {
            source.sendFailure(Component.literal("Not a server level"));
            return 0;
        }
        ServerLevel level = (ServerLevel) source.getLevel();
        int cx = source.getPlayer() != null ? source.getPlayer().blockPosition().getX() >> 4 : 0;
        int cz = source.getPlayer() != null ? source.getPlayer().blockPosition().getZ() >> 4 : 0;
        int side = 2 * radius + 1;
        int total = side * side;
        source.sendSuccess(() -> Component.literal("Generating " + total + " chunks (" + side + "x" + side + ")..."), false);
        long t0 = System.nanoTime();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                level.getChunk(cx + dx, cz + dz);
            }
        }
        double ms = (System.nanoTime() - t0) / 1_000_000.0;
        double cps = total / (ms / 1000.0);
        source.sendSuccess(() -> Component.literal(String.format(
                "Done: %d chunks in %.1f ms (%.1f chunks/s)", total, ms, cps)), false);
        source.sendSuccess(() -> Component.literal("Fly to unloaded area or new world for gen-heavy timing."), false);
        return 1;
    }
}
