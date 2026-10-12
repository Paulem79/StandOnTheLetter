package net.paulem.sotl.common.scheduler;

import net.paulem.sotl.common.SOTLSettings;

/** What the players are told alongside a letter. */
public enum LetterDisplay {
    SPINNING("Spinning..."),
    FINAL("Gone in " + SOTLSettings.DESTRUCTION_DELAY_TICKS / SOTLSettings.TICKS_PER_SECOND + "s");

    private final String subtitle;

    LetterDisplay(String subtitle) {
        this.subtitle = subtitle;
    }

    public String subtitle() {
        return subtitle;
    }
}
