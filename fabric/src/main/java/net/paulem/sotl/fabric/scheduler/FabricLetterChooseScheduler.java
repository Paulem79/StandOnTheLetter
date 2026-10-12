package net.paulem.sotl.fabric.scheduler;

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
import net.minecraft.world.level.Level;
import net.paulem.sotl.common.destruction.DestructionRunner;
import net.paulem.sotl.common.scheduler.LetterChooseScheduler;
import net.paulem.sotl.common.scheduler.LetterDisplay;
import net.paulem.sotl.fabric.destruction.FabricDestructionContext;

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
