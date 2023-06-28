package tv.quaint.stratosphere.config.bits;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import tv.quaint.objects.Identifiable;
import tv.quaint.stratosphere.Stratosphere;

public class PlotPosition implements Identifiable {
    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private double x;
    @Getter @Setter
    private double z;

    public PlotPosition(String identifier, double x, double z) {
        this.identifier = identifier;
        this.x = x;
        this.z = z;
    }

    public boolean isLocationWithin(Location location) {
        double maxRadius = Stratosphere.getMyConfig().getIslandAbsoluteSize() / 2;

        if (location.getWorld().getName().equals(Stratosphere.getMyConfig().getIslandWorldName() + "_nether")) {
            maxRadius /= 8;
            return location.getX() >= x * 8 - maxRadius && location.getX() <= x * 8 + maxRadius &&
                    location.getZ() >= z * 8 - maxRadius && location.getZ() <= z * 8 + maxRadius;
        }

        return location.getX() >= x - maxRadius && location.getX() <= x + maxRadius &&
                location.getZ() >= z - maxRadius && location.getZ() <= z + maxRadius;
    }

    public boolean isLocationWithin(double xPos, double zPos) {
        double maxRadius = Stratosphere.getMyConfig().getIslandAbsoluteSize() / 2;

        return xPos >= x - maxRadius && xPos <= x + maxRadius &&
                zPos >= z - maxRadius && zPos <= z + maxRadius;
    }
}
