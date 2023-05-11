package tv.quaint.stratosphere.plot.upgrades;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class UpgradeTask {
    @Getter @Setter
    private Player player;
    @Getter @Setter
    private PlotUpgrade upgrade;

    public UpgradeTask(Player player, PlotUpgrade upgrade) {
        this.player = player;
        this.upgrade = upgrade;
    }
}
