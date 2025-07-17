package org.egyse.scrates.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.egyse.scrates.API.CrateOpenEvent;
import org.egyse.scrates.SCrates;
import org.egyse.scrates.models.Crate;
import org.egyse.scrates.models.CrateHolder;
import org.egyse.scrates.models.CrateLocation;
import org.egyse.scrates.models.User;

public class CrateListener implements Listener {
    private final SCrates pl = SCrates.getInstance();
    private final ConfigurationSection messages = pl.getConfig().getConfigurationSection("messages");

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player player = e.getPlayer();
        boolean breakingMode = player.isSneaking() &&
                player.getGameMode() == GameMode.CREATIVE &&
                player.isOp();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Crate crate = pl.crateStorageUtil.getCrate(e.getClickedBlock().getLocation());
            if (crate != null) {
                e.setCancelled(true);

                User user = pl.playerStorageUtil.getUser(e.getPlayer().getUniqueId());
                if (!user.getKeys().containsKey(crate.getId()) || user.getKeys().get(crate.getId()) < 1) {
                    pl.sendMessage(e.getPlayer(), messages.getString("no-crate-keys"), true);
                    return;
                }

                int amount = 1;
                if (e.getPlayer().isSneaking()) {
                    if (user.getKeys().get(crate.getId()) >= 100) {
                        amount = 100;
                    } else {
                        amount = user.getKeys().get(crate.getId());
                    }
                }

                CrateOpenEvent event = new CrateOpenEvent(player, crate, amount);
                Bukkit.getServer().getPluginManager().callEvent(event);
                pl.crateUtil.openCrate(user, crate, amount);
            }
        } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            Crate crate = pl.crateStorageUtil.getCrate(e.getClickedBlock().getLocation());
            if (crate != null) {
                if (breakingMode) return;
                e.setCancelled(true);
                pl.crateUtil.previewCrate(e.getPlayer(), crate);
            }
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (!(inv.getHolder() instanceof CrateHolder h)) return;
        if (h.isCrate()) e.setCancelled(true);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        pl.hologramUtil.reloadWorldHolograms(e.getWorld().getName());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Crate crate = pl.crateStorageUtil.getCrate(e.getBlock().getLocation());
        if (crate != null) {
            Player p = e.getPlayer();
            if (p.isSneaking() && p.getGameMode() == GameMode.CREATIVE && p.isOp()) {
                e.setCancelled(true);

                CrateLocation crateLocation = new CrateLocation(e.getBlock().getLocation());

                crate.getLocations().removeIf(loc -> loc.equals(crateLocation));

                pl.crateStorageUtil.setCrate(crate);
                pl.crateStorageUtil.saveData(true);

                pl.hologramUtil.removeHologram(crateLocation);

                pl.sendMessage(p, messages.getString("crate-removed"), true);
            }
        }
    }
}
