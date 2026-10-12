package net.paulem.sotl.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.paulem.sotl.common.SOTL;
import net.paulem.sotl.common.wrapper.LoggerWrapper;
import net.paulem.sotl.common.utils.AlphabetUtils;
import net.paulem.sotl.fabric.logger.FabricLoggerWrapper;
import net.paulem.sotl.fabric.scheduler.FabricLetterChooseScheduler;
import org.slf4j.LoggerFactory;

import java.util.stream.StreamSupport;

public class FabricSOTL implements ModInitializer, SOTL<FabricSOTL> {
    public static final String MOD_ID = "sotl";

    private static FabricSOTL instance;
    private final LoggerWrapper loggerWrapper = new FabricLoggerWrapper(LoggerFactory.getLogger(MOD_ID));
    private final FabricLetterChooseScheduler letterChooseScheduler = new FabricLetterChooseScheduler();

    @Override
    public void onInitialize() {
        instance = this;
        logger().info("Initializing SOTL");

        initLetterWeights();

        ServerTickEvents.END_SERVER_TICK.register(letterChooseScheduler::tick);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            logger().info("SOTL disabled!");
        });
    }

    private void initLetterWeights() {
        AlphabetUtils.computeAndSetWeights(
                StreamSupport.stream(BuiltInRegistries.BLOCK.spliterator(), false)
                        .filter(block -> block != Blocks.AIR)
                        .map(BuiltInRegistries.BLOCK::getKey)
                        .map(Identifier::getPath)
                        .toList(),
                path -> path,
                AlphabetUtils.DEFAULT_MIN_BLOCK_COUNT
        );
    }

    @Override
    public FabricSOTL get() {
        return this;
    }

    @Override
    public LoggerWrapper logger() {
        return loggerWrapper;
    }

    public static FabricSOTL getInstance() {
        return instance;
    }
}
