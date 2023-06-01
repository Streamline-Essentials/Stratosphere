package tv.quaint.stratosphere.plot.quests;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import tv.quaint.stratosphere.plot.upgrades.PlotUpgrade;

public class QuestTask {
    @Getter @Setter
    private Player player;

    public QuestTask(Player player) {
        this.player = player;
    }
}
