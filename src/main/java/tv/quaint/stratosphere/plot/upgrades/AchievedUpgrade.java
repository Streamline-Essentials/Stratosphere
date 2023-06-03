package tv.quaint.stratosphere.plot.upgrades;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;
import tv.quaint.stratosphere.plot.SkyblockPlot;

public class AchievedUpgrade implements Identifiable {
    @Getter @Setter
    private SkyblockPlot plot;
    @Getter @Setter
    private PlotUpgrade upgrade;

    public AchievedUpgrade(SkyblockPlot plot, PlotUpgrade upgrade) {
        this.plot = plot;
        this.upgrade = upgrade;
    }

    @Override
    public String getIdentifier() {
        return plot.getIdentifier() + ":" + upgrade.getIdentifier();
    }

    @Override
    public void setIdentifier(String identifier) {
    }
}
