package tv.quaint.stratosphere.plot.upgrades;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.registries.Identifiable;

public class AchievedUpgrade implements Identifiable {
    @Getter @Setter
    private PlotUpgrade.UpgradeType type;
    @Getter @Setter
    private int tier;

    public AchievedUpgrade(PlotUpgrade.UpgradeType type, int tier) {
        this.type = type;
        this.tier = tier;
    }

    public AchievedUpgrade(String identifier, int tier) {
        this(PlotUpgrade.UpgradeType.valueOf(identifier.toUpperCase()), tier);
    }

    @Override
    public String getIdentifier() {
        return type.name();
    }

    @Override
    public void setIdentifier(String identifier) {
        type = PlotUpgrade.UpgradeType.valueOf(identifier.toUpperCase());
    }
}
