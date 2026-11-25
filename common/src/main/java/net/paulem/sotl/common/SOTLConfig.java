package net.paulem.sotl.common;

/**
 * Config file
 * @param roundIntervalSeconds
 * @param destructionDelaySeconds
 * @param restoreDelaySeconds
 * @param playerChunkRadius
 * @param playerHeightMargin
 * @param surveyDepth
 * @param rarityExponent
 * @param inventoryWeightMultiplier
 * @param maxMillisPerTick
 * @param spinningText
 * @param finalText
 * @param destructionText
 */
public record SOTLConfig(
        int roundIntervalSeconds,
        int destructionDelaySeconds,
        int restoreDelaySeconds,
        int playerChunkRadius,
        int playerHeightMargin,
        int surveyDepth,
        double rarityExponent,
        double inventoryWeightMultiplier,
        int maxMillisPerTick,
        String spinningText,
        String finalText,
        String destructionText
) {
    /** Text for the value of delay, changed in finalText */
    public static final String SECONDS_PLACEHOLDER = "{seconds}";

    public static final int DEFAULT_ROUND_INTERVAL_SECONDS = 60;
    public static final int MIN_ROUND_INTERVAL_SECONDS = 5;
    public static final int MAX_ROUND_INTERVAL_SECONDS = 3600;

    public static final int DEFAULT_DESTRUCTION_DELAY_SECONDS = 10;
    public static final int MIN_DESTRUCTION_DELAY_SECONDS = 1;
    public static final int MAX_DESTRUCTION_DELAY_SECONDS = 300;

    public static final int DEFAULT_RESTORE_DELAY_SECONDS = 10;
    public static final int MIN_RESTORE_DELAY_SECONDS = 1;
    public static final int MAX_RESTORE_DELAY_SECONDS = 300;

    public static final int DEFAULT_PLAYER_CHUNK_RADIUS = 1;
    public static final int MIN_PLAYER_CHUNK_RADIUS = 0;
    public static final int MAX_PLAYER_CHUNK_RADIUS = 4;

    public static final int DEFAULT_PLAYER_HEIGHT_MARGIN = 2;
    public static final int MIN_PLAYER_HEIGHT_MARGIN = 0;
    public static final int MAX_PLAYER_HEIGHT_MARGIN = 64;

    public static final int DEFAULT_SURVEY_DEPTH = 24;
    public static final int MIN_SURVEY_DEPTH = 0;
    public static final int MAX_SURVEY_DEPTH = 128;

    public static final double DEFAULT_RARITY_EXPONENT = 0.5;
    public static final double MIN_RARITY_EXPONENT = 0;
    public static final double MAX_RARITY_EXPONENT = 2;

    public static final double DEFAULT_INVENTORY_WEIGHT_MULTIPLIER = 1.1;
    public static final double MIN_INVENTORY_WEIGHT_MULTIPLIER = 0;
    public static final double MAX_INVENTORY_WEIGHT_MULTIPLIER = 10;

    public static final int DEFAULT_MAX_MILLIS_PER_TICK = 10;
    public static final int MIN_MAX_MILLIS_PER_TICK = 1;
    public static final int MAX_MAX_MILLIS_PER_TICK = 40;

    public static final String DEFAULT_SPINNING_TEXT = "Spinning...";
    public static final String DEFAULT_FINAL_TEXT = "Gone in " + SECONDS_PLACEHOLDER + "s";
    public static final String DEFAULT_DESTRUCTION_TEXT = "Gone!";

    public static final SOTLConfig DEFAULTS = new SOTLConfig(
            DEFAULT_ROUND_INTERVAL_SECONDS,
            DEFAULT_DESTRUCTION_DELAY_SECONDS,
            DEFAULT_RESTORE_DELAY_SECONDS,
            DEFAULT_PLAYER_CHUNK_RADIUS,
            DEFAULT_PLAYER_HEIGHT_MARGIN,
            DEFAULT_SURVEY_DEPTH,
            DEFAULT_RARITY_EXPONENT,
            DEFAULT_INVENTORY_WEIGHT_MULTIPLIER,
            DEFAULT_MAX_MILLIS_PER_TICK,
            DEFAULT_SPINNING_TEXT,
            DEFAULT_FINAL_TEXT,
            DEFAULT_DESTRUCTION_TEXT
    );

    public SOTLConfig {
        roundIntervalSeconds = Math.clamp(roundIntervalSeconds, MIN_ROUND_INTERVAL_SECONDS, MAX_ROUND_INTERVAL_SECONDS);
        destructionDelaySeconds = Math.clamp(destructionDelaySeconds, MIN_DESTRUCTION_DELAY_SECONDS, MAX_DESTRUCTION_DELAY_SECONDS);
        restoreDelaySeconds = Math.clamp(restoreDelaySeconds, MIN_RESTORE_DELAY_SECONDS, MAX_RESTORE_DELAY_SECONDS);
        playerChunkRadius = Math.clamp(playerChunkRadius, MIN_PLAYER_CHUNK_RADIUS, MAX_PLAYER_CHUNK_RADIUS);
        playerHeightMargin = Math.clamp(playerHeightMargin, MIN_PLAYER_HEIGHT_MARGIN, MAX_PLAYER_HEIGHT_MARGIN);
        surveyDepth = Math.clamp(surveyDepth, MIN_SURVEY_DEPTH, MAX_SURVEY_DEPTH);
        rarityExponent = clampOrDefault(rarityExponent, MIN_RARITY_EXPONENT, MAX_RARITY_EXPONENT, DEFAULT_RARITY_EXPONENT);
        inventoryWeightMultiplier = clampOrDefault(inventoryWeightMultiplier, MIN_INVENTORY_WEIGHT_MULTIPLIER,
                MAX_INVENTORY_WEIGHT_MULTIPLIER, DEFAULT_INVENTORY_WEIGHT_MULTIPLIER);
        maxMillisPerTick = Math.clamp(maxMillisPerTick, MIN_MAX_MILLIS_PER_TICK, MAX_MAX_MILLIS_PER_TICK);
        spinningText = spinningText == null ? DEFAULT_SPINNING_TEXT : spinningText;
        finalText = finalText == null ? DEFAULT_FINAL_TEXT : finalText;
        destructionText = destructionText == null ? DEFAULT_DESTRUCTION_TEXT : destructionText;
    }

    private static double clampOrDefault(double value, double min, double max, double fallback) {
        return Double.isNaN(value) ? fallback : Math.clamp(value, min, max);
    }

    public int roundIntervalTicks() {
        return roundIntervalSeconds * SOTLSettings.TICKS_PER_SECOND;
    }

    public int destructionDelayTicks() {
        return destructionDelaySeconds * SOTLSettings.TICKS_PER_SECOND;
    }

    public int restoreDelayTicks() {
        return restoreDelaySeconds * SOTLSettings.TICKS_PER_SECOND;
    }

    public long maxNanosPerTick() {
        return maxMillisPerTick * 1_000_000L;
    }
}
