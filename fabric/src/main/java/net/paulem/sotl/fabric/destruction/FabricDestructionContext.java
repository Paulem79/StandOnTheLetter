package net.paulem.sotl.fabric.destruction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.paulem.sotl.common.destruction.DestructionContext;
import net.paulem.sotl.fabric.destruction.model.FabricDestroyedBlock;
import net.paulem.sotl.fabric.destruction.model.FabricStoredEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FabricDestructionContext extends DestructionContext {
    private final ServerLevel world;

    private final List<LevelChunk> targetChunks = new ArrayList<>();
    private final Set<Long> queuedChunkKeys = new HashSet<>();
    private final List<FabricDestroyedBlock> destroyedBlocks = new ArrayList<>();
    private final List<FabricStoredEntity> storedEntities = new ArrayList<>();

    public FabricDestructionContext(ServerLevel world, char letter) {
        this(world, letter, 10_000_000L);
    }

    public FabricDestructionContext(ServerLevel world, char letter, long maxNanosPerTick) {
        this.world = world;
        super(Character.toString(letter), maxNanosPerTick);
    }

    public boolean addChunk(LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();
        long chunkKey = pos.pack();
        if (queuedChunkKeys.add(chunkKey)) {
            targetChunks.add(chunk);
            return true;
        }
        return false;
    }

    public ServerLevel getWorld() {
        return world;
    }

    public List<LevelChunk> getTargetChunks() {
        return targetChunks;
    }

    public List<FabricDestroyedBlock> getDestroyedBlocks() {
        return destroyedBlocks;
    }

    public List<FabricStoredEntity> getStoredEntities() {
        return storedEntities;
    }
}
