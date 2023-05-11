package tv.quaint.stratosphere.plot.events;

import io.github.thebusybiscuit.slimefun4.api.events.MultiBlockInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.members.PlotRole;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;

public class SlimeFunListener implements Listener {
    /**
     * On Slimefun interact machine event
     * This is a Player event.
     * This will check to make sure the player has the permission to use the machine.
     * @param event
     */
    @EventHandler
    public void onInteractMachineEvent(MultiBlockInteractEvent event) {
        Player player = (Player) event.getPlayer();

        SkyblockPlot plot = PlotUtils.getPlotByLocation(player.getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_SLIMEFUN_INTERACT.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_SLIMEFUN_INTERACT.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            return;
        }
    }
}
