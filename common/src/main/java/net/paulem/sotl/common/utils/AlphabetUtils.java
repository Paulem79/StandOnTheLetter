package net.paulem.sotl.common.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class AlphabetUtils {
    private AlphabetUtils() {
    }

    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int DEFAULT_MIN_BLOCK_COUNT = 10;

    private static final Pattern WORD_SEPARATOR = Pattern.compile("[_\\-\\s]+");
    private static final Map<String, Set<Character>> INITIALS_CACHE = new ConcurrentHashMap<>();

    private static final Map<Character, Double> LETTER_WEIGHTS = new HashMap<>();

    /**
     * First letter of every word of a block name, words being separated by underscores, hyphens or spaces.
     * {@code coal_ore} gives {@code [C, O]}.
     */
    public static Set<Character> initials(String name) {
        return INITIALS_CACHE.computeIfAbsent(name, key -> {
            Set<Character> initials = new LinkedHashSet<>();
            for (String word : WORD_SEPARATOR.split(key)) {
                if (word.isEmpty()) {
                    continue;
                }
                char first = Character.toUpperCase(word.charAt(0));
                if (first >= 'A' && first <= 'Z') {
                    initials.add(first);
                }
            }
            return Collections.unmodifiableSet(initials);
        });
    }

    public static void setWeights(Map<Character, Double> weights) {
        LETTER_WEIGHTS.clear();
        LETTER_WEIGHTS.putAll(weights);
    }

    public static <T> void computeAndSetWeights(Collection<T> elements, Function<T, String> nameExtractor, int minCount) {
        Map<Character, Integer> rawCounts = new HashMap<>();
        for (T element : elements) {
            String name = nameExtractor.apply(element);
            if (name != null && !name.isEmpty()) {
                for (char initial : initials(name)) {
                    rawCounts.merge(initial, 1, Integer::sum);
                }
            }
        }

        Map<Character, Double> weights = new HashMap<>();
        for (Map.Entry<Character, Integer> entry : rawCounts.entrySet()) {
            int count = entry.getValue();
            if (count >= minCount) {
                // Square the count so letters with many blocks are strongly favored
                // and letters with very few blocks are eliminated or extremely rare
                weights.put(entry.getKey(), (double) count * count);
            }
        }

        setWeights(weights);
    }

    /** Letter weights over every block of the game, used when no block was found around the players. */
    public static Map<Character, Double> getWeights() {
        return LETTER_WEIGHTS;
    }

    public static char getRandomLetter() {
        return getRandomLetter(ThreadLocalRandom.current());
    }

    public static char getRandomLetter(Random random) {
        return pickLetter(LETTER_WEIGHTS, random);
    }

    /** Picks a letter with a probability proportional to its weight, or uniformly if there is no weight. */
    public static char pickLetter(Map<Character, Double> weights, Random random) {
        double totalWeight = 0;
        for (double weight : weights.values()) {
            totalWeight += weight;
        }

        if (totalWeight > 0) {
            double roll = random.nextDouble() * totalWeight;
            double current = 0;

            for (Map.Entry<Character, Double> entry : weights.entrySet()) {
                current += entry.getValue();
                if (roll < current) {
                    return entry.getKey();
                }
            }
        }

        return ALPHABET.charAt(random.nextInt(ALPHABET.length()));
    }
}
