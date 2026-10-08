package net.paulem.sotl.common.destruction;

import java.util.HashMap;
import java.util.Map;

public abstract class DestructionContext {
    private final String letter;
    private final long maxNanosPerTick;

    private final Map<Long, Integer> chunkStartHeights = new HashMap<>();

    private int maxStartHeight = Integer.MIN_VALUE;

    protected DestructionContext(String letter, long maxNanosPerTick) {
        this.letter = letter;
        this.maxNanosPerTick = maxNanosPerTick;
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
}
