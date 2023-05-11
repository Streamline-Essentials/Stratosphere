package tv.quaint.stratosphere.plot.upgrades;

import tv.quaint.stratosphere.plot.SkyblockPlot;

import java.util.function.BiFunction;

public interface PlotUpgrader extends BiFunction<SkyblockPlot, UpgradeTask, Boolean> {
}
