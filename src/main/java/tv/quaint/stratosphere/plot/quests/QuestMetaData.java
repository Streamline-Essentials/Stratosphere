package tv.quaint.stratosphere.plot.quests;

import tv.quaint.objects.AtomicString;
import tv.quaint.stratosphere.users.MetaContainer;
import tv.quaint.stratosphere.users.MetaData;

import java.util.concurrent.ConcurrentSkipListSet;

public class QuestMetaData extends MetaData {
    public static final String QUEST_TYPE_IDENTIFIER = "quest-type";
    public static final String TYPED_IDENTIFIER = "typed";
    public static final String AMOUNT_IDENTIFIER = "amount";

    public QuestMetaData(ConcurrentSkipListSet<MetaContainer> meta) {
        super(meta);
    }

    public QuestMetaData() {
        super();
    }

    public QuestMetaData(String data) {
        super(data);
    }

    public int getAmount(String key) {
        AtomicString atomicString = new AtomicString();
        getMeta().stream().filter(container -> container.getIdentifier().equals(key)).forEach(container -> atomicString.set(container.getData()));

        try {
            return Integer.parseInt(atomicString.get());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void setAmount(String key, int amount) {
        AtomicString atomicString = new AtomicString();
        getMeta().stream().filter(container -> container.getIdentifier().equals(key)).forEach(container -> atomicString.set(container.getData()));

        if (atomicString.get() != null) {
            getMeta().removeIf(container -> container.getIdentifier().equals(key));
        }
        getMeta().add(new MetaContainer(key, String.valueOf(amount)));
    }
}
