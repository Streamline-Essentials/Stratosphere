package tv.quaint.stratosphere.plot.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.users.SkyblockUser;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BukkitListener implements Listener {
    @EventHandler
    public void onPreJoin(PlayerPreLoginEvent event) {
        SkyblockUser user = PlotUtils.getOrGetUser(event.getUniqueId().toString());
        if (user == null) {
            user = new SkyblockUser(event.getUniqueId().toString());
            PlotUtils.loadUser(user);
        }

        if (user.getPlotUuid() != null && ! user.getPlotUuid().isEmpty() && ! user.getPlotUuid().isBlank() && ! user.getPlotUuid().equals("null")) {
            SkyblockPlot plot = PlotUtils.getOrGetPlot(user.getPlotUuid());
            CompletableFuture.supplyAsync(() -> {
                World world = plot.getSkyWorld().getWorld();
                while (world == null) {
                    world = plot.getSkyWorld().getWorld();
                }
                return world;
            }).completeOnTimeout(null, 5, TimeUnit.SECONDS).join();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location spawn = Stratosphere.getMyConfig().getSpawnLocation();

        player.teleport(spawn);

        SkyblockUser user = PlotUtils.getOrGetUser(player.getUniqueId().toString());
        if (user == null) {
            user = new SkyblockUser(player.getUniqueId().toString());
            PlotUtils.loadUser(user);
        }

        if (user.getPlotUuid() != null && ! user.getPlotUuid().isEmpty() && ! user.getPlotUuid().isBlank() && ! user.getPlotUuid().equals("null")) {
            SkyblockPlot plot = PlotUtils.getOrGetPlot(user.getPlotUuid());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        PlotUtils.unloadUser(player.getUniqueId().toString());
    }
}
