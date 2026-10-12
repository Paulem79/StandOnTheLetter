package net.paulem.sotl.paper.destruction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.paulem.sotl.common.SOTLSettings;
import net.paulem.sotl.common.destruction.DestructionContext;
import net.paulem.sotl.common.destruction.PlayerArea;
import net.paulem.sotl.common.utils.ChunkUtils;
import net.paulem.sotl.paper.destruction.model.PaperDestroyedBlock;
import net.paulem.sotl.paper.destruction.model.PaperStoredEntity;
import net.paulem.sotl.paper.utils.ContainerUtils;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PaperDestructionContext extends DestructionContext<World, Chunk, PaperDestroyedBlock, PaperStoredEntity> {
    public PaperDestructionContext(World world, char letter) {
        super(world, letter);
    }

    @Override
    public long chunkKey(Chunk chunk) {
        return Chunk.getChunkKey(chunk.getX(), chunk.getZ());
    }

    @Override
    public int minY() {
        return getWorld().getMinHeight();
    }

    @Override
    public int topY() {
        return getWorld().getMaxHeight() - 1;
    }

    @Override
    public void announceDestruction() {
        getWorld().showTitle(Title.title(Component.text("Gone!"), Component.empty()));
    }

    @Override
    public List<PlayerArea<Chunk>> collectPlayerAreas() {
        return getWorld().getPlayers().stream()
                .filter(player -> player.getGameMode() != GameMode.SPECTATOR)
                .map(player -> new PlayerArea<>(
                        Math.min(topY(), player.getLocation().getBlockY() + SOTLSettings.PLAYER_HEIGHT_MARGIN),
                        chunksAround(player)
                ))
                .toList();
    }

    private List<Chunk> chunksAround(Player player) {
        Chunk center = player.getLocation().getChunk();
        return ChunkUtils.getChunksAround(center.getX(), center.getZ(), SOTLSettings.PLAYER_CHUNK_RADIUS,
                (x, z) -> center.getWorld().getChunkAt(x, z));
    }

    @Override
    public void storeAndRemoveEntities(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Player) {
                continue;
            }
            getStoredEntities().add(new PaperStoredEntity(entity.createSnapshot(), entity.getLocation().clone()));
            entity.remove();
        }
    }

    @Override
    public void destroyBlockAt(Chunk chunk, int x, int y, int z) {
        Block block = chunk.getBlock(x, y, z);
        Material type = block.getType();

        if (type.isAir() || !shouldDestroy(type.name())) {
            return;
        }

        ItemStack[] contents = ContainerUtils.captureAndClearContainer(block);
        getDestroyedBlocks().add(new PaperDestroyedBlock(block.getLocation(), block.getBlockData().clone(), contents));
        block.setType(Material.AIR, false);
    }

    @Override
    public void restoreBlock(PaperDestroyedBlock block) {
        Block target = block.pos().getBlock();
        target.setBlockData(block.state(), false);
        ContainerUtils.restoreContainer(target, block.contents());
    }

    @Override
    public void restoreEntity(PaperStoredEntity entity) {
        entity.snapshot().createEntity(entity.pos());
    }
}
