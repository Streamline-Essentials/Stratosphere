package tv.quaint.stratosphere.plot;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.StreamlineUser;
import net.streamline.api.scheduler.ModuleRunnable;
import org.bukkit.*;
import org.bukkit.entity.Player;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.flag.PlotFlag;
import tv.quaint.stratosphere.plot.members.PlotMember;
import tv.quaint.stratosphere.plot.members.PlotRole;
import tv.quaint.stratosphere.plot.members.basic.AdminRole;
import tv.quaint.stratosphere.plot.members.basic.MemberRole;
import tv.quaint.stratosphere.plot.members.basic.OwnerRole;
import tv.quaint.stratosphere.plot.members.basic.VisitorRole;
import tv.quaint.stratosphere.plot.pos.PlotFlagIdentifiers;
import tv.quaint.stratosphere.plot.pos.SpawnPos;
import tv.quaint.stratosphere.plot.schematic.tree.SchemTree;
import tv.quaint.stratosphere.plot.upgrades.AchievedUpgrade;
import tv.quaint.stratosphere.plot.upgrades.PlotUpgrade;
import tv.quaint.stratosphere.users.SkyblockUser;
import tv.quaint.stratosphere.world.SkyblockWorld;
import tv.quaint.stratosphere.plot.pos.PlotPos;
import tv.quaint.stratosphere.world.SkyblockIOBus;
import tv.quaint.storage.documents.SimpleJsonDocument;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class SkyblockPlot extends SavableResource {
    /**
     * A timer for {@link SkyblockPlot}s that will run every tick.
     * Will be used for timed tasks in the {@link SkyblockPlot}.
     */
    public static class PlotTimer extends ModuleRunnable {
        @Getter @Setter
        private SkyblockPlot plot;
        @Getter @Setter
        private int ticksLived;

        public PlotTimer(SkyblockPlot plot) {
            super(Stratosphere.getInstance(), 0, 1L);
            this.plot = plot;
            this.ticksLived = 0;
        }

        @Override
        public void run() {
            ticksLived ++;

            // Every 5 minutes, do...
            if (ticksLived % (20 * 60 * 5) == 0) {
                if (plot.getSkyWorld().getWorld().getPlayers().size() > 0) {
                    plot.addXp(10);
                    plot.messageMembers("&bYou have gained &f10 &dXP &bfor players being in your island!");
                }
            }
        }
    }

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

    @Getter @Setter
    private SkyblockWorld skyWorld;
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
    private String schemTree;
    @Getter @Setter
    private ConcurrentSkipListSet<AchievedUpgrade> achievedUpgrades;
    @Getter @Setter
    private ConcurrentSkipListSet<SkyblockUser> invitedUsers;
    @Getter @Setter
    private int level;
    @Getter @Setter
    private int xp;
    @Getter @Setter
    private PlotTimer timer;

    public SkyblockPlot(String instantiator, SchemTree schemTree, boolean newPlot) {
        super(instantiator, new PlotSerializer(instantiator));

        timer = new PlotTimer(this); // Set up the timer for this plot.

        if (newPlot) {
            this.schemTree = schemTree.getIdentifier();

            setupWorld(instantiator, schemTree);

            this.radius = 100d;

            setUpBasicSelfFlags();
            setUpBasicRoles();

            achievedUpgrades = new ConcurrentSkipListSet<>();

            PlotRole owner = getRoleById("owner");

            PlotMember plotMember = new PlotMember(UUID.fromString(instantiator), owner);
            members = new ConcurrentSkipListSet<>();
            members.add(plotMember);

            this.ownerUuid = UUID.fromString(instantiator);

            saveAll();
        } else {
            forceReload();
            this.skyWorld = SkyblockIOBus.getOrGetSkyblockWorld(instantiator);
            if (skyWorld == null) {
                Stratosphere.getInstance().logSevere("Failed to load world for plot " + instantiator);
                return;
            }
        }

        updateWorldBorder();
        this.invitedUsers = new ConcurrentSkipListSet<>(); // Might need to put this in the else statement later if I add saving for it.
    }

    public void forceReload() {
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
                String[] split = achievedUpgradeString.split(":");
                String upgradeName = split[0];
                int level = Integer.parseInt(split[1]);
                AchievedUpgrade achievedUpgrade = new AchievedUpgrade(upgradeName, level);
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
    }

    public void setupWorld(String instantiator, SchemTree tree) {
        Stratosphere.getInstance().logDebug("About to setup world for " + instantiator);

        this.skyWorld = SkyblockIOBus.getOrGetSkyblockWorld(instantiator, SkyblockWorld.WorldType.NORMAL);
        if (skyWorld == null) {
            Stratosphere.getInstance().logSevere("Failed to load world for plot " + instantiator);
            return;
        }

        SpawnPos spawnPos = new SpawnPos(this, 0, 150, 0, 0, 0);
        savedLocations = new ConcurrentSkipListSet<>();
        savedLocations.add(spawnPos);

        tree.getNormal().paste(spawnPos.toLocation());
    }

    @Override
    public void populateDefaults() {

    }

    @Override
    public void loadValues() {

    }

    @Override
    public void saveAll() {
        write("skyblock-world", getSkyWorld().getIdentifier());
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
    }

    public void addLevel(int amount) {
        this.level += amount;
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

    public void removeLevel(int amount) {
        this.level -= amount;
        saveAll();
    }

    public void addAchievedUpgrade(AchievedUpgrade achievedUpgrade) {
        achievedUpgrades.add(achievedUpgrade);
    }

    public void removeAchievedUpgrade(String upgradeName) {
        getAchievedUpgrades().removeIf(achievedUpgrade -> achievedUpgrade.getIdentifier().equalsIgnoreCase(upgradeName));
    }

    public void removeAchievedUpgrade(PlotUpgrade.Type upgradeName) {
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

    public AchievedUpgrade getAchievedUpgrade(PlotUpgrade.Type upgradeName) {
        return getAchievedUpgrade(upgradeName.toString());
    }

    public boolean hasAchievedUpgrade(String upgradeName) {
        return getAchievedUpgrade(upgradeName) != null;
    }

    public boolean hasAchievedUpgrade(PlotUpgrade.Type upgradeName) {
        return hasAchievedUpgrade(upgradeName.toString());
    }

    public void setTier(String upgradeName, int tier) {
        AchievedUpgrade upgrade = getAchievedUpgrade(upgradeName);
        if (upgrade == null) {
            upgrade = new AchievedUpgrade(upgradeName, tier);
            addAchievedUpgrade(upgrade);
        } else {
            upgrade.setTier(tier);
        }
    }

    public void setTier(PlotUpgrade.Type upgradeName, int tier) {
        setTier(upgradeName.toString(), tier);
    }

    public void increaseTier(String upgradeName, int amount) {
        setTier(upgradeName, getTier(upgradeName) + amount);
    }

    public void increaseTier(PlotUpgrade.Type upgradeName, int amount) {
        increaseTier(upgradeName.toString(), amount);
    }

    public void decreaseTier(String upgradeName, int amount) {
        setTier(upgradeName, getTier(upgradeName) - amount);
    }

    public void decreaseTier(PlotUpgrade.Type upgradeName, int amount) {
        decreaseTier(upgradeName.toString(), amount);
    }

    public int getTier(String upgradeName) {
        AchievedUpgrade upgrade = getAchievedUpgrade(upgradeName);
        return upgrade == null ? PlotUpgrade.MIN_TIER - 1 : upgrade.getTier();
    }

    public int getTier(PlotUpgrade.Type upgradeName) {
        return getTier(upgradeName.toString());
    }

    private List<String> getAchievedUpgradesAsStrings() {
        List<String> achievedUpgradesStrings = new ArrayList<>();
        getAchievedUpgrades().forEach(achievedUpgrade -> {
            achievedUpgradesStrings.add(achievedUpgrade.getIdentifier() + ":" + achievedUpgrade.getTier());
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

    public StreamlineUser getOwner() {
        return ModuleUtils.getOrGetUser(getOwnerUuid().toString());
    }

    public String getOwnerName() {
        StreamlineUser owner = getOwner();
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
            memberStrings.add(plotMember.getUuid().toString() + ":" + plotMember.getRole().getIdentifier());
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

    public void updateWorldBorder() {
        if (getSkyWorld().getWorld() == null) {
            Stratosphere.getInstance().logSevere("Failed to update world border for plot " + getIdentifier() + " because the world is null!");
            return;
        }

        World world = getSkyWorld().getWorld();
        WorldBorder worldBorder = world.getWorldBorder();

        worldBorder.setCenter(0, 0);
        worldBorder.setSize(getRadius() * 2);
        worldBorder.setWarningDistance(0);
        worldBorder.setDamageAmount(0);
        worldBorder.setDamageBuffer(0);
        worldBorder.setWarningTime(0);

        Chunk chunk = world.getChunkAt(0, 0);
        world.refreshChunk(chunk.getX(), chunk.getZ());

//        getPlayersInside().forEach((s, player) -> {
//            if (player.isOnline()) {
//                player.setWorldBorder(worldBorder);
//            }
//        });
    }

    /**
     * Returns a map with all the players inside the plot.
     * It is name -> player.
     * @return The map with all the players inside the plot.
     */
    public ConcurrentSkipListMap<String, Player> getPlayersInside() {
        if (getSkyWorld() == null) return new ConcurrentSkipListMap<>();

        if (getSkyWorld().getWorld() == null) return new ConcurrentSkipListMap<>();

        ConcurrentSkipListMap<String, Player> playersInside = new ConcurrentSkipListMap<>();

        getSkyWorld().getWorld().getPlayers().forEach(player -> {
            playersInside.put(player.getName(), player);
        });

        return playersInside;
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

    public PlotRole getRoleByUser(String uuid) {
        PlotMember member = getMember(UUID.fromString(uuid));
        if (member != null) return member.getRole();

        return getVisitorRole();
    }

    public PlotMember getMember(UUID uuid) {
        for (PlotMember member : members) {
            if (member.getUuid().equals(uuid)) return member;
        }

        return null;
    }

    public PlotRole getRole(Player player) {
        PlotMember member = getMember(player);
        if (member != null) return member.getRole();
        return getVisitorRole();
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
        return getMember(player.getUniqueId());
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

    public File getDataFolder() {
        return this.skyWorld.getDataFolder();
    }

    public SpawnPos getSpawnPos() {
        AtomicReference<SpawnPos> spawnPosAtomicReference = new AtomicReference<>();

        getSavedLocations().forEach(plotPos -> {
            if (plotPos instanceof SpawnPos) spawnPosAtomicReference.set((SpawnPos) plotPos);
        });

        return spawnPosAtomicReference.get();
    }

    public void setSpawnPos(Location location) {
        SpawnPos spawnPos = new SpawnPos(this, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        addSavedLocation(spawnPos);
    }

    public void addSavedLocation(PlotPos plotPos) {
        this.savedLocations.add(plotPos);
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
        member.getSkyblockUser().setPlotUuid(this.getUuid());
        member.getSkyblockUser().saveAll();
    }

    public void removeMember(PlotMember member) {
        members.remove(member);
        member.getSkyblockUser().setPlotUuid("");
        member.getSkyblockUser().saveAll();
    }

    public boolean isMember(String uuid) {
        for (PlotMember member : members) {
            if (member.getUuid().toString().equals(uuid)) return true;
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
        return isInPlot(world);
    }

    public boolean isInPlot(World world) {
        return world.equals(skyWorld.getWorld());
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
        kickToSpawn();

        getMembers().forEach(plotMember -> {
            plotMember.getSkyblockUser().setPlotUuid("");
        });

        this.skyWorld.delete();

        PlotUtils.removePlot(this);

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
        skyWorld.getWorld().getPlayers().stream()
                .map(player -> ModuleUtils.getOrGetPlayer(player.getUniqueId().toString()))
                .forEach(player -> player.sendMessage(message));
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

    public void invite(StreamlineUser user, StreamlineUser sender) {
        if (! user.isOnline()) return;

        if (user.getUuid().equals(sender.getUuid())) {
            sender.sendMessage("&cYou cannot invite yourself.");
            return;
        }

        SkyblockUser skyblockUser = SkyblockUser.transpose(user);
        SkyblockUser skyblockSender = SkyblockUser.transpose(sender);

        if (skyblockUser.isAlreadyInPlot()) {
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

        invitedUsers.add(skyblockUser);

        user.sendMessage("&eYou have been invited to join &c" + getOwnerName() + "&e's island.");
        user.sendMessage("&eType &b/island join " + getOwnerName() + " &eto join.");
    }

    public void acceptInvite(StreamlineUser accepter) {
        SkyblockUser skyblockUser = SkyblockUser.transpose(accepter);

        if (! isInvited(accepter.getUuid())) {
            accepter.sendMessage("&cYou have not been invited to this island.");
            return;
        }

        if (skyblockUser.isAlreadyInPlot()) {
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

        addMember(new PlotMember(UUID.fromString(skyblockUser.getUuid()), role));

        accepter.sendMessage("&eYou have joined &c" + getOwnerName() + "&e's island.");
        messageMembers("&c" + accepter.getName() + " &ehas joined the island.");
    }

    public void denyInvite(StreamlineUser denier) {
        SkyblockUser skyblockUser = SkyblockUser.transpose(denier);

        if (! isInvited(denier.getUuid())) {
            denier.sendMessage("&cYou have not been invited to this island.");
            return;
        }

        if (skyblockUser.isAlreadyInPlot()) {
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

    public void promoteUser(StreamlineUser promoted, StreamlineUser sender) {
        if (promoted.getUuid().equals(sender.getUuid())) {
            sender.sendMessage("&cYou cannot invite yourself.");
            return;
        }

        SkyblockUser skyblockUser = SkyblockUser.transpose(promoted);
        SkyblockUser skyblockSender = SkyblockUser.transpose(sender);

        PlotMember plotSender = getMember(UUID.fromString(skyblockSender.getUuid()));

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

        PlotMember member = getMember(UUID.fromString(promoted.getUuid()));

        if (member.getRole().getIdentifier().equalsIgnoreCase("member")) {
            member.setRole(getAdminRole());
            sender.sendMessage("&eYou have promoted &c" + promoted.getName() + "&e to moderator.");
            promoted.sendMessage("&eYou have been promoted to admin by &c" + sender.getName() + "&e.");
        } else if (member.getRole().getIdentifier().equalsIgnoreCase("admin")) {
            sender.sendMessage("&cThat user is already the highest role. If you want to transfer ownership, use &b/island transfer <name>&c.");
        }
    }

    public void demoteUser(StreamlineUser demoted, StreamlineUser sender) {
        if (demoted.getUuid().equals(sender.getUuid())) {
            sender.sendMessage("&cYou cannot invite yourself.");
            return;
        }

        SkyblockUser skyblockUser = SkyblockUser.transpose(demoted);
        SkyblockUser skyblockSender = SkyblockUser.transpose(sender);

        PlotMember plotSender = getMember(UUID.fromString(skyblockSender.getUuid()));

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

        PlotMember member = getMember(UUID.fromString(demoted.getUuid()));

        if (member.getRole().getIdentifier().equalsIgnoreCase("admin")) {
            member.setRole(getMemberRole());
            sender.sendMessage("&eYou have demoted &c" + demoted.getName() + "&e to member.");
            demoted.sendMessage("&eYou have been demoted to member by &c" + sender.getName() + "&e.");
        } else if (member.getRole().getIdentifier().equalsIgnoreCase("member")) {
            sender.sendMessage("&cThat user is already the lowest role.");
        }
    }

    public void transferOwnership(StreamlineUser newOwner, StreamlineUser sender) {
        if (newOwner.getUuid().equals(sender.getUuid())) {
            sender.sendMessage("&cYou cannot transfer ownership to yourself.");
            return;
        }

        SkyblockUser skyblockUser = SkyblockUser.transpose(newOwner);
        SkyblockUser skyblockSender = SkyblockUser.transpose(sender);

        PlotMember plotSender = getMember(UUID.fromString(skyblockSender.getUuid()));

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

        PlotMember member = getMember(UUID.fromString(newOwner.getUuid()));

        member.setRole(getOwnerRole());
        plotSender.setRole(getAdminRole());
        getSkyWorld().setIdentifier(skyblockUser.getUuid());
        ((SkyblockWorld.SkyblockWorldSerializer) getSkyWorld().getStorageResource()).rename(skyblockUser.getUuid());

        setOwnerUuid(UUID.fromString(skyblockUser.getUuid()));

        skyblockUser.setPlotUuid(getUuid());
        skyblockSender.setPlotUuid(getUuid());

        sender.sendMessage("&eYou have transferred ownership of the island to &c" + newOwner.getName() + "&e.");
        newOwner.sendMessage("&eYou have been promoted to owner by &c" + sender.getName() + "&e.");
    }

    public void kickFromIsland(StreamlineUser toKick, StreamlineUser sender) {
        if (toKick.getUuid().equals(sender.getUuid())) {
            sender.sendMessage("&cYou cannot kick yourself.");
            return;
        }

        SkyblockUser skyblockUser = SkyblockUser.transpose(toKick);
        SkyblockUser skyblockSender = SkyblockUser.transpose(sender);

        PlotMember plotSender = getMember(UUID.fromString(skyblockSender.getUuid()));

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

        PlotMember member = getMember(UUID.fromString(toKick.getUuid()));

        removeMember(member);
        toKick.sendMessage("&cYou have been kicked from the island by &e" + sender.getName() + "&c.");
        sender.sendMessage("&eYou have kicked &c" + toKick.getName() + "&e from the island.");
    }

    /**
     * Handles leaving the island both as a member and as the owner.
     * When an owner leaves the island, if it has members, it will deny it; if there are no members, it will delete the island.
     * If there are no members, it will delete the island.
     */
    public void leaveIsland(StreamlineUser user) {
        SkyblockUser skyblockUser = SkyblockUser.transpose(user);

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

        PlotMember member = getMember(UUID.fromString(user.getUuid()));

        removeMember(member);
        user.sendMessage("&eYou have left the island.");
    }

    public void kickToSpawn() {
        Location spawn = Stratosphere.getMyConfig().getSpawnLocation();

        getPlayersInside().forEach((s, player) -> player.teleport(spawn));
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

        getInvitedUsers().forEach(invite -> builder.append("&b").append(invite.getStreamlineUser().getName()).append("&8, "));
        if (builder.toString().endsWith("&8, "))
            builder.delete(builder.length() - 4, builder.length() - 1);

        return builder.toString();
    }
}
