package tv.quaint.stratosphere;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.SimpleModule;
import net.streamline.apib.SLAPIB;
import org.bukkit.Bukkit;
import org.bukkit.World;
import tv.quaint.stratosphere.commands.IslandCommand;
import tv.quaint.stratosphere.config.GeneratorConfig;
import tv.quaint.stratosphere.config.MyConfig;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.events.BukkitListener;
import tv.quaint.stratosphere.plot.events.PlotListener;
import tv.quaint.stratosphere.plot.events.SlimeFunListener;
import tv.quaint.stratosphere.timers.PlotKeepAliveTicker;
import tv.quaint.stratosphere.users.SkyblockUser;
import tv.quaint.stratosphere.world.SkyblockIOBus;
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
    private static PlotKeepAliveTicker plotKeepAliveTicker;

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

        myConfig = new MyConfig();
        generatorConfig = new GeneratorConfig();
        GeneratorConfig.initialize();

        PlotUtils.init();

        Bukkit.getPluginManager().registerEvents(new PlotListener(), SLAPIB.getPlugin());
        Bukkit.getPluginManager().registerEvents(new BukkitListener(), SLAPIB.getPlugin());
        if (Bukkit.getPluginManager().getPlugin("Slimefun") != null)
            Bukkit.getPluginManager().registerEvents(new SlimeFunListener(), SLAPIB.getPlugin());

        plotKeepAliveTicker = new PlotKeepAliveTicker();
    }

    @Override
    public void onDisable() {
        Bukkit.getWorlds().forEach(World::save);

        // Plugin shutdown logic
        PlotUtils.getPlots().forEach(plot -> {
            plot.saveAll();

            SkyblockIOBus.packWorld(plot.getIdentifier(), plot.getSkyWorld().getWorld());
        });

        PlotUtils.getLoadedUsers().forEach(SkyblockUser::saveAll);

        plotKeepAliveTicker.cancel();
    }
}
