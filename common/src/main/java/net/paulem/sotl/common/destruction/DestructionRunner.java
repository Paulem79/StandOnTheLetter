package net.paulem.sotl.common.destruction;

import net.paulem.sotl.common.destruction.model.DestroyedBlock;
import net.paulem.sotl.common.destruction.model.StoredEntity;
import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.common.destruction.state.PreparationState;

import java.util.function.BooleanSupplier;

/** Drives one destruction through its states: preparation, destroying, waiting, restoring. */
public final class DestructionRunner {
    private final BooleanSupplier ticker;

    private DestructionRunner(BooleanSupplier ticker) {
        this.ticker = ticker;
    }

    /** Wraps a destruction that drives itself, e.g. by dispatching work to region threads; it is polled once per tick. */
    public static DestructionRunner of(BooleanSupplier ticker) {
        return new DestructionRunner(ticker);
    }

    public static <W, K, MD extends DestroyedBlock, ME extends StoredEntity> DestructionRunner of(
            DestructionContext<W, K, MD, ME> context) {
        return new DestructionRunner(new Machine<>(context, new PreparationState<>())::tick);
    }

    /**
     * Runs the destruction for the current tick.
     *
     * @return {@code true} once the destruction lifecycle has finished
     */
    public boolean tick() {
        return ticker.getAsBoolean();
    }

    private static final class Machine<C extends DestructionContext<?, ?, ?, ?>> {
        private final C context;
        private DestructionState<C> currentState;

        private Machine(C context, DestructionState<C> initialState) {
            this.context = context;
            this.currentState = initialState;
        }

        private boolean tick() {
            long startTime = System.nanoTime();

            while (currentState != null) {
                DestructionState<C> nextState = currentState.tick(context, startTime);

                if (nextState == currentState) {
                    // Current state needs more ticks to finish its work
                    break;
                }

                currentState = nextState;
                if (currentState != null) {
                    currentState.enter(context);
                }
            }

            return currentState == null;
        }
    }
}
