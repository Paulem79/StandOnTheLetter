package net.paulem.sotl.fabric.destruction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.paulem.sotl.common.destruction.DestructionContext;
import net.paulem.sotl.fabric.destruction.model.FabricDestroyedBlock;
import net.paulem.sotl.fabric.destruction.model.FabricStoredEntity;

public class FabricDestructionContext extends DestructionContext<ServerLevel, LevelChunk, FabricDestroyedBlock, FabricStoredEntity> {
    public FabricDestructionContext(ServerLevel world, char letter) {
        super(world, letter, DestructionContext.DEFAULT_BUDGET_PER_TICK);
    }

    public boolean addChunk(LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();
        long chunkKey = pos.pack();
        return super.addChunk(chunkKey, chunk);
    }
}
