package ru.demyasha.deathpenalty;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class DeathPenaltyPlugin extends JavaPlugin implements Listener {

    private double keepPercent;
    private double durabilityDamagePercent;
    private boolean sendMessage;
    private String deathMessageTemplate;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("DeathPenaltyPlugin enabled.");
    }

    private void loadSettings() {
        FileConfiguration config = getConfig();
        this.keepPercent = clamp(config.getDouble("keep-percent", 0.50D), 0.0D, 1.0D);
        this.durabilityDamagePercent = clamp(config.getDouble("durability-damage-percent", 0.10D), 0.0D, 1.0D);
        this.sendMessage = config.getBoolean("send-message", true);
        this.deathMessageTemplate = config.getString(
                "messages.death-summary",
                "&cПри смерти выпало &e%dropped% &cпредмет(ов), сохранилось &a%kept%&c, а &6%broken% &cсломалось от прочности."
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        List<ItemStack> originalDrops = event.getDrops();
        if (originalDrops.isEmpty()) {
            return;
        }

        // Принудительно используем механику плагина даже если на сервере включен keepInventory.
        event.setKeepInventory(false);

        List<ItemStack> processedStacks = new ArrayList<>();
        int brokenByDurability = 0;

        for (ItemStack original : originalDrops) {
            if (original == null || original.getType().isAir() || original.getAmount() <= 0) {
                continue;
            }

            ItemStack item = original.clone();

            if (hasDurability(item) && applyDurabilityDamage(item, durabilityDamagePercent)) {
                brokenByDurability += item.getAmount();
                continue;
            }

            processedStacks.add(item);
        }

        event.getDrops().clear();
        event.getItemsToKeep().clear();

        int totalUnits = countTotalUnits(processedStacks);
        if (totalUnits <= 0) {
            sendSummary(event, 0, 0, brokenByDurability);
            return;
        }

        int keepUnits = calculateKeepUnits(totalUnits, keepPercent);
        int[] keepAmounts = distributeKeptUnits(processedStacks, keepUnits);

        int totalKept = 0;
        int totalDropped = 0;

        for (int i = 0; i < processedStacks.size(); i++) {
            ItemStack stack = processedStacks.get(i);
            int keepAmount = keepAmounts[i];
            int dropAmount = stack.getAmount() - keepAmount;

            if (keepAmount > 0) {
                ItemStack keptStack = stack.clone();
                keptStack.setAmount(keepAmount);
                event.getItemsToKeep().add(keptStack);
                totalKept += keepAmount;
            }

            if (dropAmount > 0) {
                ItemStack droppedStack = stack.clone();
                droppedStack.setAmount(dropAmount);
                event.getDrops().add(droppedStack);
                totalDropped += dropAmount;
            }
        }

        sendSummary(event, totalDropped, totalKept, brokenByDurability);
    }

    private void sendSummary(PlayerDeathEvent event, int dropped, int kept, int broken) {
        if (!sendMessage) {
            return;
        }

        String message = deathMessageTemplate
                .replace("%dropped%", String.valueOf(dropped))
                .replace("%kept%", String.valueOf(kept))
                .replace("%broken%", String.valueOf(broken));

        event.getPlayer().sendMessage(colorize(message));
    }

    private int countTotalUnits(List<ItemStack> items) {
        int total = 0;
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir() && item.getAmount() > 0) {
                total += item.getAmount();
            }
        }
        return total;
    }

    private int calculateKeepUnits(int totalUnits, double keepFraction) {
        if (totalUnits <= 0 || keepFraction <= 0.0D) {
            return 0;
        }
        if (keepFraction >= 1.0D) {
            return totalUnits;
        }

        int dropUnits = (int) Math.floor(totalUnits * (1.0D - keepFraction));
        dropUnits = Math.max(0, Math.min(dropUnits, totalUnits));
        return totalUnits - dropUnits;
    }

    private int[] distributeKeptUnits(List<ItemStack> items, int keepUnits) {
        int[] keepAmounts = new int[items.size()];
        if (keepUnits <= 0 || items.isEmpty()) {
            return keepAmounts;
        }

        List<Integer> unitIndexes = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            for (int amount = 0; amount < item.getAmount(); amount++) {
                unitIndexes.add(i);
            }
        }

        Collections.shuffle(unitIndexes, ThreadLocalRandom.current());

        int safeKeepUnits = Math.min(keepUnits, unitIndexes.size());
        for (int i = 0; i < safeKeepUnits; i++) {
            int itemIndex = unitIndexes.get(i);
            keepAmounts[itemIndex]++;
        }

        return keepAmounts;
    }

    private boolean hasDurability(ItemStack item) {
        return item.getType().getMaxDurability() > 0;
    }

    /**
     * @return true, если предмет сломался полностью и должен исчезнуть
     */
    private boolean applyDurabilityDamage(ItemStack item, double percentOfMaxDurability) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return false;
        }

        int maxDurability = item.getType().getMaxDurability();
        if (maxDurability <= 0) {
            return false;
        }

        int extraDamage = Math.max(1, (int) Math.ceil(maxDurability * percentOfMaxDurability));
        int newDamage = damageable.getDamage() + extraDamage;

        if (newDamage >= maxDurability) {
            return true;
        }

        damageable.setDamage(newDamage);
        item.setItemMeta(damageable);
        return false;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
