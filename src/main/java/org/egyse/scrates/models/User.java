package org.egyse.scrates.models;

import java.util.HashMap;
import java.util.UUID;

public class User {
    private UUID uuid;
    private String name;
    private HashMap<String, Integer> keys;
    private int cratesOpened;

    public User(UUID uuid, String name, HashMap<String, Integer> keys, int cratesOpened) {
        this.uuid = uuid;
        this.name = name;
        this.keys = keys;
        this.cratesOpened = cratesOpened;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Integer> getKeys() {
        return keys;
    }

    public void setKeys(HashMap<String, Integer> keys) {
        this.keys = keys;
    }

    public int getCratesOpened() {
        return cratesOpened;
    }

    public void setCratesOpened(int cratesOpened) {
        this.cratesOpened = cratesOpened;
    }
}
