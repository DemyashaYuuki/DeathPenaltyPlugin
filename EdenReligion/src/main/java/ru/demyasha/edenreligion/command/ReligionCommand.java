package ru.demyasha.edenreligion.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.demyasha.edenreligion.Choice;
import ru.demyasha.edenreligion.EdenReligionPlugin;
import ru.demyasha.edenreligion.data.PlayerChoiceStore;
import ru.demyasha.edenreligion.gui.SelectionGui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class ReligionCommand implements CommandExecutor, TabCompleter {

    private final EdenReligionPlugin plugin;
    private final PlayerChoiceStore store;
    private final SelectionGui selectionGui;

    public ReligionCommand(EdenReligionPlugin plugin, PlayerChoiceStore store, SelectionGui selectionGui) {
        this.plugin = plugin;
        this.store = store;
        this.selectionGui = selectionGui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return handleStatus(sender);
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        return switch (subcommand) {
            case "choose" -> handleChoose(sender);
            case "reload" -> handleReload(sender);
            default -> handleStatus(sender);
        };
    }

    private boolean handleStatus(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.player-only", "&cЭту команду может использовать только игрок.")));
            return true;
        }

        Optional<Choice> choice = store.getChoice(player.getUniqueId());
        String message = plugin.getConfig().getString("messages.status", "&7Твой текущий выбор: &f%choice%")
                .replace("%choice%", choice.map(Choice::getDisplayName).orElse("Не выбран"));
        player.sendMessage(color(message));
        return true;
    }

    private boolean handleChoose(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.player-only", "&cЭту команду может использовать только игрок.")));
            return true;
        }

        Optional<Choice> current = store.getChoice(player.getUniqueId());
        if (current.isPresent() && current.get() != Choice.NONE) {
            player.sendMessage(color(plugin.getConfig().getString("messages.already-chosen", "&cТы уже сделал необратимый выбор и не можешь сменить сторону.")));
            return true;
        }

        selectionGui.open(player);
        player.sendMessage(color(plugin.getConfig().getString("messages.choose-available", "&eОткрыл меню выбора стороны.")));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("edenreligion.admin")) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.no-permission", "&cУ тебя нет прав.")));
            return true;
        }

        plugin.reloadPlugin();
        sender.sendMessage(color(plugin.getConfig().getString("messages.reloaded", "&aEdenReligion перезагружен.")));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) {
            return List.of();
        }

        List<String> variants = new ArrayList<>(List.of("choose"));
        if (sender.hasPermission("edenreligion.admin")) {
            variants.add("reload");
        }

        String current = args[0].toLowerCase(Locale.ROOT);
        return variants.stream().filter(value -> value.startsWith(current)).toList();
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
