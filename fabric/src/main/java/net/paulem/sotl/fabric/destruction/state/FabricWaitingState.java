package net.paulem.sotl.fabric.destruction.state;

import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.common.destruction.state.WaitingState;
import net.paulem.sotl.fabric.destruction.FabricDestructionContext;

public class FabricWaitingState extends WaitingState<FabricDestructionContext> {
    protected FabricWaitingState(int ticks) {
        super(ticks);
    }

    @Override
    public DestructionState<FabricDestructionContext> getNextState() {
        return new RestoringState();
    }
}
