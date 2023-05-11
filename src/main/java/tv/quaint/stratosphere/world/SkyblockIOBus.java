package tv.quaint.stratosphere.world;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.SessionOwner;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.flag.PlotFlag;
import tv.quaint.stratosphere.plot.schematic.SkyblockSchematic;
import tv.quaint.stratosphere.users.SkyblockUser;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class SkyblockIOBus {
    public static File getPackingFolder() {
        File folder = new File(Stratosphere.getInstance().getDataFolder(), "packed");

        if (! folder.exists()) folder.mkdirs();

        return folder;
    }

    public static File getInstanceFolder() {
        File folder = new File(Stratosphere.getInstance().getDataFolder(), "world");

        if (! folder.exists()) folder.mkdirs();

        return folder;
    }

    public static File getPlotFolder() {
        File folder = new File(Stratosphere.getInstance().getDataFolder(), "plots");

        if (! folder.exists()) folder.mkdirs();

        return folder;
    }

    public static SkyblockWorld packWorld(String identifier, World world) {
        Bukkit.unloadWorld(world, true); // Unload the world (true = save)
        File folder = world.getWorldFolder();

        SkyblockWorld skyblockWorld = new SkyblockWorld(identifier, world);
        File newFolder = skyblockWorld.getPackedFolder();
        if (newFolder.exists()) newFolder.delete();

        try {
            folder.renameTo(newFolder);
        } catch (Exception e) {
            if (! e.getMessage().endsWith("not empty")) {
                e.printStackTrace();
            }
        }

        return skyblockWorld;
    }

    public static void unpackWorld(SkyblockWorld world) {
        try {
            File folder = world.getPackedFolder();
            File newFolder = getWorldUnpackedFolder(folder);

            if (newFolder.exists()) newFolder.delete();

            try {
                folder.renameTo(newFolder);
            } catch (Exception e) {
                if (! e.getMessage().endsWith("not empty")) {
                    e.printStackTrace();
                }
            }

            World w = Bukkit.getWorld(world.getWorldName());

            if (w == null) throw new Exception("World is null!");

            w.loadChunk(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getWorldUnpackedFolder(File packedFolder) {
        return new File(Bukkit.getWorldContainer(), packedFolder.getName());
    }

    public static World createSkyblockWorldAsWorld(String worldName, SkyblockWorld.WorldType worldType) {
        Stratosphere.getInstance().logDebug("Creating world " + worldName + " of type " + worldType.name());

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.type(worldType.toBukkitType());

        Stratosphere.getInstance().logDebug("Setting generator");

        ChunkGenerator generator = new SkyblockChunkGenerator();
        worldCreator.generator(generator);

        Stratosphere.getInstance().logDebug("Creating world");

        return worldCreator.createWorld();
    }

    public static SkyblockWorld createSkyblockWorld(String identifier, String worldName, SkyblockWorld.WorldType worldType) {
        Stratosphere.getInstance().logDebug("Creating skyblock world " + identifier + " with name " + worldName + " and type " + worldType.name());

        try {
            World world = createSkyblockWorldAsWorld(worldName, worldType);

            if (world == null) return null;

            Stratosphere.getInstance().logDebug("Creating skyblock world object");
            return new SkyblockWorld(identifier, world);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<SkyblockWorld> loadedSkyblockWorlds = new ConcurrentSkipListSet<>();

    public static void loadSkyblockWorld(SkyblockWorld skyblockWorld, boolean firstLoad) {
        if (loadedSkyblockWorlds.contains(skyblockWorld)) return;

        if (! firstLoad) unpackWorld(skyblockWorld);
        loadedSkyblockWorlds.add(skyblockWorld);
    }

    public static void loadSkyblockWorld(SkyblockWorld skyblockWorld) {
        loadSkyblockWorld(skyblockWorld, false);
    }


    public static void unloadSkyblockWorld(String identifier) {
        getLoadedSkyblockWorlds().removeIf(w -> w.getIdentifier().equals(identifier));
    }

    public static void unloadSkyblockWorld(SkyblockWorld skyblockWorld) {
        if (! loadedSkyblockWorlds.contains(skyblockWorld)) return;

        try {
            World world = Bukkit.getWorld(skyblockWorld.getIdentifier());
            SkyblockWorld packed = packWorld(skyblockWorld.getIdentifier(), world);
        } catch (Exception e) {
            // do nothing
        }

        unloadSkyblockWorld(skyblockWorld.getIdentifier());
    }

    private static SkyblockWorld getSkyblockWorld(String identifier) {
        AtomicReference<SkyblockWorld> world = new AtomicReference<>();

        loadedSkyblockWorlds.forEach(w -> {
            if (w.getIdentifier().equals(identifier)) {
                world.set(w);
            }
        });

        return world.get();
    }

    public static SkyblockWorld getOrGetSkyblockWorld(String identifier) {
        return getOrGetSkyblockWorld(identifier, SkyblockWorld.WorldType.NORMAL);
    }

    public static boolean skyblockWorldExists(String identifier) {
        File[] files = getInstanceFolder().listFiles();
        if (files == null) return false;

        for (File file : files) {
            if (file.getName().equals(identifier + ".json")) return true;
        }

        return false;
    }

    public static SkyblockWorld getOrGetSkyblockWorld(String identifier, SkyblockWorld.WorldType worldType) {
        Stratosphere.getInstance().logDebug("Getting or creating world " + identifier + " with type " + worldType.name());

        SkyblockWorld world = getSkyblockWorld(identifier);
        if (world != null) {
            Stratosphere.getInstance().logDebug("World " + identifier + " already loaded, returning.");
            return world;
        }

        boolean exists = skyblockWorldExists(identifier);
        Stratosphere.getInstance().logDebug("World " + identifier + " exists? " + exists);
        if (! exists) {
            Stratosphere.getInstance().logDebug("World " + identifier + " does not exist, creating.");

            world = createSkyblockWorld(identifier, UUID.randomUUID().toString(), worldType);
            if (world == null) {
                Stratosphere.getInstance().logDebug("World " + identifier + " could not be created, returning null.");
                return null;
            }

            loadSkyblockWorld(world, true);

            Stratosphere.getInstance().logDebug("World " + identifier + " created, returning.");
        }
        else {
            Stratosphere.getInstance().logDebug("World " + identifier + " exists, loading.");

            world = new SkyblockWorld(identifier);
            loadSkyblockWorld(world);

            Stratosphere.getInstance().logDebug("World " + identifier + " loaded, returning.");
        }

        return world;
    }

    private static ConcurrentSkipListSet<PlotFlag> plotFlags = new ConcurrentSkipListSet<>();

    public static void registerPlotFlag(PlotFlag plotFlag) {
        plotFlags.add(plotFlag);
    }

    public static PlotFlag getPlotFlag(String name) {
        for (PlotFlag plotFlag : plotFlags) {
            if (plotFlag.getIdentifier().equalsIgnoreCase(name)) {
                return plotFlag;
            }
        }
        return null;
    }

    public static File getSchematicsFolder() {
        File folder = new File(Stratosphere.getInstance().getDataFolder(), "schematics");

        if (! folder.exists()) folder.mkdirs();

        return folder;
    }

    public static File getUsersFolder() {
        File folder = new File(Stratosphere.getInstance().getDataFolder(), "users");

        if (! folder.exists()) folder.mkdirs();

        return folder;
    }

    public static SkyblockSchematic getSchematic(String identifier) {
        try {
            return new SkyblockSchematic(identifier);
        } catch (Exception e) {
            return null;
        }
    }

    public static Clipboard getClipboard(Player player) {
        SessionOwner sessionOwner = WorldEditPlugin.getInstance().wrapPlayer(player);
        return WorldEdit.getInstance().getSessionManager().get(sessionOwner).getClipboard().getClipboard();
    }

    public static SkyblockSchematic fabricate(Player player, String name) {
        Clipboard clipboard = getClipboard(player);

        if (clipboard == null) return null;

        return new SkyblockSchematic(name, clipboard);
    }

    public static void reload() {
        Stratosphere.getMyConfig().reloadTheConfig();

        PlotUtils.getLoadedUsers().forEach(SkyblockUser::forceReload);
        PlotUtils.getPlots().forEach(SkyblockPlot::forceReload);
        getLoadedSkyblockWorlds().forEach(SkyblockWorld::forceReload);
    }
}
