package org.egyse.scrates.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.egyse.scrates.SCrates;
import org.egyse.scrates.models.Crate;
import org.egyse.scrates.models.CrateLocation;
import org.egyse.scrates.models.Hologram;

import java.util.*;

public class HologramUtil {
    private final SCrates plugin = SCrates.getInstance();
    private final Map<UUID, Map<CrateLocation, List<ArmorStand>>> playerHolograms = new HashMap<>();
    private boolean placeholderAPIEnabled;
    private final boolean supportsEntityHiding; // Check if server supports entity hiding API

    public HologramUtil() {
        this.placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        this.supportsEntityHiding = checkEntityHidingSupport();
        startPlayerHologramTask();
    }

    private boolean checkEntityHidingSupport() {
        try {
            // Check if the entity hiding API is available (1.17+)
            Player.class.getMethod("hideEntity", org.bukkit.plugin.Plugin.class, org.bukkit.entity.Entity.class);
            Player.class.getMethod("showEntity", org.bukkit.plugin.Plugin.class, org.bukkit.entity.Entity.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    // Initialize holograms when needed
    public void initializeForPlayer(Player player) {
        removePlayerHolograms(player);
        for (Crate crate : plugin.crateStorageUtil.crates.values()) {
            if (!crate.getHologram().isEnabled()) continue;

            for (CrateLocation loc : crate.getLocations()) {
                if (loc.getWorld().equals(player.getWorld().getName())) {
                    createPlayerHologram(player, loc, crate);
                }
            }
        }
    }

    // Added method to replace old createHologram
    public void createHologram(CrateLocation crateLoc, Crate crate) {
        // Create hologram for all online players in the same world
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals(crateLoc.getWorld())) {
                createPlayerHologram(player, crateLoc, crate);
            }
        }
    }

    public void updateHolograms(Crate crate) {
        // Update for all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            Map<CrateLocation, List<ArmorStand>> playerMap = playerHolograms.get(player.getUniqueId());
            if (playerMap == null) continue;

            for (CrateLocation loc : crate.getLocations()) {
                List<ArmorStand> stands = playerMap.get(loc);
                if (stands != null) {
                    updateHologramText(player, stands, crate.getHologram().getText());
                }
            }
        }
    }

