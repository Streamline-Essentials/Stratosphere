package tv.quaint.stratosphere;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.SimpleModule;
import net.streamline.apib.SLAPIB;
import org.bukkit.Bukkit;
import org.bukkit.World;
import tv.quaint.stratosphere.commands.IslandCommand;
import tv.quaint.stratosphere.config.*;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.events.BukkitListener;
import tv.quaint.stratosphere.plot.events.PlotListener;
import tv.quaint.stratosphere.plot.events.SlimeFunListener;
import tv.quaint.stratosphere.plot.timers.PlotXPTimer;
import tv.quaint.stratosphere.plot.timers.UserDustTimer;
import tv.quaint.stratosphere.plot.timers.PlotKeepAliveTicker;
import tv.quaint.stratosphere.users.SkyblockUser;
import tv.quaint.thebase.lib.pf4j.PluginWrapper;

import java.util.List;

public class Stratosphere extends SimpleModule {
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
    private static PlotKeepAliveTicker plotKeepAliveTicker;

    @Getter @Setter
    private static PlotXPTimer plotXPTimer;
    @Getter @Setter
    private static UserDustTimer userDustTimer;

    public Stratosphere(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void registerCommands() {
        setCommands(List.of(
                new IslandCommand()
        ));
    }

    @Override
    public void onEnable() {
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

        Bukkit.getPluginManager().registerEvents(new PlotListener(), SLAPIB.getPlugin());
        Bukkit.getPluginManager().registerEvents(new BukkitListener(), SLAPIB.getPlugin());
        if (Bukkit.getPluginManager().getPlugin("Slimefun") != null)
            Bukkit.getPluginManager().registerEvents(new SlimeFunListener(), SLAPIB.getPlugin());

        plotKeepAliveTicker = new PlotKeepAliveTicker();

        plotXPTimer = new PlotXPTimer();
        userDustTimer = new UserDustTimer();
    }

    @Override
    public void onDisable() {
        Bukkit.getWorlds().forEach(World::save);

        //            SkyblockIOBus.packWorld(plot.getIdentifier(), plot.getSkyWorld().getWorld());
        PlotUtils.getPlots().forEach(SkyblockPlot::saveAll);

        PlotUtils.getLoadedUsers().forEach(SkyblockUser::saveAll);

        plotKeepAliveTicker.cancel();
    }
}
