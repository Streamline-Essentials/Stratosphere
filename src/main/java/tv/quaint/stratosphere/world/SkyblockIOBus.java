package tv.quaint.stratosphere.world;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.SessionOwner;
import tv.quaint.stratosphere.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.flag.PlotFlag;
import tv.quaint.stratosphere.plot.schematic.SkyblockSchematic;
import tv.quaint.stratosphere.users.SkyblockUser;

import java.io.File;
import java.util.concurrent.ConcurrentSkipListSet;

public class SkyblockIOBus {
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

    public static File getPlotSchematicsFolder() {
        File folder = new File(Stratosphere.getInstance().getDataFolder(), "plot-schematics");

        if (! folder.exists()) folder.mkdirs();

        return folder;
    }

    public static World createSkyblockWorld(String worldName, WorldType worldType) {
        MessageUtils.logDebug("Creating world " + worldName + " of type " + worldType.name());

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.type(worldType);

        MessageUtils.logDebug("Setting generator");

        ChunkGenerator generator = new SkyblockChunkGenerator();
        worldCreator.generator(generator);

        MessageUtils.logDebug("Creating world");

        return worldCreator.createWorld();
    }

    public static World createSkyblockWorld(String identifier, String worldName, WorldType worldType) {
        MessageUtils.logDebug("Creating skyblock world " + identifier + " with name " + worldName + " and type " + worldType.name());

        try {
            World world = createSkyblockWorld(worldName, worldType);

            MessageUtils.logDebug("World created!");

            return world;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static World getOrGetSkyblockWorld(String worldName, WorldType worldType) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            world = createSkyblockWorld(worldName, worldType);
        }

        return world;
    }

    public static World createSkyblockNether(String worldName, WorldType worldType) {
        MessageUtils.logDebug("Creating nether world " + worldName + " of type " + worldType.name());

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.type(worldType);
        worldCreator.environment(World.Environment.NETHER);

        MessageUtils.logDebug("Setting nether generator");

        ChunkGenerator generator = new SkyblockChunkGenerator();
        worldCreator.generator(generator);

        MessageUtils.logDebug("Creating nether world");

        return worldCreator.createWorld();
    }

    public static World createSkyblockEnd(String worldName, WorldType worldType) {
        MessageUtils.logDebug("Creating nether world " + worldName + " of type " + worldType.name());

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.type(worldType);
        worldCreator.environment(World.Environment.THE_END);

        MessageUtils.logDebug("Setting nether generator");

        ChunkGenerator generator = new SkyblockChunkGenerator();
        worldCreator.generator(generator);

        MessageUtils.logDebug("Creating nether world");

        return worldCreator.createWorld();
    }

    public static World getOrGetSkyblockWorld(WorldType worldType) {
        String worldName = Stratosphere.getMyConfig().getIslandWorldName();

        return getOrGetSkyblockWorld(worldName, worldType);
    }

    public static World getOrGetSkyblockWorld() {
        return getOrGetSkyblockWorld(WorldType.NORMAL);
    }

    public static World getOrGetSkyblockNether(String worldName, WorldType worldType) {
        if (! worldName.endsWith("_nether")) worldName = worldName + "_nether";

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            world = createSkyblockNether(worldName, worldType);
        }

        return world;
    }

    public static World getOrGetSkyblockNether(WorldType worldType) {
        String worldName = Stratosphere.getMyConfig().getIslandWorldName();

        return getOrGetSkyblockNether(worldName, worldType);
    }

    public static World getOrGetSkyblockNether() {
        return getOrGetSkyblockNether(WorldType.NORMAL);
    }

    public static World getOrGetSkyblockEnd(String worldName, WorldType worldType) {
        if (! worldName.endsWith("_the_end")) worldName = worldName + "_the_end";

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            world = createSkyblockEnd(worldName, worldType);
        }

        return world;
    }

    public static World getOrGetSkyblockEnd(WorldType worldType) {
        String worldName = Stratosphere.getMyConfig().getIslandWorldName();

        return getOrGetSkyblockEnd(worldName, worldType);
    }

    public static World getOrGetSkyblockEnd() {
        return getOrGetSkyblockEnd(WorldType.NORMAL);
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
        Stratosphere.getGeneratorConfig().reloadTheConfig();
        Stratosphere.getQuestConfig().reloadTheConfig();
        Stratosphere.getUpgradeConfig().reloadTheConfig();

        PlotUtils.getLoadedUsers().forEach(SkyblockUser::forceReload);
        PlotUtils.getPlots().forEach(SkyblockPlot::forceReload);
    }
}
