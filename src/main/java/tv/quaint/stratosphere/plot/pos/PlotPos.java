package tv.quaint.stratosphere.plot.pos;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.thebase.lib.re2j.Matcher;
import tv.quaint.utils.MatcherUtils;

import java.util.List;

public class PlotPos implements Comparable<PlotPos> {
    public enum PlotPosType {
        SPAWN,
        HOME,
        WARP
    }

    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private SkyblockPlot plot;
    @Getter @Setter
    private double x, y, z;
    @Getter @Setter
    private float yaw, pitch;
    @Getter @Setter
    private PlotPosType type;

    public PlotPos(String identifier, SkyblockPlot plot, PlotPosType type, double x, double y, double z, float yaw, float pitch) {
        this.identifier = identifier;
        this.plot = plot;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public PlotPos(String name, SkyblockPlot plot, PlotPosType type, Location location1) {
        this(name, plot, type, location1.getX(), location1.getY(), location1.getZ(), location1.getYaw(), location1.getPitch());
    }

    public PlotPos(String name, SkyblockPlot plot, Location location1) {
        this(name, plot, PlotPosType.HOME, location1);
    }

    public PlotPos(String from) {
        if (from.startsWith("[")) from = from.substring(1);
        if (from.endsWith("]")) from = from.substring(0, from.length() - 1);
        Matcher matcher = MatcherUtils.matcherBuilder("((.*?)[;])", from);
        List<String[]> matches = MatcherUtils.getGroups(matcher, 2);

        for (int i = 0; i < matches.size(); i ++) {
            String[] match = matches.get(i);

            if (i == 0) {
                this.identifier = match[1];
            }
            if (i == 1) {
                this.type = PlotPosType.valueOf(match[1]);
            }
            if (i == 2) {
                String[] split = match[1].split(",", 5);
                for (int si = 0; si < split.length; si ++) {
                    String s = split[si];
                    if (si == 0) this.x = Double.parseDouble(s);
                    if (si == 1) this.y = Double.parseDouble(s);
                    if (si == 2) this.z = Double.parseDouble(s);
                    if (si == 3) this.yaw = Float.parseFloat(s);
                    if (si == 4) this.pitch = Float.parseFloat(s);
                }
            }
        }
    }

    public Location toLocation(World world) {
        if (world == null) return null;

        return new Location(world, x, y, z, yaw, pitch);
    }

    public World getWorld() {
        return getPlot().getSkyWorld().getWorld();
    }

    public Location toLocation() {
        return toLocation(getWorld());
    }

    public void teleport(Player player) {
        try {
            player.teleport(toLocation());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return "[" + identifier + ";" + type + ";" + x + "," + y + "," + z + "," + yaw + "," + pitch + ";" + plot.getUuid() + ";]";
    }

    public static PlotPos fromString(String plotPos, SkyblockPlot plot) {
        PlotPos pos = new PlotPos(plotPos);
        pos.setPlot(plot);

        if (pos.getType() == PlotPosType.SPAWN) {
            pos = new SpawnPos(pos.getPlot(), pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());
        }

        return pos;
    }

    @Override
    public int compareTo(@NotNull PlotPos o) {
        return identifier.compareTo(o.getIdentifier());
    }
}
