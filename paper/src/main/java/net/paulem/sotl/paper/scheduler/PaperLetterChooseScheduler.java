package net.paulem.sotl.paper.scheduler;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.paulem.sotl.common.destruction.DestructionRunner;
import net.paulem.sotl.common.scheduler.LetterChooseScheduler;
import net.paulem.sotl.common.scheduler.LetterDisplay;
import net.paulem.sotl.paper.destruction.PaperDestructionContext;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;

import java.util.UUID;

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
    protected DestructionRunner createDestruction(World world, char letter) {
        return DestructionRunner.of(new PaperDestructionContext(world, letter));
    }

    @Override
    protected void showLetter(World world, char letter, LetterDisplay display) {
        Component subtitle = Component.text(display.subtitle());
        org.bukkit.Sound sound = org.bukkit.Sound.UI_BUTTON_CLICK;

        if (display == LetterDisplay.FINAL) {
            subtitle = subtitle.color(NamedTextColor.GOLD);
            sound = org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;
        }

        world.showTitle(Title.title(Component.text(letter), subtitle, 0, 70, 10));

        NamespacedKey soundKey = Registry.SOUNDS.getKey(sound);
        if (soundKey != null) {
            world.playSound(Sound.sound().type(soundKey).build());
        }
    }
}
