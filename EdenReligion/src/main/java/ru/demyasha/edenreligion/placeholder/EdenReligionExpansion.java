package ru.demyasha.edenreligion.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.demyasha.edenreligion.Choice;
import ru.demyasha.edenreligion.EdenReligionPlugin;
import ru.demyasha.edenreligion.data.PlayerChoiceStore;

public final class EdenReligionExpansion extends PlaceholderExpansion {

    private final EdenReligionPlugin plugin;
    private final PlayerChoiceStore store;

    public EdenReligionExpansion(EdenReligionPlugin plugin, PlayerChoiceStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "edenreligion";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        Choice choice = player == null ? null : store.getChoice(player.getUniqueId()).orElse(null);
        return switch (params.toLowerCase()) {
            case "symbol" -> getSymbol(choice);
            case "choice" -> choice == null ? "Не выбран" : choice.getDisplayName();
            case "raw" -> choice == null ? "NONE" : choice.name();
            default -> null;
        };
    }

    private String getSymbol(Choice choice) {
        final String key;
        if (choice == null) {
            key = "placeholders.none";
        } else {
            key = switch (choice) {
                case ASUNA -> "placeholders.asuna";
                case ERROR -> "placeholders.error";
                case AGAINST_BOTH -> "placeholders.against_both";
                case NONE -> "placeholders.none";
            };
        }
        String value = plugin.getConfig().getString(key, " ");
        return ChatColor.translateAlternateColorCodes('&', value == null ? " " : value);
    }
}
