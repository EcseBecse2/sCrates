package org.egyse.scrates.models;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Hologram {
    private boolean enabled;
    private double offset;
    private List<String> text;

    public Hologram(boolean enabled, double offset, List<String> text) {
        this.enabled = enabled;
        this.offset = offset;
        this.text = new ArrayList<>();
        for (String l : text) this.text.add(ChatColor.translateAlternateColorCodes('&', l));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public List<String> getText() {
        return text;
    }

    public void setText(List<String> text) {
        this.text = new ArrayList<>();
        for (String l : text) this.text.add(ChatColor.translateAlternateColorCodes('&', l));
    }
}
