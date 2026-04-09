package ru.demyasha.edenreligion;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.demyasha.edenreligion.command.ReligionCommand;
import ru.demyasha.edenreligion.data.PlayerChoiceStore;
import ru.demyasha.edenreligion.gui.SelectionGui;
import ru.demyasha.edenreligion.listener.SelectionListener;
import ru.demyasha.edenreligion.placeholder.EdenReligionExpansion;

public final class EdenReligionPlugin extends JavaPlugin {

    private PlayerChoiceStore store;
    private SelectionGui selectionGui;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.store = new PlayerChoiceStore(this);
        this.selectionGui = new SelectionGui(this, store);

        ReligionCommand religionCommand = new ReligionCommand(this, store, selectionGui);
        PluginCommand command = getCommand("religion");
        if (command == null) {
            throw new IllegalStateException("Command 'religion' is not defined in plugin.yml");
        }
        command.setExecutor(religionCommand);
        command.setTabCompleter(religionCommand);

        Bukkit.getPluginManager().registerEvents(new SelectionListener(this, store, selectionGui), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new EdenReligionExpansion(this, store).register();
            getLogger().info("Registered PlaceholderAPI expansion.");
        } else {
            getLogger().info("PlaceholderAPI not found, placeholders disabled.");
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        store.reload();
    }

    public PlayerChoiceStore getStore() {
        return store;
    }

    public SelectionGui getSelectionGui() {
        return selectionGui;
    }
}
