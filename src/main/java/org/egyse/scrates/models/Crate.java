package org.egyse.scrates.models;

import java.util.List;

public class Crate {
    private String id;
    private String name;
    private boolean preview;
    private Hologram hologram;
    private List<CrateLocation> locations;
    private int rows;
    private List<CratePrize> prizes;
    private double maxChance;

    public Crate(String id, String name, boolean preview, Hologram hologram, List<CrateLocation> locations, int rows, List<CratePrize> prizes, double maxChance) {
        this.id = id;
        this.name = name;
        this.preview = preview;
        this.hologram = hologram;
        this.locations = locations;
        this.rows = rows;
        this.prizes = prizes;
        this.maxChance = maxChance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public List<CrateLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<CrateLocation> locations) {
        this.locations = locations;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public List<CratePrize> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<CratePrize> prizes) {
        this.prizes = prizes;
    }

    public double getMaxChance() {
        return maxChance;
    }

    public void setMaxChance(double maxChance) {
        this.maxChance = maxChance;
    }
}
