package net.paulem.sotl.paper.destruction.state;

import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.common.destruction.state.RestoringState;
import net.paulem.sotl.paper.destruction.PaperDestructionContext;
import net.paulem.sotl.paper.destruction.model.PaperDestroyedBlock;
import net.paulem.sotl.paper.destruction.model.PaperStoredEntity;
import net.paulem.sotl.paper.utils.ContainerUtils;

import java.util.List;

public class PaperRestoringState extends RestoringState<PaperDestructionContext> {

    @Override
    public DestructionState<PaperDestructionContext> tick(PaperDestructionContext context, long startTime) {
        initIndices(context);

        List<PaperDestroyedBlock> destroyedBlocks = context.getDestroyedBlocks();
        List<PaperStoredEntity> storedEntities = context.getStoredEntities();

        while (blockIndex >= 0) {
            PaperDestroyedBlock block = destroyedBlocks.get(blockIndex--);
            block.pos().getBlock().setBlockData(block.state(), false);
            ContainerUtils.restoreContainer(block.pos().getBlock(), block.contents());

            if (System.nanoTime() - startTime >= context.getMaxNanosPerTick()) {
                return this;
            }
        }

        while (entityIndex >= 0) {
            PaperStoredEntity entity = storedEntities.get(entityIndex--);
            entity.snapshot().createEntity(entity.pos());

            if (System.nanoTime() - startTime >= context.getMaxNanosPerTick()) {
                return this;
            }
        }

        destroyedBlocks.clear();
        storedEntities.clear();
        return null;
    }
}
