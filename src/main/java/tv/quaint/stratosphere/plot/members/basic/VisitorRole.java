package tv.quaint.stratosphere.plot.members.basic;

import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.flag.PlotFlag;
import tv.quaint.stratosphere.plot.members.PlotRole;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;

public class VisitorRole extends PlotRole {
    public VisitorRole(SkyblockPlot plot) {
        super("visitor", "Visitor", plot);

        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_FLY.getIdentifier(), true));

        addFlag(new PlotFlag(PlotFlagIdentifiers.PARENT.getIdentifier(), "none"));
    }
}
