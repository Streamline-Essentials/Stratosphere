package tv.quaint.stratosphere.plot.quests;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class ThingType {
    @Getter @Setter
    private String name;

    public ThingType(String name) {
        this.name = name;
    }

    public EntityType getEntityType() {
        try {
            return EntityType.valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

    public Material getMaterial() {
        try {
            return Material.valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
