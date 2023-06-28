package tv.quaint.stratosphere.config;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.upgrades.PlotUpgrade;
import tv.quaint.stratosphere.utils.MessageUtils;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class UpgradeConfig extends SimpleConfiguration {
    @Getter @Setter
    private ConcurrentSkipListSet<PlotUpgrade> loadedUpgrades;

    public UpgradeConfig() {
        super("upgrades.yml", Stratosphere.getInstance(), false);
    }

    @Override
    public void init() {
        boolean ensureDefaults = getEnsureDefaults();

        // Set loaded quests to new set.
        setLoadedUpgrades(new ConcurrentSkipListSet<>());

        // Load all quests.
        loadAllUpgrades();

        if (ensureDefaults) {
            ensureDefaults();
        }
    }

    public void ensureDefaults() {
        PlotUpgrade upgrade1 = default1();
        loadUpgrade(upgrade1);
        saveUpgrade(upgrade1);

        PlotUpgrade upgrade2 = default2();
        loadUpgrade(upgrade2);
        saveUpgrade(upgrade2);

        PlotUpgrade upgrade3 = default3();
        loadUpgrade(upgrade3);
        saveUpgrade(upgrade3);

        PlotUpgrade upgrade4 = default4();
        loadUpgrade(upgrade4);
        saveUpgrade(upgrade4);

        PlotUpgrade upgrade5 = default5();
        loadUpgrade(upgrade5);
        saveUpgrade(upgrade5);

        PlotUpgrade upgrade6 = default6();
        loadUpgrade(upgrade6);
        saveUpgrade(upgrade6);

        PlotUpgrade upgrade7 = default7();
        loadUpgrade(upgrade7);
        saveUpgrade(upgrade7);
    }

    public void reloadTheConfig() {
        reloadResource(true);

        // Quests.
        setLoadedUpgrades(new ConcurrentSkipListSet<>());
        loadAllUpgrades();
    }

    public boolean getEnsureDefaults() {
        reloadResource();

        return getResource().getOrSetDefault("ensureDefaults", true);
    }

    public void loadAllUpgrades() {
        getLoadedUpgrades().clear();

        getResource().singleLayerKeySet().forEach(key -> {
            if (key.equalsIgnoreCase("ensureDefaults")) return;

            try {
                PlotUpgrade.UpgradeType type = PlotUpgrade.UpgradeType.valueOf(getResource().getString(key + ".type"));
                int tier = getResource().getInt(key + ".tier");
                String payload = getResource().getString(key + ".payload");
                double dustCost = getResource().getDouble(key + ".stardust.cost");
                String description = getResource().getString(key + ".description");

                PlotUpgrade upgrade = new PlotUpgrade(key, type, tier, payload, dustCost, description);

                getLoadedUpgrades().add(upgrade);
            } catch (Exception e) {
                MessageUtils.logWarning("Failed to load upgrade " + key + " from config. Error: " + e.getMessage() + " ->");
                e.printStackTrace();
            }
        });
    }

    public void loadUpgrade(PlotUpgrade upgrade) {
        getLoadedUpgrades().add(upgrade);
    }

    public void unloadUpgrade(String identifier) {
        getLoadedUpgrades().forEach(u -> {
            if (u.getIdentifier().equalsIgnoreCase(identifier)) {
                getLoadedUpgrades().remove(u);
            }
        });
    }

    public PlotUpgrade getUpgrade(String identifier) {
        AtomicReference<PlotUpgrade> quest = new AtomicReference<>();

        getLoadedUpgrades().forEach(u -> {
            if (quest.get() != null) return;

            if (u.getIdentifier().equalsIgnoreCase(identifier)) {
                quest.set(u);
            }
        });

        return quest.get();
    }

    // Default upgrades.
    // These are to be gotten from {@link PlotUpgrade.UpgradeType}.

    // PlotUpgrade.UpgradeType ordinal 0.
    public static PlotUpgrade default1() {
        return new PlotUpgrade("spawnRate1", PlotUpgrade.UpgradeType.MOB_SPAWN_RATE, 1, "2", 50000,
                "Increases the rate at which mobs spawn on your island by 2x."
        );
    }

    // PlotUpgrade.UpgradeType ordinal 1.
    public static PlotUpgrade default2() {
        return new PlotUpgrade("mobCap1", PlotUpgrade.UpgradeType.MOB_SPAWN_CAP, 1, "2", 50000,
                "Increases the amount of mobs that can spawn on your island by 2x."
        );
    }

    // PlotUpgrade.UpgradeType ordinal 2.
    public static PlotUpgrade default3() {
        return new PlotUpgrade("plotSize1", PlotUpgrade.UpgradeType.PLOT_SIZE, 1, "10", 50000,
                "Increases the size of your island by 10 blocks."
        );
    }

    // PlotUpgrade.UpgradeType ordinal 3.
    public static PlotUpgrade default4() {
        return new PlotUpgrade("partySize1", PlotUpgrade.UpgradeType.PARTY_SIZE, 1, "5", 50000,
                "Increases the size of your party to 5 members."
        );
    }

    // PlotUpgrade.UpgradeType ordinal 5. Get the values for the method from that ordinal.
    public static PlotUpgrade default5() {
        return new PlotUpgrade("plotLevel1", PlotUpgrade.UpgradeType.ISLAND_LEVEL_ADD, 1, "1", 50000,
                "Increases your island level by 1."
        );
    }

    // PlotUpgrade.UpgradeType ordinal 8.
    public static PlotUpgrade default6() {
        return new PlotUpgrade("islandLevelCap1", PlotUpgrade.UpgradeType.ISLAND_LEVEL_CAP_ADD, 1, "1", 50000,
                "Increases your island level cap by 1."
        );
    }

    // PlotUpgrade.UpgradeType ordinal 10.
    public static PlotUpgrade default7() {
        return new PlotUpgrade("generator1", PlotUpgrade.UpgradeType.ISLAND_GENERATOR_NORMAL, 1, "2", 50000,
                "Sets your island generator to the second tier."
        );
    }


    // End default upgrades.

    public void saveUpgrade(PlotUpgrade plotUpgrade) {
        getResource().set(plotUpgrade.getIdentifier() + ".type", plotUpgrade.getType().name());
        getResource().set(plotUpgrade.getIdentifier() + ".tier", plotUpgrade.getTier());
        getResource().set(plotUpgrade.getIdentifier() + ".payload", plotUpgrade.getPayload());
        getResource().set(plotUpgrade.getIdentifier() + ".stardust.cost", plotUpgrade.getDustCost());
        getResource().set(plotUpgrade.getIdentifier() + ".description", plotUpgrade.getDescription());
    }
}
