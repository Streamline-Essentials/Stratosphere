package tv.quaint.stratosphere.plot.timers;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;

public class PlotXPTimer implements Runnable {
    @Getter @Setter
    private int taskId;

    public PlotXPTimer() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Stratosphere.getInstance(), this, 0, 20 * 60 * 5);
    }

    @Override
    public void run() {
        PlotUtils.getPlots().forEach(plot -> {
            if (plot.hasPlayersInside()) {
                plot.addXp(10);
                plot.messageMembers("&bYou have gained &f10 &dXP &bfor players being in your island!");
            }
        });
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
    }
}