    public void reloadWorldHolograms(String worldName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals(worldName)) {
                initializeForPlayer(player);
            }
        }
    }

    public void reloadAllHolograms() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            initializeForPlayer(player);
        }
    }

    private void createPlayerHologram(Player player, CrateLocation crateLoc, Crate crate) {
        Location loc = crateLoc.toBukkitLocation();
        if (loc.getWorld() == null) return;

        Map<CrateLocation, List<ArmorStand>> playerMap = playerHolograms
                .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        // Remove existing hologram if present
        if (playerMap.containsKey(crateLoc)) {
            playerMap.get(crateLoc).forEach(ArmorStand::remove);
            playerMap.remove(crateLoc);
        }

        Hologram hologram = crate.getHologram();
        double yOffset = hologram.getOffset() + (hologram.getText().size() * 0.25);
        Location spawnLoc = loc.clone().add(0.5, yOffset, 0.5);

        List<ArmorStand> stands = new ArrayList<>();
        for (String line : hologram.getText()) {
            ArmorStand as = spawnLoc.getWorld().spawn(spawnLoc, ArmorStand.class);
            configureArmorStand(as);
            as.setCustomName(processLine(line, player));
            as.setCustomNameVisible(!line.isEmpty());
            stands.add(as);

            // Hide this armor stand from other players
            hideHologramFromOtherPlayers(player, as);

            spawnLoc.subtract(0, 0.25, 0);
        }

        playerMap.put(crateLoc, stands);
    }

    private void hideHologramFromOtherPlayers(Player owner, ArmorStand hologram) {
        if (supportsEntityHiding) {
            // Hide from all players except the owner
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(owner)) {
                    online.hideEntity(plugin, hologram);
                } else {
                    online.showEntity(plugin, hologram);
                }
            }
        } else {
            // Fallback for older versions - use metadata to mark as player-specific
            hologram.setMetadata("hologram-owner", new org.bukkit.metadata.FixedMetadataValue(plugin, owner.getUniqueId().toString()));
        }
    }

    private void updateHologramText(Player player, List<ArmorStand> stands, List<String> lines) {
        for (int i = 0; i < stands.size(); i++) {
            String text = i < lines.size() ? lines.get(i) : "";
            stands.get(i).setCustomName(processLine(text, player));
            stands.get(i).setCustomNameVisible(!text.isEmpty());
        }
    }

    private void configureArmorStand(ArmorStand as) {
        as.setGravity(false);
        as.setVisible(false);
        as.setMarker(true);
        as.setInvulnerable(true);
        as.setCollidable(false);
        as.setCanPickupItems(false);
        as.setSilent(true);
        as.setAI(false);
        as.setBasePlate(false);
    }

    private void startPlayerHologramTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updatePlayerHolograms();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void updatePlayerHolograms() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Map<CrateLocation, List<ArmorStand>> playerMap = playerHolograms
                    .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

            String playerWorld = player.getWorld().getName();
            int viewDistance = Bukkit.getViewDistance() * 16;
            double maxDistanceSq = viewDistance * viewDistance;

            // Update existing holograms
            Iterator<Map.Entry<CrateLocation, List<ArmorStand>>> iter = playerMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<CrateLocation, List<ArmorStand>> entry = iter.next();
                CrateLocation loc = entry.getKey();
                Crate crate = plugin.crateStorageUtil.getCrate(loc);

                // Remove invalid holograms
                if (crate == null ||
                        !crate.getHologram().isEnabled() ||
                        !loc.getWorld().equals(playerWorld)) {
                    entry.getValue().forEach(ArmorStand::remove);
                    iter.remove();
                    continue;
                }

                // Check distance
                double distanceSq = player.getLocation().distanceSquared(loc.toBukkitLocation());
                if (distanceSq > maxDistanceSq) {
                    entry.getValue().forEach(ArmorStand::remove);
                    iter.remove();
                } else {
                    // Update text
                    updateHologramText(player, entry.getValue(), crate.getHologram().getText());
                }
            }

            // Create new holograms
            for (Crate crate : plugin.crateStorageUtil.crates.values()) {
                if (!crate.getHologram().isEnabled()) continue;

                for (CrateLocation loc : crate.getLocations()) {
                    if (!loc.getWorld().equals(playerWorld) ||
                            playerMap.containsKey(loc)) {
                        continue;
                    }

                    // Check distance
                    double distanceSq = player.getLocation().distanceSquared(loc.toBukkitLocation());
                    if (distanceSq <= maxDistanceSq) {
                        createPlayerHologram(player, loc, crate);
                    }
                }
            }
        }
    }

    private String processLine(String line, Player player) {
        if (placeholderAPIEnabled) {
            try {
                return PlaceholderAPI.setPlaceholders(player, line);
            } catch (Exception e) {
                placeholderAPIEnabled = false;
                return line;
            }
        }
        return line;
    }

    public void removeHolograms(Crate crate) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Map<CrateLocation, List<ArmorStand>> playerMap = playerHolograms.get(player.getUniqueId());
            if (playerMap == null) continue;

            for (CrateLocation loc : crate.getLocations()) {
                List<ArmorStand> stands = playerMap.remove(loc);
                if (stands != null) stands.forEach(ArmorStand::remove);
            }
        }
    }

    public void removeHologram(CrateLocation location) {
        for (Map<CrateLocation, List<ArmorStand>> playerMap : playerHolograms.values()) {
            List<ArmorStand> stands = playerMap.remove(location);
            if (stands != null) stands.forEach(ArmorStand::remove);
        }
    }

    public void cleanup() {
        for (Map<CrateLocation, List<ArmorStand>> playerMap : playerHolograms.values()) {
            playerMap.values().forEach(stands -> stands.forEach(ArmorStand::remove));
        }
        playerHolograms.clear();
    }

    // Fixed method name
    public void removePlayerHolograms(Player player) {
        Map<CrateLocation, List<ArmorStand>> map = playerHolograms.remove(player.getUniqueId());
        if (map != null) {
            map.values().forEach(stands -> stands.forEach(ArmorStand::remove));
        }
    }

    public void removePlayer(Player player) {
        removePlayerHolograms(player);
    }

    // New method to handle player join
    public void onPlayerJoin(Player player) {
        // Hide all existing holograms from the new player
        for (Map<CrateLocation, List<ArmorStand>> playerMap : playerHolograms.values()) {
            for (List<ArmorStand> stands : playerMap.values()) {
                for (ArmorStand stand : stands) {
                    player.hideEntity(plugin, stand);
                }
            }
        }
    }
}