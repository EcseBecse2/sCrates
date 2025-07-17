package org.egyse.scrates.API;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.egyse.scrates.models.Crate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CrateOpenEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Crate crate;
    private final Integer amount;

    public CrateOpenEvent(Player player, Crate crate, Integer amount) {
        this.player = player;
        this.crate = crate;
        this.amount = amount;
    }

    // ======== Standard Methods ========
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public Crate getCrate() {
        return crate;
    }

    public Integer getAmount() {
        return amount;
    }
}
