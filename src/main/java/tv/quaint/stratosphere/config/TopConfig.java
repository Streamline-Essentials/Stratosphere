package tv.quaint.stratosphere.config;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class TopConfig extends SimpleConfiguration {
    @Getter @Setter
    private ConcurrentSkipListMap<SkyblockPlot.PlotType, ConcurrentSkipListMap<String, Double>> loadedTop;

    public TopConfig() {
        super("top.yml", Stratosphere.getInstance(), false);
    }

    @Override
    public void init() {
        // Load all quests.
        loadAllUpgrades();

        ensureNoFalsePlots();
    }

    public void reloadTheConfig() {
        reloadResource(true);

        // Quests.
        loadAllUpgrades();

        ensureNoFalsePlots();
    }

    public void loadAllUpgrades() {
        setLoadedTop(new ConcurrentSkipListMap<>());

        Arrays.stream(SkyblockPlot.PlotType.values()).forEach(type -> {
            ConcurrentSkipListMap<String, Double> scores = new ConcurrentSkipListMap<>();

            singleLayerKeySet(type.name()).forEach(key -> {
                try {
                    double value = getOrSetDefault(key, 0.0d);
                    scores.put(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            getLoadedTop().put(type, scores);
        });
    }

    public void loadTopScore(SkyblockPlot plot, double score) {
        if (getLoadedTop() == null) setLoadedTop(new ConcurrentSkipListMap<>());
        SkyblockPlot.PlotType type = plot.getPlotType();
        if (type == null) return;
        ConcurrentSkipListMap<String, Double> map = getLoadedTop().get(type);
        if (map == null) map = new ConcurrentSkipListMap<>();
        map.put(plot.getUuid(), score);
        getLoadedTop().put(plot.getPlotType(), map);
    }

    public void unloadTopScore(SkyblockPlot plot) {
        if (getLoadedTop() == null) setLoadedTop(new ConcurrentSkipListMap<>());

        getLoadedTop().forEach((type, map) -> {
            map.forEach((uuid, score) -> {
                if (uuid.equals(plot.getUuid())) {
                    map.remove(uuid);
                }
            });
        });
    }

    public void saveTopScore(SkyblockPlot plot, double score) {
        loadTopScore(plot, score);
        if (plot.getPlotType() == null) return;
        write(plot.getPlotType().name() + "." + plot.getUuid(), score);
    }

    public void deleteTopScore(SkyblockPlot plot) {
        unloadTopScore(plot);
        for (SkyblockPlot.PlotType type : SkyblockPlot.PlotType.values()) {
            getResource().remove( type + "." + plot.getUuid());
        }
    }

    public ConcurrentHashMap<Integer, SkyblockPlot> getTopScores(int amount) {
        ensureNoFalsePlots();

        ConcurrentHashMap<Integer, SkyblockPlot> top = new ConcurrentHashMap<>();

        ConcurrentSkipListMap<Double, String> sorted = new ConcurrentSkipListMap<>();

        for (SkyblockPlot.PlotType type : SkyblockPlot.PlotType.values()) {
            ConcurrentSkipListMap<String, Double> scores = getLoadedTop().get(type);
            if (scores == null) {
                scores = new ConcurrentSkipListMap<>();
                getLoadedTop().put(type, scores);
                continue;
            }

            for (String uuid : scores.keySet()) {
                double score = scores.get(uuid);

                while (sorted.containsKey(score)) {
                    score += 0.0001d;
                }

                sorted.put(score, uuid);
            }
        }

        int i = 0;
        for (double score : sorted.descendingKeySet()) {
            if (i >= amount) break;

            String uuid = sorted.get(score);

            SkyblockPlot plot = PlotUtils.getOrGetPlot(uuid);
            if (plot == null) continue;

            top.put(top.size() + 1, plot);
            i++;
        }

        return top;
    }

    public ConcurrentHashMap<Integer, SkyblockPlot> getTopScores(SkyblockPlot.PlotType type, int amount) {
        ensureNoFalsePlots();

        ConcurrentHashMap<Integer, SkyblockPlot> top = new ConcurrentHashMap<>();

        ConcurrentSkipListMap<Double, String> sorted = new ConcurrentSkipListMap<>();

        ConcurrentSkipListMap<String, Double> scores = getLoadedTop().get(type);
        if (scores == null) {
            scores = new ConcurrentSkipListMap<>();
            getLoadedTop().put(type, scores);
            return top;
        }

        for (String uuid : scores.keySet()) {
            double score = scores.get(uuid);

            while (sorted.containsKey(score)) {
                score += 0.0001d;
            }

            sorted.put(score, uuid);
        }

        int i = 0;
        for (double score : sorted.descendingKeySet()) {
            if (i >= amount) break;

            String uuid = sorted.get(score);

            SkyblockPlot plot = PlotUtils.getOrGetPlot(uuid);
            if (plot == null) continue;

            top.put(top.size() + 1, plot);
            i++;
        }

        return top;
    }

    public void ensureNoFalsePlots() {
        getLoadedTop().forEach((type, map) -> {
            map.forEach((uuid, score) -> {
                SkyblockPlot plot = PlotUtils.getOrGetPlot(uuid);
                if (plot == null) {
                    map.remove(uuid);
                }
            });
        });
    }
}
