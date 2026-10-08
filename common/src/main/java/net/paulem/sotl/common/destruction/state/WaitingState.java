package net.paulem.sotl.common.destruction.state;

import net.paulem.sotl.common.destruction.DestructionContext;

public abstract class WaitingState<C extends DestructionContext> implements DestructionState<C> {
    private int remainingTicks;

    protected WaitingState(int ticks) {
        this.remainingTicks = ticks;
    }

    @Override
    public DestructionState<C> tick(C context, long startTime) {
        remainingTicks--;
        if (remainingTicks <= 0) {
            return getNextState();
        }
        return this;
    }

    public abstract DestructionState<C> getNextState();
}
