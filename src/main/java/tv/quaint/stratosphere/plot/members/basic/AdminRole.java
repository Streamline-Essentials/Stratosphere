package tv.quaint.stratosphere.plot.members.basic;

import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.flag.PlotFlag;
import tv.quaint.stratosphere.plot.members.PlotRole;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;
import tv.quaint.stratosphere.plot.pos.PlotFlagType;

public class AdminRole extends PlotRole {
    public AdminRole(SkyblockPlot plot) {
        super("admin", "Admin", plot);

        addFlag(new PlotFlag(PlotFlagIdentifiers.ADMIN.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_PET_DAMAGE.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_VILLAGER_DAMAGE.getIdentifier(), true));

        addFlag(new PlotFlag(PlotFlagIdentifiers.PARENT.getIdentifier(), "member"));
    }
}
