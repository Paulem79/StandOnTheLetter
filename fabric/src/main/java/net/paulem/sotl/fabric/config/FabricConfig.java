package net.paulem.sotl.fabric.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.paulem.sotl.common.SOTLConfig;

public class FabricConfig extends MidnightConfig {
    private static final String ROUND = "round";
    private static final String AREA = "area";
    private static final String LETTER = "letter";
    private static final String MESSAGES = "messages";
    private static final String PERFORMANCE = "performance";

    @Entry(category = ROUND, min = SOTLConfig.MIN_ROUND_INTERVAL_SECONDS, max = SOTLConfig.MAX_ROUND_INTERVAL_SECONDS)
    public static int roundIntervalSeconds = SOTLConfig.DEFAULT_ROUND_INTERVAL_SECONDS;
    @Entry(category = ROUND, min = SOTLConfig.MIN_DESTRUCTION_DELAY_SECONDS, max = SOTLConfig.MAX_DESTRUCTION_DELAY_SECONDS)
    public static int destructionDelaySeconds = SOTLConfig.DEFAULT_DESTRUCTION_DELAY_SECONDS;
    @Entry(category = ROUND, min = SOTLConfig.MIN_RESTORE_DELAY_SECONDS, max = SOTLConfig.MAX_RESTORE_DELAY_SECONDS)
    public static int restoreDelaySeconds = SOTLConfig.DEFAULT_RESTORE_DELAY_SECONDS;

    @Entry(category = AREA, min = SOTLConfig.MIN_PLAYER_CHUNK_RADIUS, max = SOTLConfig.MAX_PLAYER_CHUNK_RADIUS)
    public static int playerChunkRadius = SOTLConfig.DEFAULT_PLAYER_CHUNK_RADIUS;
    @Entry(category = AREA, min = SOTLConfig.MIN_PLAYER_HEIGHT_MARGIN, max = SOTLConfig.MAX_PLAYER_HEIGHT_MARGIN)
    public static int playerHeightMargin = SOTLConfig.DEFAULT_PLAYER_HEIGHT_MARGIN;

    @Entry(category = LETTER, min = SOTLConfig.MIN_SURVEY_DEPTH, max = SOTLConfig.MAX_SURVEY_DEPTH)
    public static int surveyDepth = SOTLConfig.DEFAULT_SURVEY_DEPTH;
    @Entry(category = LETTER, min = SOTLConfig.MIN_RARITY_EXPONENT, max = SOTLConfig.MAX_RARITY_EXPONENT)
    public static double rarityExponent = SOTLConfig.DEFAULT_RARITY_EXPONENT;
    @Entry(category = LETTER, min = SOTLConfig.MIN_INVENTORY_WEIGHT_MULTIPLIER, max = SOTLConfig.MAX_INVENTORY_WEIGHT_MULTIPLIER)
    public static double inventoryWeightMultiplier = SOTLConfig.DEFAULT_INVENTORY_WEIGHT_MULTIPLIER;

    @Entry(category = MESSAGES)
    public static String spinningText = SOTLConfig.DEFAULT_SPINNING_TEXT;
    @Entry(category = MESSAGES)
    public static String finalText = SOTLConfig.DEFAULT_FINAL_TEXT;
    @Entry(category = MESSAGES)
    public static String destructionText = SOTLConfig.DEFAULT_DESTRUCTION_TEXT;

    @Entry(category = PERFORMANCE, min = SOTLConfig.MIN_MAX_MILLIS_PER_TICK, max = SOTLConfig.MAX_MAX_MILLIS_PER_TICK)
    public static int maxMillisPerTick = SOTLConfig.DEFAULT_MAX_MILLIS_PER_TICK;

    public static SOTLConfig toConfig() {
        return new SOTLConfig(
                roundIntervalSeconds,
                destructionDelaySeconds,
                restoreDelaySeconds,
                playerChunkRadius,
                playerHeightMargin,
                surveyDepth,
                rarityExponent,
                inventoryWeightMultiplier,
                maxMillisPerTick,
                spinningText,
                finalText,
                destructionText
        );
    }
}
