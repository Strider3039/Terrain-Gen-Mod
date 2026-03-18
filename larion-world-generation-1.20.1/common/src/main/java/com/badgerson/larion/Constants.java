package com.badgerson.larion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MOD_ID = "larion";
	public static final String MOD_NAME = "Larion World Generation";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	/** Override for cave carver bound (vanilla: 15). Larger value allows deeper cave generation. */
	public static final int CAVE_BOUND_OVERRIDE = 40;

	/** Max local coordinate inside a chunk section (0–15). Used for heightmap neighbor bounds. */
	public static final int CHUNK_SECTION_MAX = 15;
}
