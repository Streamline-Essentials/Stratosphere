package tv.quaint.stratosphere.config.bits;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.registries.Identifiable;
import org.bukkit.Material;
import tv.quaint.stratosphere.Stratosphere;

import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConfiguredGenerator implements Identifiable {
    @Getter
    @Setter
    private String identifier;
    @Getter
    @Setter
    private int tier;
    @Getter
    @Setter
    private ConcurrentSkipListMap<Material, Double> materials;

    public ConfiguredGenerator(String identifier, int tier, ConcurrentSkipListMap<Material, Double> materials) {
        this.identifier = identifier;
        this.tier = tier;
        this.materials = materials;
    }

    public ConfiguredGenerator(String identifier, int tier) {
        this(identifier, tier, getDefaultGenerator());
    }

    public void load() {
        Stratosphere.getGeneratorConfig().loadGenerator(this);
    }

    public void unload() {
        Stratosphere.getGeneratorConfig().unloadGenerator(this);
    }

    public void save() {
        Stratosphere.getGeneratorConfig().saveGenerator(this);
    }

    public Material poll() {
        ConcurrentSkipListMap<Double, Material> polling = new ConcurrentSkipListMap<>();

        getMaterials().forEach((material, chance) -> {
            Double highest = polling.lastKey();
            if (highest == null) highest = 0.0;

            polling.put(highest + chance, material);
        });

        Random RNG = new Random();
        double random = RNG.nextDouble(0, polling.lastKey());

        return polling.ceilingEntry(random).getValue();
    }

    public static ConcurrentSkipListMap<Material, Double> getDefaultGenerator() {
        ConcurrentSkipListMap<Material, Double> materials = new ConcurrentSkipListMap<>();
        materials.put(Material.COBBLESTONE, 25.0);
        materials.put(Material.STONE, 25.0);
        materials.put(Material.GRANITE, 5.0);
        materials.put(Material.DIORITE, 5.0);
        materials.put(Material.ANDESITE, 10.0);
        materials.put(Material.COAL, 15.0);
        materials.put(Material.IRON_ORE, 15.0);
        // do the above values add up to 100.0: yes
        return materials;
    }
}