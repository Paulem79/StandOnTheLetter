package net.paulem.sotl.paper.destruction.state;

import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.paper.destruction.PaperDestructionContext;

public class WaitingState implements DestructionState<PaperDestructionContext> {
    private int remainingTicks;

    public WaitingState(int ticks) {
        this.remainingTicks = ticks;
    }

    @Override
    public DestructionState<PaperDestructionContext> tick(PaperDestructionContext context, long startTime) {
        remainingTicks--;
        if (remainingTicks <= 0) {
            return new RestoringState();
        }
        return this;
    }
}
