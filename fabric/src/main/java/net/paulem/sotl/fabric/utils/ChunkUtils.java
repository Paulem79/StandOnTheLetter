package net.paulem.sotl.fabric.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

public final class ChunkUtils {
    private ChunkUtils() {
    }

    public static List<LevelChunk> getChunksAroundPlayer(ServerPlayer player, int radius) {
        List<LevelChunk> chunks = new ArrayList<>();
        ServerLevel level = player.level();
        ChunkPos playerChunkPos = player.chunkPosition();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                LevelChunk chunk = level.getChunk(playerChunkPos.x() + x, playerChunkPos.z() + z);
                chunks.add(chunk);
            }
        }
        return chunks;
    }
}
