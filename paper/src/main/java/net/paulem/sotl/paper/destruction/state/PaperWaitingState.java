package net.paulem.sotl.paper.destruction.state;

import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.common.destruction.state.WaitingState;
import net.paulem.sotl.paper.destruction.PaperDestructionContext;

public class PaperWaitingState extends WaitingState<PaperDestructionContext> {
    protected PaperWaitingState(int ticks) {
        super(ticks);
    }

    @Override
    public DestructionState<PaperDestructionContext> getNextState() {
        return new RestoringState();
    }
}
