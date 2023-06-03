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
import tv.quaint.stratosphere.world.SkyblockIOBus;

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
        getIslandBufferDistance();
        getIslandAbsoluteSize();
        getIslandDefaultSize();
        getIslandDefaultY();
        getIslandWorldName();

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

    public double getIslandBufferDistance() {
        reloadResource();

        return getResource().getOrSetDefault("island.size.buffer", 512d);
    }

    public double getIslandAbsoluteSize() {
        reloadResource();

        return getResource().getOrSetDefault("island.size.absolute", 1600d);
    }

    public double getIslandDefaultSize() {
        reloadResource();

        return getResource().getOrSetDefault("island.size.default", 50d);
    }

    public int getIslandDefaultY() {
        reloadResource();

        return getResource().getOrSetDefault("island.default-y-height", 150);
    }

    public String getIslandWorldName() {
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

    public void saveCurrentPlotPosition(Location location) {
        if (location == null) return;
        if (location.getWorld() == null) return;

        getResource().set("current-plot.world", location.getWorld().getName());
        getResource().set("current-plot.x", location.getX());
        getResource().set("current-plot.y", location.getY());
        getResource().set("current-plot.z", location.getZ());
        getResource().set("current-plot.yaw", location.getYaw());
        getResource().set("current-plot.pitch", location.getPitch());
    }

    public Location getNextPlotPosition() {
        reloadResource();

        String world = getResource().getOrSetDefault("current-plot.world", getIslandWorldName());
        double x = getResource().getOrSetDefault("current-plot.x", 0.0d);
        double y = getResource().getOrSetDefault("current-plot.y", 0.0d);
        double z = getResource().getOrSetDefault("current-plot.z", 0.0d);
        float yaw = getResource().getOrSetDefault("current-plot.yaw", 0.0f);
        float pitch = getResource().getOrSetDefault("current-plot.pitch", 0.0f);

        World bukkitWorld = SkyblockIOBus.getOrGetSkyblockWorld();
        if (bukkitWorld == null) return null;
        bukkitWorld.getWorldBorder().setCenter(0, 0);
        bukkitWorld.getWorldBorder().setSize(30000000);

        Location currentLocation = new Location(bukkitWorld, x, y, z, yaw, pitch);
        Location nextLocation = currentLocation.clone();

        int x_add = (int) (Math.ceil(getIslandAbsoluteSize() + getIslandBufferDistance()));
        int z_add = (int) (Math.ceil(getIslandAbsoluteSize() + getIslandBufferDistance()));

        double x_next = nextLocation.getX() + x_add;
        double z_next = nextLocation.getZ();

        if (x_next > 1000000d) {
            x_next = 0;
            z_next = z_next + z_add;
            if (z_next > 1000000d) {
                z_next = -1000000d;
            }
        }

        if (z_next > 1000000d) {
            z_next = 0;
        }

        nextLocation.setX(x_next);
        nextLocation.setZ(z_next);

        return nextLocation;
    }
}
