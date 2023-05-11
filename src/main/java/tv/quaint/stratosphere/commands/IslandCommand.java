package tv.quaint.stratosphere.commands;

import net.streamline.api.command.ModuleCommand;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.pos.PlotPos;
import tv.quaint.stratosphere.plot.schematic.SkyblockSchematic;
import tv.quaint.stratosphere.plot.schematic.tree.SchemTree;
import tv.quaint.stratosphere.plot.upgrades.AchievedUpgrade;
import tv.quaint.stratosphere.users.SkyblockUser;
import tv.quaint.stratosphere.world.SkyblockIOBus;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

public class IslandCommand extends ModuleCommand {
    public IslandCommand() {
        super(Stratosphere.getInstance(), "island", "skyhigh.command.island", "is");
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
                    if (streamlineUser.hasPermission("skyhigh.command.island.reload")) {
                        SkyblockIOBus.reload();
                        ModuleUtils.sendMessage(streamlineUser, "&aReloaded the plugin!");
                    } else {
                        ModuleUtils.sendMessage(streamlineUser, MainMessagesHandler.MESSAGES.INVALID.PERMISSIONS.get());
                    }
                    break;
                case "mainspawn":
                    if (streamlineUser.hasPermission("skyhigh.command.island.mainspawn")) {
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
                    if (streamlineUser.hasPermission("skyhigh.command.island.setspawn")) {
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
                case "spawn":
                    SkyblockPlot plot3 = PlotUtils.getOrGetPlot(player);

                    if (plot3 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    Location location1 = plot3.getSpawnPos().toLocation();

                    if (location1 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cThe spawn location has not been set!");
                        return;
                    }

                    plot3.teleport(player);
                    break;
                case "addhome":
                    if (streamlineUser.hasPermission("skyhigh.command.island.addpos")) {
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
                    if (streamlineUser.hasPermission("skyhigh.command.island.setmainspawn")) {
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

                    plot4.delete();

                    ModuleUtils.sendMessage(streamlineUser, "&aDeleted plot &b" + plot4.getUuid() + "&a!");
                    break;
                case "visit":
                    if (strings.length < 2 || strings[1] == null || strings[1].isBlank() || strings[1].isEmpty()) {
                        ModuleUtils.sendMessage(streamlineUser, "&cUsage: /island visit <player>");
                        return;
                    }

                    String targetName = strings[1];

                    Player targetPlayer = Bukkit.getPlayer(targetName);
                    if (targetPlayer == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName + " &cis not online!");
                        return;
                    }

                    SkyblockPlot targetPlot = PlotUtils.getOrGetPlot(targetPlayer);

                    if (targetPlot == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cPlayer &b" + targetName + " &cdoes not have a plot!");
                        return;
                    }

                    targetPlot.teleport(player);
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

                    SkyblockPlot plot = PlotUtils.getOrGetPlot(player);
                    if (plot == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not a member of a plot!");
                        return;
                    }

                    plot.invite(targetPlayer2, streamlineUser);

                    ModuleUtils.sendMessage(streamlineUser, "&aInvited &b" + targetName2 + " &ato your plot!");
                    break;
                case "accept":
                case "join":
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
                    SkyblockPlot plot103 = PlotUtils.getPlotByLocation(player.getLocation());
                    if (plot103 == null) {
                        ModuleUtils.sendMessage(streamlineUser, "&cYou are not inside an island!");
                        return;
                    }

                    ModuleUtils.sendMessage(streamlineUser, "&aYour role in this plot is:");
                    ModuleUtils.sendMessage(streamlineUser, "&7Name: &b" + plot103.getRole(player).getName());
                    ModuleUtils.sendMessage(streamlineUser, "&7Flags: &b" + plot103.getRole(player).getFlagsString());
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

                    StringBuilder sb = new StringBuilder("&aUpgrades:%newline%");

                    int index = 0;
                    final int finalPage = page2;
                    for (AchievedUpgrade upgrade : plot10.getAchievedUpgrades()) {
                        if (index >= (finalPage - 1) * 10 && index < finalPage * 10) {
                            sb.append("&7- &b").append(upgrade.getType().getPrettyName()).append("&7: &a").append(upgrade.getTier());
                            if (index != plot10.getAchievedUpgrades().size() - 1 && index != finalPage * 10 - 1) {
                                sb.append("%newline%");
                            }
                        }
                    }

                    ModuleUtils.sendMessage(streamlineUser, sb.toString());
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

                            SkyblockUser user = PlotUtils.getOrGetUser(streamlineUser.getUuid());
                            user.setSchematicName(schemMap2.getIdentifier());

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
                        SkyblockUser user = PlotUtils.getOrGetUser(streamlineUser.getUuid());
                        double dust = user.getStarDust();

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
                    // /island dust get <player> // This one is NOT for admins, but requires the permission "skyhigh.island.dust.get".
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

                            SkyblockUser user = PlotUtils.getOrGetUser(playerToGive);
                            SkyblockUser self = PlotUtils.getOrGetUser(streamlineUser.getUuid());

                            if (user == null) {
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

                            user.addStarDust(amountToGive);
                            self.removeStarDust(amountToGive);

                            ModuleUtils.sendMessage(streamlineUser, "&aGave " + playerToGive + " &b" + amountToGive + " &astardust!");
                            ModuleUtils.sendMessage(user.getStreamlineUser(), "&aYou were given &b" + amountToGive + " &astardust by " + streamlineUser.getName() + "!");
                            break;
                        case "take":
                            if (! streamlineUser.hasPermission("skyhigh.admin")) {
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
                            if (! streamlineUser.hasPermission("skyhigh.admin")) {
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
                            if (! streamlineUser.hasPermission("skyhigh.admin")) {
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
                            if (! streamlineUser.hasPermission("skyhigh.command.island.dust.get")) {
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
