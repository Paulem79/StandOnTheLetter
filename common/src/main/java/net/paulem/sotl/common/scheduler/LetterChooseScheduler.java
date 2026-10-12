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
 * then destroys every block none of whose words starts with it. Call {@link #tick()} once per server tick.
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

    /** Blocks around the players of the world who take part in the round, spectators excluded. */
    protected abstract BlockSurvey survey(W world);

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
            BlockSurvey survey = survey(world);
            if (!survey.hasPlayers()) {
                continue;
            }

            K key = worldKey(world);
            Random random = randoms.computeIfAbsent(key, k -> new Random(worldSeed(world) ^ System.currentTimeMillis()));

            Map<Character, Double> weights = survey.letterWeights();
            if (weights.isEmpty()) {
                weights = AlphabetUtils.getWeights();
            }

            // Avoid drawing the same letter twice in a row, unless it is the only one possible
            Map<Character, Double> candidates = new HashMap<>(weights);
            if (candidates.size() > 1) {
                candidates.remove(lastChar.get(key));
            }

            char letter = AlphabetUtils.pickLetter(candidates, random);
            lastChar.put(key, letter);

            rolls.add(new Roll(world, letter, random, weights));
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
        private final Map<Character, Double> weights;
        private int remaining = SOTLSettings.ROLL_COUNT;
        private int tickCounter = 0;

        private Roll(W world, char letter, Random random, Map<Character, Double> weights) {
            this.world = world;
            this.letter = letter;
            this.random = random;
            this.weights = weights;
        }

        /** @return {@code true} once the spinning is over */
        private boolean tick() {
            if (tickCounter++ % SOTLSettings.ROLL_INTERVAL_TICKS != 0) {
                return false;
            }
            if (remaining <= 0) {
                return true;
            }
            showLetter(world, AlphabetUtils.pickLetter(weights, random), LetterDisplay.SPINNING);
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
