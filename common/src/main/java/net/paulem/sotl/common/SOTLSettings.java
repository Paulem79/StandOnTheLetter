package net.paulem.sotl.common;

public final class SOTLSettings {
    private SOTLSettings() {
    }

    public static final int TICKS_PER_SECOND = 20;

    /** Number of letters shown while spinning. */
    public static final int ROLL_COUNT = 15;
    /** Ticks between two spinning letters. */
    public static final int ROLL_INTERVAL_TICKS = 2;

    // Read from region threads on Folia :nerd: and also never used volatile with nothing but folia bruh
    private static volatile SOTLConfig config = SOTLConfig.DEFAULTS;

    /** The configuration currently in use. */
    public static SOTLConfig config() {
        return config;
    }

    public static void apply(SOTLConfig newConfig) {
        config = newConfig;
    }
}
