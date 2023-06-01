package tv.quaint.stratosphere.plot.events;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.config.bits.ConfiguredGenerator;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.quests.PlotQuest;
import tv.quaint.stratosphere.users.SkyblockUser;

import java.util.Objects;
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
                World world = plot.getWorld();
                while (world == null) {
                    world = plot.getWorld();
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

    /**
     * Even that is fired when cobble forms from a cobble generator (flowing lava touching water).
     * Will see if it is inside a plot and if so, cancel the event and set the block to a random generator block.
     */
    @EventHandler
    public void onCobbleGen(BlockFormEvent event) {
        if (event.isCancelled()) return;

        BlockState newBlock = event.getNewState();
        if (newBlock.getType() == Material.COBBLESTONE || newBlock.getType() == Material.STONE) {

            Location loc = event.getBlock().getLocation();
            SkyblockPlot plot = PlotUtils.getPlotByLocation(loc);
            if (plot == null) return;

            int genIndex = plot.getGeneratorIndex();

            if (genIndex == -1) return;

            ConfiguredGenerator generator = Stratosphere.getGeneratorConfig().getByTier(genIndex);
            if (generator == null) return;

            Material material = generator.poll();
            if (material == null) return;

//            newBlock.setType(material);

            event.setCancelled(true);
            event.getBlock().setType(material);
            Objects.requireNonNull(loc.getWorld()).playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);
        }
    }

    @EventHandler
    public void onStatUpdate(PlayerStatisticIncrementEvent event) {
        Statistic statistic = event.getStatistic();
        Player player = event.getPlayer();
        int value = event.getNewValue() - event.getPreviousValue();

        if (event.getEntityType() != null) {
            PlotUtils.getOrGetQuester(player.getUniqueId().toString()).addAmount(statistic.name(), event.getEntityType(), value);
        } else if (event.getMaterial() != null) {
            PlotUtils.getOrGetQuester(player.getUniqueId().toString()).addAmount(statistic.name(), event.getMaterial(), value);
        } else {
            PlotUtils.getOrGetQuester(player.getUniqueId().toString()).addAmount(statistic.name(), "", value);
        }
    }
}
