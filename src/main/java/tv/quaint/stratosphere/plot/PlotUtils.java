package tv.quaint.stratosphere.plot;

import mc.obliviate.inventory.InventoryAPI;
import tv.quaint.stratosphere.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.config.MetaDataConfig;
import tv.quaint.stratosphere.plot.quests.QuestContainer;
import tv.quaint.stratosphere.plot.schematic.tree.SchemTree;
import tv.quaint.stratosphere.plot.upgrades.PlotUpgrade;
import tv.quaint.stratosphere.users.SkyblockUser;
import tv.quaint.stratosphere.world.SkyblockIOBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PlotUtils {
    @Getter @Setter
    private static ConcurrentSkipListMap<Date, Location> polledLocations = new ConcurrentSkipListMap<>();

    public static void initImmediately() {
        new InventoryAPI(Stratosphere.getInstance()).init();
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<SkyblockPlot> plots = new ConcurrentSkipListSet<>();

    public static void loadPlot(SkyblockPlot plot) {
        plots.add(plot);
    }

    public static void unloadPlot(String uuid) {
        getPlots().forEach(plot -> {
            if (plot.getUuid().equals(uuid)) getPlots().remove(plot);
        });
    }

    public static void unloadPlot(UUID uuid) {
        unloadPlot(uuid.toString());
    }

    public static void unloadPlot(SkyblockPlot plot) {
        unloadPlot(UUID.fromString(plot.getUuid()));
    }

    public static ConcurrentSkipListMap<String, SkyblockPlot> getPlotsByUUIDs() {
        ConcurrentSkipListMap<String, SkyblockPlot> r = new ConcurrentSkipListMap<>();

        getPlots().forEach(plot -> {
            r.put(plot.getUuid(), plot);
        });

        return r;
    }

    private static SkyblockPlot getPlot(String uuid) {
        return getPlotsByUUIDs().get(uuid);
    }

    private static SkyblockPlot getPlotByMember(Player player) {
        AtomicReference<SkyblockPlot> plot = new AtomicReference<>(null);

        getPlots().forEach(p -> {
            if (plot.get() != null) return;

            if (p.isMember(player)) {
                if (p.getMember(player).getRole().getIdentifier().equals("trusted")) return;
                plot.set(p);
            }
        });

        return plot.get();
    }

    private static SkyblockPlot getPlot(UUID uuid) {
        return getPlot(uuid.toString());
    }

    public static SkyblockPlot getOrGetPlot(Player player) {
        SkyblockPlot plot = getPlotByMember(player);
        if (plot != null) return plot;

        SkyblockUser user = getOrGetUser(player.getUniqueId().toString());
        String plotUuid = user.getPlotUuid();

        if (plotUuid == null || plotUuid.isEmpty()) return null;

        return getOrGetPlot(plotUuid);
    }

    public static SkyblockPlot getOrGetPlot(String uuid) {
        SkyblockPlot plot = getPlot(uuid);
        if (plot != null) return plot;

        if (! plotFileExists(uuid)) return null;

        plot = new SkyblockPlot(uuid);
        loadPlot(plot);

        return plot;
    }

    public static boolean plotFileExists(String uuid) {
        File[] files = SkyblockIOBus.getPlotFolder().listFiles();

        if (files == null) return false;

        for (File file : files) {
            if (file.getName().equals(uuid + ".json")) return true;
        }

        return false;
    }

    public static boolean userFileExists(String uuid) {
        File[] files = SkyblockIOBus.getUsersFolder().listFiles();

        if (files == null) return false;

        for (File file : files) {
            if (file.getName().equals(uuid + ".json")) return true;
        }

        return false;
    }

    public static SkyblockPlot getOrGetPlot(SkyblockUser user) {
        String plotUuid = user.getPlotUuid();

        if (plotUuid == null || plotUuid.isEmpty()) return null;

        return getOrGetPlot(plotUuid);
    }

    public static boolean isPlotLoaded(SkyblockPlot plot) {
        return isPlotLoaded(plot.getUuid());
    }

    public static boolean isPlotLoaded(String uuid) {
        return getPlotsByUUIDs().containsKey(uuid);
    }

    public static boolean isPlotLoaded(UUID uuid) {
        return isPlotLoaded(uuid.toString());
    }

    public static SkyblockPlot createPlot(SkyblockUser owner, String schemTreeName) {
        owner.sendMessage("&bCreating plot...");

        SchemTree schemTree = Stratosphere.getMyConfig().getSchematicTree(schemTreeName);

        MessageUtils.logDebug("SchemTreeName: " + schemTreeName);

        if (schemTree == null) {
            owner.sendMessage("&cSchematic set not found!");
            return null;
        }

        SkyblockUser user = getOrGetUser(owner.getUuid());

        Location nextPlotLocation = Stratosphere.getMyConfig().getNextPlotPosition();
        if (nextPlotLocation == null) {
            World skyblockWorld = SkyblockIOBus.getOrGetSkyblockWorld();
            int plotDefaultHeight = Stratosphere.getMyConfig().getIslandDefaultY();
            nextPlotLocation = new Location(skyblockWorld, 0, plotDefaultHeight, 0);
        }

        SkyblockPlot plot = new SkyblockPlot(owner.getUuid(), schemTree, nextPlotLocation);

        Stratosphere.getMyConfig().saveCurrentPlotPosition(nextPlotLocation);

        user.setPlotUuid(plot.getUuid());
        user.saveAll();

        loadPlot(plot);

        Stratosphere.getMyConfig().incrementAbsolutePlotsAmount(1);

        return plot;
    }

    public static SkyblockPlot getPlotByLocation(Location location) {
        World locationWorld = location.getWorld();
        if (locationWorld == null) return null;
        if (! locationWorld.getName().equals(Stratosphere.getMyConfig().getIslandWorldName())) return null;

        AtomicReference<SkyblockPlot> plot = new AtomicReference<>(null);

        getPlots().forEach(p -> {
            if (plot.get() != null) return;

            if (p.isInPlot(location)) plot.set(p);
        });

        if (plot.get() == null) {
            SkyblockPlot p = Stratosphere.getPlotPosConfig().getPlotAt(location);

            if (p != null) {
                plot.set(p);
                if (! p.isLoaded()) p.load();
            }
        }

        return plot.get();
    }

    public static ConcurrentSkipListSet<SkyblockPlot> getPlotsFromFiles() {
        return getPlotsFromFiles(false);
    }

    public static ConcurrentSkipListSet<SkyblockPlot> getPlotsFromFiles(boolean load) {
        ConcurrentSkipListSet<SkyblockPlot> plots = new ConcurrentSkipListSet<>();

        File[] files = SkyblockIOBus.getPlotFolder().listFiles();
        if (files == null) return plots;

        for (File file : files) {
            if (! file.exists()) continue;
            if (! file.getName().endsWith(".json")) continue;

            String uuid = file.getName().replace(".json", "");

            SkyblockPlot plot = getPlot(uuid);
            if (plot != null) continue;

            plot = new SkyblockPlot(uuid);
            plots.add(plot);

            if (load) if (! plot.isLoaded()) plot.load();
        }

        return plots;
    }

    @Getter @Setter
    private static ConcurrentSkipListSet<SkyblockUser> loadedUsers = new ConcurrentSkipListSet<>();

    public static void loadUser(SkyblockUser user) {
        loadedUsers.add(user);
    }

    public static void unloadUser(String uuid) {
        getLoadedUsers().removeIf(user -> user.getUuid().equals(uuid));
    }

    private static SkyblockUser getUser(String uuid) {
        AtomicReference<SkyblockUser> user = new AtomicReference<>(null);

        getLoadedUsers().forEach(u -> {
            if (user.get() != null) return;

            if (u.getUuid().equals(uuid)) user.set(u);
        });

        return user.get();
    }

    public static SkyblockUser getOrGetUser(String mightBeUuid) {
        String uuid = "";
        if (mightBeUuid.contains("-")) uuid = mightBeUuid;
        else uuid = Bukkit.getOfflinePlayer(mightBeUuid).getUniqueId().toString();

        if (uuid == null || uuid.isEmpty() || uuid.isBlank()) return null;

        SkyblockUser user = getUser(uuid);
        if (user != null) return user;

        if (! userFileExists(uuid)) return null;

        user = new SkyblockUser(uuid);

        loadUser(user);

        return user;
    }

    public static boolean isUserLoaded(String uuid) {
        return getUser(uuid) != null;
    }

    public static void loadAllUsers() {
        File[] files = SkyblockIOBus.getUsersFolder().listFiles();
        if (files == null) return;

        for (File file : files) {
            if (! file.exists()) return;
            if (! file.getName().endsWith(".json")) return;

            String uuid = file.getName().replace(".json", "");

            SkyblockUser user = getOrGetUser(uuid);
            if (user != null) user.load();
        }
    }

    public static void loadAllPlots() {
        File[] files = SkyblockIOBus.getPlotFolder().listFiles();
        if (files == null) return;

        for (File file : files) {
            if (! file.exists()) return;
            if (! file.getName().endsWith(".json")) return;

            String uuid = file.getName().replace(".json", "");

            SkyblockPlot plot = getOrGetPlot(uuid);
            if (! plot.isLoaded()) plot.load();
        }
    }

    public static void restoreAllPlotsToLatestSnapshot() {
        getPlots().forEach(SkyblockPlot::restoreToLatestSnapshot);
    }

    public static QuestContainer getOrGetQuester(String uuid) {
        ConcurrentSkipListSet<QuestContainer> containers = MetaDataConfig.getQuesters();

        AtomicReference<QuestContainer> container = new AtomicReference<>(null);

        containers.forEach(c -> {
            if (container.get() != null) return;

            if (c.getIdentifier().equals(uuid)) container.set(c);
        });

        if (container.get() != null) return container.get();

        QuestContainer questContainer = new QuestContainer(uuid);
        questContainer.save();

        MetaDataConfig.getQuesters().add(questContainer);

        return questContainer;
    }

    public static int getNextUpgradeTier(PlotUpgrade.UpgradeType type) {
        AtomicInteger tier = new AtomicInteger(0);

        Stratosphere.getUpgradeConfig().getLoadedUpgrades().forEach(upgrade -> {
            if (upgrade.getType() != type) return;
            if (upgrade.getTier() <= tier.get()) return;

            tier.set(upgrade.getTier());
        });

        return tier.get() + 1;
    }
}
