package net.paulem.sotl.common.destruction.state;

import net.paulem.sotl.common.destruction.DestructionContext;

import java.util.function.Supplier;

public class WaitingState<C extends DestructionContext<?, ?, ?, ?>> implements DestructionState<C> {
    private final Supplier<DestructionState<C>> next;
    private int remainingTicks;

    public WaitingState(int ticks, Supplier<DestructionState<C>> next) {
        this.remainingTicks = ticks;
        this.next = next;
    }

    @Override
    public DestructionState<C> tick(C context, long startTime) {
        remainingTicks--;
        if (remainingTicks <= 0) {
            return next.get();
        }
        return this;
    }
}
