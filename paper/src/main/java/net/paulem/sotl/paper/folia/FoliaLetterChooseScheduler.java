package net.paulem.sotl.paper.folia;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import net.paulem.sotl.common.destruction.DestructionRunner;
import net.paulem.sotl.common.scheduler.BlockSurvey;
import net.paulem.sotl.common.scheduler.SurveyRegion;
import net.paulem.sotl.paper.scheduler.PaperLetterChooseScheduler;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Folia flavour of {@link PaperLetterChooseScheduler}: it is ticked by the global region, and everything that touches
 * a player or a chunk is handed over to the entity scheduler of the player or the region scheduler of the chunk.
 */
public class FoliaLetterChooseScheduler extends PaperLetterChooseScheduler {
    private static final long SURVEY_TIMEOUT_SECONDS = 10;

    private final Plugin plugin;

    public FoliaLetterChooseScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected CompletableFuture<BlockSurvey> survey(World world) {
        // A chunk or a player which never gets to run its task must not block the rounds forever
        CompletableFuture<BlockSurvey> result = new CompletableFuture<BlockSurvey>()
                .orTimeout(SURVEY_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        BlockSurvey survey = new BlockSurvey();
        SurveyRegion region = new SurveyRegion(world.getMinHeight(), world.getMaxHeight() - 1);

        List<Player> players = world.getPlayers();
        if (players.isEmpty()) {
            result.complete(survey);
            return result;
        }

        Countdown playersDone = new Countdown(players.size(), () -> countBlocks(world, survey, region, result));
        for (Player player : players) {
            player.getScheduler().run(plugin, task -> {
                try {
                    if (player.getGameMode() == GameMode.SPECTATOR) {
                        return;
                    }

                    Location location = player.getLocation();
                    List<String> inventoryBlocks = new ArrayList<>();
                    for (ItemStack stack : player.getInventory().getContents()) {
                        if (stack != null && stack.getType().isBlock()) {
                            inventoryBlocks.add(stack.getType().name());
                        }
                    }

                    synchronized (survey) {
                        survey.addPlayer();
                        region.addPlayer(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                        inventoryBlocks.forEach(survey::addInventoryBlock);
                    }
                } finally {
                    playersDone.run();
                }
            }, playersDone);
        }

        return result;
    }

    private void countBlocks(World world, BlockSurvey survey, SurveyRegion region, CompletableFuture<BlockSurvey> result) {
        List<int[]> slices = new ArrayList<>();
        region.forEach((chunkX, chunkZ, minY, maxY) -> slices.add(new int[]{chunkX, chunkZ, minY, maxY}));
        if (slices.isEmpty()) {
            result.complete(survey);
            return;
        }

        Map<Material, Integer> counts = new ConcurrentHashMap<>();
        Countdown slicesDone = new Countdown(slices.size(), () -> {
            counts.forEach((type, count) -> survey.addBlocks(type.name(), count));
            result.complete(survey);
        });

        for (int[] slice : slices) {
            Bukkit.getRegionScheduler().run(plugin, world, slice[0], slice[1], task -> {
                try {
                    // A chunk which is not loaded has no region to read it from
                    if (!world.isChunkLoaded(slice[0], slice[1])) {
                        return;
                    }

                    ChunkSnapshot snapshot = world.getChunkAt(slice[0], slice[1]).getChunkSnapshot(false, false, false);
                    Map<Material, Integer> local = new EnumMap<>(Material.class);
                    for (int y = slice[2]; y <= slice[3]; y++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                Material type = snapshot.getBlockType(x, y, z);
                                if (!type.isAir()) {
                                    local.merge(type, 1, Integer::sum);
                                }
                            }
                        }
                    }
                    local.forEach((type, count) -> counts.merge(type, count, Integer::sum));
                } finally {
                    slicesDone.run();
                }
            });
        }
    }

    @Override
    protected void send(World world, Title title, Sound sound) {
        for (Player player : world.getPlayers()) {
            player.getScheduler().run(plugin, task -> {
                player.showTitle(title);
                if (sound != null) {
                    player.playSound(sound);
                }
            }, null);
        }
    }

    @Override
    protected DestructionRunner createDestruction(World world, char letter) {
        return DestructionRunner.of(new FoliaDestruction(plugin, world, letter)::tick);
    }

    /** Runs the action once it has itself been run the given number of times. */
    static final class Countdown implements Runnable {
        private final AtomicInteger remaining;
        private final Runnable action;

        Countdown(int count, Runnable action) {
            this.remaining = new AtomicInteger(count);
            this.action = action;
        }

        @Override
        public void run() {
            if (remaining.decrementAndGet() == 0) {
                action.run();
            }
        }
    }
}
