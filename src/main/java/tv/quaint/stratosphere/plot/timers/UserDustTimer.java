package tv.quaint.stratosphere.plot.timers;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;

public class UserDustTimer implements Runnable {
    @Getter @Setter
    private int taskId;

    public UserDustTimer() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Stratosphere.getInstance(), this, 0, 20 * 60);
    }

    @Override
    public void run() {
        PlotUtils.getLoadedUsers().forEach(user -> {
            if (user != null) {
                if (user.isAlreadyInPlot()) {
                    user.addStarDust(5);
                    user.sendMessage("&bYou have gained &f5 &dStar Dust &bfor being a member of an island!");
                }
            }
        });
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
    }
}
