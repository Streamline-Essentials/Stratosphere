package tv.quaint.stratosphere.plot.timers;

import net.streamline.apib.SLAPIB;
import org.bukkit.Bukkit;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.users.SkyblockUser;

public class UserDustTimer implements Runnable {
    public UserDustTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SLAPIB.getPlugin(), this, 0L, 20L * 60L);
    }

    @Override
    public void run() {
        PlotUtils.getLoadedUsers().forEach(user -> {
            if (user != null) {
                if (user.isAlreadyInPlot()) {
                    user.addStarDust(5);
                    user.getStreamlineUser().sendMessage("&bYou have gained &f5 &dStar Dust &bfor being a member of an island!");
                }
            }
        });
    }
}
