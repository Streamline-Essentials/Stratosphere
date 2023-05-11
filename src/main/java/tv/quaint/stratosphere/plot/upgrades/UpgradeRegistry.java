package tv.quaint.stratosphere.plot.upgrades;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.registries.AbstractRegistry;
import net.streamline.api.registries.ItemGetter;

public class UpgradeRegistry extends AbstractRegistry<PlotUpgrade, UpgradeRegistry.UpgradeIdentifier> {
    public static class UpgradeIdentifier {
        @Getter @Setter
        private PlotUpgrade.Type type;
        @Getter @Setter
        private int tier;

        public UpgradeIdentifier(PlotUpgrade.Type type, int tier) {
            this.type = type;
            this.tier = tier;
        }
    }

    public UpgradeRegistry() {
        super("sky-upgrades", PlotUpgrade.class, false);
    }

    @Override
    public ItemGetter<UpgradeIdentifier, AbstractRegistry<PlotUpgrade, UpgradeIdentifier>, PlotUpgrade> getGetter() {
        return (thing, abstractRegistry) ->
                abstractRegistry.getRegistry().stream()
                        .filter(r ->
                                r.getType().equals(thing.getType()) &&
                                        r.getTier() == thing.getTier()
                        ).findFirst().orElse(null);
    }

    public int getNextTier(PlotUpgrade.Type type) {
        int highestTier = -1;

        for (PlotUpgrade upgrade : getRegistry()) {
            if (upgrade.getType().equals(type)) {
                if (upgrade.getTier() > highestTier) {
                    highestTier = upgrade.getTier();
                }
            }
        }

        return highestTier + 1;
    }
}
