package org.egyse.scrates.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.egyse.scrates.SCrates;
import org.egyse.scrates.models.User;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStorageUtil {
    private final SCrates pl = SCrates.getInstance();

    private final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();

    public PlayerStorageUtil() {
        loadData();
        //autoSave();
    }

    public User getUser(UUID uuid) {
        return users.getOrDefault(uuid, null);
    }

    public User getUser(String name) {
        for (User u : users.values()) {
            if (u.getName().equals(name)) return u;
        }
        return null;
    }

    public void setUser(User user) {
        users.put(user.getUuid(), user);
    }

    public void userJoined(Player p) {
        if (!users.keySet().contains(p.getUniqueId())) {
            User user = new User(
                    p.getUniqueId(),
                    p.getName(),
                    new HashMap<>(),
                    0
            );

            users.put(p.getUniqueId(), user);
        }
    }

    /*public void autoSave() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, this::saveData, 20L, pl.getConfig().getInt("data.auto-save")*20L);
    }*/

    public void loadData() {
        Gson gson = new Gson();
        File file = new File(pl.getDataFolder().getAbsolutePath() + "/players.json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                System.out.println("Loading playerdata...");

                // Create proper type for HashMap<UUID, User>
                Type type = new TypeToken<HashMap<UUID, User>>(){}.getType();

                // Load into existing map
                HashMap<UUID, User> loaded = gson.fromJson(reader, type);
                if (loaded != null) {
                    users.clear();
                    users.putAll(loaded);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveData(boolean async) {
        // Create a snapshot of the data
        ConcurrentHashMap<UUID, User> dataSnapshot = new ConcurrentHashMap<>(users);

        Runnable saveTask = () -> {
            Gson gson = new Gson();
            File file = new File(pl.getDataFolder(), "players.json");
            try {
                if (!file.exists()) file.createNewFile();
                try (Writer writer = new FileWriter(file, false)) {
                    Type type = new TypeToken<ConcurrentHashMap<UUID, User>>(){}.getType();
                    gson.toJson(dataSnapshot, type, writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        if (async && !pl.isDisabling()) {
            Bukkit.getScheduler().runTaskAsynchronously(pl, saveTask);
        } else {
            saveTask.run(); // Sync execution
        }
    }
}
