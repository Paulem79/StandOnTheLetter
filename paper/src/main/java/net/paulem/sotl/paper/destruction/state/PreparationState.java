package net.paulem.sotl.paper.destruction.state;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.paper.destruction.PaperDestructionContext;
import net.paulem.sotl.paper.destruction.model.PaperStoredEntity;
import net.paulem.sotl.paper.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class PreparationState implements DestructionState<PaperDestructionContext> {

    @Override
    public DestructionState<PaperDestructionContext> tick(PaperDestructionContext context, long startTime) {
        World world = context.getWorld();
        world.showTitle(Title.title(Component.text("Gone!"), Component.empty()));

        int maxStartHeight = Integer.MIN_VALUE;

        for (Player player : world.getPlayers()) {
            int startHeight = Math.min(world.getMaxHeight() - 1, player.getLocation().getBlockY() + 2);
            if (startHeight > maxStartHeight) {
                maxStartHeight = startHeight;
            }

            List<Chunk> chunksAroundPlayer = ChunkUtils.getChunksAroundPlayer(player, 1);
            for (Chunk chunk : chunksAroundPlayer) {
                long key = Chunk.getChunkKey(chunk.getX(), chunk.getZ());
                context.getChunkStartHeights().merge(key, startHeight, Math::max);
                context.addChunk(chunk);
            }
        }

        if (context.getTargetChunks().isEmpty()) {
            return null;
        }

        context.setMaxStartHeight(maxStartHeight);

        for (Chunk chunk : context.getTargetChunks()) {
            storeAndRemoveEntities(chunk, context);
        }

        return new DestroyingState();
    }

    private void storeAndRemoveEntities(Chunk chunk, PaperDestructionContext context) {
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Player) {
                continue;
            }
            context.getStoredEntities().add(new PaperStoredEntity(entity.createSnapshot(), entity.getLocation().clone()));
            entity.remove();
        }
    }
}
