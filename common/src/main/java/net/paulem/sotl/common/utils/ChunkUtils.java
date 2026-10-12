package net.paulem.sotl.common.utils;

import java.util.ArrayList;
import java.util.List;

public final class ChunkUtils {
    private ChunkUtils() {
    }

    @FunctionalInterface
    public interface ChunkGetter<K> {
        K get(int chunkX, int chunkZ);
    }

    /** Unique key of a chunk from its coordinates. */
    public static long chunkKey(int chunkX, int chunkZ) {
        return (chunkX & 0xFFFFFFFFL) | (chunkZ & 0xFFFFFFFFL) << 32;
    }

    /** Returns the (2 * radius + 1)² chunks centered on the given chunk coordinates. */
    public static <K> List<K> getChunksAround(int centerX, int centerZ, int radius, ChunkGetter<K> getter) {
        List<K> chunks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                chunks.add(getter.get(centerX + x, centerZ + z));
            }
        }
        return chunks;
    }
}
