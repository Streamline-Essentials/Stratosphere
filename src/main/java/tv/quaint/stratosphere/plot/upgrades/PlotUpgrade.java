package tv.quaint.stratosphere.plot.upgrades;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;

public class PlotUpgrade {
    public enum Type {
        MOB_SPAWN_RATE("Mob Spawn Rate"),
        MOB_SPAWN_CAP("Mob Spawn Cap"),
        PLOT_SIZE("Plot Size"),
        PARTY_SIZE("Party Size"),
        ISLAND_LEVEL("Island Level"),
        ISLAND_LEVEL_CAP("Island Level Cap"),
        ;

        @Getter
        private final String prettyName;

        Type(String prettyName) {
            this.prettyName = prettyName;
        }
    }

    public static final int MIN_TIER = 1;

    @Getter @Setter
    private Type type;
    @Getter @Setter
    private int tier;
    @Getter @Setter
    private PlotUpgrader upgrader;

    public PlotUpgrade(Type type, int tier, PlotUpgrader upgrader) {
        this.type = type;
        this.tier = tier;
        this.upgrader = upgrader;

        if (tier < MIN_TIER) {
            throw new IllegalArgumentException("Tier must be greater than or equal to " + MIN_TIER);
        }

        PlotUtils.getUpgradeRegistry().register(this);
    }

    public PlotUpgrade(Type type, PlotUpgrader upgrader) {
        this(type, PlotUtils.getUpgradeRegistry().getNextTier(type), upgrader);
    }

    public boolean doUpgrade(SkyblockPlot plot, UpgradeTask task) {
        return upgrader.apply(plot, task);
    }
}
