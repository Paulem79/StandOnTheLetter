package net.paulem.sotl.fabric.destruction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.paulem.sotl.common.SOTLSettings;
import net.paulem.sotl.common.destruction.DestructionContext;
import net.paulem.sotl.common.destruction.PlayerArea;
import net.paulem.sotl.common.utils.ChunkUtils;
import net.paulem.sotl.fabric.destruction.model.FabricDestroyedBlock;
import net.paulem.sotl.fabric.destruction.model.FabricStoredEntity;
import net.paulem.sotl.fabric.utils.ContainerUtils;

import java.util.List;

public class FabricDestructionContext extends DestructionContext<ServerLevel, LevelChunk, FabricDestroyedBlock, FabricStoredEntity> {
    public FabricDestructionContext(ServerLevel world, char letter) {
        super(world, letter);
    }

    @Override
    public long chunkKey(LevelChunk chunk) {
        return chunk.getPos().pack();
    }

    @Override
    public int minY() {
        return getWorld().getMinY();
    }

    @Override
    public int topY() {
        return getWorld().getMaxY() - 1;
    }

    @Override
    public void announceDestruction() {
        for (ServerPlayer player : getWorld().players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 70, 10));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Gone!")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.empty()));
        }
    }

    @Override
    public List<PlayerArea<LevelChunk>> collectPlayerAreas() {
        return getWorld().players().stream()
                .map(player -> new PlayerArea<>(
                        Math.min(topY(), player.getBlockY() + SOTLSettings.PLAYER_HEIGHT_MARGIN),
                        chunksAround(player)
                ))
                .toList();
    }

    private List<LevelChunk> chunksAround(ServerPlayer player) {
        ChunkPos center = player.chunkPosition();
        return ChunkUtils.getChunksAround(center.x(), center.z(), SOTLSettings.PLAYER_CHUNK_RADIUS,
                (x, z) -> getWorld().getChunk(x, z));
    }

    @Override
    public void storeAndRemoveEntities(LevelChunk chunk) {
        ServerLevel level = getWorld();
        ChunkPos pos = chunk.getPos();
        AABB aabb = new AABB(pos.getMinBlockX(), level.getMinY(), pos.getMinBlockZ(), pos.getMaxBlockX() + 1, level.getMaxY(), pos.getMaxBlockZ() + 1);

        List<Entity> entities = level.getEntities((Entity) null, aabb, entity -> !(entity instanceof Player));
        for (Entity entity : entities) {
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            entity.save(output);
            getStoredEntities().add(new FabricStoredEntity(output.buildResult(), entity.position()));
            entity.discard();
        }
    }

    @Override
    public void destroyBlockAt(LevelChunk chunk, int x, int y, int z) {
        ChunkPos chunkPos = chunk.getPos();
        BlockPos pos = new BlockPos(chunkPos.getMinBlockX() + x, y, chunkPos.getMinBlockZ() + z);
        BlockState state = getWorld().getBlockState(pos);

        if (state.isAir() || !shouldDestroy(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath())) {
            return;
        }

        List<ItemStack> contents = ContainerUtils.captureAndClearContainer(getWorld(), pos);
        getDestroyedBlocks().add(new FabricDestroyedBlock(pos.immutable(), state, contents));
        getWorld().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    }

    @Override
    public void restoreBlock(FabricDestroyedBlock block) {
        getWorld().setBlock(block.pos(), block.state(), Block.UPDATE_ALL);
        ContainerUtils.restoreContainer(getWorld(), block.pos(), block.contents());
    }

    @Override
    public void restoreEntity(FabricStoredEntity stored) {
        if (stored.snapshot() == null) {
            return;
        }

        ServerLevel world = getWorld();
        ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), stored.snapshot());
        Entity loaded = EntityType.loadEntityRecursive(input, world, EntitySpawnReason.LOAD, entity -> {
            entity.setPos(stored.pos().x, stored.pos().y, stored.pos().z);
            return entity;
        });
        if (loaded != null) {
            world.addFreshEntity(loaded);
        }
    }
}
