package net.paulem.sotl.common.destruction;

import net.paulem.sotl.common.destruction.model.DestroyedBlock;
import net.paulem.sotl.common.destruction.model.StoredEntity;
import net.paulem.sotl.common.utils.AlphabetUtils;

import java.util.*;

/**
 * State shared by all steps of one destruction, and the platform hooks they rely on.
 *
 * @param <W>  world type
 * @param <K>  chunk type
 * @param <MD> destroyed block model
 * @param <ME> stored entity model
 */
public abstract class DestructionContext<W, K, MD extends DestroyedBlock, ME extends StoredEntity> {
    public static final long DEFAULT_BUDGET_PER_TICK = 10_000_000L; // 10 ms budget per tick by default

    private final W world;
    private final char letter;
    private final long maxNanosPerTick;

    private final Set<Long> queuedChunkKeys = new HashSet<>();
    private final List<K> targetChunks = new ArrayList<>();
    private final List<MD> destroyedBlocks = new ArrayList<>();
    private final List<ME> storedEntities = new ArrayList<>();
    private final Map<Long, Integer> chunkStartHeights = new HashMap<>();

    private int maxStartHeight = Integer.MIN_VALUE;

    protected DestructionContext(W world, char letter, long maxNanosPerTick) {
        this.world = world;
        this.letter = letter;
        this.maxNanosPerTick = maxNanosPerTick;
    }

    protected DestructionContext(W world, char letter) {
        this(world, letter, DEFAULT_BUDGET_PER_TICK);
    }

    // ---- Platform hooks ----

    /** Unique key of a chunk, used to track per-chunk data. */
    public abstract long chunkKey(K chunk);

    /** Lowest Y of the world. */
    public abstract int minY();

    /** Highest Y blocks can be destroyed from. */
    public abstract int topY();

    /** Tells the players the destruction starts. */
    public abstract void announceDestruction();

    /** Computes the area to destroy around each player of the world, spectators excluded. */
    public abstract List<PlayerArea<K>> collectPlayerAreas();

    /** Stores in {@link #getStoredEntities()} then removes every non-player entity of the chunk. */
    public abstract void storeAndRemoveEntities(K chunk);

    /** Destroys the block if it must be, and records it in {@link #getDestroyedBlocks()}. */
    public abstract void destroyBlockAt(K chunk, int x, int y, int z);

    public abstract void restoreBlock(MD block);

    public abstract void restoreEntity(ME entity);

    // ---- Shared logic ----

    /** Whether a block with this name must be destroyed: none of its words starts with the current letter. */
    public boolean shouldDestroy(String blockName) {
        return !AlphabetUtils.initials(blockName).contains(letter);
    }

    public boolean addChunk(K chunk) {
        if (queuedChunkKeys.add(chunkKey(chunk))) {
            targetChunks.add(chunk);
            return true;
        }
        return false;
    }

    public char getLetter() {
        return letter;
    }

    public long getMaxNanosPerTick() {
        return maxNanosPerTick;
    }

    public boolean isOutOfBudget(long startTime) {
        return System.nanoTime() - startTime >= maxNanosPerTick;
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

    public List<K> getTargetChunks() {
        return targetChunks;
    }

    public W getWorld() {
        return world;
    }
}
