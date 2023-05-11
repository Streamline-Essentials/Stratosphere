package tv.quaint.stratosphere.world;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.savables.SavableResource;
import org.bukkit.Bukkit;
import org.bukkit.World;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.storage.documents.SimpleJsonDocument;

import java.io.File;

public class SkyblockWorld extends SavableResource {
    public static class SkyblockWorldSerializer extends SimpleJsonDocument {
        public SkyblockWorldSerializer(String identifier) {
            super((identifier.endsWith(".json") ? identifier : identifier + ".json"), SkyblockIOBus.getInstanceFolder(), false);
        }

        @Override
        public void onInit() {

        }

        @Override
        public void onSave() {

        }

        public void rename(String uuid) {
            getSelfFile().renameTo(new File(SkyblockIOBus.getInstanceFolder(), uuid + ".json"));
        }
    }

    public enum WorldType {
        NORMAL,
        NETHER,
        END,
        ;

        public org.bukkit.WorldType toBukkitType() {
            try {
                return org.bukkit.WorldType.valueOf(this.name());
            } catch (Exception e) {
                return org.bukkit.WorldType.NORMAL;
            }
        }

        public static WorldType fromString(String string) {
            for (WorldType worldType : values()) {
                if (worldType.name().equalsIgnoreCase(string)) {
                    return worldType;
                }
            }
            return WorldType.NORMAL;
        }

        public static WorldType fromBukkitType(org.bukkit.WorldType bukkitWorldType) {
            for (WorldType worldType : values()) {
                if (worldType.name().equalsIgnoreCase(bukkitWorldType.name())) {
                    return worldType;
                }
            }
            return WorldType.NORMAL;
        }
    }

    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private String worldName;
    @Getter @Setter
    private WorldType worldType;
    @Getter @Setter
    private File dataFolder;

    public SkyblockWorld(String identifier, String worldName, File dataFolder, WorldType worldType) {
        super(identifier, new SkyblockWorldSerializer(identifier));

        Stratosphere.getInstance().logDebug("Creating new SkyblockWorld with identifier: " + identifier);

        this.identifier = identifier;
        this.worldName = worldName;
        this.dataFolder = dataFolder;
        this.worldType = worldType;

        Stratosphere.getInstance().logDebug("Created: " + identifier);
        Stratosphere.getInstance().logDebug("Saving new SkyblockWorld with identifier: " + identifier);

        saveAll();
    }

    public SkyblockWorld(String identifier) {
        super(identifier, new SkyblockWorldSerializer(identifier));

        this.identifier = identifier;

        forceReload();
    }

    public SkyblockWorld(String identifier, World world) {
        this(identifier, world.getName(), world.getWorldFolder(), WorldType.fromString(world.getWorldType().name()));
    }

    public void forceReload() {
        this.worldName = getStorageResource().get("worldName", String.class);
        this.worldType = WorldType.valueOf(getStorageResource().get("worldType", String.class));
        this.dataFolder = new File(getStorageResource().get("dataFolder", String.class));
    }

    @Override
    public void populateDefaults() {
//        worldName = getOrSetDefault("worldName", worldName);
//        worldType = WorldType.valueOf(getOrSetDefault("worldType", worldType.name()));
//        dataFolder = new File(getOrSetDefault("dataFolder", dataFolder.getAbsolutePath()));
    }

    public <V> void write(String key, V value) {
        getStorageResource().write(key, value);
    }

    @Override
    public void loadValues() {

    }

    @Override
    public void saveAll() {
        write("worldName", worldName);
        write("worldType", worldType.name());
        write("dataFolder", dataFolder.getAbsolutePath());
    }

    public File getPackedFolder() {
        File folder = new File(SkyblockIOBus.getPackingFolder(), getWorldName());

        if (! folder.exists()) folder.mkdirs();

        return folder;
    }

    public World getWorld() {
        World world = Bukkit.getWorld(getWorldName());

        if (world == null) {
            Stratosphere.getInstance().logSevere("World " + getWorldName() + " is null for world " + getIdentifier() + " and world name " + getWorldName() + "!");
        }

        return world;
    }

    public void delete() {
        getWorld().getWorldFolder().delete();
        getPackedFolder().delete();

        Bukkit.unloadWorld(getWorld(), false);

        getStorageResource().delete();

        SkyblockIOBus.unloadSkyblockWorld(this);
    }

    public int playersOnline() {
        return getWorld().getPlayers().size();
    }

    public void shutdown() {
        SkyblockIOBus.packWorld(this.identifier, this.getWorld());
    }

    public static String getWorldName(String worldName) {
        if (! worldName.contains(File.separator)) return worldName;
        else return worldName.substring(worldName.lastIndexOf(File.separator) + 1);
    }
}
