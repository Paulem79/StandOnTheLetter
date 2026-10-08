package net.paulem.sotl.fabric.scheduler;

import net.paulem.sotl.fabric.destruction.FabricDestructionContext;
import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.fabric.destruction.state.PreparationState;
import net.minecraft.server.level.ServerLevel;

public final class DestroyScheduler {
    private final FabricDestructionContext context;
    private DestructionState currentState;
    private boolean finished = false;

    public DestroyScheduler(ServerLevel world, char letter) {
        this.context = new FabricDestructionContext(world, letter);
        this.currentState = new PreparationState();
    }

    public boolean tick() {
        if (finished) {
            return true;
        }

        long startTime = System.nanoTime();

        while (currentState != null) {
            DestructionState nextState = currentState.tick(context, startTime);

            if (nextState != currentState) {
                currentState = nextState;
                if (currentState != null) {
                    currentState.enter(context);
                }
            } else {
                break;
            }
        }

        if (currentState == null) {
            finished = true;
            return true;
        }

        return false;
    }
}
