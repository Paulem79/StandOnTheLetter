package net.paulem.sotl.paper.folia;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.paulem.sotl.common.SOTLSettings;
import net.paulem.sotl.common.destruction.DestructionContext;
import net.paulem.sotl.common.utils.AlphabetUtils;
import net.paulem.sotl.common.utils.ChunkUtils;
import net.paulem.sotl.paper.destruction.model.PaperDestroyedBlock;
import net.paulem.sotl.paper.destruction.model.PaperStoredEntity;
import net.paulem.sotl.paper.utils.ContainerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * One destruction on Folia. Chunks belong to different region threads, so each chunk is destroyed then restored by
 * its own job on the region scheduler, and {@link #tick()} (called by the global region) only moves on to the next
 * phase once every job of the current one is done.
 */
final class FoliaDestruction {
    private enum Phase {PREPARE, DESTROY, WAIT, RESTORE}

    private final Plugin plugin;
    private final World world;
    private final char letter;

    private final Map<Long, ChunkJob> jobs = new ConcurrentHashMap<>();
    private final Map<Material, Boolean> destroyable = new ConcurrentHashMap<>();
    /** Tasks of the current phase still running. */
    private final AtomicInteger pending = new AtomicInteger();

    private Phase phase = Phase.PREPARE;
    private boolean prepareStarted = false;
    private int waitTicks = SOTLSettings.RESTORE_DELAY_TICKS;

    FoliaDestruction(Plugin plugin, World world, char letter) {
        this.plugin = plugin;
        this.world = world;
        this.letter = letter;
    }

    /** @return {@code true} once the destruction lifecycle has finished */
    boolean tick() {
        if (pending.get() > 0) {
            return false;
        }

        switch (phase) {
            case PREPARE -> {
                if (!prepareStarted) {
                    prepareStarted = true;
                    return !prepare();
                }
                if (jobs.isEmpty()) {
                    return true;
                }
                phase = Phase.DESTROY;
                dispatch(ChunkJob::destroy);
            }
            case DESTROY -> phase = Phase.WAIT;
            case WAIT -> {
                if (--waitTicks <= 0) {
                    phase = Phase.RESTORE;
                    dispatch(ChunkJob::restore);
                }
            }
            case RESTORE -> {
                return true;
            }
        }
        return false;
    }

    /** @return {@code false} when there is nobody to destroy blocks around */
    private boolean prepare() {
        List<Player> players = world.getPlayers();
        if (players.isEmpty()) {
            return false;
        }

        Title title = Title.title(Component.text("Gone!"), Component.empty());

        pending.set(players.size());
        for (Player player : players) {
            player.getScheduler().run(plugin, task -> {
                try {
                    player.showTitle(title);
                    if (player.getGameMode() == GameMode.SPECTATOR) {
                        return;
                    }

                    Location location = player.getLocation();
                    int startHeight = Math.min(world.getMaxHeight() - 1, location.getBlockY() + SOTLSettings.PLAYER_HEIGHT_MARGIN);
                    int centerX = location.getBlockX() >> 4;
                    int centerZ = location.getBlockZ() >> 4;
                    int radius = SOTLSettings.PLAYER_CHUNK_RADIUS;

                    for (int chunkX = centerX - radius; chunkX <= centerX + radius; chunkX++) {
                        for (int chunkZ = centerZ - radius; chunkZ <= centerZ + radius; chunkZ++) {
                            int x = chunkX;
                            int z = chunkZ;
                            jobs.computeIfAbsent(ChunkUtils.chunkKey(x, z), key -> new ChunkJob(x, z))
                                    .raiseStartHeight(startHeight);
                        }
                    }
                } finally {
                    pending.decrementAndGet();
                }
            }, pending::decrementAndGet);
        }
        return true;
    }

    private void dispatch(Consumer<ChunkJob> action) {
        pending.set(jobs.size());
        jobs.values().forEach(action);
    }

    private boolean shouldDestroy(Material type) {
        return destroyable.computeIfAbsent(type, t -> !AlphabetUtils.initials(t.name()).contains(letter));
    }

    private final class ChunkJob {
        private final int chunkX;
        private final int chunkZ;

        private int startHeight = Integer.MIN_VALUE;

        // Only touched by the region thread owning the chunk, one task at a time
        private final List<PaperDestroyedBlock> destroyedBlocks = new ArrayList<>();
        private final List<PaperStoredEntity> storedEntities = new ArrayList<>();
        private boolean entitiesStored = false;
        private int currentY;
        private int x = 0;
        private int z = 0;
        private int blockIndex;
        private int entityIndex;

        private ChunkJob(int chunkX, int chunkZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        private synchronized void raiseStartHeight(int height) {
            startHeight = Math.max(startHeight, height);
        }

        private synchronized int startHeight() {
            return startHeight;
        }

        private void destroy() {
            currentY = startHeight();
            run(this::destroyStep);
        }

        private void restore() {
            blockIndex = destroyedBlocks.size() - 1;
            entityIndex = storedEntities.size() - 1;
            run(this::restoreStep);
        }

        private void run(Runnable step) {
            Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, task -> step.run());
        }

        private void runNextTick(Runnable step) {
            Bukkit.getRegionScheduler().runDelayed(plugin, world, chunkX, chunkZ, task -> step.run(), 1L);
        }

        private boolean isOutOfBudget(long start) {
            return System.nanoTime() - start >= DestructionContext.DEFAULT_BUDGET_PER_TICK;
        }

        private void destroyStep() {
            long start = System.nanoTime();
            try {
                // A chunk which is not loaded is not ours to touch
                if (world.isChunkLoaded(chunkX, chunkZ)) {
                    Chunk chunk = world.getChunkAt(chunkX, chunkZ);

                    if (!entitiesStored) {
                        entitiesStored = true;
                        for (Entity entity : chunk.getEntities()) {
                            if (entity instanceof Player) {
                                continue;
                            }
                            storedEntities.add(new PaperStoredEntity(entity.createSnapshot(), entity.getLocation().clone()));
                            entity.remove();
                        }
                    }

                    int minHeight = world.getMinHeight();
                    while (currentY >= minHeight) {
                        while (x < 16) {
                            while (z < 16) {
                                destroyBlockAt(chunk, x, currentY, z);
                                z++;

                                if (isOutOfBudget(start)) {
                                    runNextTick(this::destroyStep);
                                    return;
                                }
                            }
                            z = 0;
                            x++;
                        }
                        x = 0;
                        currentY--;
                    }
                }
            } catch (RuntimeException e) {
                plugin.getLogger().warning("Could not destroy chunk " + chunkX + ", " + chunkZ + ": " + e);
            }
            pending.decrementAndGet();
        }

        private void destroyBlockAt(Chunk chunk, int blockX, int blockY, int blockZ) {
            Block block = chunk.getBlock(blockX, blockY, blockZ);
            Material type = block.getType();

            if (type.isAir() || !shouldDestroy(type)) {
                return;
            }

            ItemStack[] contents = ContainerUtils.captureAndClearContainer(block);
            destroyedBlocks.add(new PaperDestroyedBlock(block.getLocation(), block.getBlockData().clone(), contents));
            block.setType(Material.AIR, false);
        }

        private void restoreStep() {
            long start = System.nanoTime();
            try {
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    // Load it back first, then carry on from the region which will own it
                    world.getChunkAtAsync(chunkX, chunkZ).thenRun(() -> run(this::restoreStep));
                    return;
                }

                while (blockIndex >= 0) {
                    PaperDestroyedBlock destroyed = destroyedBlocks.get(blockIndex--);
                    Block target = destroyed.pos().getBlock();
                    target.setBlockData(destroyed.state(), false);
                    ContainerUtils.restoreContainer(target, destroyed.contents());

                    if (isOutOfBudget(start)) {
                        runNextTick(this::restoreStep);
                        return;
                    }
                }

                while (entityIndex >= 0) {
                    PaperStoredEntity stored = storedEntities.get(entityIndex--);
                    stored.snapshot().createEntity(stored.pos());

                    if (isOutOfBudget(start)) {
                        runNextTick(this::restoreStep);
                        return;
                    }
                }

                destroyedBlocks.clear();
                storedEntities.clear();
            } catch (RuntimeException e) {
                plugin.getLogger().warning("Could not restore chunk " + chunkX + ", " + chunkZ + ": " + e);
            }
            pending.decrementAndGet();
        }
    }
}
