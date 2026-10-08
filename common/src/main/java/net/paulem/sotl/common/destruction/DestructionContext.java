package net.paulem.sotl.common.destruction;

import net.paulem.sotl.common.destruction.model.DestroyedBlock;
import net.paulem.sotl.common.destruction.model.StoredEntity;

import java.util.*;

public abstract class DestructionContext<W, C, MD extends DestroyedBlock, ME extends StoredEntity> {
    public static final long DEFAULT_BUDGET_PER_TICK = 10_000_000L; // 10 ms budget per tick by default

    private final W world;
    private final String letter;
    private final long maxNanosPerTick;

    protected final Set<Long> queuedChunkKeys = new HashSet<>();
    protected final List<C> targetChunks = new ArrayList<>();
    private final List<MD> destroyedBlocks = new ArrayList<>();
    private final List<ME> storedEntities = new ArrayList<>();
    private final Map<Long, Integer> chunkStartHeights = new HashMap<>();

    private int maxStartHeight = Integer.MIN_VALUE;

    protected DestructionContext(W world, char letter, long maxNanosPerTick) {
        this.world = world;
        this.letter = Character.toString(letter);
        this.maxNanosPerTick = maxNanosPerTick;
    }

    protected boolean addChunk(long chunkKey, C chunk) {
        if (queuedChunkKeys.add(chunkKey)) {
            targetChunks.add(chunk);
            return true;
        }
        return false;
    }

    public String getLetter() {
        return letter;
    }

    public long getMaxNanosPerTick() {
        return maxNanosPerTick;
    }

    public Map<Long, Integer> getChunkStartHeights() {
        return chunkStartHeights;
    }

    public int getMaxStartHeight() {
        return maxStartHeight;
    }

    public void setMaxStartHeight(int maxStartHeight) {
        this.maxStartHeight = maxStartHeight;
    }

    public List<MD> getDestroyedBlocks() {
        return destroyedBlocks;
    }

    public List<ME> getStoredEntities() {
        return storedEntities;
    }

    public List<C> getTargetChunks() {
        return targetChunks;
    }

    public W getWorld() {
        return world;
    }
}
