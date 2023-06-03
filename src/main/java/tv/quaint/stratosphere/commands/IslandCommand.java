package tv.quaint.stratosphere.commands;

import tv.quaint.stratosphere.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import tv.quaint.stratosphere.plot.upgrades.UpgradeTask;
import tv.quaint.stratosphere.users.SkyblockUser;
import tv.quaint.stratosphere.world.SkyblockIOBus;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class IslandCommand extends AbstractQuaintCommand {
    public IslandCommand() {
//        super(Stratosphere.getInstance(), "island", "stratosphere.command.island", "is");
        super("island", Stratosphere.getInstance());

        MessageUtils.logInfo("IslandCommand registered!");
    }
    
    public boolean isUsable(String[] args, int index) {
        return args.length > index && args[index] != null && ! args[index].isEmpty() && ! args[index].isBlank() && ! args[index].equals("");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        run(sender, command, label, args);
        return true;
    }

    public void run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (! (sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "&cYou must be a player to use this command!");
            return;
        }
        Player player = (Player) sender;

        SkyblockUser skyblockUser = PlotUtils.getOrGetUser(player.getUniqueId().toString());
        if (skyblockUser == null) {
            MessageUtils.sendMessage(player, "&cFailed to get your user data!");
            return;
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "create":
                    String schematic = "default";

                    if (isUsable(args, 1)) {
                        schematic = args[1];
                    }

                    if (Objects.equals(schematic, "?")) {
                        StringBuilder builder = new StringBuilder("&aAvailable schematics: &b");

                        for (SchemTree schemTree : Stratosphere.getMyConfig().getSchematicTrees()) {
                            builder.append(schemTree.getIdentifier()).append("&7, &b");
                        }

                        if (builder.toString().endsWith("&7, &b")) {
                            builder.delete(builder.length() - 7, builder.length());
                        }

                        skyblockUser.sendMessage(builder.toString());
                        return;
                    }

                    SkyblockPlot plot0 = PlotUtils.getOrGetPlot(player);

                    if (plot0 != null) {
                        skyblockUser.sendMessage("&cYou are already a member of a plot!");
                        plot0.getSpawnPos().teleport(player);
                        return;
                    }

                    plot0 = PlotUtils.createPlot(skyblockUser, schematic);
                    if (plot0 == null) {
                        skyblockUser.sendMessage("&cFailed to create plot!");
                        return;
                    }

                    skyblockUser.sendMessage("&aCreated plot &b" + plot0.getUuid() + "&a!");

                    plot0.teleport(player);
                    break;
                case "reload":
                    if (skyblockUser.hasPermission("stratosphere.island.reload")) {
                        SkyblockIOBus.reload();
                        skyblockUser.sendMessage("&aReloaded the plugin!");
                    } else {
                        skyblockUser.sendMessage("&cYou do not have enough permissions to do this...!");
                    }
                    break;
                case "spawn":
                    if (skyblockUser.hasPermission("stratosphere.island.mainspawn")) {
                        Location location = Stratosphere.getMyConfig().getSpawnLocation();

                        if (location == null) {
                            skyblockUser.sendMessage("&cThe main spawn location has not been set!");
                            return;
                        }

                        player.teleport(location);

                        skyblockUser.sendMessage("&aTeleported to the main spawn location!");
                    } else {
                        skyblockUser.sendMessage("&cYou do not have enough permissions to do this...!");
                    }
                    break;
                case "setspawn":
                    if (skyblockUser.hasPermission("stratosphere.island.setspawn")) {
                        SkyblockPlot plot1 = PlotUtils.getOrGetPlot(player);

                        if (plot1 == null) {
                            skyblockUser.sendMessage("&cYou are not a member of a plot!");
                            return;
                        }

                        Location location = player.getLocation();

                        plot1.setSpawnPos(location);

                        skyblockUser.sendMessage("&aSet the spawn location to X: &f" + location.getBlockX() + " &aY: &f" + location.getBlockY() + " &aZ: &f" + location.getBlockZ());
                    } else {
                        skyblockUser.sendMessage("&cYou do not have enough permissions to do this...!");
                    }
                    break;
                case "addhome":
                    if (skyblockUser.hasPermission("stratosphere.island.addhome")) {
                        if (! isUsable(args, 1)) {
                            skyblockUser.sendMessage("&cYou must specify a home name!");
                            return;
                        }

                        String addHomeName = args[1];

                        SkyblockPlot plot4 = PlotUtils.getOrGetPlot(player);

                        if (plot4 == null) {
                            skyblockUser.sendMessage("&cYou are not a member of a plot!");
                            return;
                        }

                        Location location2 = player.getLocation();

                        PlotPos plotPos = new PlotPos(addHomeName, plot4, location2);

                        plot4.addSavedLocation(plotPos);

                        skyblockUser.sendMessage("&aAdded a position to X: &f" + location2.getBlockX() + " &aY: &f" + location2.getBlockY() + " &aZ: &f" + location2.getBlockZ());
                    } else {
                        skyblockUser.sendMessage("&cYou do not have enough permissions to do this...!");
                    }
                    break;
                case "home":
                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cYou must specify a home name!");
                        return;
                    }

                    String goHomeName = args[1];

                    SkyblockPlot plot5 = PlotUtils.getOrGetPlot(player);

                    if (plot5 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    PlotPos plotPos = plot5.getSavedLocation(goHomeName);

                    if (plotPos == null) {
                        skyblockUser.sendMessage("&cThat position does not exist!");
                        return;
                    }

                    plotPos.teleport(player);
                    break;
                case "setmainspawn":
                    if (skyblockUser.hasPermission("stratosphere.island.setmainspawn")) {
                        Location location = player.getLocation();

                        Stratosphere.getMyConfig().saveSpawnLoaction(location);

                        skyblockUser.sendMessage("&aSet the main spawn location to X: &f" + location.getBlockX() + " &aY: &f" + location.getBlockY() + " &aZ: &f" + location.getBlockZ());
                    } else {
                        skyblockUser.sendMessage("&cYou do not have enough permissions to do this...!");
                    }
                    break;
                case "teleport":
                case "go":
                    SkyblockPlot plot2 = PlotUtils.getOrGetPlot(player);

                    if (plot2 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    plot2.getSpawnPos().teleport(player);

                    skyblockUser.sendMessage("&aTeleported to the plot spawn!");
                    break;
                case "list":
                    if (! skyblockUser.hasPermission("stratosphere.island.list")) {
                        skyblockUser.sendMessage("&cYou do not have enough permissions to do this...!");
                    }

                    int page = 1;

                    if (isUsable(args, 1)) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            skyblockUser.sendMessage("&cInvalid page number!");
                            return;
                        }
                    }

                    ConcurrentSkipListSet<SkyblockPlot> plots = PlotUtils.getPlots();

                    skyblockUser.sendMessage("&aPlots:");

                    int i = 0;
                    for (SkyblockPlot plotThing : plots) {
                        if (i >= (page - 1) * 10 && i < page * 10)
                            skyblockUser.sendMessage("&b" + plotThing.getUuid() + " &a- " + plotThing.getOwnerName());
                        i ++;
                    }
                    break;
                case "delete":
                    SkyblockPlot plot4 = PlotUtils.getOrGetPlot(player);

                    if (plot4 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    String uuid = plot4.getUuid();

                    plot4.delete();

                    skyblockUser.sendMessage("&aDeleted plot &b" + uuid + "&a!");
                    break;
                case "visit":
                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cYou must specify a player name!");
                        return;
                    }

                    String targetName = args[1];

                    SkyblockUser targetUser = PlotUtils.getOrGetUser(targetName);
                    if (targetUser == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName + " &cdoes not exist!");
                        return;
                    }

                    SkyblockPlot targetPlot = PlotUtils.getOrGetPlot(targetUser);

                    if (targetPlot == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName + " &cdoes not have a plot!");
                        return;
                    }

                    if (targetPlot.isPrivate()) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName + " &cdoes not have a publicly visitable plot!");
                        return;
                    }

                    targetPlot.teleport(player);

                    skyblockUser.sendMessage("&aTeleported to &b" + targetName + "'s &aplot!");
                    break;
                case "join":
                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cYou must specify a player name!");
                        return;
                    }

                    String targetName1 = args[1];

                    Player targetPlayer1 = Bukkit.getPlayer(targetName1);
                    if (targetPlayer1 == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName1 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot targetPlot1 = PlotUtils.getOrGetPlot(targetPlayer1);

                    if (targetPlot1 == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName1 + " &cdoes not have a plot!");
                        return;
                    }

                    if (! targetPlot1.isAnyoneCanJoin()) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName1 + " &cdoes not have an open to join plot!");
                        return;
                    }

                    PlotMember member = new PlotMember(player.getUniqueId(), targetPlot1.getMemberRole());
                    targetPlot1.addMember(member);

                    skyblockUser.sendMessage("&aJoined &b" + targetName1 + "'s &aplot!");
                    break;
                case "bypass":
                    if (! skyblockUser.hasPermission("stratosphere.island.bypass")) {
                        skyblockUser.sendMessage("&cYou do not have permission to use this command!");
                        return;
                    }

                    SkyblockUser targetUser1 = PlotUtils.getOrGetUser(skyblockUser.getUuid());

                    if (isUsable(args, 1)) {
                        String targetName2 = args[1];

                        Player targetPlayer2 = Bukkit.getPlayer(targetName2);
                        if (targetPlayer2 == null) {
                            skyblockUser.sendMessage("&cPlayer &b" + targetName2 + " &cis not online!");
                            return;
                        }

                        targetUser1 = PlotUtils.getOrGetUser(targetPlayer2.getUniqueId().toString());
                    }

                    if (targetUser1 == null) {
                        skyblockUser.sendMessage("&cCould not find user!");
                        return;
                    }

                    targetUser1.setBypassingPlots(! targetUser1.isBypassingPlots());

                    if (! targetUser1.equals(skyblockUser)) {
                        skyblockUser.sendMessage("&aSet bypassing plots to &f" + targetUser1.isBypassingPlots() + " &afor &f" + targetUser1.getDisplayName() + "&a!");
                    }
                    targetUser1.sendMessage("&aYou are now &f" + (targetUser1.isBypassingPlots() ? "bypassing" : "respecting") + " &aplots!");
                    break;
                case "top":
                    SkyblockPlot.PlotType plotType = null;

                    int topAmount = 10;

                    if (isUsable(args, 1)) {
                        try {
                            plotType = SkyblockPlot.PlotType.valueOf(args[1].toUpperCase());
                        } catch (IllegalArgumentException exception) {
                            skyblockUser.sendMessage("&cInvalid plot type!");
                            return;
                        }
                        if (isUsable(args, 2)) {
                            try {
                                topAmount = Integer.parseInt(args[2]);
                            } catch (NumberFormatException exception) {
                                skyblockUser.sendMessage("&cInvalid amount!");
                                return;
                            }
                        }
                    }

                    ConcurrentHashMap<Integer, SkyblockPlot> scores;

                    if (plotType == null) {
                        scores = Stratosphere.getTopConfig().getTopScores(topAmount);

                        skyblockUser.sendMessage("&aTop Islands:");
                    } else {
                        scores = Stratosphere.getTopConfig().getTopScores(plotType, topAmount);

                        skyblockUser.sendMessage("&aTop Islands for &f" + plotType.name() + "&a:");
                    }

                    int position = 1;
                    for (int topPlotI : scores.keySet()) {
                        SkyblockPlot plot = scores.get(topPlotI);
                        if (position > topAmount) {
                            break;
                        }

                        skyblockUser.sendMessage("&b#&f" + position + "&7: " + plot.getOwnerAsUser().getDisplayName() + "&7'&es Island " +
                                "&7(&c" + plot.getPlotType() + "&7) &9-> &f" + plot.calculateScore() + " &escore");

                        position ++;
                    }
                    break;
                case "upgradetype":
                    SkyblockPlot plot = PlotUtils.getOrGetPlot(player);
                    if (plot == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    plot.upgradeType(player);
                    break;
                case "invite":
                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cYou must specify a player name!");
                        return;
                    }

                    String targetName2 = args[1];

                    SkyblockUser targetPlayer2 = PlotUtils.getOrGetUser(targetName2);
                    if (targetPlayer2 == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName2 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot120 = PlotUtils.getOrGetPlot(player);
                    if (plot120 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    plot120.invite(targetPlayer2, skyblockUser);
                    break;
                case "loadall":
                    if (! player.hasPermission("stratosphere.island.loadall")) {
                        skyblockUser.sendMessage("&cYou do not have permission to use this command!");
                        return;
                    }

                    CompletableFuture.runAsync(PlotUtils::loadAllPlots);
                    skyblockUser.sendMessage("&aLoaded all plots!");
                    break;
                case "fixall":
                    if (! player.hasPermission("stratosphere.island.fixall")) {
                        skyblockUser.sendMessage("&cYou do not have permission to use this command!");
                        return;
                    }

//                    CompletableFuture.runAsync(PlotUtils::restoreAllPlotsToLatestSnapshot);
                    skyblockUser.sendMessage("&cThis is currently disabled!");
                    break;
                case "accept":
                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cYou must specify a player name!");
                        return;
                    }

                    String targetName3 = args[1];

                    SkyblockUser targetPlayer3 = PlotUtils.getOrGetUser(targetName3);
                    if (targetPlayer3 == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName3 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plotThing = PlotUtils.getOrGetPlot(targetPlayer3.getUuid());
                    if (plotThing == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName3 + " &cdoes not have a plot!");
                        return;
                    }

                    plotThing.acceptInvite(skyblockUser);
                    break;
                case "deny":
                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cYou must specify a player name!");
                        return;
                    }

                    String targetName4 = args[1];

                    SkyblockUser targetPlayer4 = PlotUtils.getOrGetUser(targetName4);
                    if (targetPlayer4 == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName4 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot7 = PlotUtils.getOrGetPlot(targetPlayer4.getUuid());
                    if (plot7 == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName4 + " &cdoes not have a plot!");
                        return;
                    }

                    plot7.denyInvite(skyblockUser);
                    break;
                case "leave":
                    SkyblockPlot plot101 = PlotUtils.getOrGetPlot(player);
                    if (plot101 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    plot101.leaveIsland(skyblockUser);
                    break;
                case "info":
                    SkyblockPlot plot102 = PlotUtils.getOrGetPlot(player);
                    if (plot102 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    skyblockUser.sendMessage("&aPlot &b" + plot102.getUuid() + "&a:");
                    skyblockUser.sendMessage("&aOwner: &b" + plot102.getOwnerName());
                    skyblockUser.sendMessage("&aMembers: &b" + plot102.getMemberNames());
                    skyblockUser.sendMessage("&aInvited: &b" + plot102.getInvitedNames());
                    skyblockUser.sendMessage("&aXP: &b" + plot102.getXp());
                    skyblockUser.sendMessage("&aLevel: &b" + plot102.getLevel());
                    break;
                case "current-plot-role":
                    SkyblockPlot plot110 = PlotUtils.getPlotByLocation(player.getLocation());
                    if (plot110 == null) {
                        skyblockUser.sendMessage("&cYou are not inside an island!");
                        return;
                    }

                    skyblockUser.sendMessage("&aYour role in this plot is:");
                    skyblockUser.sendMessage("&7Name: &b" + plot110.getRole(player).getName());
                    skyblockUser.sendMessage("&7Flags: &b" + plot110.getRole(player).getFlagsString());
                    break;
                case "promote":
                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cYou must specify a player name!");
                        return;
                    }

                    String targetName5 = args[1];

                    SkyblockUser targetPlayer5 = PlotUtils.getOrGetUser(targetName5);
                    if (targetPlayer5 == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName5 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot6 = PlotUtils.getOrGetPlot(player);
                    if (plot6 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    plot6.promoteUser(targetPlayer5, skyblockUser);
                    break;
                case "demote":
                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cYou must specify a player name!");
                        return;
                    }

                    String targetName6 = args[1];

                    SkyblockUser targetPlayer6 = PlotUtils.getOrGetUser(targetName6);
                    if (targetPlayer6 == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName6 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot100 = PlotUtils.getOrGetPlot(player);
                    if (plot100 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    plot100.demoteUser(targetPlayer6, skyblockUser);
                    break;
                case "transfer":
                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cYou must specify a player name!");
                        return;
                    }

                    String targetName7 = args[1];

                    SkyblockUser targetPlayer7 = PlotUtils.getOrGetUser(targetName7);
                    if (targetPlayer7 == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName7 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot8 = PlotUtils.getOrGetPlot(player);
                    if (plot8 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    plot8.transferOwnership(targetPlayer7, skyblockUser);
                    break;
                case "kick":
                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cYou must specify a player name!");
                        return;
                    }

                    String targetName8 = args[1];

                    SkyblockUser targetPlayer8 = PlotUtils.getOrGetUser(targetName8);
                    if (targetPlayer8 == null) {
                        skyblockUser.sendMessage("&cPlayer &b" + targetName8 + " &cis not online!");
                        return;
                    }

                    SkyblockPlot plot9 = PlotUtils.getOrGetPlot(player);
                    if (plot9 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    plot9.kickFromIsland(targetPlayer8, skyblockUser);
                    break;
                case "upgrade":
                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cYou must specify an upgrade name!");
                        return;
                    }

                    String upgradeName = args[1];

                    SkyblockPlot plot104 = PlotUtils.getOrGetPlot(player);

                    SkyblockUser user = PlotUtils.getOrGetUser(player.getUniqueId().toString());

                    if (plot104 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    PlotUpgrade plotUpgrade = Stratosphere.getUpgradeConfig().getUpgrade(upgradeName);
                    if (plotUpgrade == null) {
                        skyblockUser.sendMessage("&cInvalid upgrade name!");
                        return;
                    }

                    if (plotUpgrade.getDustCost() > user.getStarDust()) {
                        skyblockUser.sendMessage("&cYou do not have enough dust to purchase this upgrade!");
                        return;
                    }

                    if (! plot104.canPurchaseUpgrade(plotUpgrade)) {
                        skyblockUser.sendMessage("&cYou cannot purchase this upgrade! This is most likely due to it having too high or too low of an upgrade tier.");
                        return;
                    }

                    plot104.addAchievedUpgrade(new AchievedUpgrade(plot104, plotUpgrade));

                    UpgradeTask task = new UpgradeTask(player, plotUpgrade);
                    plotUpgrade.doUpgrade(plot104, task);

                    user.sendMessage("&aYou have purchased the upgrade &b" + plotUpgrade.getIdentifier() + "&a!");
                    break;
                case "upgrades":
                    int page102 = 1;

                    if (isUsable(args, 1)) {
                        try {
                            page102 = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            skyblockUser.sendMessage("&cInvalid page number!");
                            return;
                        }
                    }

                    if (Stratosphere.getUpgradeConfig().getLoadedUpgrades().size() <= 0) {
                        skyblockUser.sendMessage("&cThere are no upgrades loaded!");
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

                    skyblockUser.sendMessage(sb102.toString());
                    break;
                case "generators":
                    int page103 = 1;

                    if (isUsable(args, 1)) {
                        try {
                            page103 = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            skyblockUser.sendMessage("&cInvalid page number!");
                            return;
                        }
                    }

                    if (Stratosphere.getGeneratorConfig().getLoadedGenerators().size() <= 0) {
                        skyblockUser.sendMessage("&cThere are no generators loaded!");
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

                    skyblockUser.sendMessage(sb103.toString());
                    break;
                case "upgraded":
                    int page2 = 1;

                    if (isUsable(args, 1)) {
                        try {
                            page2 = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            skyblockUser.sendMessage("&cInvalid page number!");
                            return;
                        }
                    }

                    SkyblockPlot plot10 = PlotUtils.getOrGetPlot(player);
                    if (plot10 == null) {
                        skyblockUser.sendMessage("&cYou are not a member of a plot!");
                        return;
                    }

                    int maxPages = (int) Math.ceil(plot10.getAchievedUpgrades().size() / 10.0);

                    StringBuilder sb = new StringBuilder("&aUpgraded &7(&epage &f" + page2 + " &eof &f" + maxPages + "&7):%newline%");

                    int index = 0;
                    final int finalPage = page2;
                    for (AchievedUpgrade upgraded : plot10.getAchievedUpgrades()) {
                        if (index >= (finalPage - 1) * 10 && index < finalPage * 10) {
                            sb.append("&7- &b").append(upgraded.getUpgrade().getType().getPrettyName()).append("&7: &a").append(upgraded.getUpgrade().getTier());
                            if (index != plot10.getAchievedUpgrades().size() - 1) {
                                sb.append("%newline%");
                            }
                        }
                    }

                    skyblockUser.sendMessage(sb.toString());
                    break;
                case "quests":
                    int page100 = 1;

                    if (isUsable(args, 1)) {
                        try {
                            page100 = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            skyblockUser.sendMessage("&cInvalid page number!");
                            return;
                        }
                    }

                    if (Stratosphere.getQuestConfig().getLoadedQuests().size() <= 0) {
                        skyblockUser.sendMessage("&cThere are no quests loaded!");
                        return;
                    }

                    SkyblockUser questUser = PlotUtils.getOrGetUser(player.getUniqueId().toString());
                    if (questUser == null) {
                        skyblockUser.sendMessage("&cCould not find your user!");
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

                    skyblockUser.sendMessage(sb100.toString());
                    break;
                case "schem":
                case "schematic":
                case "schematics":
                    if (! skyblockUser.hasPermission("stratosphere.island.schematics")) {
                        skyblockUser.sendMessage("&cYou do not have permission to use this command!");
                        return;
                    }

                    if (! isUsable(args, 1)) {
                        skyblockUser.sendMessage("&cUsage: /island schem <save | load | remove | paste> <schematic>");
                        return;
                    }

                    switch (args[1].toLowerCase()) {
                        case "save":
                            CompletableFuture.runAsync(() -> {
                                if (! isUsable(args, 2)) {
                                    skyblockUser.sendMessage("&cUsage: /island schem save <schematic>");
                                    return;
                                }

                                String schematicName = args[2];

                                SkyblockSchematic schemMap = null;

                                try {
                                    schemMap = SkyblockIOBus.fabricate(player, schematicName);
                                } catch (Exception e) {
                                    skyblockUser.sendMessage("&cSchematic " + schematicName + " does not exist!");
                                    return;
                                }

                                if (schemMap == null) {
                                    skyblockUser.sendMessage("&cFailed to save schematic!");
                                    return;
                                }

                                schemMap.save();

                                skyblockUser.sendMessage("&aSaved schematic " + schematicName + "!");
                            });
                            break;
                        case "load":
                            if (! isUsable(args, 2)) {
                                skyblockUser.sendMessage("&cUsage: /island schem load <schematic>");
                                return;
                            }

                            String schematicName2 = args[2];

                            SkyblockSchematic schemMap2 = null;

                            try {
                                schemMap2 = new SkyblockSchematic(schematicName2);
                            } catch (Exception e) {
                                skyblockUser.sendMessage("&cSchematic " + schematicName2 + " does not exist!");
                                return;
                            }

                            SkyblockUser user100 = PlotUtils.getOrGetUser(skyblockUser.getUuid());
                            user100.setSchematicName(schemMap2.getIdentifier());

                            skyblockUser.sendMessage("&aLoaded schematic " + schemMap2.getIdentifier() + "!");
                            break;
                        case "delete":
                        case "remove":
                            SkyblockUser user2 = PlotUtils.getOrGetUser(skyblockUser.getUuid());

                            String schematicName3 = user2.getSchematicName();

                            SkyblockSchematic schemMap3 = null;
                            try {
                                schemMap3 = new SkyblockSchematic(schematicName3);
                            } catch (Exception e) {
                                skyblockUser.sendMessage("&cSchematic " + schematicName3 + " does not exist!");
                                return;
                            }

                            schemMap3.delete();

                            skyblockUser.sendMessage("&aDeleted schematic " + schematicName3 + "!");
                            break;
                        case "paste":
                            SkyblockUser user3 = PlotUtils.getOrGetUser(skyblockUser.getUuid());

                            String schematicName4 = user3.getSchematicName();

                            SkyblockSchematic schemMap4 = null;
                            try {
                                schemMap4 = new SkyblockSchematic(schematicName4);
                            } catch (Exception e) {
                                skyblockUser.sendMessage("&cSchematic " + schematicName4 + " does not exist!");
                                return;
                            }

                            try {
                                schemMap4.paste(player.getLocation());
                            } catch (Exception e) {
                                skyblockUser.sendMessage("&cSchematic is not loaded!");
                                e.printStackTrace();
                                return;
                            }

                            skyblockUser.sendMessage("&aPasted schematic!");
                            break;
                    }
                    break;
                case "dust":
                case "stardust":
                    if (! isUsable(args, 1)) {
                        SkyblockUser user100 = PlotUtils.getOrGetUser(skyblockUser.getUuid());
                        double dust = user100.getStarDust();

                        skyblockUser.sendMessage("&aYou have &b" + dust + " &astardust!");
//                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island dust <give | take | set> <player> <amount>");
                        return;
                    }

                    // Follow this pattern:
                    // /island dust give <player> <amount> // This one is NOT for admins.
                    // /island dust take <player> <amount> // This one IS for admins.
                    // /island dust set <player> <amount> // This one IS for admins.
                    // /island dust add <player> <amount> // This one IS for admins.
                    // /island dust get <player> // This one is NOT for admins, but requires the permission "stratosphere.island.dust.get".
                    // For admin commands, do not subtract or add or set from or to the executor's dust.
                    // Do one at a time.
                    switch (args[1].toLowerCase()) {
                        case "pay":
                        case "give":
                            if (! isUsable(args, 2) || ! isUsable(args, 3)) {
                                skyblockUser.sendMessage("&cUsage: /island dust give <player> <amount>");
                                return;
                            }

                            String playerToGive = args[2];

                            double amountToGive = 0;

                            try {
                                amountToGive = Double.parseDouble(args[3]);
                            } catch (Exception e) {
                                skyblockUser.sendMessage("&cInvalid amount!");
                                return;
                            }

                            SkyblockUser user100 = PlotUtils.getOrGetUser(playerToGive);
                            SkyblockUser self = PlotUtils.getOrGetUser(skyblockUser.getUuid());

                            if (user100 == null) {
                                skyblockUser.sendMessage("&cPlayer " + playerToGive + " does not exist!");
                                return;
                            }
                            if (self == null) {
                                skyblockUser.sendMessage("&cPlayer " + skyblockUser.getName() + " does not exist!");
                                return;
                            }

                            if (self.getStarDust() < amountToGive) {
                                skyblockUser.sendMessage("&cYou do not have enough stardust!");
                                return;
                            }

                            user100.addStarDust(amountToGive);
                            self.removeStarDust(amountToGive);

                            skyblockUser.sendMessage("&aGave " + playerToGive + " &b" + amountToGive + " &astardust!");
                            user100.sendMessage("&aYou were given &b" + amountToGive + " &astardust by " + skyblockUser.getName() + "!");
                            break;
                        case "take":
                            if (! skyblockUser.hasPermission("stratosphere.admin")) {
                                skyblockUser.sendMessage("&cYou do not have permission to use this command!");
                                return;
                            }

                            if (! isUsable(args, 2) || ! isUsable(args, 3)) {
                                skyblockUser.sendMessage("&cUsage: /island dust take <player> <amount>");
                                return;
                            }

                            String playerToTake = args[2];

                            double amountToTake = 0;

                            try {
                                amountToTake = Double.parseDouble(args[3]);
                            } catch (Exception e) {
                                skyblockUser.sendMessage("&cInvalid amount!");
                                return;
                            }

                            SkyblockUser user2 = PlotUtils.getOrGetUser(playerToTake);

                            if (user2 == null) {
                                skyblockUser.sendMessage("&cPlayer " + playerToTake + " does not exist!");
                                return;
                            }

                            user2.removeStarDust(amountToTake);

                            skyblockUser.sendMessage("&aTook &b" + amountToTake + " &astardust from " + playerToTake + "!");
                            break;
                        case "set":
                            if (! skyblockUser.hasPermission("stratosphere.admin")) {
                                skyblockUser.sendMessage("&cYou do not have permission to use this command!");
                                return;
                            }

                            if (! isUsable(args, 2) || ! isUsable(args, 3)) {
                                skyblockUser.sendMessage("&cUsage: /island dust set <player> <amount>");
                                return;
                            }

                            String playerToSet = args[2];

                            double amountToSet = 0;

                            try {
                                amountToSet = Double.parseDouble(args[3]);
                            } catch (Exception e) {
                                skyblockUser.sendMessage("&cInvalid amount!");
                                return;
                            }

                            SkyblockUser user3 = PlotUtils.getOrGetUser(playerToSet);

                            if (user3 == null) {
                                skyblockUser.sendMessage("&cPlayer " + playerToSet + " does not exist!");
                                return;
                            }

                            user3.setStarDust(amountToSet);

                            skyblockUser.sendMessage("&aSet " + playerToSet + "'s stardust to &b" + amountToSet + "&a!");
                            break;
                        case "add":
                            if (! skyblockUser.hasPermission("stratosphere.admin")) {
                                skyblockUser.sendMessage("&cYou do not have permission to use this command!");
                                return;
                            }

                            if (! isUsable(args, 2) || ! isUsable(args, 3)) {
                                skyblockUser.sendMessage("&cUsage: /island dust add <player> <amount>");
                                return;
                            }

                            String playerToAdd = args[2];

                            double amountToAdd = 0;

                            try {
                                amountToAdd = Double.parseDouble(args[3]);
                            } catch (Exception e) {
                                skyblockUser.sendMessage("&cInvalid amount!");
                                return;
                            }

                            SkyblockUser user4 = PlotUtils.getOrGetUser(playerToAdd);

                            if (user4 == null) {
                                skyblockUser.sendMessage("&cPlayer " + playerToAdd + " does not exist!");
                                return;
                            }

                            user4.addStarDust(amountToAdd);

                            skyblockUser.sendMessage("&aAdded &b" + amountToAdd + " &astardust to " + playerToAdd + "!");
                            break;
                        case "get":
                            if (! skyblockUser.hasPermission("stratosphere.command.island.dust.get")) {
                                skyblockUser.sendMessage("&cYou do not have permission to use this command!");
                                return;
                            }

                            if (! isUsable(args, 2)) {
                                skyblockUser.sendMessage("&cUsage: /island dust get <player>");
                                return;
                            }

                            String playerToGet = args[2];

                            SkyblockUser user5 = PlotUtils.getOrGetUser(playerToGet);

                            if (user5 == null) {
                                skyblockUser.sendMessage("&cPlayer " + playerToGet + " does not exist!");
                                return;
                            }

                            skyblockUser.sendMessage("&a" + playerToGet + " has &b" + user5.getStarDust() + " &astardust!");
                            break;
                    }
                    break;
                case "help":
                    int page1000 = 1;

                    if (isUsable(args, 1)) {
                        try {
                            page1000 = Integer.parseInt(args[1]);
                        } catch (Exception e) {
                            skyblockUser.sendMessage("&cInvalid page!");
                            return;
                        }
                    }

                    if (page1000 < 1) {
                        skyblockUser.sendMessage("&cInvalid page!");
                        return;
                    }

                    String aHelp = getDefaultAnswer(skyblockUser, page1000);

                    skyblockUser.sendMessage(aHelp);
                default:
                    String a = getDefaultAnswer(skyblockUser, 1);

                    skyblockUser.sendMessage(a);
                    break;
            }
        } else {
            String a = getDefaultAnswer(skyblockUser, 1);

            skyblockUser.sendMessage(a);
        }
    }

    public static String getDefaultAnswer(SkyblockUser user, int page) {
        List<String> lines = new ArrayList<>();

        lines.add("&b/island dust give <player> <amount> &7- &aGive a player stardust.");
        lines.add("&b/island dust take <player> <amount> &7- &aTake stardust from a player.");
        lines.add("&b/island dust set <player> <amount> &7- &aSet a player's stardust.");
        lines.add("&b/island dust add <player> <amount> &7- &aAdd stardust to a player.");
        lines.add("&b/island dust get <player> &7- &aGet a player's stardust.");

        lines.add("&b/island create <schematic tree name> &7- &aCreate an island.");
        lines.add("&b/island delete &7- &aDelete your island.");

        lines.add("&b/island go &7- &aTeleport to your island's spawn.");
        if (user.hasPermission("stratosphere.island.setspawn")) lines.add("&b/island setspawn &7- &aSet your island's spawn.");

        if (user.hasPermission("stratosphere.island.addhome")) lines.add("&b/island addhome &7- &aSet your island's home\n");
        lines.add("&b/island home <name> &7- &aTeleport to your island's home.");

        lines.add("&b/island invite <player> &7- &aInvite a player to your island.");
        lines.add("&b/island accept <player> &7- &aAccept an island invite from a player.");
        lines.add("&b/island deny <player> &7- &aDeny an island invite from a player.");
        lines.add("&b/island join <player> &7- &aJoin a player's island if it is open to joins.");

        lines.add("&b/island kick <player> &7- &aKick a player from your island.");
        lines.add("&b/island leave &7- &aLeave your island.");
        lines.add("&b/island visit &7- &aVisit another player's island.");

        lines.add("&b/island promote <player> &7- &aPromote a player in your island.");
        lines.add("&b/island demote <player> &7- &aDemote a player in your island.");
        lines.add("&b/island transfer <player> &7- &aTransfer your island to another player.");

        lines.add("&b/island upgrade <upgrade> &7- &aUpgrade your island with a specific upgrade.");
        lines.add("&b/island upgrades <page> &7- &aShows a list of all island upgrades.");
        lines.add("&b/island upgraded <page> &7- &aShows a list of your island's upgrades.");

        lines.add("&b/island generators <page> &7- &aShows a list of all island generators.");
        lines.add("&b/island quests <page> &7- &aShows a list of all island quests.");

//                    lines.add("&b/island setbiome <biome> &7- Set your island's biome.");

        lines.add("&b/island current-plot-role &7- &aView your role in your current plot.");
        lines.add("&b/island info &7- &aView your island's info.");

        lines.add("&b/island top &7- &aList the top islands (without respecting island type).");
        lines.add("&b/island top <type> &7- &aList the top islands (with respecting island type).");

        lines.add("&b/island upgradetype &7- &aUpgrade your island type.");

        if (user.hasPermission("stratosphere.island.mainspawn")) lines.add("&b/island spawn &7- &aTeleport to the server's main spawn.");
        if (user.hasPermission("stratosphere.island.setmainspawn")) lines.add("&b/island setmainspawn &7- &aSets the server's main spawn.");
        if (user.hasPermission("stratosphere.island.list")) lines.add("&b/island list &7- &aList all islands.");
        if (user.hasPermission("stratosphere.island.reload")) lines.add("&b/island reload &7- &aReloads the plugin.");
        if (user.hasPermission("stratosphere.island.bypass")) lines.add("&b/island bypass &7- &aToggles island flag bypass mode.");
        if (user.hasPermission("stratosphere.island.loadall")) lines.add("&b/island loadall &7- &aForce-loads all islands.");
        if (user.hasPermission("stratosphere.island.schematics")) lines.add("&b/island schematics <save | load | delete | paste> &7- &aManage island schematics.");

        lines.add("&b/island help &7- &aShows this help menu.");

        int maxPages = (int) Math.ceil(lines.size() / 10.0);

        StringBuilder sb = new StringBuilder("&aIsland Help &7(&epage &f" + page + " &eof &f" + maxPages + "&7):%newline%");

        int index = 0;
        for (String line : lines) {
            if (index >= (page - 1) * 10 && index < page * 10) {
                sb.append(line);
                if (index != lines.size() - 1) {
                    sb.append("%newline%");
                }
            }
        }

        sb.append("%newline%&aUse &f/island help <page> &ato view other pages.");

        return sb.toString();
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (! (sender instanceof Player)) return new ArrayList<>();
        Player player = (Player) sender;

        SkyblockUser user = PlotUtils.getOrGetUser(player.getUniqueId().toString());
        if (user == null) return new ArrayList<>();

        ConcurrentSkipListSet<String> options = new ConcurrentSkipListSet<>();

        if (args.length == 1) {
            options.add("dust");

            options.add("create");
            options.add("delete");

            options.add("go");
            options.add("setspawn");
            options.add("addhome");
            options.add("home");

            options.add("invite");
            options.add("accept");
            options.add("deny");
            options.add("join");

            options.add("kick");
            options.add("leave");
            options.add("visit");

            options.add("promote");
            options.add("demote");
            options.add("transfer");

            options.add("upgrade");
            options.add("upgrades");
            options.add("upgraded");

            options.add("generators");
            options.add("quests");

            options.add("current-plot-role");
            options.add("info");

            options.add("top");

            options.add("upgradetype");

            options.add("spawn");
            options.add("setmainspawn");

            options.add("list");
            options.add("reload");
            options.add("bypass");
            options.add("loadall");
            options.add("schematics");

            options.add("help");
        } else if (args.length >= 2) {
            SkyblockPlot plot = PlotUtils.getOrGetPlot(user);

            if (args[0].equalsIgnoreCase("dust")) {
                if (args.length == 2) {
                    options.add("give");
                    options.add("take");
                    options.add("set");
                    options.add("add");
                    options.add("get");
                }
                if (args.length == 3) {
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                }
            }
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length == 2) {
                    Stratosphere.getMyConfig().getSchematicTrees().forEach((schematicTree) -> options.add(schematicTree.getIdentifier()));
                }
            }
            if (args[0].equalsIgnoreCase("home")) {
                if (args.length == 2) {
                    if (plot != null) {
                        plot.getSavedLocations().forEach((home) -> options.add(home.getIdentifier()));
                    }
                }
            }
            if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("accept")
                    || args[0].equalsIgnoreCase("deny") || args[0].equalsIgnoreCase("join")) {
                if (args.length == 2) {
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                }
            }
            if (args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("promote")
                    || args[0].equalsIgnoreCase("demote") || args[0].equalsIgnoreCase("transfer")) {
                if (args.length == 2) {
                    if (plot != null) {
                        plot.getMembers().forEach((member) -> options.add(member.getUser().getName()));
                    }
                }
            }
            if (args[0].equalsIgnoreCase("upgrade")) {
                if (args.length == 2) {
                    Stratosphere.getUpgradeConfig().getLoadedUpgrades().forEach((upgrade) -> options.add(upgrade.getIdentifier()));
                }
            }
            if (args[0].equalsIgnoreCase("upgrades") || args[0].equalsIgnoreCase("upgraded")
                    || args[0].equalsIgnoreCase("generators") || args[0].equalsIgnoreCase("quests")) {
                if (args.length == 2) {
                    options.add("1");
                    options.add("2");
                    options.add("3");
                    options.add("4");
                    options.add("5");
                }
            }
            if (args[0].equalsIgnoreCase("top")) {
                if (args.length == 2) {
                    Arrays.stream(SkyblockPlot.PlotType.values()).forEach((plotType) -> options.add(plotType.name()));
                }
            }
            if (args[0].equalsIgnoreCase("schematics")) {
                if (args.length == 2) {
                    options.add("save");
                    options.add("load");
                    options.add("delete");
                    options.add("paste");
                }
                if (args.length == 3) {
                    options.add("");
                }
            }
        }

        return options.stream().toList();
    }
}
