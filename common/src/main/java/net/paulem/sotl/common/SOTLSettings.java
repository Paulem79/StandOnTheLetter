package net.paulem.sotl.common;

public final class SOTLSettings {
    private SOTLSettings() {
    }

    public static final int TICKS_PER_SECOND = 20;

    /** Delay between two letter rounds. */
    public static final int LETTER_INTERVAL_TICKS = TICKS_PER_SECOND * 60;
    /** Number of letters shown while spinning. */
    public static final int ROLL_COUNT = 15;
    /** Ticks between two spinning letters. */
    public static final int ROLL_INTERVAL_TICKS = 2;
    /** Delay between the final letter announcement and the destruction. */
    public static final int DESTRUCTION_DELAY_TICKS = TICKS_PER_SECOND * 10;
    /** Delay between the destruction and the restoration. */
    public static final int RESTORE_DELAY_TICKS = TICKS_PER_SECOND * 10;

    /** Radius (in chunks) destroyed around each player. */
    public static final int PLAYER_CHUNK_RADIUS = 1;
    /** Blocks above the feet of a player that are still destroyed. */
    public static final int PLAYER_HEIGHT_MARGIN = 2;

    /** Blocks below the feet of a player scanned to pick the letter. */
    public static final int SURVEY_DEPTH = 24;
    /** How much rarer blocks are favored: a block weighs 1 / count^exponent. */
    public static final double RARITY_EXPONENT = 0.5;
    /** Weight multiplier of a block a player has in their inventory. */
    public static final double INVENTORY_WEIGHT_MULTIPLIER = 1.1;
}
