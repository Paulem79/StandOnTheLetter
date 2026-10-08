package net.paulem.sotl.paper;

import net.paulem.sotl.common.SOTL;
import net.paulem.sotl.common.logger.LoggerWrapper;
import net.paulem.sotl.common.utils.AlphabetUtils;
import net.paulem.sotl.paper.logger.PaperLoggerWrapper;
import net.paulem.sotl.paper.scheduler.LetterChooseScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class PaperSOTL extends JavaPlugin implements SOTL<PaperSOTL> {
    private static PaperSOTL instance;
    private LoggerWrapper logger;

    @Override
    public void onEnable() {
        instance = this;
        logger = new PaperLoggerWrapper(getLogger());

        logger().info("SOTL enabled!");

        initLetterWeights();

        Bukkit.getScheduler().runTaskTimer(this, new LetterChooseScheduler(), 0L, 20L * 60);

        super.onEnable();
    }

    private void initLetterWeights() {
        AlphabetUtils.computeAndSetWeights(
                Arrays.stream(Material.values())
                        .filter(m -> m.isBlock() && !m.isAir() && !m.isLegacy())
                        .toList(),
                Material::name,
                AlphabetUtils.DEFAULT_MIN_BLOCK_COUNT
        );
    }

    @Override
    public void onDisable() {
        logger().info("SOTL disabled!");

        super.onDisable();
    }

    @Override
    public PaperSOTL get() {
        return instance;
    }

    @Override
    public LoggerWrapper logger() {
        return logger;
    }

    public static PaperSOTL getInstance() {
        return instance;
    }
}