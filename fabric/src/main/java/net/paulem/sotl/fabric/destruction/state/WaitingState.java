package net.paulem.sotl.fabric.destruction.state;

import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.fabric.destruction.FabricDestructionContext;

public class WaitingState implements DestructionState<FabricDestructionContext> {
    private int remainingTicks;

    public WaitingState(int ticks) {
        this.remainingTicks = ticks;
    }

    @Override
    public DestructionState<FabricDestructionContext> tick(FabricDestructionContext context, long startTime) {
        remainingTicks--;
        if (remainingTicks <= 0) {
            return new RestoringState();
        }
        return this;
    }
}
