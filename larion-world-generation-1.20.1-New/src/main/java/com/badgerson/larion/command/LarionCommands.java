package com.badgerson.larion.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Phase 7 benchmark: measures time to generate N chunks. Run in a fresh area (or new world)
 * for generation-only timing. See DEVELOPMENT_MAP.md Phase 7 and PHASE7-BENCHMARK.md.
 */
public final class LarionCommands {

    private static final int DEFAULT_RADIUS = 5;
    private static final int MIN_RADIUS = 1;
    private static final int MAX_RADIUS = 12;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("larion")
                        .then(Commands.literal("benchmark")
                                .executes(ctx -> runBenchmark(ctx, DEFAULT_RADIUS))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(MIN_RADIUS, MAX_RADIUS))
                                        .executes(ctx -> runBenchmark(ctx, IntegerArgumentType.getInteger(ctx, "radius"))))));
    }

    private static int runBenchmark(CommandContext<CommandSourceStack> ctx, int radius) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getLevel() instanceof ServerLevel)) {
            source.sendFailure(Component.literal("Not in a server level"));
            return 0;
        }
        ServerLevel level = (ServerLevel) source.getLevel();

        int centerChunkX = source.getPlayer() != null
                ? source.getPlayer().blockPosition().getX() >> 4
                : 0;
        int centerChunkZ = source.getPlayer() != null
                ? source.getPlayer().blockPosition().getZ() >> 4
                : 0;

        int side = 2 * radius + 1;
        int totalChunks = side * side;

        source.sendSuccess(() -> Component.literal("Generating " + totalChunks + " chunks (" + side + "x" + side + ")..."), false);

        long startNs = System.nanoTime();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                level.getChunk(centerChunkX + dx, centerChunkZ + dz);
            }
        }
        long elapsedNs = System.nanoTime() - startNs;
        double elapsedMs = elapsedNs / 1_000_000.0;
        double chunksPerSec = totalChunks / (elapsedMs / 1000.0);

        String msg = String.format("Generated %d chunks in %.1f ms (%.1f chunks/s)",
                totalChunks, elapsedMs, chunksPerSec);
        source.sendSuccess(() -> Component.literal(msg), false);
        source.sendSuccess(() -> Component.literal("Tip: run in a fresh area or new world for generation-only timing."), false);

        return 1;
    }
}
