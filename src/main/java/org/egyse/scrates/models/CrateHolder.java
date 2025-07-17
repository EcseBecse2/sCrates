package org.egyse.scrates.models;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CrateHolder implements InventoryHolder {
    private Inventory inv;
    private boolean crate;

    public CrateHolder(@NotNull Inventory inv) {
        this.inv = inv;
        this.crate = true;
    }
    public CrateHolder(@NotNull Inventory inv, @Nullable boolean crate) {
        this.crate = crate;
        this.inv = inv;
    }

    public boolean isCrate() {
        return this.crate;
    }

    @Override
    public Inventory getInventory() {
        return this.inv;
    }

    public void setInv(Inventory inv) {
        this.inv = inv;
    }
}
