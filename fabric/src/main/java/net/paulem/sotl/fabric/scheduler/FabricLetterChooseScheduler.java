package net.paulem.sotl.fabric.scheduler;

import net.minecraft.core.Holder;
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
import net.paulem.sotl.common.utils.AlphabetUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FabricLetterChooseScheduler {
    private final Map<ResourceKey<Level>, Random> randoms = new HashMap<>();
    private final Map<ResourceKey<Level>, Character> lastChar = new HashMap<>();

    private final List<ActiveRoll> activeRolls = new ArrayList<>();
    private final List<DelayedDestroy> delayedDestroys = new ArrayList<>();
    private final List<FabricDestroyScheduler> activeDestroys = new ArrayList<>();

    public void run(MinecraftServer server) {
        for (ServerLevel world : server.getAllLevels()) {
            ResourceKey<Level> key = world.dimension();
            randoms.computeIfAbsent(key, _ -> new Random(world.getSeed() ^ System.currentTimeMillis()));

            char letter = ' ';
            char last = lastChar.getOrDefault(key, ' ');
            while (letter == last || letter == ' ') {
                letter = AlphabetUtils.getRandomLetter(randoms.get(key));
            }
            lastChar.put(key, letter);

            roll(world);
        }
    }

    public void tick(MinecraftServer server) {
        Iterator<ActiveRoll> rollIterator = activeRolls.iterator();
        while (rollIterator.hasNext()) {
            ActiveRoll roll = rollIterator.next();
            if (roll.tick()) {
                rollIterator.remove();
                end(roll.world);
            }
        }

        Iterator<DelayedDestroy> delayedIterator = delayedDestroys.iterator();
        while (delayedIterator.hasNext()) {
            DelayedDestroy delayed = delayedIterator.next();
            if (delayed.tick()) {
                delayedIterator.remove();
                activeDestroys.add(new FabricDestroyScheduler(delayed.world, delayed.letter));
            }
        }

        Iterator<FabricDestroyScheduler> destroyIterator = activeDestroys.iterator();
        while (destroyIterator.hasNext()) {
            FabricDestroyScheduler destroy = destroyIterator.next();
            if (destroy.tick()) {
                destroyIterator.remove();
            }
        }
    }

    private void roll(ServerLevel world) {
        activeRolls.add(new ActiveRoll(world));
    }

    private void end(ServerLevel world) {
        char letter = lastChar.get(world.dimension());
        showFinalLetter(world, letter);
        delayedDestroys.add(new DelayedDestroy(world, letter, 20 * 10));
    }

    private void showFinalLetter(ServerLevel world, char letter) {
        showLetter(world, letter, Component.literal("Gone in 10s").withColor(0xFFAA00), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_LEVELUP));
    }

    private void showLetter(ServerLevel world, char letter, Component subtitle, Holder<SoundEvent> soundHolder) {
        for (ServerPlayer player : world.players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 70, 10));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(String.valueOf(letter))));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.connection.send(new ClientboundSoundPacket(
                    soundHolder,
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

    private class ActiveRoll {
        private final ServerLevel world;
        private int times = 15;
        private int tickCounter = 0;

        public ActiveRoll(ServerLevel world) {
            this.world = world;
        }

        public boolean tick() {
            if (tickCounter % 2 == 0) {
                if (times <= 0) {
                    return true;
                }
                char letter = AlphabetUtils.getRandomLetter(randoms.get(world.dimension()));
                showLetter(world, letter, Component.literal("Spinning..."), SoundEvents.UI_BUTTON_CLICK);
                times--;
            }
            tickCounter++;
            return false;
        }
    }

    private static class DelayedDestroy {
        private final ServerLevel world;
        private final char letter;
        private int remainingTicks;

        public DelayedDestroy(ServerLevel world, char letter, int delayTicks) {
            this.world = world;
            this.letter = letter;
            this.remainingTicks = delayTicks;
        }

        public boolean tick() {
            remainingTicks--;
            return remainingTicks <= 0;
        }
    }
}
