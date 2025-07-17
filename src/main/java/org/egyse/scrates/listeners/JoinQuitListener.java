package org.egyse.scrates.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.egyse.scrates.SCrates;

public class JoinQuitListener implements Listener {
    private final SCrates pl = SCrates.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        pl.playerStorageUtil.userJoined(e.getPlayer());
        pl.hologramUtil.onPlayerJoin(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        pl.hologramUtil.removePlayer(e.getPlayer());
    }
}
