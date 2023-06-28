package tv.quaint.stratosphere.plot.schematic;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import tv.quaint.stratosphere.world.SkyblockIOBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SkyblockSchematic {
    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private Clipboard clipboard;

    public SkyblockSchematic(String identifier, Clipboard clipboard) {
        this.identifier = identifier;
        this.clipboard = clipboard;
    }

    public void save() {
        File file = getFile(identifier);

        try (ClipboardWriter writer = BuiltInClipboardFormat.FAST.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return getFile(identifier);
    }

    public boolean delete() {
        return getFile().delete();
    }

    public void paste(@NotNull Location location, boolean ignoreAir) {
        World world = location.getWorld();
        if (world == null) return;

        BlockVector3 vector = BlockVector3.at(location.getX(), location.getY(), location.getZ());

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(world))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(vector)
                    .ignoreAirBlocks(ignoreAir)
                    .build();
            Operations.complete(operation);
        }
    }

    public void paste(@NotNull Location location) {
        paste(location, true);
    }

    public static File getFile(String identifier) {
        if (! identifier.endsWith(".schem")) identifier += ".schem";
        return new File(SkyblockIOBus.getSchematicsFolder(), identifier);
    }

    public static Clipboard read(String identifier) throws IllegalArgumentException {
        File file = getFile(identifier);

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) throw new IllegalArgumentException("Unknown schematic format: " + file.getName());

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public SkyblockSchematic(String identifier) {
        this(identifier, read(identifier));
    }
}
