package tv.quaint.stratosphere;

import io.streamlined.bukkit.BukkitBase;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import tv.quaint.stratosphere.commands.IslandCommand;
import tv.quaint.stratosphere.config.*;
import tv.quaint.stratosphere.placeholders.StratosphereExpansion;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.events.BukkitListener;
import tv.quaint.stratosphere.plot.events.PlotListener;
import tv.quaint.stratosphere.plot.events.SlimeFunListener;
import tv.quaint.stratosphere.plot.timers.PlotKeepAliveTicker;
import tv.quaint.stratosphere.plot.timers.PlotXPTimer;
import tv.quaint.stratosphere.plot.timers.UserDustTimer;
import tv.quaint.stratosphere.users.SkyblockUser;

public class Stratosphere extends BukkitBase {
    @Getter @Setter
    private static Stratosphere instance;
    @Getter @Setter
    private static MyConfig myConfig;
    @Getter @Setter
    private static GeneratorConfig generatorConfig;
    @Getter @Setter
    private static QuestConfig questConfig;
    @Getter @Setter
    private static MetaDataConfig metaDataConfig;
    @Getter @Setter
    private static UpgradeConfig upgradeConfig;
    @Getter @Setter
    private static TopConfig topConfig;
    @Getter @Setter
    private static PlotPosConfig plotPosConfig;

    @Getter @Setter
    private static PlotKeepAliveTicker plotKeepAliveTicker;

    @Getter @Setter
    private static PlotXPTimer plotXPTimer;
    @Getter @Setter
    private static UserDustTimer userDustTimer;

    @Getter @Setter
    private static IslandCommand islandCommand;

    @Override
    public void enable() {
        // Plugin startup logic
        instance = this;
        PlotUtils.initImmediately();

        myConfig = new MyConfig();
        generatorConfig = new GeneratorConfig();
        GeneratorConfig.initialize();
        questConfig = new QuestConfig();
        metaDataConfig = new MetaDataConfig();
        upgradeConfig = new UpgradeConfig();
        topConfig = new TopConfig();
        plotPosConfig = new PlotPosConfig();

        Bukkit.getPluginManager().registerEvents(new PlotListener(), this);
        Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);
        if (Bukkit.getPluginManager().getPlugin("Slimefun") != null)
            Bukkit.getPluginManager().registerEvents(new SlimeFunListener(), this);

        plotKeepAliveTicker = new PlotKeepAliveTicker();

        plotXPTimer = new PlotXPTimer();
        userDustTimer = new UserDustTimer();

        islandCommand = new IslandCommand();

        new StratosphereExpansion().register();
    }

    @Override
    public void disable() {
        Bukkit.getWorlds().forEach(World::save);

        //            SkyblockIOBus.packWorld(plot.getIdentifier(), plot.getSkyWorld().getWorld());
        PlotUtils.getPlots().forEach(SkyblockPlot::saveAll);

        PlotUtils.getLoadedUsers().forEach(SkyblockUser::saveAll);

        plotKeepAliveTicker.cancel();
    }
}
