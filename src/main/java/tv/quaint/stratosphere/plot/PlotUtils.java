package tv.quaint.stratosphere.plot;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.configs.given.CachedUUIDsHandler;
import net.streamline.api.savables.users.StreamlineUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.schematic.tree.SchemTree;
import tv.quaint.stratosphere.plot.upgrades.PlotUpgrade;
import tv.quaint.stratosphere.plot.upgrades.UpgradeRegistry;
import tv.quaint.stratosphere.users.SkyblockUser;

import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class PlotUtils {
    @Getter @Setter
    private static UpgradeRegistry upgradeRegistry;

    public static void init() {
        upgradeRegistry = new UpgradeRegistry();

        for (int i = 1; i <= 10; i++) {
            int finalI = i;
            new PlotUpgrade(PlotUpgrade.Type.MOB_SPAWN_RATE, i, (plot, task) -> {
                plot.setTier(PlotUpgrade.Type.MOB_SPAWN_RATE, finalI);

                plot.messageMembers("&aYour mob spawn rate has been upgraded to tier &f" + finalI + "&a!");

                return true;
            });

            new PlotUpgrade(PlotUpgrade.Type.MOB_SPAWN_CAP, i, (plot, task) -> {
                plot.setTier(PlotUpgrade.Type.MOB_SPAWN_CAP, finalI);

                plot.messageMembers("&aYour mob spawn cap has been upgraded to tier &f" + finalI + "&a!");

                return true;
            });

            new PlotUpgrade(PlotUpgrade.Type.PLOT_SIZE, i, (plot, task) -> {
                plot.setTier(PlotUpgrade.Type.PLOT_SIZE, finalI);

                plot.messageMembers("&aYour plot size has been upgraded to tier &f" + finalI + "&a!");

                return true;
            });

            new PlotUpgrade(PlotUpgrade.Type.PARTY_SIZE, i, (plot, task) -> {
                plot.setTier(PlotUpgrade.Type.PARTY_SIZE, finalI);

                plot.messageMembers("&aYour party size has been upgraded to tier &f" + finalI + "&a!");

                return true;
            });

            new PlotUpgrade(PlotUpgrade.Type.ISLAND_LEVEL, i, (plot, task) -> {
                plot.setTier(PlotUpgrade.Type.ISLAND_LEVEL, finalI);

                plot.messageMembers("&aYour island level has been upgraded to tier &f" + finalI + "&a!");

                return true;
            });

            new PlotUpgrade(PlotUpgrade.Type.ISLAND_LEVEL_CAP, i, (plot, task) -> {
                plot.setTier(PlotUpgrade.Type.ISLAND_LEVEL_CAP, finalI);

                plot.messageMembers("&aYour island level cap has been upgraded to tier &f" + finalI + "&a!");

                return true;
            });
        }

    }

    @Getter @Setter
    private static ConcurrentSkipListSet<SkyblockPlot> plots = new ConcurrentSkipListSet<>();

    public static void addPlot(SkyblockPlot plot) {
        plots.add(plot);
    }

    public static void removePlot(String uuid) {
        getPlots().forEach(plot -> {
            if (plot.getUuid().equals(uuid)) getPlots().remove(plot);
        });
    }

    public static void removePlot(UUID uuid) {
        removePlot(uuid.toString());
    }

    public static void removePlot(SkyblockPlot plot) {
        removePlot(UUID.fromString(plot.getUuid()));
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

            if (p.isMember(player)) plot.set(p);
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

        plot = new SkyblockPlot(uuid, null, false);
        addPlot(plot);

        return plot;
    }

    public static SkyblockPlot getOrGetPlot(SkyblockUser user) {
        String plotUuid = user.getPlotUuid();

        if (plotUuid == null || plotUuid.isEmpty()) return null;

        return getOrGetPlot(plotUuid);
    }

    public static boolean isPlot(String uuid) {
        return getPlotsByUUIDs().containsKey(uuid);
    }

    public static boolean isPlot(UUID uuid) {
        return isPlot(uuid.toString());
    }

    public static SkyblockPlot createPlot(StreamlineUser owner, String schemTreeName) {
        owner.sendMessage("&bCreating plot...");

        SchemTree schemTree = Stratosphere.getMyConfig().getSchematicTree(schemTreeName);

        Stratosphere.getInstance().logDebug("SchemTreeName: " + schemTreeName);

        if (schemTree == null) {
            owner.sendMessage("&cSchematic set not found!");
            return null;
        }

        Stratosphere.getInstance().logDebug("SchemTree: " + schemTree);

        SkyblockUser user = getOrGetUser(owner.getUuid());
        Stratosphere.getInstance().logDebug("User: " + user);
        SkyblockPlot plot = new SkyblockPlot(owner.getUuid(), schemTree, true);

        user.setPlotUuid(plot.getUuid());
        user.saveAll();

        addPlot(plot);

        return plot;
    }

    public static SkyblockPlot getPlotByLocation(Location location) {
        AtomicReference<SkyblockPlot> plot = new AtomicReference<>(null);

        getPlots().forEach(p -> {
            if (plot.get() != null) return;

            if (p.isInPlot(location)) plot.set(p);
        });

        return plot.get();
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
        if (mightBeUuid.length() == 32 && mightBeUuid.contains("-")) uuid = mightBeUuid;
        else uuid = CachedUUIDsHandler.getCachedUUID(mightBeUuid);

        if (uuid == null || uuid.isEmpty() || uuid.isBlank()) return null;

        SkyblockUser user = getUser(uuid);
        if (user != null) return user;

        user = new SkyblockUser(uuid);

        loadUser(user);

        return user;
    }

    public static boolean isUserLoaded(String uuid) {
        return getUser(uuid) != null;
    }
}
