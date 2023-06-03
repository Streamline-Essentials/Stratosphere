package tv.quaint.stratosphere.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.quests.PlotQuest;
import tv.quaint.stratosphere.plot.quests.ThingType;
import tv.quaint.stratosphere.plot.quests.bits.QuestReward;
import tv.quaint.stratosphere.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class QuestConfig extends SimpleConfiguration {
    @Getter @Setter
    private ConcurrentSkipListSet<PlotQuest> loadedQuests;

    public QuestConfig() {
        super("quests.yml", Stratosphere.getInstance(), false);

        reloadTheConfig();
    }

    @Override
    public void init() {
        boolean ensureDefaults = getEnsureDefaults();

        // Set loaded quests to new set.
        setLoadedQuests(new ConcurrentSkipListSet<>());

        // Load all quests.
        loadAllQuests();

        if (ensureDefaults) {
            ensureDefaults();
        }
    }

    public void ensureDefaults() {
        PlotQuest quest1 = defaultQuest1();
        loadQuest(quest1);
        saveQuest(quest1);

        PlotQuest quest2 = defaultQuest2();
        loadQuest(quest2);
        saveQuest(quest2);

        PlotQuest quest3 = defaultQuest3();
        loadQuest(quest3);
        saveQuest(quest3);
    }

    public void reloadTheConfig() {
        reloadResource(true);

        // Quests.
        setLoadedQuests(new ConcurrentSkipListSet<>());
        loadAllQuests();
    }

    public boolean getEnsureDefaults() {
        reloadResource();

        return getResource().getOrSetDefault("ensureDefaults", true);
    }

    public void loadAllQuests() {
        getLoadedQuests().clear();

        getResource().singleLayerKeySet().forEach(key -> {
            if (key.equalsIgnoreCase("ensureDefaults")) return;

            try {
                Statistic type = Statistic.valueOf(getResource().getString(key + ".type"));
                ThingType thingType = new ThingType(getResource().getString(key + ".thing"));
                int amount = getResource().getInt(key + ".amount");
                String description = getResource().getString(key + ".description");

                List<QuestReward> rewards = new ArrayList<>();

                getResource().singleLayerKeySet(key + ".rewards").forEach(k -> {
                    QuestReward reward = new QuestReward(getResource().getString(key + ".rewards." + k));

                    if (reward.getPayload().equals("null")) {
                        MessageUtils.logWarning("Quest " + key + " has a null reward for key " + k + "! Skipping...");
                        return;
                    }

                    rewards.add(reward);
                });

                PlotQuest quest = new PlotQuest(key, type, thingType, amount, rewards, description);

                loadQuest(quest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void loadQuest(PlotQuest quest) {
        getLoadedQuests().add(quest);
    }

    public void unloadQuest(String identifier) {
        getLoadedQuests().forEach(q -> {
            if (q.getIdentifier().equalsIgnoreCase(identifier)) {
                getLoadedQuests().remove(q);
            }
        });
    }

    public PlotQuest getQuest(String identifier) {
        AtomicReference<PlotQuest> quest = new AtomicReference<>();

        getLoadedQuests().forEach(q -> {
            if (quest.get() != null) return;

            if (q.getIdentifier().equalsIgnoreCase(identifier)) {
                quest.set(q);
            }
        });

        return quest.get();
    }

    public static PlotQuest defaultQuest1() {
        return new PlotQuest("1", Statistic.MINE_BLOCK, new ThingType(Material.COAL_ORE.name()), 100, List.of(
                new QuestReward(QuestReward.RewardType.DUST, "100"),
                new QuestReward(QuestReward.RewardType.MESSAGE, "&7You &acompleted &7the quest for &dmining &f100 &cCOAL_ORE&8!")
        ), "&dMine &f100 &cCOAL_ORE&8, &7and get &a+&f100 &bDust&8!");
    }

    public static PlotQuest defaultQuest2() {
        return new PlotQuest("2", Statistic.USE_ITEM, new ThingType(Material.DIAMOND_BLOCK.name()), 1000, List.of(
                new QuestReward(QuestReward.RewardType.LEVELS, "1"),
                new QuestReward(QuestReward.RewardType.MESSAGE, "&7You &acompleted &7the quest for &dplacing &f1000 &cDIAMOND_BLOCK&8!")
        ), "&dPlace &f1000 &cDIAMOND_BLOCK&8, &7and get &a+&f1 &bplot level&8!");
    }

    public static PlotQuest defaultQuest3() {
        return new PlotQuest("3", Statistic.KILL_ENTITY, new ThingType(EntityType.ZOMBIE.name()), 5000, List.of(
                new QuestReward(QuestReward.RewardType.LEVELS, "2"),
                new QuestReward(QuestReward.RewardType.MESSAGE, "&7You &acompleted &7the quest for &dkilling &f5000 &cZOMBIE&8!")
        ), "&dKill &f5000 &cZOMBIE&8, &7and get &a+&f2 &bplot levels&8!");
    }

    public void saveQuest(PlotQuest plotQuest) {
        getResource().set(plotQuest.getIdentifier() + ".type", plotQuest.getType().name());
        getResource().set(plotQuest.getIdentifier() + ".thing", plotQuest.getThing().getName());
        getResource().set(plotQuest.getIdentifier() + ".amount", plotQuest.getAmount());
        getResource().set(plotQuest.getIdentifier() + ".description", plotQuest.getDescription());

        int i = 0;
        for (QuestReward reward : plotQuest.getRewards()) {
            getResource().set(plotQuest.getIdentifier() + ".rewards." + i, reward.getType() + "! " + reward.getPayload());
            i++;
        }

        getResource().write();
    }
}
