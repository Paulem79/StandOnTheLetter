package net.paulem.sotl.paper.utils;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class ChunkUtils {
    private ChunkUtils() {
        /* This utility class should not be instantiated */
    }

    public static List<Chunk> getChunksAroundPlayer(Player player, int radius) {
        List<Chunk> chunks = new ArrayList<>();
        Chunk centerChunk = player.getLocation().getChunk();
        World world = centerChunk.getWorld();

        int centerX = centerChunk.getX();
        int centerZ = centerChunk.getZ();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Chunk chunk = world.getChunkAt(centerX + x, centerZ + z);
                chunks.add(chunk);
            }
        }
        return chunks;
    }
}
