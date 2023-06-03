package tv.quaint.stratosphere.config;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.storage.documents.SimpleJsonDocument;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.quests.QuestContainer;

import java.util.concurrent.ConcurrentSkipListSet;

public class MetaDataConfig extends SimpleJsonDocument {
    @Getter @Setter
    private static ConcurrentSkipListSet<QuestContainer> questers;

    public MetaDataConfig() {
        super("metadata.json", Stratosphere.getInstance(), false);
    }

    @Override
    public void onInit() {
        questers = new ConcurrentSkipListSet<>();
        questers.addAll(getQuestContainers());
    }

    public void reloadTheConfig() {
        reloadResource(true);

        questers = new ConcurrentSkipListSet<>();
        questers.addAll(getQuestContainers());
    }

    @Override
    public void onSave() {

    }

    public ConcurrentSkipListSet<QuestContainer> getQuestContainers() {
        reloadResource();

        ConcurrentSkipListSet<QuestContainer> questContainers = new ConcurrentSkipListSet<>();

        getResource().singleLayerKeySet("quests-players").forEach(key -> {
            try {
                QuestContainer questContainer = new QuestContainer(key);
                questContainer.parseMeta(getResource().getString("quests-players." + key));

                questContainers.add(questContainer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return questContainers;
    }

    public void saveQuestContainer(QuestContainer container) {
        getResource().set("quests-players." + container.getIdentifier(), container.getMetaData().toData());

        save();
    }
}
