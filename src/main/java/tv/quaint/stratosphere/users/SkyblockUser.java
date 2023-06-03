package tv.quaint.stratosphere.users;

import tv.quaint.stratosphere.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tv.quaint.savables.SavableResource;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.plot.SkyblockPlot;
import tv.quaint.stratosphere.plot.quests.PlotQuest;
import tv.quaint.stratosphere.world.SkyblockIOBus;
import tv.quaint.storage.documents.SimpleJsonDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class SkyblockUser extends SavableResource {
    public static class SkyblockUserSerializer extends SimpleJsonDocument {
        public SkyblockUserSerializer(String identifier) {
            super((identifier.endsWith(".json") ? identifier : identifier + ".json"), SkyblockIOBus.getUsersFolder(), false);
        }

        @Override
        public void onInit() {

        }

        @Override
        public void onSave() {

        }
    }

    @Getter @Setter
    private String schematicName;
    @Getter @Setter
    private String plotUuid;
    @Getter @Setter
    private double starDust;
    @Getter @Setter
    private List<String> completedQuests;
    @Getter @Setter
    private String username;
    @Getter @Setter
    private boolean bypassingPlots;

    public SkyblockUser(String uuid) {
        super(uuid, new SkyblockUserSerializer(uuid));

        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        if (player != null) {
            username = player.getName();
        }

        completedQuests = new ArrayList<>();
    }

    public void forceReload() {
        loadValues();
    }

    @Override
    public void populateDefaults() {
        plotUuid = getOrSetDefault("plot-id", "");
        schematicName = getOrSetDefault("schematic-name", "");
        starDust = getOrSetDefault("star-dust", 0.0d);
        completedQuests = getOrSetDefault("completed-quests", new ArrayList<>());
        username = getOrSetDefault("username", "");
        bypassingPlots = getOrSetDefault("bypassing-plots", false);
    }

    @Override
    public void loadValues() {
        plotUuid = getOrSetDefault("plot-id", "");
        schematicName = getOrSetDefault("schematic-name", "");
        starDust = getOrSetDefault("star-dust", 0.0d);
        completedQuests = getOrSetDefault("completed-quests", new ArrayList<>());
        username = getOrSetDefault("username", "");
        bypassingPlots = getOrSetDefault("bypassing-plots", false);
    }

    @Override
    public void saveAll() {
        set("plot-id", plotUuid);
        set("schematic-name", schematicName);
        set("star-dust", starDust);
        set("completed-quests", completedQuests);
        set("username", username);
        set("bypassing-plots", bypassingPlots);
    }

    public void addStarDust(double amount) {
        starDust += amount;
        saveAll();
    }

    public void removeStarDust(double amount) {
        starDust -= amount;
        saveAll();
    }

    public boolean hasStarDust(double amount) {
        return starDust >= amount;
    }

    public boolean isAlreadyInPlot() {
        if (plotUuid == null) return false;
        return ! plotUuid.isEmpty() && ! plotUuid.isBlank();
    }

    public SkyblockPlot getOrGetPlot() {
        return PlotUtils.getOrGetPlot(plotUuid);
    }

    public ConcurrentSkipListSet<PlotQuest> getCompletedQuestsAsQuests() {
        ConcurrentSkipListSet<PlotQuest> quests = new ConcurrentSkipListSet<>();

        for (String questId : completedQuests) {
            PlotQuest quest = Stratosphere.getQuestConfig().getQuest(questId);
            if (quest != null) quests.add(quest);
        }

        return quests;
    }

    public boolean isQuestCompleted(String identifier) {
        if (identifier == null) return false;
        if (completedQuests == null) return false;

        return completedQuests.contains(identifier);
    }

    public void addCompletedQuest(String identifier) {
        if (identifier == null) return;
        if (completedQuests == null) completedQuests = new ArrayList<>();

        completedQuests.add(identifier);
        saveAll();
    }

    public void removeCompletedQuest(String identifier) {
        if (identifier == null) return;
        if (completedQuests == null) completedQuests = new ArrayList<>();

        completedQuests.remove(identifier);
        saveAll();
    }

    public void load() {
        PlotUtils.loadUser(this);
    }

    public void unload() {
        PlotUtils.unloadUser(getUuid());
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(UUID.fromString(getUuid()));
    }

    public boolean isOnline() {
        return getBukkitPlayer() != null;
    }

    public void sendMessage(String message) {
        if (! isOnline()) {
            MessageUtils.logWarning("Tried to send message to offline player: " + getDisplayName());
            return;
        }

        MessageUtils.sendMessage(getBukkitPlayer(), message);
    }

    public String getName() {
        return username;
    }

    public String getDisplayName() {
        if (! isOnline()) return getName();

        return getBukkitPlayer().getDisplayName();
    }

    public boolean hasPermission(String permission) {
        if (! isOnline()) return false;

        return getBukkitPlayer().hasPermission(permission);
    }
}
