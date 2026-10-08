package net.paulem.sotl.paper.destruction;

import net.paulem.sotl.common.destruction.DestructionContext;
import net.paulem.sotl.paper.destruction.model.PaperDestroyedBlock;
import net.paulem.sotl.paper.destruction.model.PaperStoredEntity;
import org.bukkit.Chunk;
import org.bukkit.World;

public class PaperDestructionContext extends DestructionContext<World, Chunk, PaperDestroyedBlock, PaperStoredEntity> {
    public PaperDestructionContext(World world, char letter) {
        super(world, letter, DestructionContext.DEFAULT_BUDGET_PER_TICK);
    }

    public boolean addChunk(Chunk chunk) {
        long chunkKey = Chunk.getChunkKey(chunk.getX(), chunk.getZ());
        return super.addChunk(chunkKey, chunk);
    }
}
