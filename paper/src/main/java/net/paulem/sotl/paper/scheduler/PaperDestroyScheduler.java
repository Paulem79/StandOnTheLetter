package net.paulem.sotl.paper.scheduler;

import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.paper.destruction.PaperDestructionContext;
import net.paulem.sotl.paper.destruction.state.PaperPreparationState;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public final class PaperDestroyScheduler extends BukkitRunnable {
    private final PaperDestructionContext context;
    private DestructionState<PaperDestructionContext> currentState;

    public PaperDestroyScheduler(World world, char letter) {
        this.context = new PaperDestructionContext(world, letter);
        this.currentState = new PaperPreparationState();
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();

        while (currentState != null) {
            DestructionState nextState = currentState.tick(context, startTime);

            if (nextState != currentState) {
                currentState = nextState;
                if (currentState != null) {
                    currentState.enter(context);
                }
            } else {
                // Current state needs more ticks to finish its work
                break;
            }
        }

        if (currentState == null) {
            cancel();
        }
    }
}