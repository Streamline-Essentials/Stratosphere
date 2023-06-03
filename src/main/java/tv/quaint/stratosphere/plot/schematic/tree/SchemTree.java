package tv.quaint.stratosphere.plot.schematic.tree;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;
import tv.quaint.stratosphere.plot.schematic.SkyblockSchematic;

import javax.annotation.Nullable;

public class SchemTree implements Identifiable {
    @Getter @Setter
    private String identifier;
    @Setter @Nullable
    private SchemBranch normal;
    @Setter @Nullable
    private SchemBranch nether;
    @Setter @Nullable
    private SchemBranch end;

    public SchemTree(String identifier, @Nullable SchemBranch normal, @Nullable SchemBranch nether, @Nullable SchemBranch end) {
        this.identifier = identifier;
        this.normal = normal;
        this.nether = nether;
        this.end = end;
    }

    public SkyblockSchematic getNormal() {
        SchemBranch schemBranch = normal;
        if (schemBranch == null) {
            schemBranch = nether;
            if (schemBranch == null) {
                schemBranch = end;
                if (schemBranch == null) {
                    return null;
                }
            }
        }

        return schemBranch.getSchemMap();
    }

    public SkyblockSchematic getNether() {
        SchemBranch schemBranch = nether;
        if (schemBranch == null) {
            schemBranch = normal;
            if (schemBranch == null) {
                schemBranch = end;
                if (schemBranch == null) {
                    return null;
                }
            }
        }

        return schemBranch.getSchemMap();
    }

    public SkyblockSchematic getEnd() {
        SchemBranch schemBranch = end;
        if (schemBranch == null) {
            schemBranch = normal;
            if (schemBranch == null) {
                schemBranch = nether;
                if (schemBranch == null) {
                    return null;
                }
            }
        }

        return schemBranch.getSchemMap();
    }
}
