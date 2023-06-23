package tv.quaint.stratosphere.plot.timers;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.config.bits.PlotPosition;
import tv.quaint.stratosphere.plot.PlotUtils;

public class PlotKeepAliveTicker implements Runnable {
    @Getter @Setter
    private int taskId;

    public PlotKeepAliveTicker() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Stratosphere.getInstance(), this, 0, 5);
    }

    @Override
    public void run() {
        PlotUtils.getPlots().forEach(plot -> {
            plot.updateWorldBorder();

            if (! plot.hasMemberOnline()) {
                plot.saveAll();
                plot.unload();
            }

            PlotPosition pos = plot.getPlotPosition();
            Stratosphere.getPlotPosConfig().savePlotPosition(pos);
        });
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
    }
}
