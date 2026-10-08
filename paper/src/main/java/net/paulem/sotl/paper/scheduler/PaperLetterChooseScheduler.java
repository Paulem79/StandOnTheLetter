package net.paulem.sotl.paper.scheduler;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.paulem.sotl.common.utils.AlphabetUtils;
import net.paulem.sotl.paper.PaperSOTL;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PaperLetterChooseScheduler implements Runnable {
    private final Map<UUID, Random> randoms = new HashMap<>();
    private final Map<UUID, Character> lastChar = new HashMap<>();

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            randoms.computeIfAbsent(world.getUID(), _ -> new Random(world.getSeed() ^ System.currentTimeMillis()));

            char letter = ' ';
            char last = lastChar.getOrDefault(world.getUID(), ' ');
            while (letter == last || letter == ' ') {
                letter = AlphabetUtils.getRandomLetter(randoms.get(world.getUID()));
            }
            lastChar.put(world.getUID(), letter);

            roll(world);
        }
    }

    private void roll(World world) {
        new BukkitRunnable() {
            private int times = 15;

            @Override
            public void run() {
                if (times <= 0) {
                    cancel();
                    end(world);
                    return;
                }

                char letter = AlphabetUtils.getRandomLetter(randoms.get(world.getUID()));
                showLetter(world, letter, Component.text("Spinning..."), org.bukkit.Sound.UI_BUTTON_CLICK);

                times--;
            }
        }.runTaskTimer(PaperSOTL.getInstance(), 0L, 2L);
    }

    private void end(World world) {
        char letter = lastChar.get(world.getUID());

        showFinalLetter(world, letter);
        new PaperDestroyScheduler(world, letter).runTaskTimer(PaperSOTL.getInstance(), 20L * 10, 1L);
    }

    private void showFinalLetter(World world, char letter) {
        showLetter(world, letter, Component.text("Gone in 10s").color(NamedTextColor.GOLD), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP);
    }

    private void showLetter(World world, char letter, Component subtitle, org.bukkit.Sound sound) {
        Title title = Title.title(Component.text(letter), subtitle, 0, 70, 10);

        world.showTitle(title);

        NamespacedKey arrowHitSoundKey = Registry.SOUNDS.getKey(sound);
        if(arrowHitSoundKey != null) {
            world.playSound(Sound.sound().type(arrowHitSoundKey).build());
        }
    }
}
