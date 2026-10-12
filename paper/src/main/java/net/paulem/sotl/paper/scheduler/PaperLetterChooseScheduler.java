package net.paulem.sotl.paper.scheduler;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.paulem.sotl.common.destruction.DestructionRunner;
import net.paulem.sotl.common.scheduler.BlockSurvey;
import net.paulem.sotl.common.scheduler.LetterChooseScheduler;
import net.paulem.sotl.common.scheduler.LetterDisplay;
import net.paulem.sotl.common.scheduler.SurveyRegion;
import net.paulem.sotl.paper.destruction.PaperDestructionContext;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PaperLetterChooseScheduler extends LetterChooseScheduler<World, UUID> {

    @Override
    protected Iterable<World> worlds() {
        return Bukkit.getWorlds();
    }

    @Override
    protected UUID worldKey(World world) {
        return world.getUID();
    }

    @Override
    protected long worldSeed(World world) {
        return world.getSeed();
    }

    @Override
    protected CompletableFuture<BlockSurvey> survey(World world) {
        BlockSurvey survey = new BlockSurvey();
        SurveyRegion region = new SurveyRegion(world.getMinHeight(), world.getMaxHeight() - 1);

        for (Player player : world.getPlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            survey.addPlayer();
            region.addPlayer(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());

            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack != null && stack.getType().isBlock()) {
                    survey.addInventoryBlock(stack.getType().name());
                }
            }
        }

        Map<Material, Integer> counts = new EnumMap<>(Material.class);
        region.forEach((chunkX, chunkZ, minY, maxY) -> {
            ChunkSnapshot snapshot = world.getChunkAt(chunkX, chunkZ).getChunkSnapshot(false, false, false);
            for (int y = minY; y <= maxY; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        Material type = snapshot.getBlockType(x, y, z);
                        if (!type.isAir()) {
                            counts.merge(type, 1, Integer::sum);
                        }
                    }
                }
            }
        });
        counts.forEach((type, count) -> survey.addBlocks(type.name(), count));

        return CompletableFuture.completedFuture(survey);
    }

    @Override
    protected DestructionRunner createDestruction(World world, char letter) {
        return DestructionRunner.of(new PaperDestructionContext(world, letter));
    }

    private static final Duration TICK = Duration.ofMillis(50);

    @Override
    protected void showLetter(World world, char letter, LetterDisplay display) {
        Component subtitle = Component.text(display.subtitle());
        org.bukkit.Sound sound = org.bukkit.Sound.UI_BUTTON_CLICK;

        if (display == LetterDisplay.FINAL) {
            subtitle = subtitle.color(NamedTextColor.GOLD);
            sound = org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;
        }

        Title title = Title.title(Component.text(letter), subtitle, Title.Times.times(Duration.ZERO, TICK.multipliedBy(70), TICK.multipliedBy(10)));
        NamespacedKey soundKey = Registry.SOUNDS.getKey(sound);

        send(world, title, soundKey == null ? null : Sound.sound().type(soundKey).build());
    }

    /** Shows the title and plays the sound (if any) to every player of the world. */
    protected void send(World world, Title title, Sound sound) {
        world.showTitle(title);
        if (sound != null) {
            world.playSound(sound);
        }
    }
}
