package tv.quaint.stratosphere.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import tv.quaint.storage.StorageUtils;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.schematic.SkyblockSchematic;
import tv.quaint.stratosphere.plot.schematic.tree.BranchType;
import tv.quaint.stratosphere.plot.schematic.tree.SchemBranch;
import tv.quaint.stratosphere.plot.schematic.tree.SchemTree;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;
import tv.quaint.stratosphere.world.SkyblockIOBus;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MyConfig extends SimpleConfiguration {
    @Getter @Setter
    public ConcurrentSkipListSet<SchemTree> schematicTrees;

    public MyConfig() {
        super("config.yml", Stratosphere.getInstance(), true);
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
        getCurrentPlotCount();
        getIslandLevellingEquation();
        getNetherUnlockCost();
        getEndUnlockCost();

        // Schematic Stuff.
        ensureDefaultSchematics();
        schematicTrees = new ConcurrentSkipListSet<>();
        schematicTrees = getSchematicTreesFromConfig();

        // Spawn Stuff.
        getSpawnLocation();
    }

    public void reloadTheConfig() {
        reloadResource(true);

        // Schematic Stuff.
        ensureDefaultSchematics();
        schematicTrees = getSchematicTreesFromConfig();
    }

    public String getConnectionURI() {
        return getResource().getOrSetDefault("database.uri", "jdbc:SQLITE:plugins/StreamlineCore/module-resources/stratosphere/stratosphere.db");
    }

    public String getTablePrefix() {
        return getResource().getOrSetDefault("database.tablePrefix", "skyhigh_");
    }

    public int getSaveIntervalInTicks() {
        return getResource().getOrSetDefault("database.saveIntervalInTicks", 20 * 60 * 5);
    }

    public double getIslandBufferDistance() {
        return getResource().getOrSetDefault("island.size.buffer", 512d);
    }

    public double getIslandAbsoluteSize() {
        return getResource().getOrSetDefault("island.size.absolute", 1600d);
    }

    public double getIslandDefaultSize() {
        return getResource().getOrSetDefault("island.size.default", 50d);
    }

    public int getIslandDefaultY() {
        return getResource().getOrSetDefault("island.default-y-height", 150);
    }

    public String getIslandWorldName() {
        return getResource().getOrSetDefault("island.world.name", "Stratosphere");
    }

    public ConcurrentSkipListSet<SchemTree> getSchematicTreesFromConfig() {
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

    public Location getCurrentPlotPosition() {
        String world = getResource().getOrSetDefault("current-plot.world", getIslandWorldName());
        double x = getResource().getOrSetDefault("current-plot.x", 0.0d);
        double y = getResource().getOrSetDefault("current-plot.y", 0.0d);
        double z = getResource().getOrSetDefault("current-plot.z", 0.0d);
        float yaw = getResource().getOrSetDefault("current-plot.yaw", 0.0f);
        float pitch = getResource().getOrSetDefault("current-plot.pitch", 0.0f);

        World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) return null;
        bukkitWorld.getWorldBorder().setCenter(0, 0);
        bukkitWorld.getWorldBorder().setSize(30000000);

        return new Location(bukkitWorld, x, y, z, yaw, pitch);
    }

    public int getCurrentPlotCount() {
        return getResource().getOrSetDefault("island.current-count", 0);
    }

    public int setAbsolutePlotsAmount(int amount) {
        getResource().set("island.current-count", amount);

        return amount;
    }

    public int incrementAbsolutePlotsAmount(int amount) {
        int i = getCurrentPlotCount();

        i += amount;

        return setAbsolutePlotsAmount(i);
    }

    public int getAbsolutePlotsFilesAmount() {
        File[] files = SkyblockIOBus.getPlotFolder().listFiles();

        AtomicInteger amount = new AtomicInteger(0);

        if (files == null) return amount.get();

        Arrays.stream(files).forEach(file -> {
            if (file.isDirectory()) return;
            if (! file.getName().endsWith(".json")) return;

            amount.getAndIncrement();
        });

        return amount.get();
    }

    public Location getNextPlotPosition(int from) {
        double x = 0d;
        int y = getIslandDefaultY();
        double z = 0d;

        for (int i = 0; i < from; i ++) {
            x = x + (int) (Math.ceil(getIslandAbsoluteSize() + getIslandBufferDistance()));
            if (x > 10000000d) {
                x = 0d;
                z = z + (int) (Math.ceil(getIslandAbsoluteSize() + getIslandBufferDistance()));
                if (z > 10000000d) {
                    x = -10000000d;
                    z = -10000000d;
                }
            }
        }

        return new Location(SkyblockIOBus.getOrGetSkyblockWorld(), x, y, z);
    }

    public Location getNextPlotPosition() {
        int amount = getCurrentPlotCount();

        return getNextPlotPosition(amount);
    }

    public boolean doesSchemExist(String filename) {
        if (filename == null) return false;
        if (! filename.endsWith(".schem")) filename = filename + ".schem";

        File[] files = SkyblockIOBus.getSchematicsFolder().listFiles();

        if (files == null) return false;

        for (File file : files) {
            if (file.isDirectory()) continue;
            if (! file.getName().endsWith(".schem")) continue;

            if (file.getName().equals(filename)) return true;
        }

        return false;
    }

    public void ensureDefaultSchematics() {
        List<String> defaultSchemNames = Arrays.asList(
                "normal",
                "desert",
                "nether",
                "end"
        );

        defaultSchemNames.forEach(name -> {
            if (! doesSchemExist(name)) {
                String filename = name;
                if (! filename.endsWith(".schem")) filename = filename + ".schem";
                File file = new File(SkyblockIOBus.getSchematicsFolder(), filename);

                StorageUtils.ensureFileFromSelf(this.getClass().getClassLoader(), SkyblockIOBus.getSchematicsFolder(), file, filename);
            }
        });
    }

    public String getIslandLevellingEquation() {
        return getResource().getOrSetDefault("island.levelling-equation", "0.001 * %plot_level_real% ^ 2 + 100");
    }

    public int getNetherUnlockCost() {
        return getOrSetDefault("island.unlocks.nether.cost", 1000);
    }

    public int getEndUnlockCost() {
        return getOrSetDefault("island.unlocks.end.cost", 1000);
    }
}
