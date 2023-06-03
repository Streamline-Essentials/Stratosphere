package tv.quaint.stratosphere.plot.quests;

import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.users.SkyblockUser;

import java.util.UUID;

public class QuestContainer implements Identifiable {
//    public static final String COMPLETEDS_KEY = "completeds";

    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private QuestMetaData metaData;

    public String getUuid() {
        return identifier;
    }

    public QuestContainer(String uuid) {
        this.identifier = uuid;
        this.metaData = new QuestMetaData();
    }

    public QuestContainer(SkyblockUser user) {
        this(user.getUuid());
    }

    public void save() {
        Stratosphere.getMetaDataConfig().saveQuestContainer(this);
    }

    public void parseMeta(String string) {
        metaData = new QuestMetaData(string);
    }

    public SkyblockUser asSkyblockUser() {
        return PlotUtils.getOrGetUser(identifier);
    }

    public boolean isComplete(String identifier, Material material, int amount) {
        return isComplete(identifier, material.name(), amount);
    }

    public void setAmount(String identifier, Material material, int amount) {
        setAmount(identifier, material.name(), amount);
    }

    public int getAmount(String identifier, Material material) {
        return getAmount(identifier, material.name());
    }

    public boolean isComplete(String identifier, EntityType entity, int amount) {
        return isComplete(identifier, entity.name(), amount);
    }

    public void setAmount(String identifier, EntityType entity, int amount) {
        setAmount(identifier, entity.name(), amount);
    }

    public int getAmount(String identifier, EntityType entity) {
        return getAmount(identifier, entity.name());
    }

    public void addAmount(String identifier, Material material, int amount) {
        addAmount(identifier, material.name(), amount);
    }

    public void addAmount(String identifier, EntityType entity, int amount) {
        addAmount(identifier, entity.name(), amount);
    }

    public void removeAmount(String identifier, Material material, int amount) {
        removeAmount(identifier, material.name(), amount);
    }

    public void removeAmount(String identifier, EntityType entity, int amount) {
        removeAmount(identifier, entity.name(), amount);
    }

    public boolean isComplete(String identifier, String typed, int amount) {
        if (! hasNeeded(identifier, typed)) return false;

        String amountStr = metaData.getMeta(getIdentifer(identifier, typed));

        int amountInt = 0;

        try {
            amountInt = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return amountInt >= amount;
    }

    public void setAmount(String identifier, String typed, int amount) {
        metaData.setMeta(getIdentifer(identifier, typed), String.valueOf(amount));

        doQuestCheck(identifier, typed);

        save();
    }

    public String getIdentifer(String identifier, String typed) {
        return identifier + ":::" + typed;
    }

    public void doQuestCheck(String identifier, String typed) {
        Stratosphere.getQuestConfig().getLoadedQuests().forEach(quest -> {
            if (quest.getType().name().equals(identifier)) {
                if (quest.getThing().getName().equals(typed)) {
                    if (quest.getAmount() > getAmount(identifier, typed)) return;

                    if (! isOnline()) return;
                    Player bukkitPlayer = getBukkitPlayer();

                    SkyblockUser user = getSkyblockUser();
                    if (user.isQuestCompleted(quest.getIdentifier())) return;

                    quest.doQuest(bukkitPlayer);

                    user.addCompletedQuest(quest.getIdentifier());
                    user.saveAll();
                }
            }
        });
    }

    public int getAmount(String identifier, String typed) {
        if (! hasNeeded(identifier, typed)) return 0;

        String amountStr = metaData.getMeta(getIdentifer(identifier, typed));

        int amountInt = 0;

        try {
            amountInt = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return amountInt;
    }

    public void addAmount(String identifier, String typed, int amount) {
        setAmount(identifier, typed, getAmount(identifier, typed) + amount);
    }

    public void removeAmount(String identifier, String typed, int amount) {
        setAmount(identifier, typed, getAmount(identifier, typed) - amount);
    }

    public boolean hasNeeded(String identifier, String typed) {
        return metaData.hasMeta(getIdentifer(identifier, typed));
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(UUID.fromString(identifier));
    }

    public boolean isOnline() {
        return getBukkitPlayer() != null;
    }

    public SkyblockUser getSkyblockUser() {
        return PlotUtils.getOrGetUser(identifier);
    }

//    public void addCompleted(String identifier, String typed) {
//        ConcurrentSkipListMap<String, String> completeds = getCompleteds();
//        completeds.put(identifier, typed);
//        setCompleteds(completeds);
//    }
//
//    public void removeCompleted(String identifier) {
//        ConcurrentSkipListMap<String, String> completeds = getCompleteds();
//        completeds.remove(identifier);
//        setCompleteds(completeds);
//    }
//
//    public ConcurrentSkipListMap<String, String> getCompleteds() {
//        ConcurrentSkipListMap<String, String> completeds = new ConcurrentSkipListMap<>();
//        if (! metaData.hasMeta(COMPLETEDS_KEY)) return completeds;
//
//        String[] split = metaData.getMeta(COMPLETEDS_KEY).split(";");
//        for (String s : split) {
//            String[] split1 = s.split(":");
//            completeds.put(split1[0], split1[1]);
//        }
//
//        return completeds;
//    }
//
//    public void setCompleteds(ConcurrentSkipListMap<String, String> completeds) {
//        StringBuilder builder = new StringBuilder();
//        completeds.forEach((s, s2) -> builder.append(s).append(":").append(s2).append(";"));
//        metaData.setMeta(COMPLETEDS_KEY, builder.toString());
//    }
}
