package net.paulem.sotl.common.scheduler;

import net.paulem.sotl.common.SOTLConfig;
import net.paulem.sotl.common.SOTLSettings;

/** What the players are told alongside a letter. */
public enum LetterDisplay {
    SPINNING,
    FINAL;

    public String subtitle() {
        SOTLConfig config = SOTLSettings.config();
        return switch (this) {
            case SPINNING -> config.spinningText();
            case FINAL -> config.finalText()
                    .replace(SOTLConfig.SECONDS_PLACEHOLDER, String.valueOf(config.destructionDelaySeconds()));
        };
    }
}
