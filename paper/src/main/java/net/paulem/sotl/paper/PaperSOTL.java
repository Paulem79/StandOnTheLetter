package net.paulem.sotl.paper;

import net.paulem.sotl.common.SOTL;
import net.paulem.sotl.common.wrapper.LoggerWrapper;
import net.paulem.sotl.common.utils.AlphabetUtils;
import net.paulem.sotl.paper.logger.PaperLoggerWrapper;
import net.paulem.sotl.paper.scheduler.PaperLetterChooseScheduler;
import net.paulem.sotl.paper.folia.FoliaLetterChooseScheduler;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class PaperSOTL extends JavaPlugin implements SOTL<PaperSOTL> {
    private static final int BSTATS_ID = 34663;

    private static PaperSOTL instance;
    private LoggerWrapper logger;

    @Override
    public void onEnable() {
        instance = this;
        logger = new PaperLoggerWrapper(getLogger());

        logger().info("SOTL enabled!");

        initLetterWeights();

        if (isFolia()) {
            FoliaLetterChooseScheduler scheduler = new FoliaLetterChooseScheduler(this);
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> scheduler.tick(), 1L, 1L);
        } else {
            Bukkit.getScheduler().runTaskTimer(this, new PaperLetterChooseScheduler(), 0L, 1L);
        }

        new Metrics(this, BSTATS_ID);

        super.onEnable();
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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