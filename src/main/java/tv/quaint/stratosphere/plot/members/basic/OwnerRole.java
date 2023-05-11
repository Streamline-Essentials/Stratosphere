package tv.quaint.stratosphere.plot.members.basic;

import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.flag.PlotFlag;
import tv.quaint.stratosphere.plot.members.PlotRole;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;

public class OwnerRole extends PlotRole {
    public OwnerRole(SkyblockPlot plot) {
        super("owner", "Owner", plot);

        addFlag(new PlotFlag(PlotFlagIdentifiers.ADMIN.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.PERMISSION_EDIT.getIdentifier(), true));

        addFlag(new PlotFlag(PlotFlagIdentifiers.PARENT.getIdentifier(), "admin"));
    }
}
