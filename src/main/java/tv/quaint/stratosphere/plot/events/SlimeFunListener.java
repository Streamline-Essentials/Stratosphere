package tv.quaint.stratosphere.plot.events;

import io.github.thebusybiscuit.slimefun4.api.events.MultiBlockInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;

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

        if (PlotListener.shouldCancel(player, PlotFlagIdentifiers.CAN_SLIMEFUN_INTERACT.getIdentifier())) {
            event.setCancelled(true);
            return;
        }
    }
}
