package tv.quaint.stratosphere.plot.upgrades;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;

public class PlotUpgrade implements Identifiable {
    public enum UpgradeType {
        MOB_SPAWN_RATE("Mob Spawn Rate"),
        MOB_SPAWN_CAP("Mob Spawn Cap"),
        PLOT_SIZE("Plot Size"),
        PARTY_SIZE("Party Size"),
        ISLAND_LEVEL_SET("Set Island Level"),
        ISLAND_LEVEL_ADD("Add Island Level"),
        ISLAND_LEVEL_REMOVE("Remove Island Level"),
        ISLAND_LEVEL_CAP_SET("Set Island Level Cap"),
        ISLAND_LEVEL_CAP_ADD("Add Island Level Cap"),
        ISLAND_LEVEL_CAP_REMOVE("Remove Island Level Cap"),
        ISLAND_GENERATOR_NORMAL("Island Generator"),
        ;

        @Getter
        private final String prettyName;

        UpgradeType(String prettyName) {
            this.prettyName = prettyName;
        }
    }

    public static final int MIN_TIER = 1;

    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private UpgradeType type;
    @Getter @Setter
    private int tier;
    @Getter @Setter
    private String payload;
    @Getter @Setter
    private double dustCost;
    @Getter @Setter
    private String description;

    public PlotUpgrade(String identifier, UpgradeType type, int tier, String payload, double dustCost, String description) {
        this.identifier = identifier;
        this.type = type;
        this.tier = tier;
        this.payload = payload;
        this.dustCost = dustCost;
        this.description = description;

        if (tier < MIN_TIER) {
            throw new IllegalArgumentException("Tier must be greater than or equal to " + MIN_TIER);
        }
    }

    public PlotUpgrade(String identifier, UpgradeType type, String payload, double dustCost, String description) {
        this(identifier, type, PlotUtils.getNextUpgradeTier(type), payload, dustCost, description);
    }

    public double getDouble() {
        try {
            return Double.parseDouble(payload);
        } catch (Exception e) {
            e.printStackTrace();
            return 0d;
        }
    }

    public int getInt() {
        try {
            return Integer.parseInt(payload);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean doUpgrade(SkyblockPlot plot, UpgradeTask task) {
        if (plot == null) return false;

        switch (getType()) {
            case MOB_SPAWN_RATE:
                plot.setSpawnRateIndex(getInt());
                break;
            case MOB_SPAWN_CAP:
                plot.setSpawnCapIndex(getInt());
                break;
            case PLOT_SIZE:
                plot.setRadius(getDouble());
                break;
            case PARTY_SIZE:
                plot.setMaxPartySize(getInt());
                break;
            case ISLAND_LEVEL_SET:
                plot.setLevel(getInt());
                break;
            case ISLAND_LEVEL_ADD:
                plot.addLevel(getInt());
                break;
            case ISLAND_LEVEL_REMOVE:
                plot.removeLevel(getInt());
                break;
            case ISLAND_LEVEL_CAP_SET:
                plot.setMaxLevel(getInt());
                break;
            case ISLAND_LEVEL_CAP_ADD:
                plot.addMaxLevel(getInt());
                break;
            case ISLAND_LEVEL_CAP_REMOVE:
                plot.removeMaxLevel(getInt());
                break;
            case ISLAND_GENERATOR_NORMAL:
                plot.setGeneratorIndex(getTier());
                break;
            default:
                return false;
        }

        return true;
    }

    public void save() {
        Stratosphere.getUpgradeConfig().saveUpgrade(this);
    }

    public void load() {
        Stratosphere.getUpgradeConfig().loadUpgrade(this);
    }

    public void unload() {
        Stratosphere.getUpgradeConfig().unloadUpgrade(getIdentifier());
    }
}
