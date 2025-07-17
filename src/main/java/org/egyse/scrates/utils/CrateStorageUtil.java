package org.egyse.scrates.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.egyse.scrates.SCrates;
import org.egyse.scrates.models.Crate;
import org.egyse.scrates.models.CrateLocation;
import org.egyse.scrates.models.CratePrize;
import org.egyse.scrates.models.Hologram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CrateStorageUtil {
    private final SCrates pl = SCrates.getInstance();
    public final ConcurrentHashMap<String, Crate> crates = new ConcurrentHashMap<>();

    public CrateStorageUtil() {
        loadData();
    }

    public Crate getCrate(String name) {
        return crates.getOrDefault(name, null);
    }

    public Crate getCrate(CrateLocation location) {
        for (Crate crate : crates.values()) {
            for (CrateLocation loc : crate.getLocations()) {
                if (loc.equals(location)) return crate;
            }
        }
        return null;
    }

    public Crate getCrate(Location l) {
        CrateLocation location = new CrateLocation(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
        return getCrate(location);
    }

    public void setCrate(Crate crate) {
        if (!crates.containsKey(crate.getId())) {
            // Create holograms for new crates
            for (CrateLocation loc : crate.getLocations()) {
                pl.hologramUtil.createHologram(loc, crate);
            }
        }
        crates.put(crate.getId(), crate);
    }

    public void loadData() {
        crates.clear();
        FileConfiguration cratesConfig = pl.getCratesConfig();
        ConfigurationSection section = cratesConfig.getConfigurationSection("crates");
        if (section != null) {
            for (String i : section.getKeys(false)) {
                ConfigurationSection current = section.getConfigurationSection(i);

                double maxChance = 0;
                List<CratePrize> prizes = new ArrayList<>();
                ConfigurationSection prizeSection = current.getConfigurationSection("prizes");
                if (prizeSection != null) {
                    for (String p : prizeSection.getKeys(false)) {
                        ConfigurationSection currentPrize = prizeSection.getConfigurationSection(p);

                        ItemStack itemStack = new ItemStack(Material.matchMaterial(currentPrize.getString("material")), currentPrize.getInt("amount"));
                        ItemMeta meta = itemStack.getItemMeta();
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', currentPrize.getString("displayname")));

                        List<String> lore = new ArrayList<>();
                        for (String line : currentPrize.getStringList("lore")) { lore.add(ChatColor.translateAlternateColorCodes('&', line)); }
                        meta.setLore(lore);
                        itemStack.setItemMeta(meta);

                        CratePrize prize = new CratePrize(
                                itemStack,
                                currentPrize.getDouble("chance"),
                                currentPrize.getStringList("commands")
                        );

                        if (prize.getChance() > maxChance) maxChance = prize.getChance();
                        prizes.add(prize);
                    }
                }

                Hologram hologram = new Hologram(
                        current.getBoolean("hologram.enabled"),
                        current.getDouble("hologram.offset"),
                        current.getStringList("hologram.text")
                );

                List<CrateLocation> locations = new ArrayList<>();
                for (String str : current.getStringList("locations")) {
                    String[] s = str.split(";");
                    locations.add(new CrateLocation(
                            s[0], Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3]))
                    );
                }

                Crate crate = new Crate(
                        i,
                        current.getString("displayname"),
                        current.getBoolean("preview"),
                        hologram,
                        locations,
                        current.getInt("rows"),
                        prizes,
                        maxChance
                );

                crates.put(i, crate);
                System.out.println(i + " crate has been loaded!");
            }
        }
    }

    public void saveData(boolean async) {
        ConcurrentHashMap<String, Crate> cratesSnapshot = new ConcurrentHashMap<>(crates);
        Runnable saveTask = () -> {
            FileConfiguration cratesConfig = pl.getCratesConfig();
            cratesConfig.set("crates", null);
            ConfigurationSection cratesSection = cratesConfig.createSection("crates");

            for (HashMap.Entry<String, Crate> entry : cratesSnapshot.entrySet()) {
                String crateId = entry.getKey();
                Crate crate = entry.getValue();
                ConfigurationSection crateSection = cratesSection.createSection(crateId);

                crateSection.set("displayname", crate.getName());
                crateSection.set("rows", crate.getRows());
                crateSection.set("preview", crate.isPreview());

                Hologram hologram = crate.getHologram();
                crateSection.set("hologram.enabled", hologram.isEnabled());
                crateSection.set("hologram.offset", hologram.getOffset());

                List<String> holoText = new ArrayList<>();
                for (String l : hologram.getText()) holoText.add(l.replace("§", "&"));
                crateSection.set("hologram.text", holoText);

                List<String> locationStrings = new ArrayList<>();
                for (CrateLocation loc : crate.getLocations()) {
                    locationStrings.add(loc.getWorld() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ());
                }
                crateSection.set("locations", locationStrings);

                ConfigurationSection prizesSection = crateSection.createSection("prizes");
                List<CratePrize> prizes = crate.getPrizes();
                for (int i = 0; i < prizes.size(); i++) {
                    CratePrize prize = prizes.get(i);
                    ConfigurationSection prizeSection = prizesSection.createSection(String.valueOf(i));
                    ItemStack item = prize.getDisplayItem();

                    // Save item properties
                    prizeSection.set("material", item.getType().name());
                    prizeSection.set("amount", item.getAmount());

                    // Convert display name and lore (replace § with &)
                    if (item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta.hasDisplayName()) {
                            prizeSection.set("displayname", meta.getDisplayName().replace('§', '&'));
                        }
                        if (meta.hasLore()) {
                            List<String> convertedLore = new ArrayList<>();
                            if (meta.getLore() != null && !meta.getLore().isEmpty()) {
                                for (String line : meta.getLore()) {
                                    convertedLore.add(line.replace('§', '&'));
                                }
                            }
                            prizeSection.set("lore", convertedLore);
                        }
                    }

                    // Save chance and commands
                    prizeSection.set("chance", prize.getChance());
                    prizeSection.set("commands", prize.getCommands());
                }
            }

            // Save to disk
            pl.saveCratesConfig();
        };

        if (async && !pl.isDisabling()) {
            Bukkit.getScheduler().runTaskAsynchronously(pl, saveTask);
        } else {
            saveTask.run(); // Sync execution
        }
    }
}