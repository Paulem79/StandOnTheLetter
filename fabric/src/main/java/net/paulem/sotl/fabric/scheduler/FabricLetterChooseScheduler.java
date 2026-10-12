package net.paulem.sotl.fabric.scheduler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.paulem.sotl.common.destruction.DestructionRunner;
import net.paulem.sotl.common.scheduler.BlockSurvey;
import net.paulem.sotl.common.scheduler.LetterChooseScheduler;
import net.paulem.sotl.common.scheduler.LetterDisplay;
import net.paulem.sotl.common.scheduler.SurveyRegion;
import net.paulem.sotl.fabric.destruction.FabricDestructionContext;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FabricLetterChooseScheduler extends LetterChooseScheduler<ServerLevel, ResourceKey<Level>> {
    private MinecraftServer server;

    public void tick(MinecraftServer server) {
        this.server = server;
        tick();
    }

    @Override
    protected Iterable<ServerLevel> worlds() {
        return server.getAllLevels();
    }

    @Override
    protected ResourceKey<Level> worldKey(ServerLevel world) {
        return world.dimension();
    }

    @Override
    protected long worldSeed(ServerLevel world) {
        return world.getSeed();
    }

    @Override
    protected CompletableFuture<BlockSurvey> survey(ServerLevel world) {
        BlockSurvey survey = new BlockSurvey();
        SurveyRegion region = new SurveyRegion(world.getMinY(), world.getMaxY() - 1);

        for (ServerPlayer player : world.players()) {
            if (player.isSpectator()) {
                continue;
            }
            survey.addPlayer();
            region.addPlayer(player.getBlockX(), player.getBlockY(), player.getBlockZ());

            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (stack.getItem() instanceof BlockItem blockItem) {
                    survey.addInventoryBlock(blockName(blockItem.getBlock()));
                }
            }
        }

        Map<Block, Integer> counts = new IdentityHashMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        region.forEach((chunkX, chunkZ, minY, maxY) -> {
            LevelChunk chunk = world.getChunk(chunkX, chunkZ);
            for (int y = minY; y <= maxY; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = chunk.getBlockState(pos.set((chunkX << 4) + x, y, (chunkZ << 4) + z));
                        if (!state.isAir()) {
                            counts.merge(state.getBlock(), 1, Integer::sum);
                        }
                    }
                }
            }
        });
        counts.forEach((block, count) -> survey.addBlocks(blockName(block), count));

        return CompletableFuture.completedFuture(survey);
    }

    private static String blockName(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    @Override
    protected DestructionRunner createDestruction(ServerLevel world, char letter) {
        return DestructionRunner.of(new FabricDestructionContext(world, letter));
    }

    @Override
    protected void showLetter(ServerLevel world, char letter, LetterDisplay display) {
        Component subtitle = Component.literal(display.subtitle());
        Holder<SoundEvent> sound = SoundEvents.UI_BUTTON_CLICK;

        if (display == LetterDisplay.FINAL) {
            subtitle = Component.literal(display.subtitle()).withColor(0xFFAA00);
            sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_LEVELUP);
        }

        for (ServerPlayer player : world.players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 70, 10));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(String.valueOf(letter))));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.connection.send(new ClientboundSoundPacket(
                    sound,
                    SoundSource.MASTER,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    1.0f,
                    1.0f,
                    world.getRandom().nextLong()
            ));
        }
    }
}
