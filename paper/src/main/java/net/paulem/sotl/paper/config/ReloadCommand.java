package net.paulem.sotl.paper.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class ReloadCommand implements CommandExecutor {
    private final PaperConfig config;

    public ReloadCommand(PaperConfig config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            return false;
        }

        if (config.load()) {
            sender.sendMessage(Component.text("StandOnTheLetter configuration reloaded!", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Invalid config.yml", NamedTextColor.RED));
        }
        return true;
    }
}
