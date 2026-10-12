package net.paulem.sotl.common.scheduler;

import net.paulem.sotl.common.SOTLSettings;
import net.paulem.sotl.common.utils.ChunkUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/** Chunks, and the heights within them, scanned around the players to pick the letter. */
public final class SurveyRegion {
    @FunctionalInterface
    public interface ChunkSlice {
        void accept(int chunkX, int chunkZ, int minY, int maxY);
    }

    // Chunk key -> {chunkX, chunkZ, minY, maxY}
    private final Map<Long, int[]> slices = new LinkedHashMap<>();
    private final int worldMinY;
    private final int worldTopY;

    public SurveyRegion(int worldMinY, int worldTopY) {
        this.worldMinY = worldMinY;
        this.worldTopY = worldTopY;
    }

    public void addPlayer(int blockX, int blockY, int blockZ) {
        int minY = Math.max(worldMinY, blockY - SOTLSettings.SURVEY_DEPTH);
        int maxY = Math.min(worldTopY, blockY + SOTLSettings.PLAYER_HEIGHT_MARGIN);
        if (minY > maxY) {
            return;
        }

        int centerX = blockX >> 4;
        int centerZ = blockZ >> 4;
        int radius = SOTLSettings.PLAYER_CHUNK_RADIUS;

        for (int chunkX = centerX - radius; chunkX <= centerX + radius; chunkX++) {
            for (int chunkZ = centerZ - radius; chunkZ <= centerZ + radius; chunkZ++) {
                int[] slice = slices.get(ChunkUtils.chunkKey(chunkX, chunkZ));
                if (slice == null) {
                    slices.put(ChunkUtils.chunkKey(chunkX, chunkZ), new int[]{chunkX, chunkZ, minY, maxY});
                } else {
                    slice[2] = Math.min(slice[2], minY);
                    slice[3] = Math.max(slice[3], maxY);
                }
            }
        }
    }

    public void forEach(ChunkSlice consumer) {
        for (int[] slice : slices.values()) {
            consumer.accept(slice[0], slice[1], slice[2], slice[3]);
        }
    }
}
