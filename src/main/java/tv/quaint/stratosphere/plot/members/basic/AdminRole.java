package tv.quaint.stratosphere.plot.members.basic;

import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.flag.PlotFlag;
import tv.quaint.stratosphere.plot.members.PlotRole;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;
import tv.quaint.stratosphere.plot.pos.PlotFlagType;

public class AdminRole extends PlotRole {
    public AdminRole(SkyblockPlot plot) {
        super("admin", "Admin", plot);

        for (PlotFlagIdentifiers identifier : PlotFlagIdentifiers.values()) {
            if (! identifier.getType().equals(PlotFlagType.PLAYER)) return;
            if (identifier == PlotFlagIdentifiers.PERMISSION_EDIT) return;

            addFlag(new PlotFlag(identifier.getIdentifier(), true));
        }

        addFlag(new PlotFlag(PlotFlagIdentifiers.PARENT.getIdentifier(), "member"));
    }
}
