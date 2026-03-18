package com.badgerson.larion;

/**
 * Shared constants for the Larion mod.
 * Used by mixins, density types, and surface rules.
 */
public final class Constants {
    private Constants() {}

    /** Mod ID; must match {@code mod_id} in gradle.properties and @Mod value. */
    public static final String MOD_ID = "larion_new";

    /** Cave carver vertical bound (blocks). Lower = less vertical carving work per chunk (vanilla 15). */
    public static final int CAVE_BOUND_OVERRIDE = 20;

    /** Chunk section index maximum (15 for 16 sections). Used in steep slope predicates. */
    public static final int CHUNK_SECTION_MAX = 15;
}
