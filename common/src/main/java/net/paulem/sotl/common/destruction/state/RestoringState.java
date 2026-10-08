package net.paulem.sotl.common.destruction.state;

import net.paulem.sotl.common.destruction.DestructionContext;

public abstract class RestoringState<C extends DestructionContext<?, ?, ?, ?>> implements DestructionState<C> {
    protected int blockIndex = -1;
    protected int entityIndex = -1;
    protected boolean initialized = false;

    @Override
    public void enter(C context) {
        initIndices(context);
    }

    protected void initIndices(C context) {
        if (!initialized) {
            this.blockIndex = context.getDestroyedBlocks().size() - 1;
            this.entityIndex = context.getStoredEntities().size() - 1;
            this.initialized = true;
        }
    }
}
