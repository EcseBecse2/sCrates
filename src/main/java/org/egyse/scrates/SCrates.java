package org.egyse.scrates;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.egyse.scrates.commands.CrateCommand;
import org.egyse.scrates.listeners.CrateListener;
import org.egyse.scrates.listeners.JoinQuitListener;
import org.egyse.scrates.utils.*;

public final class SCrates extends JavaPlugin {
    private static SCrates instance;

    private ConfigUtil configUtil;
    public PlayerStorageUtil playerStorageUtil;
    public CrateStorageUtil crateStorageUtil;
    public HologramUtil hologramUtil;
    public CrateUtil crateUtil;
    public PlaceholderAPIUtil placeholderAPIUtil;

    private volatile boolean disabling = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configUtil = new ConfigUtil();
        configUtil.loadCratesConfig();

        playerStorageUtil = new PlayerStorageUtil();
        crateStorageUtil = new CrateStorageUtil();
        hologramUtil = new HologramUtil();
        crateUtil = new CrateUtil();
        playerStorageUtil = new PlayerStorageUtil();
        placeholderAPIUtil = new PlaceholderAPIUtil();

        getServer().getPluginManager().registerEvents(new CrateListener(), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(), this);

        getCommand("crate").setExecutor(new CrateCommand());

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPIUtil.register();
        }

        startAutoSaveTask();
    }

    @Override
    public void onDisable() {
        disabling = true;
        playerStorageUtil.saveData(false);
        crateStorageUtil.saveData(false);
        hologramUtil.cleanup();
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPIUtil.unregister();
        }
    }

    private void startAutoSaveTask() {
        int interval = getConfig().getInt("data.auto-save") * 20;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            playerStorageUtil.saveData(true);
            //crateStorageUtil.saveData();
            saveCratesConfig();
        }, 20L, interval);
    }

    public void sendMessage(CommandSender sender, String message, boolean includePrefix) {
        if (sender instanceof Player p) {
            if (includePrefix) p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.prefix") + message));
            else p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        } else {
            System.out.println(message);
        }
    }

    public FileConfiguration getCratesConfig() { return configUtil.getCratesConfig(); }
    public void saveCratesConfig() { configUtil.saveCratesConfig(); }
    public void reloadCratesConfig() { configUtil.reloadCratesConfig(); crateStorageUtil.loadData(); hologramUtil.reloadAllHolograms(); }

    public boolean isDisabling() {
        return disabling;
    }

    public static SCrates getInstance() { return instance; }
}
