package tv.quaint.stratosphere.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import tv.quaint.storage.documents.SimpleJsonDocument;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.config.bits.PlotPosition;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.quests.QuestContainer;

import java.util.concurrent.ConcurrentSkipListSet;

public class PlotPosConfig extends SimpleJsonDocument {
    public PlotPosConfig() {
        super("plot-positions.json", Stratosphere.getInstance(), false);
    }

    @Override
    public void onInit() {
    }

    @Override
    public void onSave() {

    }

    public ConcurrentSkipListSet<PlotPosition> getPlotPositions() {
        reloadResource();

        ConcurrentSkipListSet<PlotPosition> r = new ConcurrentSkipListSet<>();

        getResource().singleLayerKeySet("plot-positions").forEach(key -> {
            try {
                double x = getResource().getDouble("plot-positions." + key + ".x");
                double z = getResource().getDouble("plot-positions." + key + ".z");

                PlotPosition questContainer = new PlotPosition(key, x, z);

                r.add(questContainer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return r;
    }

    public void savePlotPosition(PlotPosition plotPosition) {
        getResource().set("plot-positions." + plotPosition.getIdentifier() + ".x", plotPosition.getX());
        getResource().set("plot-positions." + plotPosition.getIdentifier() + ".z", plotPosition.getZ());

        save();
    }

    public SkyblockPlot getPlotAt(double x, double z) {
        for (PlotPosition plotPosition : getPlotPositions()) {
            if (plotPosition.isLocationWithin(x, z)) {
                return PlotUtils.getOrGetPlot(plotPosition.getIdentifier());
            }
        }

        return null;
    }

    public SkyblockPlot getPlotAt(Location location) {
        for (PlotPosition plotPosition : getPlotPositions()) {
            if (plotPosition.isLocationWithin(location)) {
                return PlotUtils.getOrGetPlot(plotPosition.getIdentifier());
            }
        }

        return null;
    }
}
