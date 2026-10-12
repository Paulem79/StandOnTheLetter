package net.paulem.sotl.common.destruction.state;

import net.paulem.sotl.common.SOTLSettings;
import net.paulem.sotl.common.destruction.DestructionContext;
import net.paulem.sotl.common.destruction.model.DestroyedBlock;
import net.paulem.sotl.common.destruction.model.StoredEntity;

import java.util.List;

/** Destroys layer by layer, from the top down, within a time budget per tick. */
public class DestroyingState<W, K, MD extends DestroyedBlock, ME extends StoredEntity>
        implements DestructionState<DestructionContext<W, K, MD, ME>> {
    private int currentY;
    private int currentChunkIndex = 0;
    private int x = 0;
    private int z = 0;
    private int minHeight;

    @Override
    public void enter(DestructionContext<W, K, MD, ME> context) {
        this.minHeight = context.minY();
        this.currentY = (context.getMaxStartHeight() == Integer.MIN_VALUE)
                ? context.topY()
                : context.getMaxStartHeight();
        this.currentChunkIndex = 0;
        this.x = 0;
        this.z = 0;
    }

    @Override
    public DestructionState<DestructionContext<W, K, MD, ME>> tick(DestructionContext<W, K, MD, ME> context, long startTime) {
        List<K> targetChunks = context.getTargetChunks();

        while (currentY >= minHeight) {
            while (currentChunkIndex < targetChunks.size()) {
                K chunk = targetChunks.get(currentChunkIndex);
                int startHeight = context.getChunkStartHeights().getOrDefault(context.chunkKey(chunk), context.topY());

                if (currentY <= startHeight) {
                    while (x < 16) {
                        while (z < 16) {
                            context.destroyBlockAt(chunk, x, currentY, z);
                            z++;

                            if (context.isOutOfBudget(startTime)) {
                                return this;
                            }
                        }
                        z = 0;
                        x++;
                    }
                    x = 0;
                }

                currentChunkIndex++;
            }
            currentChunkIndex = 0;
            currentY--;
        }

        return new WaitingState<>(SOTLSettings.RESTORE_DELAY_TICKS, RestoringState::new);
    }
}
