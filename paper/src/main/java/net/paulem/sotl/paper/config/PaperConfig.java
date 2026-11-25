package net.paulem.sotl.paper.config;

import net.paulem.sotl.common.SOTLConfig;
import net.paulem.sotl.common.SOTLSettings;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class PaperConfig {
    private final Plugin plugin;

    public PaperConfig(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Reads the file, creating it first if it is missing, and applies it.
     */
    public boolean load() {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            plugin.saveDefaultConfig();
        }

        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not read " + file, e);
            return false;
        }

        SOTLSettings.apply(new SOTLConfig(
                yaml.getInt("round.interval-seconds", SOTLConfig.DEFAULT_ROUND_INTERVAL_SECONDS),
                yaml.getInt("round.destruction-delay-seconds", SOTLConfig.DEFAULT_DESTRUCTION_DELAY_SECONDS),
                yaml.getInt("round.restore-delay-seconds", SOTLConfig.DEFAULT_RESTORE_DELAY_SECONDS),
                yaml.getInt("area.player-chunk-radius", SOTLConfig.DEFAULT_PLAYER_CHUNK_RADIUS),
                yaml.getInt("area.player-height-margin", SOTLConfig.DEFAULT_PLAYER_HEIGHT_MARGIN),
                yaml.getInt("letter.survey-depth", SOTLConfig.DEFAULT_SURVEY_DEPTH),
                yaml.getDouble("letter.rarity-exponent", SOTLConfig.DEFAULT_RARITY_EXPONENT),
                yaml.getDouble("letter.inventory-weight-multiplier", SOTLConfig.DEFAULT_INVENTORY_WEIGHT_MULTIPLIER),
                yaml.getInt("performance.max-millis-per-tick", SOTLConfig.DEFAULT_MAX_MILLIS_PER_TICK),
                yaml.getString("messages.spinning", SOTLConfig.DEFAULT_SPINNING_TEXT),
                yaml.getString("messages.final", SOTLConfig.DEFAULT_FINAL_TEXT),
                yaml.getString("messages.destruction", SOTLConfig.DEFAULT_DESTRUCTION_TEXT)
        ));
        return true;
    }
}
