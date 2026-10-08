package net.paulem.sotl.paper.destruction;

import net.paulem.sotl.common.destruction.DestructionContext;
import net.paulem.sotl.paper.destruction.model.PaperDestroyedBlock;
import net.paulem.sotl.paper.destruction.model.PaperStoredEntity;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.*;

public class PaperDestructionContext extends DestructionContext {
    private final World world;

    private final List<Chunk> targetChunks = new ArrayList<>();
    private final Set<Long> queuedChunkKeys = new HashSet<>();
    private final List<PaperDestroyedBlock> destroyedBlocks = new ArrayList<>();
    private final List<PaperStoredEntity> storedEntities = new ArrayList<>();

    public PaperDestructionContext(World world, char letter) {
        this(world, letter, 10_000_000L); // 10 ms budget per tick by default
    }

    public PaperDestructionContext(World world, char letter, long maxNanosPerTick) {
        this.world = world;
        super(Character.toString(letter), maxNanosPerTick);
    }

    public boolean addChunk(Chunk chunk) {
        long chunkKey = Chunk.getChunkKey(chunk.getX(), chunk.getZ());
        if (queuedChunkKeys.add(chunkKey)) {
            targetChunks.add(chunk);
            return true;
        }
        return false;
    }

    public World getWorld() {
        return world;
    }

    public List<Chunk> getTargetChunks() {
        return targetChunks;
    }

    public List<PaperDestroyedBlock> getDestroyedBlocks() {
        return destroyedBlocks;
    }

    public List<PaperStoredEntity> getStoredEntities() {
        return storedEntities;
    }
}
