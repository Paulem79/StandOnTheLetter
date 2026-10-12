package net.paulem.sotl.common.destruction.state;

import net.paulem.sotl.common.destruction.DestructionContext;
import net.paulem.sotl.common.destruction.PlayerArea;
import net.paulem.sotl.common.destruction.model.DestroyedBlock;
import net.paulem.sotl.common.destruction.model.StoredEntity;

public class PreparationState<W, K, MD extends DestroyedBlock, ME extends StoredEntity>
        implements DestructionState<DestructionContext<W, K, MD, ME>> {

    @Override
    public DestructionState<DestructionContext<W, K, MD, ME>> tick(DestructionContext<W, K, MD, ME> context, long startTime) {
        context.announceDestruction();

        int maxStartHeight = Integer.MIN_VALUE;

        for (PlayerArea<K> area : context.collectPlayerAreas()) {
            maxStartHeight = Math.max(maxStartHeight, area.startHeight());

            for (K chunk : area.chunks()) {
                context.getChunkStartHeights().merge(context.chunkKey(chunk), area.startHeight(), Math::max);
                context.addChunk(chunk);
            }
        }

        if (context.getTargetChunks().isEmpty()) {
            return null;
        }

        context.setMaxStartHeight(maxStartHeight);

        for (K chunk : context.getTargetChunks()) {
            context.storeAndRemoveEntities(chunk);
        }

        return new DestroyingState<>();
    }
}
