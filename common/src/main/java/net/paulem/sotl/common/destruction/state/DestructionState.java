package net.paulem.sotl.common.destruction.state;

import net.paulem.sotl.common.destruction.DestructionContext;

public interface DestructionState<C extends DestructionContext<?, ?, ?, ?>> {

    /**
     * Called once when this state is entered.
     *
     * @param context the destruction context
     */
    default void enter(C context) {
    }

    /**
     * Executed each tick.
     *
     * @param context the destruction context
     * @param startTime the start time of the current tick in nanoseconds
     * @return the next DestructionState to transition to, {@code this} to stay in the current state for the next tick,
     *         or {@code null} if the destruction lifecycle has finished.
     */
    DestructionState<C> tick(C context, long startTime);
}
