package org.egyse.scrates.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.egyse.scrates.SCrates;
import org.egyse.scrates.models.Crate;
import org.egyse.scrates.models.CrateHolder;
import org.egyse.scrates.models.CratePrize;
import org.egyse.scrates.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CrateUtil {
    private final SCrates pl = SCrates.getInstance();

    NamespacedKey crateInvKey = new NamespacedKey(pl, "crateInvKey");

    public void openCrate(User user, Crate crate, int amount) {
        // Update keys synchronously
        Map<String, Integer> keys = user.getKeys();
        keys.put(crate.getId(), keys.getOrDefault(crate.getId(), 0) - amount);
        user.setCratesOpened(user.getCratesOpened() + amount);
        pl.playerStorageUtil.setUser(user);

        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            // Precompute total chance once
            double totalChance = crate.getPrizes().stream()
                    .mapToDouble(CratePrize::getChance)
                    .sum();

            // Use ThreadLocalRandom for better performance
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            List<String> commands = new ArrayList<>(); // Pre-size

            for (int i = 0; i < amount; i++) {
                double randomValue = rnd.nextDouble() * totalChance;
                CratePrize selected = null;

                // Weighted selection algorithm
                for (CratePrize prize : crate.getPrizes()) {
                    if (randomValue < prize.getChance()) {
                        selected = prize;
                        break;
                    }
                    randomValue -= prize.getChance();
                }

                // Fallback to first prize if none selected
                if (selected == null && !crate.getPrizes().isEmpty()) {
                    selected = crate.getPrizes().get(0);
                }

                if (selected != null) {
                    commands.addAll(selected.getCommands());
                }
            }

            // Execute commands in batches on main thread
            Bukkit.getScheduler().runTask(pl, () -> {
                String playerName = user.getName();
                for (String command : commands) {
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            command.replace("{player}", playerName)
                    );
                }
            });
        });
    }

    private ItemStack fillItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

    public void previewCrate(Player player, Crate crate) {
        if (!crate.isPreview()) return;

        int rows = crate.getRows()+2;
        Inventory inventory = Bukkit.createInventory(new CrateHolder(player.getInventory()), rows*9, ChatColor.translateAlternateColorCodes('&', crate.getName()));

        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < rows * 9; i++) slots.add(i);
        for (int i = 0; i < 9; i++) { inventory.setItem(i, fillItem); slots.remove(Integer.valueOf(i)); }
        for (int i = rows*9-1; i > (rows-1)*9-1; i--) { inventory.setItem(i, fillItem); slots.remove(Integer.valueOf(i)); }

        int counter = 0;
        for (CratePrize prize : crate.getPrizes()) {
            if (counter >= slots.size()) break;
            ItemStack item = prize.getDisplayItem().clone();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
             List<String> lore = meta.getLore() != null ?
                        new ArrayList<>(meta.getLore()) :
                        new ArrayList<>();

                for (int i = 0; i < lore.size(); i++) {
                    lore.set(i, lore.get(i).replace("{chance}", String.valueOf(prize.getChance())));
                }

                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slots.get(counter), item);
            counter++;
        }

        player.openInventory(inventory);
    }
}
