package tv.quaint.stratosphere.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.schematic.SkyblockSchematic;
import tv.quaint.stratosphere.plot.schematic.tree.BranchType;
import tv.quaint.stratosphere.plot.schematic.tree.SchemBranch;
import tv.quaint.stratosphere.plot.schematic.tree.SchemTree;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class MyConfig extends SimpleConfiguration {
    @Getter @Setter
    public ConcurrentSkipListSet<SchemTree> schematicTrees;

    public MyConfig() {
        super("config.yml", Stratosphere.getInstance(), false);

        reloadTheConfig();
    }

    @Override
    public void init() {
        // Database Stuff.
        getConnectionURI();
        getTablePrefix();
        getSaveIntervalInTicks();

        // Island Stuff.
        getIslandAbsoluteSize();
        getIslandFolderName();

        // Schematic Stuff.
        write("schematics.default.normal", "normal");
        write("schematics.default.nether", "nether");
        write("schematics.default.end", "end");
        schematicTrees = new ConcurrentSkipListSet<>();
        schematicTrees = getSchematicTreesFromConfig();

        // Spawn Stuff.
        getSpawnLocation();
    }

    public void reloadTheConfig() {
        reloadResource(true);

        schematicTrees = getSchematicTreesFromConfig();
    }

    public String getConnectionURI() {
        reloadResource();

        return getResource().getOrSetDefault("database.uri", "jdbc:SQLITE:plugins/StreamlineCore/module-resources/stratosphere/stratosphere.db");
    }

    public String getTablePrefix() {
        reloadResource();

        return getResource().getOrSetDefault("database.tablePrefix", "skyhigh_");
    }

    public int getSaveIntervalInTicks() {
        reloadResource();

        return getResource().getOrSetDefault("database.saveIntervalInTicks", 20 * 60 * 5);
    }

    public int getIslandBufferDistance() {
        reloadResource();

        return getResource().getOrSetDefault("island.size.buffer", 512);
    }

    public int getIslandAbsoluteSize() {
        reloadResource();

        return getResource().getOrSetDefault("island.size.absolute", 1600);
    }

    public String getIslandFolderName() {
        reloadResource();

        return getResource().getOrSetDefault("island.world.name", "Stratosphere");
    }

    public ConcurrentSkipListSet<SchemTree> getSchematicTreesFromConfig() {
        reloadResource();
        ConcurrentSkipListSet<SchemTree> schemTrees = new ConcurrentSkipListSet<>();

        getResource().singleLayerKeySet("schematics").forEach(schem -> {
            SkyblockSchematic normalSchem;
            SkyblockSchematic netherSchem;
            SkyblockSchematic endSchem;

            String normalString = getResource().getOrSetDefault("schematics." + schem + ".normal", "normal");
            String netherSting = getResource().getOrSetDefault("schematics." + schem + ".nether", "nether");
            String endString = getResource().getOrSetDefault("schematics." + schem + ".end", "end");

            try {
                normalSchem = new SkyblockSchematic(normalString);
                if (normalSchem == null) return;
            } catch (Exception e) {
                return;
            }

            try {
                netherSchem = new SkyblockSchematic(netherSting);
                if (netherSchem == null) return;
            } catch (Exception e) {
                return;
            }

            try {
                endSchem = new SkyblockSchematic(endString);
                if (endSchem == null) return;
            } catch (Exception e) {
                return;
            }

            SchemBranch normalBranch = new SchemBranch(normalSchem, BranchType.NORMAL);
            SchemBranch netherBranch = new SchemBranch(netherSchem, BranchType.NETHER);
            SchemBranch endBranch = new SchemBranch(endSchem, BranchType.END);

            SchemTree schemTree = new SchemTree(schem, normalBranch, netherBranch, endBranch);

            schemTrees.add(schemTree);
        });

        return schemTrees;
    }

    public SchemTree getSchematicTree(String identifier) {
        AtomicReference<SchemTree> schemTree = new AtomicReference<>(null);

        getSchematicTrees().forEach(tree -> {
            if (schemTree.get() != null) return;

            if (tree.getIdentifier().equalsIgnoreCase(identifier)) {
                schemTree.set(tree);
            }
        });

        return schemTree.get();
    }

    public Location getSpawnLocation() {
        reloadResource();

        String world = getResource().getOrSetDefault("spawn.world", "world");
        double x = getResource().getOrSetDefault("spawn.x", 0.0);
        double y = getResource().getOrSetDefault("spawn.y", 0.0);
        double z = getResource().getOrSetDefault("spawn.z", 0.0);
        float yaw = getResource().getOrSetDefault("spawn.yaw", 0.0f);
        float pitch = getResource().getOrSetDefault("spawn.pitch", 0.0f);

        World bukkitWorld = Bukkit.getWorld(world);

        if (bukkitWorld == null) {
            return null;
        }

        return new Location(bukkitWorld, x, y, z, yaw, pitch);
    }

    public void saveSpawnLoaction(Location location) {
        getResource().set("spawn.world", location.getWorld().getName());
        getResource().set("spawn.x", location.getX());
        getResource().set("spawn.y", location.getY());
        getResource().set("spawn.z", location.getZ());
        getResource().set("spawn.yaw", location.getYaw());
        getResource().set("spawn.pitch", location.getPitch());
    }
}
