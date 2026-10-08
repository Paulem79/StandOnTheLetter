package net.paulem.sotl.fabric.destruction.state;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.fabric.destruction.FabricDestructionContext;
import net.paulem.sotl.fabric.destruction.model.FabricDestroyedBlock;
import net.paulem.sotl.fabric.utils.ContainerUtils;

import java.util.List;

public class FabricDestroyingState implements DestructionState<FabricDestructionContext> {
    private int currentY;
    private int currentChunkIndex = 0;
    private int x = 0;
    private int z = 0;
    private int minHeight;

    @Override
    public void enter(FabricDestructionContext context) {
        this.minHeight = context.getWorld().getMinY();
        this.currentY = (context.getMaxStartHeight() == Integer.MIN_VALUE)
                ? context.getWorld().getMaxY() - 1
                : context.getMaxStartHeight();
        this.currentChunkIndex = 0;
        this.x = 0;
        this.z = 0;
    }

    @Override
    public DestructionState<FabricDestructionContext> tick(FabricDestructionContext context, long startTime) {
        List<LevelChunk> targetChunks = context.getTargetChunks();

        while (currentY >= minHeight) {
            while (currentChunkIndex < targetChunks.size()) {
                LevelChunk chunk = targetChunks.get(currentChunkIndex);
                ChunkPos chunkPos = chunk.getPos();
                long key = chunkPos.pack();
                int startHeight = context.getChunkStartHeights().getOrDefault(key, context.getWorld().getMaxY() - 1);

                if (currentY <= startHeight) {
                    while (x < 16) {
                        while (z < 16) {
                            int worldX = chunkPos.getMinBlockX() + x;
                            int worldZ = chunkPos.getMinBlockZ() + z;
                            BlockPos pos = new BlockPos(worldX, currentY, worldZ);
                            BlockState state = context.getWorld().getBlockState(pos);

                            if (doDeleteBlock(state, context.getLetter())) {
                                List<ItemStack> contents = ContainerUtils.captureAndClearContainer(context.getWorld(), pos);
                                context.getDestroyedBlocks().add(new FabricDestroyedBlock(
                                        pos.immutable(),
                                        state,
                                        contents
                                ));
                                context.getWorld().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
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

        return new FabricWaitingState(20 * 10);
    }

    private boolean doDeleteBlock(BlockState state, String letter) {
        if (state.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return !path.toUpperCase().startsWith(letter);
    }
}
