package ru.demyasha.edenreligion.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.demyasha.edenreligion.Choice;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public final class PlayerChoiceStore {

    private final JavaPlugin plugin;
    private File file;
    private YamlConfiguration config;

    public PlayerChoiceStore(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder.");
        }

        this.file = new File(plugin.getDataFolder(), plugin.getConfig().getString("storage-file", "players.yml"));
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Could not create storage file: " + file.getName());
                }
            } catch (IOException exception) {
                plugin.getLogger().log(Level.SEVERE, "Could not create storage file.", exception);
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public Optional<Choice> getChoice(UUID uuid) {
        return Choice.fromSerialized(config.getString(uuid.toString()));
    }

    public boolean hasChoice(UUID uuid) {
        return getChoice(uuid).isPresent();
    }

    public void setChoice(UUID uuid, Choice choice) {
        config.set(uuid.toString(), choice.name());
        save();
    }

    public void removeChoice(UUID uuid) {
        config.set(uuid.toString(), null);
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not save player choices.", exception);
        }
    }
}
