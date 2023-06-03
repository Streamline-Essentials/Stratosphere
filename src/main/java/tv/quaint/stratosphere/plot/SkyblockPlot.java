package tv.quaint.stratosphere.plot;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;
import tv.quaint.savables.SavableResource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import tv.quaint.storage.documents.SimpleJsonDocument;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.flag.PlotFlag;
import tv.quaint.stratosphere.plot.members.PlotMember;
import tv.quaint.stratosphere.plot.members.PlotRole;
import tv.quaint.stratosphere.plot.members.basic.AdminRole;
import tv.quaint.stratosphere.plot.members.basic.MemberRole;
import tv.quaint.stratosphere.plot.members.basic.OwnerRole;
import tv.quaint.stratosphere.plot.members.basic.VisitorRole;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;
import tv.quaint.stratosphere.plot.pos.PlotPos;
import tv.quaint.stratosphere.plot.pos.SpawnPos;
import tv.quaint.stratosphere.plot.schematic.tree.SchemTree;
import tv.quaint.stratosphere.plot.upgrades.AchievedUpgrade;
import tv.quaint.stratosphere.plot.upgrades.PlotUpgrade;
import tv.quaint.stratosphere.users.SkyblockUser;
import tv.quaint.stratosphere.utils.MessageUtils;
import tv.quaint.stratosphere.world.SkyblockIOBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class SkyblockPlot extends SavableResource {
    public static class PlotSerializer extends SimpleJsonDocument {
        public PlotSerializer(String identifier) {
            super((identifier.endsWith(".json") ? identifier : identifier + ".json"), SkyblockIOBus.getPlotFolder(), false);
        }

        @Override
        public void onInit() {

        }

        @Override
        public void onSave() {

        }
    }

    public enum PlotType {
        SOLO,
        DUO,
        MULTI,
        ;
    }

    public static final int DEFAULT_MAX_HEART_HEALTH = 1000;

    @Getter @Setter @NonNull
    private Location locationalOffset;
    @Getter @Setter
    private double radius;
    @Getter @Setter
    private ConcurrentSkipListSet<PlotPos> savedLocations;
    @Getter @Setter
    private ConcurrentSkipListSet<PlotRole> roles;
    @Getter @Setter
    private ConcurrentSkipListSet<PlotMember> members;
    @Getter @Setter
    private ConcurrentSkipListSet<PlotFlag> selfFlags;
    @Getter @Setter
    private UUID ownerUuid;
    @Getter @Setter
    private String instantiator;
    @Getter @Setter
    private String schemTree;
    @Getter @Setter
    private ConcurrentSkipListSet<AchievedUpgrade> achievedUpgrades;
    @Getter @Setter
    private ConcurrentSkipListSet<SkyblockUser> invitedUsers;
    @Setter
    private int level;
    @Getter @Setter
    private int maxLevel;
    @Getter @Setter
    private int xp;
    @Getter @Setter
    private int spawnCapIndex;
    @Getter @Setter
    private int spawnRateIndex;
    @Getter @Setter
    private int maxPartySize;
    @Getter @Setter
    private int generatorIndex;
    @Getter @Setter
    private boolean isPrivate;
    @Getter @Setter
    private boolean anyoneCanJoin;
    @Getter @Setter
    private int heartMaxHealth;
    @Getter @Setter
    private int heartCurrentHealth;
    @Getter @Setter
    private long createdAtMillis;
    @Getter @Setter
    private long lastPlayedMillis;
    @Getter @Setter
    private long secondsNotPlayed;
    @Getter @Setter
    private long secondsPlayed;
    @Getter @Setter
    private PlotType plotType;

    @Getter @Setter
    private boolean lastTickPlayersWereOn;
    @Getter
    private final BukkitTask task;
    @Getter @Setter
    private ConcurrentSkipListMap<Long, Player> cachedPlayerUpgradeRequests = new ConcurrentSkipListMap<>();

    public int getLevel() {
        return Math.min(level, maxLevel);
    }

    public int getRealLevel() {
        return level;
    }

    private SkyblockPlot(String instantiator, boolean newPlot) {
        super(instantiator, new PlotSerializer(instantiator));

        task = Bukkit.getScheduler().runTaskTimer(Stratosphere.getInstance(), this::tick, 1, 20);
    }

    public SkyblockPlot(String instantiator, SchemTree schemTree, @NonNull Location locationalOffset) {
        this(instantiator, true);
        this.createdAtMillis = System.currentTimeMillis();
        this.lastPlayedMillis = System.currentTimeMillis();
        this.secondsNotPlayed = 0;
        this.secondsPlayed = 0;
        this.plotType = PlotType.SOLO;

        this.instantiator = instantiator;

        this.locationalOffset = locationalOffset;

        this.schemTree = schemTree.getIdentifier();

        setupWorld(instantiator, schemTree);

        this.radius = Stratosphere.getMyConfig().getIslandDefaultSize();

        this.maxLevel = 20;
        this.maxPartySize = 10;
        this.generatorIndex = 1;

        setUpBasicSelfFlags();
        setUpBasicRoles();

        achievedUpgrades = new ConcurrentSkipListSet<>();

        this.members = new ConcurrentSkipListSet<>();

        this.ownerUuid = UUID.fromString(instantiator);
        PlotMember member = new PlotMember(ownerUuid, getOwnerRole());
        addMember(member);

        this.isPrivate = false;
        this.anyoneCanJoin = false;

        this.heartMaxHealth = DEFAULT_MAX_HEART_HEALTH;
        this.heartCurrentHealth = DEFAULT_MAX_HEART_HEALTH;

        saveAll();

        updateWorldBorder();
        this.invitedUsers = new ConcurrentSkipListSet<>(); // Might need to put this in the else statement later if I add saving for it.
    }

    public SkyblockPlot(String instantiator) {
        this(instantiator, false);

        forceReload();

        updateWorldBorder();
        this.invitedUsers = new ConcurrentSkipListSet<>(); // Might need to put this in the else statement later if I add saving for it.
    }

    public void forceReload() {
        this.instantiator = getOrSetDefault("instantiator", getUuid());
        this.locationalOffset = getLocationalOffsetFromString(getOrSetDefault("locational-offset", Stratosphere.getMyConfig().getIslandWorldName() + ";0,0,0"));
        this.schemTree = getOrSetDefault("schem-tree", "default");
        this.radius = getOrSetDefault("radius", 100d);
        this.savedLocations = new ConcurrentSkipListSet<>();
        this.savedLocations = getSavedLocationsFromStrings(getOrSetDefault("saved-positions", new ArrayList<>()));
        this.selfFlags = new ConcurrentSkipListSet<>();
        this.roles = new ConcurrentSkipListSet<>();

        // TODO: add ability to add custom roles
        setUpBasicSelfFlags();
        setUpBasicRoles();

        this.achievedUpgrades = new ConcurrentSkipListSet<>();
        List<String> achievedUpgradesStrings = getOrSetDefault("achieved-upgrades", new ArrayList<>());
        achievedUpgradesStrings.forEach(achievedUpgradeString -> {
            try {
                PlotUpgrade upgrade = Stratosphere.getUpgradeConfig().getUpgrade(achievedUpgradeString);
                if (upgrade == null) return;
                AchievedUpgrade achievedUpgrade = new AchievedUpgrade(this, upgrade);
                achievedUpgrades.add(achievedUpgrade);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        this.members = new ConcurrentSkipListSet<>();
        List<String> memberStrings = getOrSetDefault("members", new ArrayList<>());
        memberStrings.forEach(memberString -> {
            try {
                String[] split = memberString.split(":");
                UUID uuid = UUID.fromString(split[0]);
                PlotRole role = getRoleById(split[1]);
                PlotMember plotMember = new PlotMember(uuid, role);
                members.add(plotMember);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        this.ownerUuid = UUID.fromString(getOrSetDefault("owner-uuid", getUuid()));

        this.level = getOrSetDefault("level", 1);
        this.xp = getOrSetDefault("xp", 0);
        this.maxLevel = getOrSetDefault("max-level", 20);

        this.maxPartySize = getOrSetDefault("max-party-size", 10);
        this.generatorIndex = getOrSetDefault("generator-index", 1);

        this.spawnCapIndex = getOrSetDefault("spawn-cap-index", 1);
        this.spawnRateIndex = getOrSetDefault("spawn-rate-index", 1);

        this.isPrivate = getOrSetDefault("is-private", false);
        this.anyoneCanJoin = getOrSetDefault("can-any-join", false);

        this.heartMaxHealth = getOrSetDefault("heart-max-health", DEFAULT_MAX_HEART_HEALTH);
        this.heartCurrentHealth = getOrSetDefault("heart-current-health", DEFAULT_MAX_HEART_HEALTH);

        this.invitedUsers = new ConcurrentSkipListSet<>();

        this.lastPlayedMillis = getOrSetDefault("last-played-millis", System.currentTimeMillis());
        this.createdAtMillis = getOrSetDefault("created-at-millis", -1L);

        this.secondsNotPlayed = getOrSetDefault("seconds-not-played", 0L);
        this.secondsPlayed = getOrSetDefault("seconds-played", 0L);

        this.plotType = PlotType.valueOf(getOrSetDefault("plot-type", PlotType.SOLO.name()));
    }

    public void setupWorld(String instantiator, SchemTree tree) {
        MessageUtils.logDebug("About to setup world for " + instantiator);

        SpawnPos spawnPos = new SpawnPos(this, getLocationalOffset().getBlockX(), 150, getLocationalOffset().getBlockZ(), 0, 0);
        savedLocations = new ConcurrentSkipListSet<>();
        savedLocations.add(spawnPos);

        tree.getNormal().paste(spawnPos.toLocation());

        spawnCapIndex = 1;
        spawnRateIndex = 1;
    }

    public SkyblockUser getInstantiatorAsUser() {
        return PlotUtils.getOrGetUser(instantiator);
    }

    public SkyblockUser getOwnerAsUser() {
        return PlotUtils.getOrGetUser(ownerUuid.toString());
    }

    @Override
    public void populateDefaults() {

    }

    @Override
    public void loadValues() {

    }

    @Override
    public void saveAll() {
        write("instantiator", getInstantiator());
        write("locational-offset", getLocationalOffsetAsString());
        write("radius", getRadius());
        write("saved-positions", getSavedLocationsAsStrings());
        write("self-flags", getSelfFlagsAsStrings());
        write("roles", getPlotRolesAsStrings());
        write("members", getMembersAsStrings());
        write("owner-uuid", getOwnerUuid().toString());
        write("schem-tree", getSchemTree());
        write("achieved-upgrades", getAchievedUpgradesAsStrings());
//        write("invited-users", getInvitedUsersAsStrings());
        write("level", getLevel());
        write("xp", getXp());
        write("max-level", getMaxLevel());
        write("max-party-size", getMaxPartySize());
        write("spawn-cap-index", getSpawnCapIndex());
        write("spawn-rate-index", getSpawnRateIndex());
        write("generator-index", getGeneratorIndex());
        write("is-private", isPrivate());
        write("can-any-join", isAnyoneCanJoin());

        write("heart-max-health", getHeartMaxHealth());
        write("heart-current-health", getHeartCurrentHealth());

        write("last-played-millis", getLastPlayedMillis());
        write("created-at-millis", getCreatedAtMillis());
        write("seconds-not-played", getSecondsNotPlayed());
        write("seconds-played", getSecondsPlayed());

        write("plot-type", getPlotType().name());

        // Saves the schematic of the island
        saveSnapshotSchematic();
    }

    public String getLocationalOffsetAsString() {
        World world = getLocationalOffset().getWorld();
        if (world == null) world = SkyblockIOBus.getOrGetSkyblockWorld();

        return world.getName() + ";" +
                getLocationalOffset().getBlockX() + "," + getLocationalOffset().getBlockY() + "," + getLocationalOffset().getBlockZ();
    }

    public Location getLocationalOffsetFromString(String string) {
        try {
            String[] split = string.split(";");
            String worldName = getWorld().getName();
            String[] coords = split[1].split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);
            return new Location(Bukkit.getWorld(worldName), x, y, z);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addMaxLevel(int amount) {
        this.maxLevel += amount;
        saveAll();
    }

    public void removeMaxLevel(int amount) {
        this.maxLevel -= amount;
        saveAll();
    }

    public void addSpawnCapIndex(int amount) {
        this.spawnCapIndex += amount;
        saveAll();
    }

    public void removeSpawnCapIndex(int amount) {
        this.spawnCapIndex -= amount;
        saveAll();
    }

    public void addSpawnRateIndex(int amount) {
        this.spawnRateIndex += amount;
        saveAll();
    }

    public void removeSpawnRateIndex(int amount) {
        this.spawnRateIndex -= amount;
        saveAll();
    }

    public void addLevel(int amount) {
        this.level += amount;
        saveAll();
    }

    public void removeLevel(int amount) {
        this.level -= amount;
        saveAll();
    }

    public void addXp(int amount) {
        this.xp += amount;
        saveAll();
    }

    public void removeXp(int amount) {
        this.xp -= amount;
        saveAll();
    }

    public void addMaxPartySize(int amount) {
        this.maxPartySize += amount;
        saveAll();
    }

    public void removeMaxPartySize(int amount) {
        this.maxPartySize -= amount;
        saveAll();
    }

    public void addRadius(double amount) {
        this.radius += amount;
        saveAll();
    }

    public void removeRadius(double amount) {
        this.radius -= amount;
        saveAll();
    }

    public void addAchievedUpgrade(AchievedUpgrade achievedUpgrade) {
        achievedUpgrades.add(achievedUpgrade);
    }

    public void removeAchievedUpgrade(String upgradeName) {
        getAchievedUpgrades().removeIf(achievedUpgrade -> achievedUpgrade.getIdentifier().equalsIgnoreCase(upgradeName));
    }

    public void removeAchievedUpgrade(PlotUpgrade.UpgradeType upgradeName) {
        removeAchievedUpgrade(upgradeName.toString());
    }

    public AchievedUpgrade getAchievedUpgrade(String upgradeName) {
        AtomicReference<AchievedUpgrade> achievedUpgrade = new AtomicReference<>();
        getAchievedUpgrades().forEach(achievedUpgrade1 -> {
            if (achievedUpgrade.get() != null) return;

            if (achievedUpgrade1.getIdentifier().equalsIgnoreCase(upgradeName)) {
                achievedUpgrade.set(achievedUpgrade1);
            }
        });
        return achievedUpgrade.get();
    }

    public AchievedUpgrade getAchievedUpgrade(PlotUpgrade.UpgradeType upgradeName) {
        AtomicReference<AchievedUpgrade> achievedUpgrade = new AtomicReference<>();
        getAchievedUpgrades().forEach(achievedUpgrade1 -> {
            if (achievedUpgrade.get() != null) return;

            if (achievedUpgrade1.getUpgrade().getType().equals(upgradeName)) {
                achievedUpgrade.set(achievedUpgrade1);
            }
        });
        return achievedUpgrade.get();
    }

    public boolean hasAchievedUpgrade(String upgradeName) {
        return getAchievedUpgrade(upgradeName) != null;
    }

    public boolean hasAchievedUpgrade(PlotUpgrade.UpgradeType upgradeName) {
        return getAchievedUpgrade(upgradeName) != null;
    }

    public int getUpgradedTier(PlotUpgrade.UpgradeType upgradeType) {
        AtomicReference<Integer> tier = new AtomicReference<>(0);
        getAchievedUpgrades().forEach(achievedUpgrade -> {
            if (achievedUpgrade.getUpgrade().getType().equals(upgradeType)) {
                tier.set(achievedUpgrade.getUpgrade().getTier());
            }
        });
        return tier.get();
    }

    public boolean canPurchaseUpgrade(PlotUpgrade upgrade) {
        if (upgrade == null) return false;
        int tier = getUpgradedTier(upgrade.getType());
        return tier == upgrade.getTier() - 1;
    }

    private List<String> getAchievedUpgradesAsStrings() {
        List<String> achievedUpgradesStrings = new ArrayList<>();
        getAchievedUpgrades().forEach(achievedUpgrade -> {
            achievedUpgradesStrings.add(achievedUpgrade.getUpgrade().getIdentifier());
        });
        return achievedUpgradesStrings;
    }

    public PlotPos getSavedLocation(String name) {
        AtomicReference<PlotPos> savedLocation = new AtomicReference<>();
        getSavedLocations().forEach(plotPos -> {
            if (savedLocation.get() != null) return;

            if (plotPos.getIdentifier().equalsIgnoreCase(name)) {
                savedLocation.set(plotPos);
            }
        });
        return savedLocation.get();
    }

    public SkyblockUser getOwner() {
        return PlotUtils.getOrGetUser(getOwnerUuid().toString());
    }

    public String getOwnerName() {
        SkyblockUser owner = getOwner();
        return owner == null ? "Unknown" : owner.getName();
    }

    private List<String> getPlotRolesAsStrings() {
        List<String> roleStrings = new ArrayList<>();
        getRoles().forEach(plotRole -> {
            roleStrings.add(plotRole.getIdentifier() + ":" + plotRole.getFlagsString());
        });
        return roleStrings;
    }

    public List<String> getSelfFlagsAsStrings() {
        List<String> flagStrings = new ArrayList<>();
        getSelfFlags().forEach(plotFlag -> {
            flagStrings.add(plotFlag.getIdentifier() + ":" + plotFlag.getValue());
        });
        return flagStrings;
    }

    private List<String> getMembersAsStrings() {
        List<String> memberStrings = new ArrayList<>();
        getMembers().forEach(plotMember -> {
            memberStrings.add(plotMember.getUuid() + ":" + plotMember.getRole().getIdentifier());
        });
        return memberStrings;
    }

    public <V> void write(String key, V value) {
        getStorageResource().write(key, value);
    }

    public void addFlag(PlotFlag flag) {
        selfFlags.add(flag);
    }

    public void removeFlag(String identifier) {
        selfFlags.removeIf(plotFlag -> plotFlag.getIdentifier().equalsIgnoreCase(identifier));
    }

    public PlotFlag getFlag(String identifier) {
        AtomicReference<PlotFlag> flag = new AtomicReference<>();

        selfFlags.forEach(plotFlag -> {
            if (flag.get() != null) return;

            if (plotFlag.getIdentifier().equalsIgnoreCase(identifier)) {
                flag.set(plotFlag);
            }
        });

        return flag.get();
    }

    public boolean hasFlag(String identifier) {
        return getFlag(identifier) != null;
    }

    public World getWorld() {
        return SkyblockIOBus.getOrGetSkyblockWorld();
    }

    public double getSize() {
        return getRadius() * 2;
    }

    public WorldBorder getWorldBorder(WorldBorder from) {
        if (from == null) {
            from = Bukkit.createWorldBorder();
        }

        from.setCenter(getLocationalOffset());
        from.setSize(getSize());
        from.setWarningDistance(0);
        from.setWarningTime(0);
        from.setDamageAmount(0);
        from.setDamageBuffer(0);

        return from;
    }

    public void updateWorldBorder() {
        if (getWorld() == null) return;

        getPlayersInsideByName().forEach((s, player) -> {
            if (player.isOnline()) {
                WorldBorder worldBorder = player.getWorldBorder();

                worldBorder = getWorldBorder(worldBorder);

                player.setWorldBorder(worldBorder);
            }
        });
    }

    /**
     * Returns a map with all the players inside the plot.
     * It is name -> player.
     * @return The map with all the players inside the plot.
     */
    public ConcurrentSkipListMap<String, Player> getPlayersInsideByName() {
        if (getWorld() == null) return new ConcurrentSkipListMap<>();

        ConcurrentSkipListMap<String, Player> playersInside = new ConcurrentSkipListMap<>();

        getWorld().getPlayers().forEach(player -> {
            Location location = player.getLocation();

            WorldBorder worldBorder = getWorldBorder(null);
            if (worldBorder.isInside(location)) playersInside.put(player.getName(), player);
        });

        return playersInside;
    }

    /**
     * Returns a map with all the players inside the plot.
     * It is uuid -> player.
     * @return The map with all the players inside the plot.
     */
    public ConcurrentSkipListMap<String, Player> getPlayersInsideByUUID() {
        ConcurrentSkipListMap<String, Player> playersInside = new ConcurrentSkipListMap<>();

        getPlayersInsideByName().forEach((s, player) -> {
            playersInside.put(player.getUniqueId().toString(), player);
        });

        return playersInside;
    }

    public boolean hasPlayersInside() {
        return getPlayersInsideByName().size() > 0;
    }

    public void setUpBasicSelfFlags() {
        selfFlags = new ConcurrentSkipListSet<>();

        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_SPAWN.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_PICKUP.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_DROP.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_GRIEF.getIdentifier(), true));
        addFlag(new PlotFlag(PlotFlagIdentifiers.CAN_MOB_EXPLODE.getIdentifier(), false));
    }

    public void teleport(Player player) {
        player.teleport(getSpawnPos().toLocation());

        player.setVelocity(new Vector(0, 0, 0));

        WorldBorder worldBorder = player.getWorldBorder();

        worldBorder = getWorldBorder(worldBorder);

        player.setWorldBorder(worldBorder);

        updateWorldBorder();
    }

    public void setUpBasicRoles() {
        this.roles = new ConcurrentSkipListSet<>();

        PlotRole owner = new OwnerRole(this);
        PlotRole guest = new AdminRole(this);
        PlotRole member = new MemberRole(this);
        PlotRole visitor = new VisitorRole(this);

        roles.add(owner);
        roles.add(guest);
        roles.add(member);
        roles.add(visitor);
    }

    public PlotRole getRoleById(String identifier) {
        for (PlotRole role : roles) {
            if (role.getIdentifier().equalsIgnoreCase(identifier)) return role;
        }

        return null;
    }

    public PlotMember getMember(String uuid) {
        for (PlotMember member : members) {
            if (member.getUuid().equals(uuid)) return member;
        }

        if (getOwner().getUuid().equals(uuid)) {
            PlotMember member = new PlotMember(uuid, getOwnerRole());
            addMember(member);

            return member;
        }

        return null;
    }

    public PlotRole getRoleByUser(String uuid) {
        PlotMember member = getMember(uuid);
        if (member != null) return member.getRole();

        return getVisitorRole();
    }

    public PlotRole getRole(Player player) {
        return getRoleByUser(player.getUniqueId().toString());
    }

    public PlotRole getOwnerRole() {
        PlotRole role = getRoleById("owner");
        if (role == null) {
            role = new OwnerRole(this);
            addRole(role);
        }
        return role;
    }

    public PlotRole getAdminRole() {
        PlotRole role = getRoleById("admin");
        if (role == null) {
            role = new AdminRole(this);
            addRole(role);
        }
        return role;
    }

    public PlotRole getMemberRole() {
        PlotRole role = getRoleById("member");
        if (role == null) {
            role = new MemberRole(this);
            addRole(role);
        }
        return role;
    }

    public PlotRole getVisitorRole() {
        PlotRole role = getRoleById("visitor");
        if (role == null) {
            role = new VisitorRole(this);
            addRole(role);
        }
        return role;
    }

    public PlotRole getParentRole(PlotRole role) {
        if (! role.hasFlag(PlotFlagIdentifiers.PARENT.getIdentifier())) return null;

        String parentIdentifier = role.getFlag(PlotFlagIdentifiers.PARENT.getIdentifier()).getValue();

        return getRoleById(parentIdentifier);
    }

    public ConcurrentSkipListSet<PlotRole> getSubRoles(PlotRole role) {
        ConcurrentSkipListSet<PlotRole> r = new ConcurrentSkipListSet<>();

        getRoles().forEach(plotRole -> {
            if (plotRole.hasFlag(PlotFlagIdentifiers.PARENT.getIdentifier())) {
                if (plotRole.getFlag(PlotFlagIdentifiers.PARENT.getIdentifier()).getValue().equalsIgnoreCase(role.getIdentifier())) {
                    r.add(plotRole);
                }
            }
        });

        return r;
    }

    public PlotMember getMember(Player player) {
        return getMember(player.getUniqueId().toString());
    }

    public void addRole(PlotRole role) {
        this.roles.add(role);
    }

    public void removeRole(String identifier) {
        for (PlotRole role : roles) {
            if (role.getIdentifier().equalsIgnoreCase(identifier)) {
                roles.remove(role);
                break;
            }
        }
    }

    public void removeRole(PlotRole role) {
        removeRole(role.getIdentifier());
    }

    public SpawnPos getSpawnPos() {
        AtomicReference<SpawnPos> spawnPosAtomicReference = new AtomicReference<>(null);

        getSavedLocations().forEach(plotPos -> {
            try {
                if (plotPos.getIdentifier().equals("spawn")) spawnPosAtomicReference.set((SpawnPos) plotPos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return spawnPosAtomicReference.get();
    }

    public void setSpawnPos(Location location) {
        getSpawnPos().setX(location.getX());
        getSpawnPos().setY(location.getY());
        getSpawnPos().setZ(location.getZ());
        getSpawnPos().setYaw(location.getYaw());
        getSpawnPos().setPitch(location.getPitch());
    }

    public void addSavedLocation(PlotPos plotPos) {
        if (hasSavedLocation(plotPos.getIdentifier())) {
            removeSavedLocation(plotPos.getIdentifier());
        }

        this.savedLocations.add(plotPos);
    }

    public void removeSavedLocation(String identifier) {
        getSavedLocations().forEach(plotPos -> {
            if (plotPos.getIdentifier().equalsIgnoreCase(identifier)) {
                savedLocations.remove(plotPos);
            }
        });
    }

    public boolean hasSavedLocation(String identifier) {
        AtomicBoolean hasSavedLocation = new AtomicBoolean(false);

        getSavedLocations().forEach(plotPos -> {
            if (plotPos.getIdentifier().equalsIgnoreCase(identifier)) hasSavedLocation.set(true);
        });

        return hasSavedLocation.get();
    }

    public List<String> getSavedLocationsAsStrings() {
        List<String> savedLocations = new ArrayList<>();
        this.savedLocations.forEach(plotPos -> savedLocations.add(plotPos.toString()));
        return savedLocations;
    }

    public ConcurrentSkipListSet<PlotPos> getSavedLocationsFromStrings(List<String> savedLocations) {
        ConcurrentSkipListSet<PlotPos> plotPositions = new ConcurrentSkipListSet<>();
        savedLocations.forEach(plotPos -> {
            PlotPos plotPosition = PlotPos.fromString(plotPos, this);
            if (plotPosition.getType() == PlotPos.PlotPosType.SPAWN)
                plotPosition = new SpawnPos(this,
                        plotPosition.getX(), plotPosition.getY(), plotPosition.getZ(),
                        plotPosition.getYaw(), plotPosition.getPitch());

            plotPositions.add(plotPosition);
        });
        return plotPositions;
    }

    public void addMember(PlotMember member) {
        members.add(member);
        member.getUser().setPlotUuid(this.getUuid());
        member.getUser().saveAll();

        messageMembers("&a" + member.getUser().getUsername() + " &7has joined the plot.");
    }

    public void removeMember(PlotMember member) {
        members.remove(member);
        member.getUser().setPlotUuid("");
        member.getUser().saveAll();
    }

    public boolean isMember(String uuid) {
        for (PlotMember member : members) {
            if (member.getUuid().equals(uuid)) return true;
        }
        return false;
    }

    public boolean isMember(UUID uuid) {
        return isMember(uuid.toString());
    }

    public boolean isMember(Player player) {
        return isMember(player.getUniqueId());
    }

    public boolean isOwner(String uuid) {
        return ownerUuid.toString().equals(uuid);
    }

    public boolean isOwner(UUID uuid) {
        return isOwner(uuid.toString());
    }

    public boolean isOwner(Player player) {
        return isOwner(player.getUniqueId());
    }

    public boolean isInPlot(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        WorldBorder worldBorder = getWorldBorder();
        if (worldBorder == null) return false;

        return worldBorder.isInside(location);
    }

    public WorldBorder getWorldBorder() {
        return getWorldBorder(null);
    }

    public boolean isInRadius(double x, double z, boolean square) {
        // make sure to not do a circle check if the plot is a square
        return square ? isInSquareRadius(x, z) : isInCircleRadius(x, z);
    }

    public boolean isInRadius(double x, double z) {
        return isInRadius(x, z, true);
    }

    public boolean isInSquareRadius(double x, double z) {
        return x >= -radius && x <= radius && z >= -radius && z <= radius;
    }

    public boolean isInCircleRadius(double x, double z) {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)) <= radius;
    }

    public void delete() {
        task.cancel();

        kickToSpawn();

        getMembers().forEach(plotMember -> {
            plotMember.getUser().setPlotUuid("");
            plotMember.getUser().saveAll();
        });

//        this.skyWorld.delete();

        deleteTopScore();

        PlotUtils.unloadPlot(this);

        try {
            this.dispose();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void messageMembers(String message) {
        getMembers().forEach(member -> member.message(message));
    }

    public void broadcastWorld(String message) {
        getPlayersInsideByName().forEach((s, player) -> {
            SkyblockUser user = PlotUtils.getOrGetUser(player.getUniqueId().toString());
            if (user != null) user.sendMessage(message);
        });
    }

    public SkyblockUser getInvited(String uuid) {
        AtomicReference<SkyblockUser> user = new AtomicReference<>();

        invitedUsers.forEach(invited -> {
            if (invited.getUuid().equals(uuid)) user.set(invited);
        });

        return user.get();
    }

    public boolean isInvited(String uuid) {
        return getInvited(uuid) != null;
    }

    public void addInvited(SkyblockUser user) {
        invitedUsers.add(user);
    }

    public void removeInvited(SkyblockUser user) {
        invitedUsers.remove(user);
    }

    public void removeInvited(String uuid) {
        getInvitedUsers().removeIf(user -> user.getUuid().equals(uuid));
    }

    public void invite(SkyblockUser user, SkyblockUser sender) {
        if (! user.isOnline()) return;

        if (user.getUuid().equals(sender.getUuid())) {
            sender.sendMessage("&cYou cannot invite yourself.");
            return;
        }

        if (getPlotType() == PlotType.SOLO && getMembers().size() >= 1) {
            user.sendMessage("&cYou cannot invite users to a solo island. Upgrade your island type to invite users &7(&e/island upgradetype&7)&c.");
            return;
        }
        if (getPlotType() == PlotType.DUO && getMembers().size() >= 2) {
            user.sendMessage("&cYou cannot invite more than 1 user to a duo island. Upgrade your island type to invite users &7(&e/island upgradetype&7)&c.");
            return;
        }

        if (user.isAlreadyInPlot()) {
            sender.sendMessage("&cThat user is already in an island.");
            return;
        }

        if (isMember(user.getUuid())) {
            sender.sendMessage("&cThat user is already a member of this island.");
            return;
        }

        if (isInvited(user.getUuid())) {
            sender.sendMessage("&cThat user is already invited to this island.");
            return;
        }

        invitedUsers.add(user);

        user.sendMessage("&eYou have been invited to join &c" + getOwnerName() + "&e's island.");
        user.sendMessage("&eType &b/island accept " + getOwnerName() + " &eto join.");

        sender.sendMessage("&aInvited &b" + user.getDisplayName() + " &ato your plot!");
    }

    public void acceptInvite(SkyblockUser accepter) {
        if (! isInvited(accepter.getUuid())) {
            accepter.sendMessage("&cYou have not been invited to this island.");
            return;
        }

        if (accepter.isAlreadyInPlot()) {
            accepter.sendMessage("&cYou are already in an island.");
            removeInvited(accepter.getUuid());
            return;
        }

        if (isMember(accepter.getUuid())) {
            accepter.sendMessage("&cYou are already a member of this island.");
            removeInvited(accepter.getUuid());
            return;
        }

        PlotRole role = getMemberRole();

        addMember(new PlotMember(UUID.fromString(accepter.getUuid()), role));

        accepter.sendMessage("&eYou have joined &c" + getOwnerName() + "&e's island.");
        messageMembers("&c" + accepter.getName() + " &ehas joined the island.");
    }

    public void denyInvite(SkyblockUser denier) {
        if (! isInvited(denier.getUuid())) {
            denier.sendMessage("&cYou have not been invited to this island.");
            return;
        }

        if (denier.isAlreadyInPlot()) {
            denier.sendMessage("&cYou are already in an island.");
            removeInvited(denier.getUuid());
            return;
        }

        if (isMember(denier.getUuid())) {
            denier.sendMessage("&cYou are already a member of this island.");
            removeInvited(denier.getUuid());
            return;
        }

        removeInvited(denier.getUuid());
        denier.sendMessage("&eYou have denied the invite to &c" + getOwnerName() + "&e's island.");
    }

    public void promoteUser(SkyblockUser promoted, SkyblockUser sender) {
        if (promoted.getUuid().equals(sender.getUuid())) {
            sender.sendMessage("&cYou cannot invite yourself.");
            return;
        }

        PlotMember plotSender = getMember(sender.getUuid());

        if (! isMember(promoted.getUuid())) {
            sender.sendMessage("&cThat user is not a member of this island.");
            return;
        }

        if (plotSender.getRole().hasFlag(PlotFlagIdentifiers.PERMISSION_EDIT.getIdentifier())) {
            if (! Boolean.parseBoolean(plotSender.getRole().getFlag(PlotFlagIdentifiers.PERMISSION_EDIT.getIdentifier()).getValue())) {
                sender.sendMessage("&cYou do not have permission to promote users.");
                return;
            }
        }

        if (isOwner(promoted.getUuid())) {
            sender.sendMessage("&cThat user is already the owner of this island.");
            return;
        }

        PlotMember member = getMember(promoted.getUuid());

        if (member.getRole().getIdentifier().equalsIgnoreCase("member")) {
            member.setRole(getAdminRole());
            sender.sendMessage("&eYou have promoted &c" + promoted.getName() + "&e to moderator.");
            promoted.sendMessage("&eYou have been promoted to admin by &c" + sender.getName() + "&e.");
        } else if (member.getRole().getIdentifier().equalsIgnoreCase("admin")) {
            sender.sendMessage("&cThat user is already the highest role. If you want to transfer ownership, use &b/island transfer <name>&c.");
        }
    }

    public void demoteUser(SkyblockUser demoted, SkyblockUser sender) {
        if (demoted.getUuid().equals(sender.getUuid())) {
            sender.sendMessage("&cYou cannot invite yourself.");
            return;
        }

        PlotMember plotSender = getMember(sender.getUuid());

        if (! isMember(demoted.getUuid())) {
            sender.sendMessage("&cThat user is not a member of this island.");
            return;
        }

        if (plotSender.getRole().hasFlag(PlotFlagIdentifiers.PERMISSION_EDIT.getIdentifier())) {
            if (! Boolean.parseBoolean(plotSender.getRole().getFlag(PlotFlagIdentifiers.PERMISSION_EDIT.getIdentifier()).getValue())) {
                sender.sendMessage("&cYou do not have permission to demote users.");
                return;
            }
        }

        if (isOwner(demoted.getUuid())) {
            sender.sendMessage("&cThat user is already the owner of this island.");
            return;
        }

        PlotMember member = getMember(demoted.getUuid());

        if (member.getRole().getIdentifier().equalsIgnoreCase("admin")) {
            member.setRole(getMemberRole());
            sender.sendMessage("&eYou have demoted &c" + demoted.getName() + "&e to member.");
            demoted.sendMessage("&eYou have been demoted to member by &c" + sender.getName() + "&e.");
        } else if (member.getRole().getIdentifier().equalsIgnoreCase("member")) {
            sender.sendMessage("&cThat user is already the lowest role.");
        }
    }

    public void transferOwnership(SkyblockUser newOwner, SkyblockUser sender) {
        if (newOwner.getUuid().equals(sender.getUuid())) {
            sender.sendMessage("&cYou cannot transfer ownership to yourself.");
            return;
        }

        PlotMember plotSender = getMember(sender.getUuid());

        if (! isMember(newOwner.getUuid())) {
            sender.sendMessage("&cThat user is not a member of this island.");
            return;
        }

        if (! isOwner(plotSender.getUuid())) {
            sender.sendMessage("&cYou are not the owner of this island.");
            return;
        }

        if (isOwner(newOwner.getUuid())) {
            sender.sendMessage("&cThat user is already the owner of this island.");
            return;
        }

        PlotMember member = getMember(newOwner.getUuid());

        member.setRole(getOwnerRole());
        plotSender.setRole(getAdminRole());

        setOwnerUuid(UUID.fromString(newOwner.getUuid()));

        newOwner.setPlotUuid(getUuid());
        sender.setPlotUuid(getUuid());

        sender.sendMessage("&eYou have transferred ownership of the island to &c" + newOwner.getName() + "&e.");
        newOwner.sendMessage("&eYou have been promoted to owner by &c" + sender.getName() + "&e.");
    }

    public void kickFromIsland(SkyblockUser toKick, SkyblockUser sender) {
        if (toKick.getUuid().equals(sender.getUuid())) {
            sender.sendMessage("&cYou cannot kick yourself.");
            return;
        }

        PlotMember plotSender = getMember(sender.getUuid());

        if (! isMember(toKick.getUuid())) {
            sender.sendMessage("&cThat user is not a member of this island.");
            return;
        }

        if (isOwner(toKick.getUuid())) {
            sender.sendMessage("&cYou cannot kick the owner of the island.");
            return;
        }

        if (plotSender.getRole().hasFlag(PlotFlagIdentifiers.PERMISSION_EDIT.getIdentifier())) {
            if (! Boolean.parseBoolean(plotSender.getRole().getFlag(PlotFlagIdentifiers.PERMISSION_EDIT.getIdentifier()).getValue())) {
                sender.sendMessage("&cYou do not have permission to kick users.");
                return;
            }
        }

        PlotMember member = getMember(toKick.getUuid());

        removeMember(member);
        toKick.sendMessage("&cYou have been kicked from the island by &e" + sender.getName() + "&c.");
        sender.sendMessage("&eYou have kicked &c" + toKick.getName() + "&e from the island.");
    }

    /**
     * Handles leaving the island both as a member and as the owner.
     * When an owner leaves the island, if it has members, it will deny it; if there are no members, it will delete the island.
     * If there are no members, it will delete the island.
     */
    public void leaveIsland(SkyblockUser user) {
        if (! isMember(user.getUuid())) {
            user.sendMessage("&cYou are not a member of this island.");
            return;
        }

        if (isOwner(user.getUuid())) {
            if (getMembers().size() > 1) {
                user.sendMessage("&cYou cannot leave the island as the owner. You must transfer ownership to another member.");
                return;
            } else {
                delete();
                user.sendMessage("&eYou have left and deleted the island.");
                return;
            }
        }

        PlotMember member = getMember(user.getUuid());

        removeMember(member);
        user.sendMessage("&eYou have left the island.");
        messageMembers("&e" + user.getName() + " &chas left the island.");
    }

    public void kickToSpawn() {
        Location spawn = Stratosphere.getMyConfig().getSpawnLocation();

        getPlayersInsideByName().forEach((s, player) -> player.teleport(spawn));
    }

    public String getMemberNames() {
        StringBuilder builder = new StringBuilder();

        getMembers().forEach(member -> builder.append("&b").append(member.getUser().getName()).append("&8, "));
        if (builder.toString().endsWith("&8, "))
            builder.delete(builder.length() - 4, builder.length() - 1);

        return builder.toString();
    }

    public String getInvitedNames() {
        StringBuilder builder = new StringBuilder();

        getInvitedUsers().forEach(invite -> builder.append("&b").append(invite.getName()).append("&8, "));
        if (builder.toString().endsWith("&8, "))
            builder.delete(builder.length() - 4, builder.length() - 1);

        return builder.toString();
    }

    public boolean isLoaded() {
        return PlotUtils.isPlotLoaded(this);
    }

    public void load() {
        if (isLoaded()) return;

        PlotUtils.loadPlot(this);
    }

    public void unload() {
        if (! isLoaded()) return;

        PlotUtils.unloadPlot(this);
    }

    public boolean areAnyMembersOnline() {
        AtomicBoolean online = new AtomicBoolean(false);

        getMembers().forEach(member -> {
            Player player = Bukkit.getPlayer(UUID.fromString(member.getUser().getUuid()));

            if (player != null) {
                online.set(true);
            }
        });

        return online.get();
    }

    public boolean tryUnload() {
        if (areAnyMembersOnline()) return false;

        saveAll();
        unload();
        return true;
    }

    public int getSnapshotCount() {
        File folder = SkyblockIOBus.getPlotSchematicsFolder();
        File[] files = folder.listFiles();

        if (files == null) return 0;

        return (int) Arrays.stream(files).filter(file -> file.getName().startsWith(getUuid())).count();
    }

    public int getSnapshotNumber() {
        int count = 0;
        File file = new File(SkyblockIOBus.getPlotSchematicsFolder(), getUuid() + "-" + count + ".schematic");

        while (file.exists()) {
            count ++;
            file = new File(SkyblockIOBus.getPlotSchematicsFolder(), getUuid() + "-" + (count) + ".schematic");
        }

        return count;
    }

    public File getNextSnapshotFile() {
        return new File(SkyblockIOBus.getPlotSchematicsFolder(), getUuid() + "-" + getSnapshotNumber() + ".schematic");
    }

    public void saveSnapshotSchematic() {
        CompletableFuture.runAsync(() -> {
            File file = getNextSnapshotFile();

            try (ClipboardWriter writer = BuiltInClipboardFormat.FAST.getWriter(new FileOutputStream(file))) {
                writer.write(getSnapshot());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public File getSnapshotFile(int number) {
        return getSnapshotFile(String.valueOf(number));
    }

    public File getSnapshotFile(String number) {
        return new File(SkyblockIOBus.getPlotSchematicsFolder(), getUuid() + "-" + number + ".schematic");
    }

    public void pasteSnapshotSchematic(String number) {
        File file = getSnapshotFile(number);
        if (! file.exists()) return;

        World world = getWorld();
        if (world == null) return;

        BlockVector3 vector = BlockVector3.at(getLocationalOffset().getX(), getLocationalOffset().getY(), getLocationalOffset().getZ());

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(world))) {
            Operation operation = new ClipboardHolder(readClipboard(number))
                    .createPaste(editSession)
                    .to(vector)
                    .ignoreAirBlocks(true)
                    .build();
            Operations.complete(operation);
        }
    }

    public Clipboard readClipboard(String number) throws IllegalArgumentException {
        File file = getSnapshotFile(number);

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) throw new IllegalArgumentException("Unknown schematic format: " + file.getName());

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Clipboard getSnapshot() {
        BukkitWorld world = new BukkitWorld(getWorld());

        Region region = new CuboidRegion(world, getMin(), getMax());
        Clipboard clipboard = Clipboard.create(region);

        clipboard.setOrigin(BlockVector3.at(getLocationalOffset().getBlockX(), getLocationalOffset().getBlockY(), getLocationalOffset().getBlockZ()));

        return clipboard;
    }

    public BlockVector3 getMin() {
        Location stepPos = getLocationalOffset();
        int x = stepPos.getBlockX() - (int) Math.ceil(getRadius());
        int z = stepPos.getBlockZ() - (int) Math.ceil(getRadius());
        int y = getWorld().getMinHeight();

        return BlockVector3.at(x, y, z);
    }

    public BlockVector3 getMax() {
        Location stepPos = getLocationalOffset();
        int x = stepPos.getBlockX() + (int) Math.ceil(getRadius());
        int z = stepPos.getBlockZ() + (int) Math.ceil(getRadius());
        int y = getWorld().getMaxHeight();

        return BlockVector3.at(x, y, z);
    }

    public void restoreToLatestSnapshot() {
        int num = getSnapshotNumber() - 1;

        if (num < 0) return;

        pasteSnapshotSchematic(String.valueOf(num));

        getPlayersInsideByName().forEach((s, player) -> player.sendMessage("&eYour island has been restored to the latest snapshot."));
    }


    public boolean hasMemberOnline() {
        return getMembers().stream().anyMatch(member -> Bukkit.getPlayer(UUID.fromString(member.getUser().getUuid())) != null);
    }

    public double calculateScore() {
        double weight_level = 100;
//        int weight_members = 10;
        double weight_size = 7;
        double weight_upgrades = 2;
        double weight_total_quests = 5;

        double weight_score = 1000;

        double levelScore = getLevel() * weight_level;
//        int membersScore = getMembers().size() * weight_members;
        double sizeScore = (int) (getRadius() * weight_size);
        double upgradesScore = getAchievedUpgrades().size() * weight_upgrades;
        double totalQuestsScore = getAchievedQuests() * weight_total_quests;

        double scoreScore = checkPlayQuality() * weight_score;

        return levelScore + sizeScore + upgradesScore + totalQuestsScore + scoreScore;
    }

    public ConcurrentSkipListSet<PlotMember> getMembersInside() {
        ConcurrentSkipListSet<PlotMember> members = new ConcurrentSkipListSet<>();

        getPlayersInsideByUUID().forEach((uuid, player) -> {
            if (isMember(player)) {
                members.add(getMember(player));
            }
        });

        return members;
    }

    public int getMembersInsideCount() {
        return getMembersInside().size();
    }

    public void tick() {
        if (getMembersInsideCount() > 0) {
            secondsPlayed++;

            if (! lastTickPlayersWereOn) {
                lastTickPlayersWereOn = true;

                long diff = System.currentTimeMillis() - lastPlayedMillis;
                // convert to seconds
                diff /= 1000;

                secondsNotPlayed += diff;

                lastPlayedMillis = System.currentTimeMillis();
            }
        } else {
            if (lastTickPlayersWereOn) {
                lastTickPlayersWereOn = false;
                lastPlayedMillis = System.currentTimeMillis();
            }
        }

        ensureCorrectType();

        updateTopScore();
    }

    public void ensureCorrectType() {
        if (getMembers().size() > 2 && (getPlotType() != PlotType.MULTI)) {
            setPlotType(PlotType.MULTI);
            saveAll();
        }
        if (getMembers().size() == 2 && (getPlotType() != PlotType.DUO && getPlotType() != PlotType.MULTI)) {
            setPlotType(PlotType.DUO);
            saveAll();
        }
//        if (getMembers().size() == 1 && (getPlotType() != PlotType.SOLO && getPlotType() != PlotType.DUO && getPlotType() != PlotType.MULTI)) {
//            setPlotType(PlotType.SOLO);
//        }
    }

    public double checkPlayQuality() {
        double played_weight = 100;
        double not_played_weight = 50;

        double played_score = secondsPlayed * played_weight;
        double not_played_score = secondsNotPlayed * not_played_weight;

        double total = played_score + not_played_score;

        double percentage = played_score / total;

        percentage = Math.round(percentage * 100.0) / 100.0;
        return percentage;
    }

    public int getAchievedQuests() {
        AtomicInteger count = new AtomicInteger(0);

        getMembers().forEach(member -> {
            int questsDone = member.getUser().getCompletedQuestsAsQuests().size();
            count.addAndGet(questsDone);
        });

        return count.get();
    }

    public void updateTopScore() {
        Stratosphere.getTopConfig().unloadTopScore(this);
        Stratosphere.getTopConfig().saveTopScore(this, calculateScore());
    }

    public void deleteTopScore() {
        Stratosphere.getTopConfig().deleteTopScore(this);
    }

    public void cachePlayerTypeRequest(Player player) {
        uncachedPlayerTypeRequest(player);
        getCachedPlayerUpgradeRequests().put(System.currentTimeMillis(), player);
    }

    public void uncachedPlayerTypeRequest(Player player) {
        getCachedPlayerUpgradeRequests().forEach((time, p) -> {
            if (p.getUniqueId().equals(player.getUniqueId())) {
                getCachedPlayerUpgradeRequests().remove(time);
            }
        });
    }

    public long getCachedPlayerTypeRequest(Player player) {
        AtomicLong time = new AtomicLong(-1);

        getCachedPlayerUpgradeRequests().forEach((t, p) -> {
            if (p.getUniqueId().equals(player.getUniqueId())) {
                time.set(t);
            }
        });

        return time.get();
    }

    public boolean isPlayerCachedAsTypeRequest(Player player) {
        return getCachedPlayerTypeRequest(player) != -1;
    }

    public void upgradeType(Player sender) {
        if (! isMember(sender)) return;
        if (! isOwner(sender)) return;

        SkyblockUser user = PlotUtils.getOrGetUser(sender.getUniqueId().toString());

        if (getPlotType() == PlotType.MULTI) {
            user.sendMessage("&cYour plot is already the maximum type.");
            return;
        }

        if (! isPlayerCachedAsTypeRequest(sender)) {
            cachePlayerTypeRequest(sender);
            user.sendMessage("&eThis action is not able to be undone! If you want to proceed, type the command again...");
            return;
        } else {
            if (System.currentTimeMillis() - getCachedPlayerTypeRequest(sender) > 10 * 1000) {
                cachePlayerTypeRequest(sender);
                user.sendMessage("&eThis action is not able to be undone! If you want to proceed, type the command again...");
                return;
            }
        }

        if (getPlotType() == PlotType.DUO) {
            Stratosphere.getTopConfig().deleteTopScore(this);

            setPlotType(PlotType.MULTI);
            user.sendMessage("&aYour plot has been upgraded to a multi plot.");
        } else if (getPlotType() == PlotType.SOLO) {
            Stratosphere.getTopConfig().deleteTopScore(this);

            setPlotType(PlotType.DUO);
            user.sendMessage("&aYour plot has been upgraded to a duo plot.");
        } else {
            user.sendMessage("&cYour plot is already the maximum type.");
        }

        Stratosphere.getTopConfig().saveTopScore(this, calculateScore());

        uncachedPlayerTypeRequest(sender);
    }
}
