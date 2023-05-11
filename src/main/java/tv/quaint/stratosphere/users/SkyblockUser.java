package tv.quaint.stratosphere.users;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.SavableResource;
import net.streamline.api.savables.users.StreamlineUser;
import tv.quaint.stratosphere.plot.PlotUtils;
import tv.quaint.stratosphere.world.SkyblockIOBus;
import tv.quaint.storage.documents.SimpleJsonDocument;

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

    public SkyblockUser(String uuid) {
        super(uuid, new SkyblockUserSerializer(uuid));
    }

    public void forceReload() {
        loadValues();
    }

    @Override
    public void populateDefaults() {
        plotUuid = getOrSetDefault("plot-id", "");
        schematicName = getOrSetDefault("schematic-name", "");
        starDust = getOrSetDefault("star-dust", 0.0d);
    }

    @Override
    public void loadValues() {
        plotUuid = getOrSetDefault("plot-id", "");
        schematicName = getOrSetDefault("schematic-name", "");
        starDust = getOrSetDefault("star-dust", 0.0d);
    }

    @Override
    public void saveAll() {
        set("plot-id", plotUuid);
        set("schematic-name", schematicName);
        set("star-dust", starDust);
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

    public StreamlineUser getStreamlineUser() {
        return ModuleUtils.getOrGetUser(getIdentifier());
    }

    public static SkyblockUser transpose(StreamlineUser user) {
        return PlotUtils.getOrGetUser(user.getIdentifier());
    }
}
