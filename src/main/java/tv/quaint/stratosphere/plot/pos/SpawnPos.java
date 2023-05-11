package tv.quaint.stratosphere.plot.pos;

import tv.quaint.stratosphere.plot.SkyblockPlot;

public class SpawnPos extends PlotPos {
    public SpawnPos(SkyblockPlot plot, double x, double y, double z, float yaw, float pitch) {
        super("spawn", plot, PlotPosType.SPAWN, x, y, z, yaw, pitch);
    }
}
