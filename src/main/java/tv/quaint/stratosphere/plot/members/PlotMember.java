package tv.quaint.stratosphere.plot.members;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.users.SkyblockUser;

import java.util.UUID;

public class PlotMember implements Comparable<PlotMember> {
    @Getter
    private final UUID uuid; // UUID of the player
    @Getter @Setter
    private PlotRole role;

    public PlotMember(UUID uuid, PlotRole role) {
        this.uuid = uuid;
        this.role = role;
    }

    @Override
    public int compareTo(PlotMember o) {
        return uuid.compareTo(o.getUuid());
    }

    public StreamlineUser getUser() {
        return ModuleUtils.getOrGetUser(uuid.toString());
    }

    public SkyblockUser getSkyblockUser() {
        return PlotUtils.getOrGetUser(uuid.toString());
    }

    public void message(String message) {
        getUser().sendMessage(message);
    }
}
