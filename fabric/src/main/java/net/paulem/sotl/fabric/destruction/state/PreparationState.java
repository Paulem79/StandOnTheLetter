package net.paulem.sotl.fabric.destruction.state;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.paulem.sotl.common.destruction.state.DestructionState;
import net.paulem.sotl.fabric.destruction.FabricDestructionContext;
import net.paulem.sotl.fabric.destruction.model.FabricStoredEntity;
import net.paulem.sotl.fabric.utils.ChunkUtils;

import java.util.List;

public class PreparationState implements DestructionState<FabricDestructionContext> {

    @Override
    public DestructionState<FabricDestructionContext> tick(FabricDestructionContext context, long startTime) {
        ServerLevel world = context.getWorld();

        for (ServerPlayer player : world.players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 70, 10));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Gone!")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.empty()));
        }

        int maxStartHeight = Integer.MIN_VALUE;

        for (ServerPlayer player : world.players()) {
            int startHeight = Math.min(world.getMaxY() - 1, player.getBlockY() + 2);
            if (startHeight > maxStartHeight) {
                maxStartHeight = startHeight;
            }

            List<LevelChunk> chunksAroundPlayer = ChunkUtils.getChunksAroundPlayer(player, 1);
            for (LevelChunk chunk : chunksAroundPlayer) {
                ChunkPos chunkPos = chunk.getPos();
                long key = chunkPos.pack();
                context.getChunkStartHeights().merge(key, startHeight, Math::max);
                context.addChunk(chunk);
            }
        }

        if (context.getTargetChunks().isEmpty()) {
            return null;
        }

        context.setMaxStartHeight(maxStartHeight);

        for (LevelChunk chunk : context.getTargetChunks()) {
            storeAndRemoveEntities(chunk, context);
        }

        return new DestroyingState();
    }

    private void storeAndRemoveEntities(LevelChunk chunk, FabricDestructionContext context) {
        ServerLevel level = context.getWorld();
        ChunkPos pos = chunk.getPos();
        AABB aabb = new AABB(pos.getMinBlockX(), level.getMinY(), pos.getMinBlockZ(), pos.getMaxBlockX() + 1, level.getMaxY(), pos.getMaxBlockZ() + 1);

        List<Entity> entities = level.getEntities((Entity) null, aabb, entity -> !(entity instanceof Player));
        for (Entity entity : entities) {
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            entity.save(output);
            context.getStoredEntities().add(new FabricStoredEntity(output.buildResult(), entity.position()));
            entity.discard();
        }
    }
}
