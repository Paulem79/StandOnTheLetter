package net.paulem.sotl.paper.destruction.state;

import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.paper.destruction.PaperDestructionContext;
import net.paulem.sotl.paper.destruction.model.PaperDestroyedBlock;
import net.paulem.sotl.paper.destruction.model.PaperStoredEntity;
import net.paulem.sotl.paper.utils.ContainerUtils;

import java.util.List;

public class RestoringState implements DestructionState<PaperDestructionContext> {
    private int blockIndex = -1;
    private int entityIndex = -1;
    private boolean initialized = false;

    @Override
    public void enter(PaperDestructionContext context) {
        initIndices(context);
    }

    private void initIndices(PaperDestructionContext context) {
        if (!initialized) {
            this.blockIndex = context.getDestroyedBlocks().size() - 1;
            this.entityIndex = context.getStoredEntities().size() - 1;
            this.initialized = true;
        }
    }

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
