package tv.quaint.stratosphere.plot.timers;

import net.streamline.apib.SLAPIB;
import org.bukkit.Bukkit;
import org.bukkit.World;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.world.SkyblockIOBus;

public class PlotXPTimer implements Runnable {
    public PlotXPTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SLAPIB.getPlugin(), this, 0L, 20L * 60L * 5L);
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
}
