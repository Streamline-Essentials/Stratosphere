package tv.quaint.stratosphere.plot.quests;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.registries.Identifiable;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.quests.bits.QuestReward;

import java.util.List;

public class PlotQuest implements Identifiable {
    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private Statistic type;
    @Getter @Setter
    private ThingType thing;
    @Getter @Setter
    private int amount;
    @Getter @Setter
    private List<QuestReward> rewards;
    @Getter @Setter
    private String description;

    public PlotQuest(String identifier, Statistic type, ThingType thing, int amount, List<QuestReward> rewards, String description) {
        this.identifier = identifier;
        this.type = type;
        this.thing = thing;
        this.amount = amount;
        this.rewards = rewards;
        this.description = description;
    }

    public int doQuest(Player finisher) {
        int amount = 0;

        for (QuestReward reward : getRewards()) {
            if (reward.reward(finisher)) amount++;
        }

        return amount;
    }

    public void load() {
        Stratosphere.getQuestConfig().loadQuest(this);
    }

    public void unload() {
        Stratosphere.getQuestConfig().unloadQuest(this.getIdentifier());
    }

    public void save() {
        Stratosphere.getQuestConfig().saveQuest(this);
    }
}
