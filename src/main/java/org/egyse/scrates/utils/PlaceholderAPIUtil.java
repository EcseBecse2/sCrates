package org.egyse.scrates.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.egyse.scrates.SCrates;
import org.egyse.scrates.models.Crate;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIUtil extends PlaceholderExpansion {
    private final SCrates pl = SCrates.getInstance();

    @Override
    public @NotNull String getIdentifier() {
        return "crate";
    }

    @Override
    public @NotNull String getAuthor() {
        return "EcseBecse2";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        String[] l = identifier.split("_");
        if (l[0].equalsIgnoreCase("keys")) {
            String cratename = l[1];
            Crate crate = pl.crateStorageUtil.getCrate(cratename);
            if (crate != null) {
                return String.valueOf(pl.playerStorageUtil.getUser(player.getUniqueId()).getKeys().getOrDefault(crate.getId(), 0));
            }
        }

        return null;
    }
}
