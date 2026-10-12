package net.paulem.sotl.common.scheduler;

import net.paulem.sotl.common.SOTLSettings;
import net.paulem.sotl.common.utils.AlphabetUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Blocks found around the players of a world, used to pick the next letter. */
public final class BlockSurvey {
    private final Map<String, Integer> blockCounts = new HashMap<>();
    private final Set<String> inventoryBlocks = new HashSet<>();
    private int players = 0;

    /** Records a player taking part in the round. */
    public void addPlayer() {
        players++;
    }

    public void addBlocks(String name, int count) {
        blockCounts.merge(name, count, Integer::sum);
    }

    public void addInventoryBlock(String name) {
        inventoryBlocks.add(name);
    }

    public boolean hasPlayers() {
        return players > 0;
    }

    /**
     * Weight of each letter: every block found weighs more the rarer it is, slightly more if a player carries it,
     * and its weight is shared between the initials of its words.
     */
    public Map<Character, Double> letterWeights() {
        Map<Character, Double> weights = new HashMap<>();

        for (Map.Entry<String, Integer> entry : blockCounts.entrySet()) {
            Set<Character> initials = AlphabetUtils.initials(entry.getKey());
            if (initials.isEmpty()) {
                continue;
            }

            double weight = 1.0 / Math.pow(entry.getValue(), SOTLSettings.RARITY_EXPONENT);
            if (inventoryBlocks.contains(entry.getKey())) {
                weight *= SOTLSettings.INVENTORY_WEIGHT_MULTIPLIER;
            }

            double share = weight / initials.size();
            for (char initial : initials) {
                weights.merge(initial, share, Double::sum);
            }
        }

        return weights;
    }
}
