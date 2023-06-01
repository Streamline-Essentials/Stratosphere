package tv.quaint.stratosphere.commands;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.config.bits.ConfiguredGenerator;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.members.PlotMember;
import tv.quaint.stratosphere.plot.pos.PlotPos;
import tv.quaint.stratosphere.plot.quests.PlotQuest;
import tv.quaint.stratosphere.plot.schematic.SkyblockSchematic;
import tv.quaint.stratosphere.plot.schematic.tree.SchemTree;
import tv.quaint.stratosphere.plot.upgrades.AchievedUpgrade;
import tv.quaint.stratosphere.plot.upgrades.PlotUpgrade;
import tv.quaint.stratosphere.plot.upgrades.UpgradeRegistry;
import tv.quaint.stratosphere.plot.upgrades.UpgradeTask;
import tv.quaint.stratosphere.users.SkyblockUser;
import tv.quaint.stratosphere.world.SkyblockIOBus;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class IslandCommand extends ModuleCommand {
    public IslandCommand() {
        super(Stratosphere.getInstance(), "island", "stratosphere.command.island", "is");
    }

    @Override
    public void run(StreamlineUser streamlineUser, String[] strings) {
        Player player = Bukkit.getPlayer(UUID.fromString(streamlineUser.getUuid()));
        if (player == null) {
            ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.PLAYER_SELF.get());
            return;
        }

        if (strings.length > 0 && strings[0] != null && ! strings[0].isBlank() && ! strings[0].isEmpty()) {
            switch (strings[0]) {
                case "create":
                    String schematic = "default";

                    if (strings.length > 2 && strings[1] != null && ! strings[1].isBlank() && ! strings[1].isEmpty()) {
                        schematic = strings[1];
                    }

                    if (Objects.equals(schematic, "?")) {
                        StringBuilder builder = new StringBuilder("&aAvailable schematics: &b");

                        for (SchemTree schemTree : Stratosphere.getMyConfig().getSchematicTrees()) {
                            builder.append(schemTree.getIdentifier()).append("&7, &b");
                        }

                        if (builder.toString().endsWith("&7, &b")) {
                            builder.delete(builder.length() - 7, builder.length());
                        }

                        ModuleUtils.sendMessage(streamlineUser, builder.toString());
                        return;
                    }

                    SkyblockPlot plot0 = PlotUtils.getOrGetPlot(player);

                    if (plot0 != null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are already a member of a plot!");
                        plot0.getSpawnPos().teleport(player);
                        return;
                    }

                    plot0 = PlotUtils.createPlot(streamlineUser, schematic);
                    if (plot0 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cFailed to create plot!");
                        return;
                    }

                    ModuleUtils.sendMessage(streamlineUser, "&aCreated plot &b" + plot0.getUuid() + "&a!");

                    plot0.teleport(player);
                    break;
                case "reload":
                    if (streamlineUser.hasPermission("stratosphere.command.island.reload")) {
                        SkyblockIOBus.reload();
                        ModuleUtils.sendMessage(streamlineUser, "&aReloaded the plugin!");
                    } else {
                        ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.PERMISSIONS.get());
                    }
                    break;
                case "spawn":
                    if (streamlineUser.hasPermission("stratosphere.command.island.mainspawn")) {
                        Location location = Stratosphere.getMyConfig().getSpawnLocation();

                        if (location == null) {
                            ModuleUtils.sendMessage(streamlineUser, "&cThe main spawn location has not been set!");
                            return;
                        }

                        player.teleport(location);

                        ModuleUtils.sendMessage(streamlineUser, "&aTeleported to the main spawn location!");
                    } else {
                        ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.PERMISSIONS.get());
                    }
                    break;
                case "setspawn":
                    if (streamlineUser.hasPermission("stratosphere.command.island.setspawn")) {
                        SkyblockPlot plot1 = PlotUtils.getOrGetPlot(player);

                        if (plot1 == null) {
                            ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                            return;
                        }

                        Location location = player.getLocation();

                        plot1.setSpawnPos(location);

                        ModuleUtils.sendMessage(streamlineUser, "&aSet the spawn location to X: &f" + location.getBlockX() + " &aY: &f" + location.getBlockY() + " &aZ: &f" + location.getBlockZ());
                    } else {
                        ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.PERMISSIONS.get());
                    }
                    break;
                case "addhome":
                    if (streamlineUser.hasPermission("stratosphere.command.island.addpos")) {
                        if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                            ModuleUtils.sendMessage(streamlineUser, "&cYou must specify a home name!");
                            return;
                        }

                        String name = strings[1];

                        SkyblockPlot plot4 = PlotUtils.getOrGetPlot(player);

                        if (plot4 == null) {
                            ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                            return;
                        }

                        Location location2 = player.getLocation();

                        PlotPos plotPos = new PlotPos(name, plot4, location2);

                        plot4.addSavedLocation(plotPos);

                        ModuleUtils.sendMessage(streamlineUser, "&aAdded a position to X: &f" + location2.getBlockX() + " &aY: &f" + location2.getBlockY() + " &aZ: &f" + location2.getBlockZ());
                    } else {
                        ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.PERMISSIONS.get());
                    }
                    break;
                case "home":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou must specify a home name!");
                        return;
                    }

                    String name1 = strings[1];

                    SkyblockPlot plot5 = PlotUtils.getOrGetPlot(player);

                    if (plot5 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    PlotPos plotPos = plot5.getSavedLocation(name1);

                    if (plotPos == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cThat position does not exist!");
                        return;
                    }

                    plotPos.teleport(player);
                    break;
                case "setmainspawn":
                    if (streamlineUser.hasPermission("stratosphere.command.island.setmainspawn")) {
                        Location location = player.getLocation();

                        Stratosphere.getMyConfig().saveSpawnLoaction(location);

                        ModuleUtils.sendMessage(streamlineUser, "&aSet the main spawn location to X: &f" + location.getBlockX() + " &aY: &f" + location.getBlockY() + " &aZ: &f" + location.getBlockZ());
                    } else {
                        ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.PERMISSIONS.get());
                    }
                    break;
                case "teleport":
                case "go":
                    SkyblockPlot plot2 = PlotUtils.getOrGetPlot(player);

                    if (plot2 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    plot2.getSpawnPos().teleport(player);

                    ModuleUtils.sendMessage(streamlineUser, "&aTeleported to the plot spawn!");
                    break;
                case "list":
                    int page = 1;

                    if (strings.length > 1 && strings[1] != null && ! strings[1].isBlank() && ! strings[1].isEmpty()) {
                        try {
                            page = Integer.parseInt(strings[1]);
                        } catch (NumberFormatException e) {
                            ModuleUtils.sendMessage(streamlineUser, "&cInvalid page number!");
                            return;
                        }
                    }

                    ConcurrentSkipListSet<SkyblockPlot> plots = PlotUtils.getPlots();

                    ModuleUtils.sendMessage(streamlineUser, "&aPlots:");

                    int i = 0;
                    for (SkyblockPlot plotThing : plots) {
                        if (i >= (page - 1) * 10 && i < page * 10)
                            ModuleUtils.sendMessage(streamlineUser, "&b" + plotThing.getUuid() + " &a- " + plotThing.getOwnerName());
                        i ++;
                    }
                    break;
                case "delete":
                    SkyblockPlot plot4 = PlotUtils.getOrGetPlot(player);

                    if (plot4 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    String uuid = plot4.getUuid();

                    plot4.delete();

                    ModuleUtils.sendMessage(streamlineUser, "&aDeleted plot &b" + uuid + "&a!");
                    break;
                case "visit":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island visit <player>");
                        return;
                    }

                    String targetName = strings[1];

                    SkyblockUser targetUser = PlotUtils.getOrGetUser(targetName);
                    if (targetUser == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName + " &cdoes not exist!");
                        return;
                    }

                    SkyblockPlot targetPlot = PlotUtils.getOrGetPlot(targetUser);

                    if (targetPlot == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName + " &cdoes not have a plot!");
                        return;
                    }

                    if (targetPlot.isPrivate()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName + " &cdoes not have a publicly visitable plot!");
                        return;
                    }

                    targetPlot.teleport(player);

                    ModuleUtils.sendMessage(streamlineUser, "&aTeleported to &b" + targetName + "'s &aplot!");
                    break;
                case "join":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island join <player>");
                        return;
                    }

                    String targetName1 = strings[1];

                    Player targetPlayer1 = Bukkit.getPlayer(targetName1);
                    if (targetPlayer1 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName1 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot targetPlot1 = PlotUtils.getOrGetPlot(targetPlayer1);

                    if (targetPlot1 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName1 + " &cdoes not have a plot!");
                        return;
                    }

                    if (! targetPlot1.isAnyoneCanJoin()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName1 + " &cdoes not have an open to join plot!");
                        return;
                    }

                    PlotMember member = new PlotMember(player.getUniqueId(), targetPlot1.getMemberRole());
                    targetPlot1.addMember(member);

                    ModuleUtils.sendMessage(streamlineUser, "&aJoined &b" + targetName1 + "'s &aplot!");
                    break;
                case "bypass":
                    if (! streamlineUser.hasPermission("stratosphere.bypass")) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou do not have permission to use this command!");
                        return;
                    }

                    SkyblockUser targetUser1 = PlotUtils.getOrGetUser(streamlineUser.getUuid());

                    if (strings.length == 2 && strings[1] != null && ! strings[1].isBlank() && ! strings[1].isEmpty()) {
                        String targetName2 = strings[1];

                        Player targetPlayer2 = Bukkit.getPlayer(targetName2);
                        if (targetPlayer2 == null) {
                            ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName2 + " &cis not online!");
                            return;
                        }

                        targetUser1 = PlotUtils.getOrGetUser(targetPlayer2.getUniqueId().toString());
                    }

                    if (targetUser1 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cCould not find user!");
                        return;
                    }

                    targetUser1.setBypassingPlots(! targetUser1.isBypassingPlots());

                    if (! targetUser1.getStreamlineUser().equals(streamlineUser)) {
                        streamlineUser.sendMessage("&aSet bypassing plots to &f" + targetUser1.isBypassingPlots() + " &afor &f" + targetUser1.getStreamlineUser().getDisplayName() + "&a!");
                    }
                    targetUser1.getStreamlineUser().sendMessage("&aYou are now &f" + (targetUser1.isBypassingPlots() ? "bypassing" : "respecting") + " &aplots!");
                    break;
                case "top":
                    SkyblockPlot.PlotType plotType = null;

                    int topAmount = 10;

                    if (strings.length > 1 && strings[1] != null && ! strings[1].isBlank() && ! strings[1].isEmpty()) {
                        try {
                            plotType = SkyblockPlot.PlotType.valueOf(strings[1].toUpperCase());
                        } catch (IllegalArgumentException exception) {
                            ModuleUtils.sendMessage(streamlineUser, "&cInvalid plot type!");
                            return;
                        }
                        if (strings.length > 2 && strings[2] != null && ! strings[2].isBlank() && ! strings[2].isEmpty()) {
                            try {
                                topAmount = Integer.parseInt(strings[2]);
                            } catch (NumberFormatException exception) {
                                ModuleUtils.sendMessage(streamlineUser, "&cInvalid amount!");
                                return;
                            }
                        }
                    }

                    ConcurrentHashMap<Integer, SkyblockPlot> scores;

                    if (plotType == null) {
                        scores = Stratosphere.getTopConfig().getTopScores(topAmount);

                        ModuleUtils.sendMessage(streamlineUser, "&aTop Islands:");
                    } else {
                        scores = Stratosphere.getTopConfig().getTopScores(plotType, topAmount);

                        ModuleUtils.sendMessage(streamlineUser, "&aTop Islands for &f" + plotType.name() + "&a:");
                    }

                    int position = 1;
                    for (int topPlotI : scores.keySet()) {
                        SkyblockPlot plot = scores.get(topPlotI);
                        if (position > topAmount) {
                            break;
                        }

                        streamlineUser.sendMessage("&b#&f" + position + "&7: " + plot.getOwnerAsUser().getDisplayName() + "&7'&es Island " +
                                "&7(&c" + plot.getPlotType() + "&7) &9-> &f" + plot.calculateScore() + " &escore");

                        position ++;
                    }
                    break;
                case "upgradetype":
                    SkyblockPlot plot = PlotUtils.getOrGetPlot(player);
                    if (plot == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    plot.upgradeType(player);
                    break;
                case "invite":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island invite <player>");
                        return;
                    }

                    String targetName2 = strings[1];

                    StreamlineUser targetPlayer2 = ModuleUtils.getOrGetUserByName(targetName2);
                    if (targetPlayer2 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName2 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot120 = PlotUtils.getOrGetPlot(player);
                    if (plot120 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    plot120.invite(targetPlayer2, streamlineUser);
                    break;
                case "loadall":
                    if (! player.hasPermission("streamline.island.loadall")) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou do not have permission to use this command!");
                        return;
                    }

                    CompletableFuture.runAsync(PlotUtils::loadAllPlots);
                    ModuleUtils.sendMessage(streamlineUser, "&aLoaded all plots!");
                    break;
                case "fixall":
                    if (! player.hasPermission("streamline.island.fixall")) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou do not have permission to use this command!");
                        return;
                    }

