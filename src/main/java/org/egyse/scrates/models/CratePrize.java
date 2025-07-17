package org.egyse.scrates.models;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CratePrize {
    private ItemStack displayItem;
    private double chance;
    private List<String> commands;

    public CratePrize(ItemStack displayItem, double chance, List<String> commands) {
        this.displayItem = displayItem;
        this.chance = chance;
        this.commands = commands;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public void setDisplayItem(ItemStack displayItem) {
        this.displayItem = displayItem;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
}
