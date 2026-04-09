package ru.demyasha.edenreligion.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import ru.demyasha.edenreligion.Choice;
import ru.demyasha.edenreligion.EdenReligionPlugin;
import ru.demyasha.edenreligion.data.PlayerChoiceStore;

import java.util.ArrayList;
import java.util.List;

public final class SelectionGui {

    private static final int SIZE = 27;
    private static final int SLOT_ASUNA = 10;
    private static final int SLOT_ERROR = 12;
    private static final int SLOT_NEUTRAL = 14;
    private static final int SLOT_NONE = 16;

    private final EdenReligionPlugin plugin;
    private final PlayerChoiceStore store;

    public SelectionGui(EdenReligionPlugin plugin, PlayerChoiceStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, SIZE, getTitle());

        if (plugin.getConfig().getBoolean("menu.filler-enabled", true)) {
            fillInventory(inventory);
        }

        inventory.setItem(SLOT_ASUNA, createHead(
                plugin.getConfig().getString("heads.asuna-owner", "BladeDemyasha"),
                plugin.getConfig().getString("menu.asuna-name", "&fЯ за Асуну"),
                plugin.getConfig().getStringList("menu.permanent-lore")
        ));
        inventory.setItem(SLOT_ERROR, createHead(
                plugin.getConfig().getString("heads.error-owner", "ErrorNightmare"),
                plugin.getConfig().getString("menu.error-name", "&fЯ за Error'a"),
                plugin.getConfig().getStringList("menu.permanent-lore")
        ));
        inventory.setItem(SLOT_NEUTRAL, createItem(
                Material.NETHERITE_SWORD,
                plugin.getConfig().getString("menu.neutral-name", "&fЯ против обоих"),
                plugin.getConfig().getStringList("menu.permanent-lore")
        ));
        inventory.setItem(SLOT_NONE, createItem(
                Material.BARRIER,
                plugin.getConfig().getString("menu.none-name", "&fЯ не участвую в лоре"),
                plugin.getConfig().getStringList("menu.none-lore")
        ));

        player.openInventory(inventory);
    }

    public boolean isSelectionInventory(Player player) {
        return player.getOpenInventory().getTitle().equals(getTitle());
    }

    public String getTitle() {
        return color(plugin.getConfig().getString("menu.title", "На чьей стороне ты?"));
    }

    public Choice choiceBySlot(int slot) {
        return switch (slot) {
            case SLOT_ASUNA -> Choice.ASUNA;
            case SLOT_ERROR -> Choice.ERROR;
            case SLOT_NEUTRAL -> Choice.AGAINST_BOTH;
            case SLOT_NONE -> Choice.NONE;
            default -> null;
        };
    }

    public void applyChoice(Player player, Choice choice) {
        if (choice == null || store.hasChoice(player.getUniqueId())) {
            return;
        }

        store.setChoice(player.getUniqueId(), choice);
        player.closeInventory();

        if (choice == Choice.NONE) {
            player.sendMessage(color(plugin.getConfig().getString("messages.neutral-opt-out",
                    "&7Ты отказался от участия в лоре. Позже можно использовать &f/religion choose&7.")));
            return;
        }

        String template = plugin.getConfig().getString("messages.selected", "&aТвой выбор сохранён: &f%choice%");
        player.sendMessage(color(template.replace("%choice%", choice.getDisplayName())));
    }

    private void fillInventory(Inventory inventory) {
        Material material = Material.matchMaterial(plugin.getConfig().getString("menu.filler-material", "BLACK_STAINED_GLASS_PANE"));
        if (material == null) {
            material = Material.BLACK_STAINED_GLASS_PANE;
        }

        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }

        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }
    }

    private ItemStack createHead(String ownerName, String displayName, List<String> loreLines) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta rawMeta = item.getItemMeta();
        if (!(rawMeta instanceof SkullMeta meta)) {
            return createItem(Material.PLAYER_HEAD, displayName, loreLines);
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);
        meta.setOwningPlayer(owner);
        applyMeta(meta, displayName, loreLines);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, String displayName, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            applyMeta(meta, displayName, loreLines);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void applyMeta(ItemMeta meta, String displayName, List<String> loreLines) {
        meta.setDisplayName(color(displayName));

        List<String> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(color(line));
        }
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
