package net.paulem.sotl.common.scheduler;

import net.paulem.sotl.common.SOTLSettings;
import net.paulem.sotl.common.destruction.DestructionRunner;
import net.paulem.sotl.common.utils.AlphabetUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Picks a letter for each world every {@link SOTLSettings#LETTER_INTERVAL_TICKS}, spins it, announces it
 * then destroys everything not starting with it. Call {@link #tick()} once per server tick.
 *
 * @param <W> world type
 * @param <K> key identifying a world
 */
public abstract class LetterChooseScheduler<W, K> implements Runnable {
    private final Map<K, Random> randoms = new HashMap<>();
    private final Map<K, Character> lastChar = new HashMap<>();

    private final List<Roll> rolls = new ArrayList<>();
    private final List<PendingDestruction> pendingDestructions = new ArrayList<>();
    private final List<DestructionRunner> destructions = new ArrayList<>();

    private int ticks = 0;

    protected abstract Iterable<W> worlds();

    protected abstract K worldKey(W world);

    protected abstract long worldSeed(W world);

    protected abstract void showLetter(W world, char letter, LetterDisplay display);

    protected abstract DestructionRunner createDestruction(W world, char letter);

    @Override
    public void run() {
        tick();
    }

    public void tick() {
        ticks++;
        if (ticks % SOTLSettings.LETTER_INTERVAL_TICKS == 0) {
            startRound();
        }

        rolls.removeIf(roll -> {
            if (!roll.tick()) {
                return false;
            }
            end(roll.world, roll.letter);
            return true;
        });

        pendingDestructions.removeIf(pending -> {
            if (--pending.remainingTicks > 0) {
                return false;
            }
            destructions.add(createDestruction(pending.world, pending.letter));
            return true;
        });

        destructions.removeIf(DestructionRunner::tick);
    }

    private void startRound() {
        for (W world : worlds()) {
            K key = worldKey(world);
            Random random = randoms.computeIfAbsent(key, _ -> new Random(worldSeed(world) ^ System.currentTimeMillis()));

            char last = lastChar.getOrDefault(key, ' ');
            char letter = ' ';
            while (letter == last || letter == ' ') {
                letter = AlphabetUtils.getRandomLetter(random);
            }
            lastChar.put(key, letter);

            rolls.add(new Roll(world, letter, random));
        }
    }

    private void end(W world, char letter) {
        showLetter(world, letter, LetterDisplay.FINAL);
        pendingDestructions.add(new PendingDestruction(world, letter, SOTLSettings.DESTRUCTION_DELAY_TICKS));
    }

    private final class Roll {
        private final W world;
        private final char letter;
        private final Random random;
        private int remaining = SOTLSettings.ROLL_COUNT;
        private int tickCounter = 0;

        private Roll(W world, char letter, Random random) {
            this.world = world;
            this.letter = letter;
            this.random = random;
        }

        /** @return {@code true} once the spinning is over */
        private boolean tick() {
            if (tickCounter++ % SOTLSettings.ROLL_INTERVAL_TICKS != 0) {
                return false;
            }
            if (remaining <= 0) {
                return true;
            }
            showLetter(world, AlphabetUtils.getRandomLetter(random), LetterDisplay.SPINNING);
            remaining--;
            return false;
        }
    }

    private final class PendingDestruction {
        private final W world;
        private final char letter;
        private int remainingTicks;

        private PendingDestruction(W world, char letter, int remainingTicks) {
            this.world = world;
            this.letter = letter;
            this.remainingTicks = remainingTicks;
        }
    }
}
