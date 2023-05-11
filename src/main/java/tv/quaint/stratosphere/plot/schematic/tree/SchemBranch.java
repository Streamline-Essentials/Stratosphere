package tv.quaint.stratosphere.plot.schematic.tree;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.stratosphere.plot.schematic.SkyblockSchematic;

public class SchemBranch {
    @Getter @Setter
    private SkyblockSchematic schemMap;
    @Getter @Setter
    private BranchType branchType;

    public SchemBranch(SkyblockSchematic schemMap, BranchType branchType) {
        this.schemMap = schemMap;
        this.branchType = branchType;
    }
}