//                    CompletableFuture.runAsync(PlotUtils::restoreAllPlotsToLatestSnapshot);
                    ModuleUtils.sendMessage(streamlineUser, "&cThis is currently disabled!");
                    break;
                case "accept":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island join <player>");
                        return;
                    }

                    String targetName3 = strings[1];

                    StreamlineUser targetPlayer3 = ModuleUtils.getOrGetUserByName(targetName3);
                    if (targetPlayer3 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName3 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plotThing = PlotUtils.getOrGetPlot(targetPlayer3.getUuid());
                    if (plotThing == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName3 + " &cdoes not have a plot!");
                        return;
                    }

                    plotThing.acceptInvite(streamlineUser);
                    break;
                case "deny":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island deny <player>");
                        return;
                    }

                    String targetName4 = strings[1];

                    StreamlineUser targetPlayer4 = ModuleUtils.getOrGetUserByName(targetName4);
                    if (targetPlayer4 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName4 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot7 = PlotUtils.getOrGetPlot(targetPlayer4.getUuid());
                    if (plot7 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName4 + " &cdoes not have a plot!");
                        return;
                    }

                    plot7.denyInvite(streamlineUser);
                    break;
                case "leave":
                    SkyblockPlot plot101 = PlotUtils.getOrGetPlot(player);
                    if (plot101 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    plot101.leaveIsland(streamlineUser);
                    break;
                case "info":
                    SkyblockPlot plot102 = PlotUtils.getOrGetPlot(player);
                    if (plot102 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    ModuleUtils.sendMessage(streamlineUser, "&aPlot &b" + plot102.getUuid() + "&a:");
                    ModuleUtils.sendMessage(streamlineUser, "&aOwner: &b" + plot102.getOwnerName());
                    ModuleUtils.sendMessage(streamlineUser, "&aMembers: &b" + plot102.getMemberNames());
                    ModuleUtils.sendMessage(streamlineUser, "&aInvited: &b" + plot102.getInvitedNames());
                    ModuleUtils.sendMessage(streamlineUser, "&aXP: &b" + plot102.getXp());
                    ModuleUtils.sendMessage(streamlineUser, "&aLevel: &b" + plot102.getLevel());
                    break;
                case "current-plot-role":
                    SkyblockPlot plot110 = PlotUtils.getPlotByLocation(player.getLocation());
                    if (plot110 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not inside an island!");
                        return;
                    }

                    ModuleUtils.sendMessage(streamlineUser, "&aYour role in this plot is:");
                    ModuleUtils.sendMessage(streamlineUser, "&7Name: &b" + plot110.getRole(player).getName());
                    ModuleUtils.sendMessage(streamlineUser, "&7Flags: &b" + plot110.getRole(player).getFlagsString());
                    break;
                case "promote":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island promote <player>");
                        return;
                    }

                    String targetName5 = strings[1];

                    StreamlineUser targetPlayer5 = ModuleUtils.getOrGetUserByName(targetName5);
                    if (targetPlayer5 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName5 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot6 = PlotUtils.getOrGetPlot(player);
                    if (plot6 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    plot6.promoteUser(targetPlayer5, streamlineUser);
                    break;
                case "demote":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island demote <player>");
                        return;
                    }

                    String targetName6 = strings[1];

                    StreamlineUser targetPlayer6 = ModuleUtils.getOrGetUserByName(targetName6);
                    if (targetPlayer6 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName6 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot100 = PlotUtils.getOrGetPlot(player);
                    if (plot100 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    plot100.demoteUser(targetPlayer6, streamlineUser);
                    break;
                case "transfer":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island transfer <player>");
                        return;
                    }

                    String targetName7 = strings[1];

                    StreamlineUser targetPlayer7 = ModuleUtils.getOrGetUserByName(targetName7);
                    if (targetPlayer7 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName7 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot8 = PlotUtils.getOrGetPlot(player);
                    if (plot8 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    plot8.transferOwnership(targetPlayer7, streamlineUser);
                    break;
                case "kick":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island kick <player>");
                        return;
                    }

                    String targetName8 = strings[1];

                    StreamlineUser targetPlayer8 = ModuleUtils.getOrGetUserByName(targetName8);
                    if (targetPlayer8 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName8 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot9 = PlotUtils.getOrGetPlot(player);
                    if (plot9 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    plot9.kickFromIsland(targetPlayer8, streamlineUser);
                    break;
                case "upgrade":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island upgrade <upgrade>");
                        return;
                    }

                    String upgradeName = strings[1];

                    SkyblockPlot plot104 = PlotUtils.getOrGetPlot(player);

                    SkyblockUser user = PlotUtils.getOrGetUser(player.getUniqueId().toString());

                    if (plot104 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    int tier = 1;

                    AchievedUpgrade achievedUpgrade = plot104.getAchievedUpgrade(upgradeName);

                    if (achievedUpgrade != null) tier = achievedUpgrade.getTier() + 1;

                    PlotUpgrade.UpgradeType type;
                    try {
                        type = PlotUpgrade.UpgradeType.valueOf(upgradeName.toUpperCase());
                    } catch (Exception e) {
                        ModuleUtils.sendMessage(streamlineUser, "&cInvalid upgrade name!");
                        return;
                    }

                    UpgradeRegistry.UpgradeIdentifier upgradeIdentifier = new UpgradeRegistry.UpgradeIdentifier(type, tier);
                    PlotUpgrade plotUpgrade = PlotUtils.getUpgradeRegistry().getGetter().get(upgradeIdentifier, PlotUtils.getUpgradeRegistry());

                    if (plotUpgrade == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cInvalid upgrade name!");
                        return;
                    }

                    if (plotUpgrade.getDustCost() > user.getStarDust()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou do not have enough dust to purchase this upgrade!");
                        return;
                    }

                    UpgradeTask task = new UpgradeTask(player, plotUpgrade);
                    plotUpgrade.doUpgrade(plot104, task);
                    break;
                case "upgrades":
                    int page102 = 1;

                    if (strings.length > 1) {
                        try {
                            page102 = Integer.parseInt(strings[1]);
                        } catch (NumberFormatException e) {
                            ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island quests [page]");
                            return;
                        }
                    }

                    if (Stratosphere.getUpgradeConfig().getLoadedUpgrades().size() <= 0) {
                        ModuleUtils.sendMessage(streamlineUser, "&cThere are no upgrades loaded!");
                        return;
                    }

                    int actualPage = Math.max(1, Math.min(page102, Stratosphere.getUpgradeConfig().getLoadedUpgrades().size()));

                    int maxPages2 = Stratosphere.getUpgradeConfig().getLoadedUpgrades().size();

                    StringBuilder sb102 = new StringBuilder("&aUpgrades &7(&epage &f" + actualPage + " &eof &f" + maxPages2 + "&7):%newline%");

                    PlotUpgrade upgrade = new ArrayList<>(Stratosphere.getUpgradeConfig().getLoadedUpgrades()).get(actualPage - 1);
                    sb102.append("&7- &b").append(upgrade.getIdentifier());
                    sb102.append("%newline%   &7> &eType&7: &c").append(upgrade.getType().name());
                    sb102.append("%newline%   &7> &eTier&7: &f").append(upgrade.getTier());
//                    sb102.append("%newline%   &7> &ePayload&7: ").append(upgrade.getPayload());
                    sb102.append("%newline%   &7> &eNeeded Dust&7: &f").append(upgrade.getDustCost());
                    sb102.append("%newline%   &7> &eDescription&7: &b").append(upgrade.getDescription());

                    ModuleUtils.sendMessage(streamlineUser, sb102.toString());
                    break;
                case "generators":
                    int page103 = 1;

                    if (strings.length > 1) {
                        try {
                            page103 = Integer.parseInt(strings[1]);
                        } catch (NumberFormatException e) {
                            ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island quests [page]");
                            return;
                        }
                    }

                    if (Stratosphere.getGeneratorConfig().getLoadedGenerators().size() <= 0) {
                        ModuleUtils.sendMessage(streamlineUser, "&cThere are no generators loaded!");
                        return;
                    }

                    int actualPage3 = Math.max(1, Math.min(page103, Stratosphere.getGeneratorConfig().getLoadedGenerators().size()));

                    int maxPages1 = Stratosphere.getGeneratorConfig().getLoadedGenerators().size();

                    StringBuilder sb103 = new StringBuilder("&aGenerators &7(&epage &f" + actualPage3 + " &eof &f" + maxPages1 + "&7):%newline%");

                    ConfiguredGenerator generator = new ArrayList<>(Stratosphere.getGeneratorConfig().getLoadedGenerators()).get(actualPage3 - 1);
                    sb103.append("&7- &b").append(generator.getIdentifier());
                    sb103.append("%newline%   &7> &aTier: ").append(generator.getTier());
//                    sb102.append("%newline%   &7> &aPayload: ").append(upgrade.getPayload());
                    sb103.append("%newline%   &7> &aMaterials: ");
                    for (Map.Entry<Material, Double> entry : generator.getMaterials().entrySet()) {
                        Material material = entry.getKey();
                        double chance = entry.getValue();
                        sb103.append("%newline%      &7- &c").append(material.name()).append(" &e(&f").append(chance).append(" &aweight&e)");
                    }

                    ModuleUtils.sendMessage(streamlineUser, sb103.toString());
                    break;
                case "upgraded":
                    int page2 = 1;

                    if (strings.length > 1) {
                        try {
                            page2 = Integer.parseInt(strings[1]);
                        } catch (NumberFormatException e) {
                            ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island upgraded [page]");
                            return;
                        }
                    }

                    SkyblockPlot plot10 = PlotUtils.getOrGetPlot(player);
                    if (plot10 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    int maxPages = (int) Math.ceil(plot10.getAchievedUpgrades().size() / 10.0);

                    StringBuilder sb = new StringBuilder("&aUpgraded &7(&epage &f" + page2 + " &eof &f" + maxPages + "&7):%newline%");

                    int index = 0;
                    final int finalPage = page2;
                    for (AchievedUpgrade upgraded : plot10.getAchievedUpgrades()) {
                        if (index >= (finalPage - 1) * 10 && index < finalPage * 10) {
                            sb.append("&7- &b").append(upgraded.getType().getPrettyName()).append("&7: &a").append(upgraded.getTier());
                            if (index != plot10.getAchievedUpgrades().size() - 1 && index != finalPage * 10 - 1) {
                                sb.append("%newline%");
                            }
                        }
                    }

                    ModuleUtils.sendMessage(streamlineUser, sb.toString());
                    break;
                case "quests":
                    int page100 = 1;

                    if (strings.length > 1) {
                        try {
                            page100 = Integer.parseInt(strings[1]);
                        } catch (NumberFormatException e) {
                            ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island quests [page]");
                            return;
                        }
                    }

                    if (Stratosphere.getQuestConfig().getLoadedQuests().size() <= 0) {
                        ModuleUtils.sendMessage(streamlineUser, "&cThere are no quests loaded!");
                        return;
                    }

                    SkyblockUser questUser = PlotUtils.getOrGetUser(player.getUniqueId().toString());
                    if (questUser == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cCould not find your user!");
                        return;
                    }

                    int maxPages3 = Stratosphere.getQuestConfig().getLoadedQuests().size();

                    int actualPage2 = Math.max(1, Math.min(page100, Stratosphere.getQuestConfig().getLoadedQuests().size()));

                    StringBuilder sb100 = new StringBuilder("&aQuests &7(&epage &f" + actualPage2 + " &eof &f" + maxPages3 + "&7):%newline%");

                    PlotQuest quest = new ArrayList<>(Stratosphere.getQuestConfig().getLoadedQuests()).get(actualPage2 - 1);
                    sb100.append("&7- &b").append(quest.getIdentifier());
                    sb100.append("%newline%   &7> &eType&7: &c").append(quest.getType().name());
                    sb100.append("%newline%   &7> &eSub Type&7: &c").append(quest.getThing().getName());
                    sb100.append("%newline%   &7> &eNeeded Amount&7: &f").append(quest.getAmount());
//                    sb100.append("%newline%   &7> &aRewards: ");
//                    for (QuestReward reward : quest.getRewards()) {
//                        sb100.append("%newline%      &7- &a").append(reward.getType()).append(": &f").append(reward.getPayload());
//                    }
                    sb100.append("%newline%   &7> &eDescription&7: &b").append(quest.getDescription());
                    sb100.append("%newline%%newline%   &7> &eCompleted&7: &b").append(questUser.isQuestCompleted(quest.getIdentifier()));

                    ModuleUtils.sendMessage(streamlineUser, sb100.toString());
                    break;
                case "schem":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island schem <save | load | remove | paste> <schematic>");
                        return;
                    }

                    switch (strings[1]) {
                        case "save":
                            CompletableFuture.runAsync(() -> {
                                if (strings.length < 3 || strings[2] == null || strings[2].isBlank() || strings[2].isEmpty()) {
                                    ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island schem save <schematic>");
                                    return;
                                }

                                String schematicName = strings[2];

                                SkyblockSchematic schemMap = null;

                                try {
                                    schemMap = SkyblockIOBus.fabricate(player, schematicName);
                                } catch (Exception e) {
                                    ModuleUtils.sendMessage(streamlineUser, "&cSchematic " + schematicName + " does not exist!");
                                    return;
                                }

                                if (schemMap == null) {
                                    ModuleUtils.sendMessage(streamlineUser, "&cFailed to save schematic!");
                                    return;
                                }

                                schemMap.save();

                                ModuleUtils.sendMessage(streamlineUser, "&aSaved schematic " + schematicName + "!");
                            });
                            break;
                        case "load":
                            if (strings.length < 3 || strings[2] == null || strings[2].isBlank() || strings[2].isEmpty()) {
                                ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island schem load <schematic>");
                                return;
                            }

                            String schematicName2 = strings[2];

                            SkyblockSchematic schemMap2 = null;

                            try {
                                schemMap2 = new SkyblockSchematic(schematicName2);
                            } catch (Exception e) {
                                ModuleUtils.sendMessage(streamlineUser, "&cSchematic " + schematicName2 + " does not exist!");
                                return;
                            }

                            SkyblockUser user100 = PlotUtils.getOrGetUser(streamlineUser.getUuid());
                            user100.setSchematicName(schemMap2.getIdentifier());

                            ModuleUtils.sendMessage(streamlineUser, "&aLoaded schematic " + schemMap2.getIdentifier() + "!");
                            break;
                        case "delete":
                        case "remove":
                            SkyblockUser user2 = PlotUtils.getOrGetUser(streamlineUser.getUuid());

                            String schematicName3 = user2.getSchematicName();

                            SkyblockSchematic schemMap3 = null;
                            try {
                                schemMap3 = new SkyblockSchematic(schematicName3);
                            } catch (Exception e) {
                                ModuleUtils.sendMessage(streamlineUser, "&cSchematic " + schematicName3 + " does not exist!");
                                return;
                            }

                            schemMap3.delete();

                            ModuleUtils.sendMessage(streamlineUser, "&aDeleted schematic " + schematicName3 + "!");
                            break;
                        case "paste":
                            SkyblockUser user3 = PlotUtils.getOrGetUser(streamlineUser.getUuid());

                            String schematicName4 = user3.getSchematicName();

                            SkyblockSchematic schemMap4 = null;
                            try {
                                schemMap4 = new SkyblockSchematic(schematicName4);
                            } catch (Exception e) {
                                ModuleUtils.sendMessage(streamlineUser, "&cSchematic " + schematicName4 + " does not exist!");
                                return;
                            }

                            try {
                                schemMap4.paste(player.getLocation());
                            } catch (Exception e) {
                                ModuleUtils.sendMessage(streamlineUser, "&cSchematic is not loaded!");
                                e.printStackTrace();
                                return;
                            }

                            ModuleUtils.sendMessage(streamlineUser, "&aPasted schematic!");
                            break;
                    }
                    break;
                case "dust":
                case "stardust":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        SkyblockUser user100 = PlotUtils.getOrGetUser(streamlineUser.getUuid());
                        double dust = user100.getStarDust();

                        streamlineUser.sendMessage("&aYou have &b" + dust + " &astardust!");
//                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island dust <give | take | set> <player> <amount>");
                        return;
                    }

                    String dustAction = strings[1];

                    // Follow this pattern:
                    // /island dust give <player> <amount> // This one is NOT for admins.
                    // /island dust take <player> <amount> // This one IS for admins.
                    // /island dust set <player> <amount> // This one IS for admins.
                    // /island dust add <player> <amount> // This one IS for admins.
                    // /island dust get <player> // This one is NOT for admins, but requires the permission "stratosphere.island.dust.get".
                    // For admin commands, do not subtract or add or set from or to the executor's dust.
                    // Do one at a time.
                    switch (dustAction) {
                        case "pay":
                        case "give":
                            if (strings.length < 4 || strings[2] == null || strings[2].isBlank() || strings[2].isEmpty() || strings[3] == null || strings[3].isBlank() || strings[3].isEmpty()) {
                                ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island dust give <player> <amount>");
                                return;
                            }

                            String playerToGive = strings[2];

                            double amountToGive = 0;

                            try {
                                amountToGive = Double.parseDouble(strings[3]);
                            } catch (Exception e) {
                                ModuleUtils.sendMessage(streamlineUser, "&cInvalid amount!");
                                return;
                            }

                            SkyblockUser user100 = PlotUtils.getOrGetUser(playerToGive);
                            SkyblockUser self = PlotUtils.getOrGetUser(streamlineUser.getUuid());

                            if (user100 == null) {
                                ModuleUtils.sendMessage(streamlineUser, "&cPlayer " + playerToGive + " does not exist!");
                                return;
                            }
                            if (self == null) {
                                ModuleUtils.sendMessage(streamlineUser, "&cPlayer " + streamlineUser.getName() + " does not exist!");
                                return;
                            }

                            if (self.getStarDust() < amountToGive) {
                                ModuleUtils.sendMessage(streamlineUser, "&cYou do not have enough stardust!");
                                return;
                            }

                            user100.addStarDust(amountToGive);
                            self.removeStarDust(amountToGive);

                            ModuleUtils.sendMessage(streamlineUser, "&aGave " + playerToGive + " &b" + amountToGive + " &astardust!");
                            ModuleUtils.sendMessage(user100.getStreamlineUser(), "&aYou were given &b" + amountToGive + " &astardust by " + streamlineUser.getName() + "!");
                            break;
                        case "take":
                            if (! streamlineUser.hasPermission("stratosphere.admin")) {
                                ModuleUtils.sendMessage(streamlineUser, "&cYou do not have permission to use this command!");
                                return;
                            }

                            if (strings.length < 4 || strings[2] == null || strings[2].isBlank() || strings[2].isEmpty() || strings[3] == null || strings[3].isBlank() || strings[3].isEmpty()) {
                                ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island dust take <player> <amount>");
                                return;
                            }

                            String playerToTake = strings[2];

                            double amountToTake = 0;

                            try {
                                amountToTake = Double.parseDouble(strings[3]);
                            } catch (Exception e) {
                                ModuleUtils.sendMessage(streamlineUser, "&cInvalid amount!");
                                return;
                            }

                            SkyblockUser user2 = PlotUtils.getOrGetUser(playerToTake);

                            if (user2 == null) {
                                ModuleUtils.sendMessage(streamlineUser, "&cPlayer " + playerToTake + " does not exist!");
                                return;
                            }

                            user2.removeStarDust(amountToTake);

                            ModuleUtils.sendMessage(streamlineUser, "&aTook &b" + amountToTake + " &astardust from " + playerToTake + "!");
                            break;
                        case "set":
                            if (! streamlineUser.hasPermission("stratosphere.admin")) {
                                ModuleUtils.sendMessage(streamlineUser, "&cYou do not have permission to use this command!");
                                return;
                            }

                            if (strings.length < 4 || strings[2] == null || strings[2].isBlank() || strings[2].isEmpty() || strings[3] == null || strings[3].isBlank() || strings[3].isEmpty()) {
                                ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island dust set <player> <amount>");
                                return;
                            }

                            String playerToSet = strings[2];

                            double amountToSet = 0;

                            try {
                                amountToSet = Double.parseDouble(strings[3]);
                            } catch (Exception e) {
                                ModuleUtils.sendMessage(streamlineUser, "&cInvalid amount!");
                                return;
                            }

                            SkyblockUser user3 = PlotUtils.getOrGetUser(playerToSet);

                            if (user3 == null) {
                                ModuleUtils.sendMessage(streamlineUser, "&cPlayer " + playerToSet + " does not exist!");
                                return;
                            }

                            user3.setStarDust(amountToSet);

                            ModuleUtils.sendMessage(streamlineUser, "&aSet " + playerToSet + "'s stardust to &b" + amountToSet + "&a!");
                            break;
                        case "add":
                            if (! streamlineUser.hasPermission("stratosphere.admin")) {
                                ModuleUtils.sendMessage(streamlineUser, "&cYou do not have permission to use this command!");
                                return;
                            }

                            if (strings.length < 4 || strings[2] == null || strings[2].isBlank() || strings[2].isEmpty() || strings[3] == null || strings[3].isBlank() || strings[3].isEmpty()) {
                                ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island dust add <player> <amount>");
                                return;
                            }

                            String playerToAdd = strings[2];

                            double amountToAdd = 0;

                            try {
                                amountToAdd = Double.parseDouble(strings[3]);
                            } catch (Exception e) {
                                ModuleUtils.sendMessage(streamlineUser, "&cInvalid amount!");
                                return;
                            }

                            SkyblockUser user4 = PlotUtils.getOrGetUser(playerToAdd);

                            if (user4 == null) {
                                ModuleUtils.sendMessage(streamlineUser, "&cPlayer " + playerToAdd + " does not exist!");
                                return;
                            }

                            user4.addStarDust(amountToAdd);

                            ModuleUtils.sendMessage(streamlineUser, "&aAdded &b" + amountToAdd + " &astardust to " + playerToAdd + "!");
                            break;
                        case "get":
                            if (! streamlineUser.hasPermission("stratosphere.command.island.dust.get")) {
                                ModuleUtils.sendMessage(streamlineUser, "&cYou do not have permission to use this command!");
                                return;
                            }

                            if (strings.length < 3 || strings[2] == null || strings[2].isBlank() || strings[2].isEmpty()) {
                                ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island dust get <player>");
                                return;
                            }

                            String playerToGet = strings[2];

                            SkyblockUser user5 = PlotUtils.getOrGetUser(playerToGet);

                            if (user5 == null) {
                                ModuleUtils.sendMessage(streamlineUser, "&cPlayer " + playerToGet + " does not exist!");
                                return;
                            }

                            ModuleUtils.sendMessage(streamlineUser, "&a" + playerToGet + " has &b" + user5.getStarDust() + " &astardust!");
                            break;
                    }
                    break;
                default:
                    // Grab the help menu from the switch statements above.
                    StringBuilder builder = new StringBuilder("&b&lIsland Help\n");
                    builder.append("&b/island dust give <player> <amount> &7- Give a player stardust\n");
                    builder.append("&b/island dust take <player> <amount> &7- Take stardust from a player\n");
                    builder.append("&b/island dust set <player> <amount> &7- Set a player's stardust\n");
                    builder.append("&b/island dust add <player> <amount> &7- Add stardust to a player\n");
                    builder.append("&b/island dust get <player> &7- Get a player's stardust\n");
                    builder.append("&b/island create <schematic tree name> &7- Create an island\n");
                    builder.append("&b/island delete &7- Delete your island\n");
                    builder.append("&b/island go &7- Teleport to your island\n");
                    builder.append("&b/island addhome &7- Set your island's home\n");
                    builder.append("&b/island invite <player> &7- Invite a player to your island\n");
//                    builder.append("&b/island uninvite <player> &7- Uninvite a player from your island\n");
                    builder.append("&b/island kick <player> &7- Kick a player from your island\n");
                    builder.append("&b/island leave &7- Leave your island\n");
                    builder.append("&b/island home <name> &7- Teleport to your island's home\n");
                    builder.append("&b/island delete &7- Delete your island\n");
                    builder.append("&b/island setspawn &7- Set your island's spawn\n");
                    builder.append("&b/island spawn &7- Teleport to your island's spawn\n");
//                    builder.append("&b/island setbiome <biome> &7- Set your island's biome\n");
                    builder.append("&b/island current-plot-role &7- View your role in your current plot\n");
                    builder.append("&b/island visit &7- Visit another player's island\n");

                    streamlineUser.sendMessage(builder.toString());
                    break;
            }
        } else {
            // Grab the help menu from the switch statements above.
            StringBuilder builder = new StringBuilder("&b&lIsland Help\n");
            builder.append("&b/island dust give <player> <amount> &7- Give a player stardust\n");
            builder.append("&b/island dust take <player> <amount> &7- Take stardust from a player\n");
            builder.append("&b/island dust set <player> <amount> &7- Set a player's stardust\n");
            builder.append("&b/island dust add <player> <amount> &7- Add stardust to a player\n");
            builder.append("&b/island dust get <player> &7- Get a player's stardust\n");
            builder.append("&b/island create <schematic tree name> &7- Create an island\n");
            builder.append("&b/island delete &7- Delete your island\n");
            builder.append("&b/island go &7- Teleport to your island\n");
            builder.append("&b/island addhome &7- Set your island's home\n");
            builder.append("&b/island invite <player> &7- Invite a player to your island\n");
//                    builder.append("&b/island uninvite <player> &7- Uninvite a player from your island\n");
            builder.append("&b/island kick <player> &7- Kick a player from your island\n");
            builder.append("&b/island leave &7- Leave your island\n");
            builder.append("&b/island home <name> &7- Teleport to your island's home\n");
            builder.append("&b/island delete &7- Delete your island\n");
            builder.append("&b/island setspawn &7- Set your island's spawn\n");
            builder.append("&b/island spawn &7- Teleport to your island's spawn\n");
//                    builder.append("&b/island setbiome <biome> &7- Set your island's biome\n");
            builder.append("&b/island current-plot-role &7- View your role in your current plot\n");
            builder.append("&b/island visit &7- Visit another player's island\n");

            streamlineUser.sendMessage(builder.toString());
        }
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser streamlineUser, String[] strings) {
        return null;
    }
}
