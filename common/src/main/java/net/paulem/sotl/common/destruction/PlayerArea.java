package net.paulem.sotl.common.destruction;

import java.util.List;

/**
 * Area to destroy around one player.
 *
 * @param startHeight highest Y destroyed in these chunks
 * @param chunks      chunks around the player
 */
public record PlayerArea<K>(int startHeight, List<K> chunks) {
}
