package org.egyse.scrates.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.egyse.scrates.SCrates;

import java.io.File;
import java.io.IOException;

public class ConfigUtil {
    private final SCrates pl = SCrates.getInstance();

    private File cratesFile;
    private FileConfiguration cratesConfig;

    public void reloadCratesConfig() {
        System.out.println("Reloading the crates config...");
        loadCratesConfig();
    }

    public void loadCratesConfig() {
        cratesFile = new File(pl.getDataFolder(), "crates.yml");
        if (!cratesFile.exists()) {
            pl.saveResource("crates.yml", false);
        }
        cratesConfig = YamlConfiguration.loadConfiguration(cratesFile);
    }

    public void saveCratesConfig() {
        try {
            cratesConfig.save(cratesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getCratesConfig() {
        return cratesConfig;
    }
}
