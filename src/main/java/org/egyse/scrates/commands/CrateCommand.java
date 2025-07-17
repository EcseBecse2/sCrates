package org.egyse.scrates.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.egyse.scrates.SCrates;
import org.egyse.scrates.models.Crate;
import org.egyse.scrates.models.CrateLocation;
import org.egyse.scrates.models.User;

import java.text.ParseException;

public class CrateCommand implements CommandExecutor {
    private final SCrates pl = SCrates.getInstance();
    private ConfigurationSection messages;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        messages = pl.getConfig().getConfigurationSection("messages");

        if (commandSender instanceof Player p) {
            if (!p.isOp()) {
                pl.sendMessage(p, messages.getString("no-permission"), true);
                return true;
            }
        }

        // /crate set <name>
        // /crate givekey <player> <name> <amount>
        if (strings.length == 1) {
            if (strings[0].equalsIgnoreCase("list")) {
                pl.sendMessage(commandSender, messages.getString("list").replace("{crates}", String.join(", ", pl.crateStorageUtil.crates.keySet().stream().toList())), true);
            } else if (strings[0].equalsIgnoreCase("reload")) {
                pl.reloadCratesConfig();
                pl.reloadConfig();
                pl.sendMessage(commandSender, messages.getString("reloaded"), true);
            } else {
                for (String l : messages.getStringList("help")) {
                    pl.sendMessage(commandSender, ChatColor.translateAlternateColorCodes('&', l), false);
                }
            }
        } else if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("set")) {
                if (!(commandSender instanceof Player p)) {
                    pl.sendMessage(commandSender, messages.getString("player-not-found"), true);
                    return true;
                }

                Crate crate = pl.crateStorageUtil.getCrate(strings[1]);
                if (crate == null) {
                    pl.sendMessage(commandSender, messages.getString("invalid-crate"), true);
                    return true;
                }

                Block block = p.getTargetBlockExact(5);
                if (block == null || block.getType() == Material.AIR) {
                    pl.sendMessage(commandSender, messages.getString("no-target-block"), true);
                    return true;
                }

                CrateLocation location = new CrateLocation(block.getLocation());

                Crate previous = pl.crateStorageUtil.getCrate(location);
                if (previous != null) {
                    previous.getLocations().removeIf(crateLocation -> crateLocation.equals(location));
                    pl.crateStorageUtil.setCrate(previous);
                }
                pl.hologramUtil.removeHologram(location);

                crate.getLocations().add(location);

                pl.crateStorageUtil.setCrate(crate);

                pl.crateStorageUtil.saveData(true);

                pl.hologramUtil.createHologram(location, crate);

                pl.sendMessage(commandSender, messages.getString("crate-set"), true);
            } else {
                for (String l : messages.getStringList("help")) {
                    pl.sendMessage(commandSender, ChatColor.translateAlternateColorCodes('&', l), false);
                }
            }
        } else if (strings.length == 3) {
            if (strings[0].equalsIgnoreCase("giveall")) {
                Crate crate = pl.crateStorageUtil.getCrate(strings[1]);
                if (crate == null) {
                    pl.sendMessage(commandSender, messages.getString("invalid-crate"), true);
                    return true;
                }

                int amount;
                try {
                    amount = Integer.parseInt(strings[2]);
                } catch (Exception e) {
                    pl.sendMessage(commandSender, messages.getString("invalid-amount"), true);
                    return true;
                }

                if (amount < 1 || amount > 200) {
                    pl.sendMessage(commandSender, messages.getString("invalid-amount"), true);
                    return true;
                }

                for (Player p : pl.getServer().getOnlinePlayers()) {
                    User user = pl.playerStorageUtil.getUser(p.getUniqueId());

                    user.getKeys().put(crate.getId(), user.getKeys().getOrDefault(crate.getId(), 0) + amount);
                    pl.playerStorageUtil.setUser(user);
                }

                pl.sendMessage(commandSender, messages.getString("give-all")
                                .replace(
                                        "{player}",
                                        (commandSender instanceof Player player) ? player.getName() : "CONSOLE"
                                )
                                .replace(
                                        "{crate}",
                                        crate.getName()
                                )
                        , true);
            } else {
                for (String l : messages.getStringList("help")) {
                    pl.sendMessage(commandSender, ChatColor.translateAlternateColorCodes('&', l), false);
                }
            }
        } else if (strings.length == 4) {
            if (strings[0].equalsIgnoreCase("givekey") || strings[0].equalsIgnoreCase("give")) {
                User user = pl.playerStorageUtil.getUser(strings[1]);
                if (user == null) {
                    pl.sendMessage(commandSender, messages.getString("invalid-player"), true);
                    return true;
                }

                Crate crate = pl.crateStorageUtil.getCrate(strings[2]);
                if (crate == null) {
                    pl.sendMessage(commandSender, messages.getString("invalid-crate"), true);
                    return true;
                }

                int amount;
                try {
                    amount = Integer.parseInt(strings[3]);
                } catch (Exception e) {
                    pl.sendMessage(commandSender, messages.getString("invalid-amount"), true);
                    return true;
                }

                if (amount < 1 || amount > 200) {
                    pl.sendMessage(commandSender, messages.getString("invalid-amount"), true);
                    return true;
                }

                user.getKeys().put(crate.getId(), user.getKeys().getOrDefault(crate.getId(), 0) + amount);
                pl.playerStorageUtil.setUser(user);
                pl.sendMessage(commandSender, messages.getString("successfully-gave")
                        .replace("{player}", user.getName())
                        .replace("{crate}", crate.getName())
                        .replace("{amount}", String.valueOf(amount)), true);
                pl.sendMessage((CommandSender) Bukkit.getOfflinePlayer(user.getUuid()), messages.getString("received-keys")
                        .replace("{crate}", crate.getName())
                        .replace("{amount}", String.valueOf(amount)), true);
            } else {
                for (String l : messages.getStringList("help")) {
                    pl.sendMessage(commandSender, ChatColor.translateAlternateColorCodes('&', l), false);
                }
            }
        } else {
            for (String l : messages.getStringList("help")) {
                pl.sendMessage(commandSender, ChatColor.translateAlternateColorCodes('&', l), false);
            }
        }

        return true;
    }
}
