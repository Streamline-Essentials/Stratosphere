package tv.quaint.stratosphere.config.bits;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import tv.quaint.objects.Identifiable;
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
            Double highest = 0.0;
            try {
                highest = polling.lastKey();
                if (highest == null) highest = 0.0;
            } catch (Exception e) {
                // do nothing
            }
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
        materials.put(Material.COAL_ORE, 15.0);
        materials.put(Material.IRON_ORE, 15.0);
        return materials;
    }

    public static ConcurrentSkipListMap<Material, Double> getDefaultGenerator2() {
        ConcurrentSkipListMap<Material, Double> materials = new ConcurrentSkipListMap<>();
        materials.put(Material.COBBLESTONE, 15.0);
        materials.put(Material.STONE, 25.0);
        materials.put(Material.GRANITE, 2.5);
        materials.put(Material.DIORITE, 2.5);
        materials.put(Material.ANDESITE, 5.0);
        materials.put(Material.COAL_ORE, 25.0);
        materials.put(Material.IRON_ORE, 25.0);
        return materials;
    }

    public static ConcurrentSkipListMap<Material, Double> getDefaultGenerator3() {
        ConcurrentSkipListMap<Material, Double> materials = new ConcurrentSkipListMap<>();
        materials.put(Material.COBBLESTONE, 2.0);
        materials.put(Material.STONE, 5.0);
        materials.put(Material.GRANITE, 1.0);
        materials.put(Material.DIORITE, 1.0);
        materials.put(Material.ANDESITE, 1.0);
        materials.put(Material.COAL_ORE, 25.0);
        materials.put(Material.IRON_ORE, 25.0);
        materials.put(Material.GOLD_ORE, 20.0);
        materials.put(Material.REDSTONE_ORE, 20.0);
        return materials;
    }

    public static ConcurrentSkipListMap<Material, Double> getDefaultGenerator4() {
        ConcurrentSkipListMap<Material, Double> materials = new ConcurrentSkipListMap<>();
        materials.put(Material.COBBLESTONE, 2.0);
        materials.put(Material.STONE, 5.0);
        materials.put(Material.GRANITE, 1.0);
        materials.put(Material.DIORITE, 1.0);
        materials.put(Material.ANDESITE, 1.0);
        materials.put(Material.COAL_ORE, 25.0);
        materials.put(Material.IRON_ORE, 20.0);
        materials.put(Material.GOLD_ORE, 10.0);
        materials.put(Material.REDSTONE_ORE, 20.0);
        materials.put(Material.DIAMOND_ORE, 5.0);
        materials.put(Material.LAPIS_ORE, 10.0);
        return materials;
    }

    public static ConcurrentSkipListMap<Material, Double> getDefaultGenerator5() {
        ConcurrentSkipListMap<Material, Double> materials = new ConcurrentSkipListMap<>();
        materials.put(Material.COBBLESTONE, 2.0);
        materials.put(Material.STONE, 5.0);
        materials.put(Material.GRANITE, 1.0);
        materials.put(Material.DIORITE, 1.0);
        materials.put(Material.ANDESITE, 1.0);
        materials.put(Material.COAL_ORE, 25.0);
        materials.put(Material.IRON_ORE, 20.0);
        materials.put(Material.GOLD_ORE, 10.0);
        materials.put(Material.REDSTONE_ORE, 20.0);
        materials.put(Material.DIAMOND_ORE, 5.0);
        materials.put(Material.LAPIS_ORE, 5.0);
        materials.put(Material.EMERALD_ORE, 5.0);
        return materials;
    }
}