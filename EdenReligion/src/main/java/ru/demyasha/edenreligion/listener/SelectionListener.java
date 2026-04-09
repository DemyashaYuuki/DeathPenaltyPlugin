package ru.demyasha.edenreligion.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.demyasha.edenreligion.Choice;
import ru.demyasha.edenreligion.EdenReligionPlugin;
import ru.demyasha.edenreligion.data.PlayerChoiceStore;
import ru.demyasha.edenreligion.gui.SelectionGui;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SelectionListener implements Listener {

    private final EdenReligionPlugin plugin;
    private final PlayerChoiceStore store;
    private final SelectionGui selectionGui;
    private final Set<UUID> quittingPlayers = ConcurrentHashMap.newKeySet();

    public SelectionListener(EdenReligionPlugin plugin, PlayerChoiceStore store, SelectionGui selectionGui) {
        this.plugin = plugin;
        this.store = store;
        this.selectionGui = selectionGui;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (store.hasChoice(player.getUniqueId())) {
            return;
        }

        long delay = plugin.getConfig().getLong("open-on-first-join-delay-ticks", 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && !store.hasChoice(player.getUniqueId())) {
                selectionGui.open(player);
            }
        }, delay);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!selectionGui.isSelectionInventory(player)) {
            return;
        }

        event.setCancelled(true);

        Choice choice = selectionGui.choiceBySlot(event.getRawSlot());
        if (choice != null) {
            selectionGui.applyChoice(player, choice);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!event.getView().getTitle().equals(selectionGui.getTitle())) {
            return;
        }
        if (quittingPlayers.contains(player.getUniqueId())) {
            return;
        }
        if (store.hasChoice(player.getUniqueId())) {
            return;
        }

        long delay = plugin.getConfig().getLong("reopen-after-close-delay-ticks", 2L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && !store.hasChoice(player.getUniqueId())) {
                selectionGui.open(player);
            }
        }, delay);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        quittingPlayers.add(uuid);
        Bukkit.getScheduler().runTaskLater(plugin, () -> quittingPlayers.remove(uuid), 20L);
    }
}
