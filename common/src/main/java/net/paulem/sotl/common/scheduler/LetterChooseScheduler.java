package net.paulem.sotl.common.scheduler;

import net.paulem.sotl.common.SOTLConfig;
import net.paulem.sotl.common.SOTLSettings;
import net.paulem.sotl.common.destruction.DestructionRunner;
import net.paulem.sotl.common.utils.AlphabetUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Picks a letter for each world every {@link SOTLConfig#roundIntervalSeconds()}, spins it, announces it
 * then destroys every block none of whose words starts with it. Call {@link #tick()} once per server tick.
 *
 * @param <W> world type
 * @param <K> key identifying a world
 */
public abstract class LetterChooseScheduler<W, K> implements Runnable {
    private final Map<K, Random> randoms = new HashMap<>();
    private final Map<K, Character> lastChar = new HashMap<>();

    private final List<PendingSurvey> pendingSurveys = new ArrayList<>();
    private final List<Roll> rolls = new ArrayList<>();
    private final List<PendingDestruction> pendingDestructions = new ArrayList<>();
    private final List<ActiveDestruction> destructions = new ArrayList<>();
    /**
     * Worlds with a round in progress */
    private final Set<K> busyWorlds = new HashSet<>();

    private int ticksSinceLastRound = 0;

    protected abstract Iterable<W> worlds();

    protected abstract K worldKey(W world);

    protected abstract long worldSeed(W world);

    /**
     * Blocks around the players of the world who take part in the round, spectators excluded.
     * May complete on any thread: the result is read back on the ticking one.
     */
    protected abstract CompletableFuture<BlockSurvey> survey(W world);

    protected abstract void showLetter(W world, char letter, LetterDisplay display);

    protected abstract DestructionRunner createDestruction(W world, char letter);

    @Override
    public void run() {
        tick();
    }

    public void tick() {
        if (++ticksSinceLastRound >= SOTLSettings.config().roundIntervalTicks()) {
            ticksSinceLastRound = 0;
            startRounds();
        }

        pendingSurveys.removeIf(pending -> {
            if (!pending.future.isDone()) {
                return false;
            }
            if (pending.future.isCompletedExceptionally()) {
                busyWorlds.remove(worldKey(pending.world));
            } else {
                startRound(pending.world, pending.future.join());
            }
            return true;
        });

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
            DestructionRunner runner = createDestruction(pending.world, pending.letter);
            destructions.add(new ActiveDestruction(worldKey(pending.world), runner));
            return true;
        });

        destructions.removeIf(destruction -> {
            if (!destruction.runner.tick()) {
                return false;
            }
            busyWorlds.remove(destruction.key);
            return true;
        });
    }

    private void startRounds() {
        for (W world : worlds()) {
            K key = worldKey(world);
            if (!busyWorlds.contains(key)) {
                pendingSurveys.add(new PendingSurvey(world, survey(world)));
                busyWorlds.add(key);
            }
        }
    }

    private void startRound(W world, BlockSurvey survey) {
        K key = worldKey(world);
        if (!survey.hasPlayers()) {
            busyWorlds.remove(key);
            return;
        }

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

    private void end(W world, char letter) {
        showLetter(world, letter, LetterDisplay.FINAL);
        pendingDestructions.add(new PendingDestruction(world, letter, SOTLSettings.config().destructionDelayTicks()));
    }

    private final class ActiveDestruction {
        private final K key;
        private final DestructionRunner runner;

        private ActiveDestruction(K key, DestructionRunner runner) {
            this.key = key;
            this.runner = runner;
        }
    }

    private final class PendingSurvey {
        private final W world;
        private final CompletableFuture<BlockSurvey> future;

        private PendingSurvey(W world, CompletableFuture<BlockSurvey> future) {
            this.world = world;
            this.future = future;
        }
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
