package tv.quaint.stratosphere.plot.events;

import io.github.thebusybiscuit.slimefun4.api.events.MultiBlockInteractEvent;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.members.PlotRole;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;

import java.util.Objects;

public class SlimeFunListener implements Listener {
    /**
     * On Slimefun interact machine event
     * This is a Player event.
     * This will check to make sure the player has the permission to use the machine.
     * @param event The event.
     */
    @EventHandler
    public void onInteractMachineEvent(MultiBlockInteractEvent event) {
        Player player = event.getPlayer();
        StreamlineUser user = ModuleUtils.getOrGetUser(player.getUniqueId().toString());

        SkyblockPlot plot = PlotUtils.getPlotByLocation(player.getLocation());
        if (plot == null) return;

        PlotRole role = plot.getRole(player);
        if (role == null) {
            event.setCancelled(true);
            user.sendMessage("&cYou do not have permission to use this machine as we could not find your role for this skyblock island.");
            return;
        }

        boolean can = false;
        if (role.hasFlag(PlotFlagIdentifiers.CAN_SLIMEFUN_INTERACT.getIdentifier())) {
            can = Boolean.parseBoolean(role.getFlag(PlotFlagIdentifiers.CAN_SLIMEFUN_INTERACT.getIdentifier()).getValue());
        }
        if (! can) {
            event.setCancelled(true);
            user.sendMessage("&cYou do not have permission to use this machine.");
            return;
        }
    }
}
