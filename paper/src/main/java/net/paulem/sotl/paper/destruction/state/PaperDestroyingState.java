package net.paulem.sotl.paper.destruction.state;

import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.paper.destruction.PaperDestructionContext;
import net.paulem.sotl.paper.destruction.model.PaperDestroyedBlock;
import net.paulem.sotl.paper.utils.ContainerUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PaperDestroyingState implements DestructionState<PaperDestructionContext> {
    private int currentY;
    private int currentChunkIndex = 0;
    private int x = 0;
    private int z = 0;
    private int minHeight;

    @Override
    public void enter(PaperDestructionContext context) {
        this.minHeight = context.getWorld().getMinHeight();
        this.currentY = (context.getMaxStartHeight() == Integer.MIN_VALUE)
                ? context.getWorld().getMaxHeight() - 1
                : context.getMaxStartHeight();
        this.currentChunkIndex = 0;
        this.x = 0;
        this.z = 0;
    }

    @Override
    public DestructionState<PaperDestructionContext> tick(PaperDestructionContext context, long startTime) {
        List<Chunk> targetChunks = context.getTargetChunks();

        while (currentY >= minHeight) {
            while (currentChunkIndex < targetChunks.size()) {
                Chunk chunk = targetChunks.get(currentChunkIndex);
                long key = Chunk.getChunkKey(chunk.getX(), chunk.getZ());
                int startHeight = context.getChunkStartHeights().getOrDefault(key, context.getWorld().getMaxHeight() - 1);

                if (currentY <= startHeight) {
                    while (x < 16) {
                        while (z < 16) {
                            Block block = chunk.getBlock(x, currentY, z);

                            if (doDeleteBlock(block.getType(), context.getLetter())) {
                                ItemStack[] contents = ContainerUtils.captureAndClearContainer(block);
                                context.getDestroyedBlocks().add(new PaperDestroyedBlock(
                                        block.getLocation(),
                                        block.getBlockData().clone(),
                                        contents
                                ));
                                block.setType(Material.AIR, false);
                            }

                            z++;

                            if (System.nanoTime() - startTime >= context.getMaxNanosPerTick()) {
                                return this;
                            }
                        }
                        z = 0;
                        x++;
                    }
                    x = 0;
                }

                currentChunkIndex++;
            }
            currentChunkIndex = 0;
            currentY--;
        }

        // Wait 10 seconds (20 ticks/sec * 10) before restoring
        return new PaperWaitingState(20 * 10);
    }

    private boolean doDeleteBlock(Material blockType, String letter) {
        return !blockType.isAir() && !blockType.name().startsWith(letter);
    }
}
