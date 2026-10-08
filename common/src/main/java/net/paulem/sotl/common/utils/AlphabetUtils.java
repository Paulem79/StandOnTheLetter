package net.paulem.sotl.common.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public final class AlphabetUtils {
    private AlphabetUtils() {
    }

    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int DEFAULT_MIN_BLOCK_COUNT = 10;

    private static final Map<Character, Integer> LETTER_WEIGHTS = new HashMap<>();
    private static int totalWeight = 0;

    public static void setWeights(Map<Character, Integer> weights) {
        LETTER_WEIGHTS.clear();
        LETTER_WEIGHTS.putAll(weights);
        totalWeight = LETTER_WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
    }

    public static <T> void computeAndSetWeights(Collection<T> elements, Function<T, String> nameExtractor, int minCount) {
        Map<Character, Integer> rawCounts = new HashMap<>();
        for (T element : elements) {
            String name = nameExtractor.apply(element);
            if (name != null && !name.isEmpty()) {
                char firstChar = Character.toUpperCase(name.charAt(0));
                if (firstChar >= 'A' && firstChar <= 'Z') {
                    rawCounts.put(firstChar, rawCounts.getOrDefault(firstChar, 0) + 1);
                }
            }
        }

        Map<Character, Integer> weights = new HashMap<>();
        for (Map.Entry<Character, Integer> entry : rawCounts.entrySet()) {
            int count = entry.getValue();
            if (count >= minCount) {
                // Square the count so letters with many blocks are strongly favored
                // and letters with very few blocks are eliminated or extremely rare
                weights.put(entry.getKey(), count * count);
            }
        }

        setWeights(weights);
    }

    public static Map<Character, Integer> getWeights() {
        return LETTER_WEIGHTS;
    }

    public static char getRandomLetter() {
        return getRandomLetter(ThreadLocalRandom.current());
    }

    public static char getRandomLetter(Random random) {
        if (totalWeight <= 0) {
            return ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }

        int roll = random.nextInt(totalWeight);
        int current = 0;

        for (Map.Entry<Character, Integer> entry : LETTER_WEIGHTS.entrySet()) {
            current += entry.getValue();
            if (roll < current) {
                return entry.getKey();
            }
        }

        return ALPHABET.charAt(random.nextInt(ALPHABET.length()));
    }
}