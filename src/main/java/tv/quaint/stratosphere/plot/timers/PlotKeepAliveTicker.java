package tv.quaint.stratosphere.plot.timers;

import net.streamline.api.scheduler.ModuleRunnable;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;

public class PlotKeepAliveTicker extends ModuleRunnable {
    public PlotKeepAliveTicker() {
        super(Stratosphere.getInstance(), 0, 5);
    }

    @Override
    public void run() {
        PlotUtils.getPlots().forEach(plot -> {
            plot.updateWorldBorder();

            if (! plot.hasMemberOnline()) {
                plot.saveAll();
                plot.unload();
            }
        });
    }
}
