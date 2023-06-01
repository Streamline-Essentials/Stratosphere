package tv.quaint.stratosphere.plot.members;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.users.SkyblockUser;

import java.util.UUID;

public class PlotMember implements Comparable<PlotMember> {
    @Getter
    private final String uuid; // UUID of the player
    @Getter @Setter
    private PlotRole role;

    public PlotMember(String uuid, PlotRole role) {
        this.uuid = uuid;
        this.role = role;
    }

    public PlotMember(UUID uuid, PlotRole role) {
        this(uuid.toString(), role);
    }

    @Override
    public int compareTo(PlotMember o) {
        return uuid.compareTo(o.getUuid());
    }

    public StreamlineUser getUser() {
        return ModuleUtils.getOrGetUser(uuid);
    }

    public SkyblockUser getSkyblockUser() {
        return PlotUtils.getOrGetUser(uuid);
    }

    public void message(String message) {
        getUser().sendMessage(message);
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        return getBukkitPlayer() != null;
    }
}
