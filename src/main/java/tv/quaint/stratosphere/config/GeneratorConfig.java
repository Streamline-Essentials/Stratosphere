package tv.quaint.stratosphere.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.config.bits.ConfiguredGenerator;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class GeneratorConfig extends SimpleConfiguration {
    @Getter @Setter
    private ConcurrentSkipListSet<ConfiguredGenerator> loadedGenerators;

    public GeneratorConfig() {
        super("config.yml", Stratosphere.getInstance(), false);

        reloadTheConfig();
    }

    public static void initialize() {
        // Set up default generator.
        ConfiguredGenerator def = new ConfiguredGenerator("default", 1);
        def.save();
        def.load();
    }

    @Override
    public void init() {
        // Set loaded generators to new set.
        setLoadedGenerators(new ConcurrentSkipListSet<>());

        // Load all generators.
        loadAllGenerators();
    }

    public void reloadTheConfig() {
        reloadResource(true);

        // Generators.
        setLoadedGenerators(new ConcurrentSkipListSet<>());
        loadAllGenerators();
    }

    public void loadGenerator(ConfiguredGenerator generator) {
        loadedGenerators.add(generator);
    }

    public void unloadGenerator(ConfiguredGenerator generator) {
        loadedGenerators.remove(generator);
    }

    public ConfiguredGenerator getGenerator(String identifier) {
        AtomicReference<ConfiguredGenerator> generator = new AtomicReference<>();
        loadedGenerators.forEach(gen -> {
            if (gen.getIdentifier().equalsIgnoreCase(identifier)) {
                generator.set(gen);
            }
        });
        return generator.get();
    }

    public boolean isGeneratorLoaded(String identifier) {
        return getGenerator(identifier) != null;
    }

    public void loadAllGenerators() {
        ConcurrentSkipListSet<ConfiguredGenerator> generators = new ConcurrentSkipListSet<>();

        singleLayerKeySet().forEach(key -> {
            try {
                int tier = getOrSetDefault(key + ".tier", 1);
                ConcurrentSkipListMap<Material, Double> materials = new ConcurrentSkipListMap<>();

                singleLayerKeySet(key + ".materials").forEach(mat -> {
                    try {
                        materials.put(
                                Material.valueOf(mat),
                                getOrSetDefault(key + ".materials." + mat, 1.0)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                ConfiguredGenerator generator = new ConfiguredGenerator(key, tier, materials);

                generators.add(generator);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        setLoadedGenerators(generators);
    }

    public void saveGenerator(ConfiguredGenerator configuredGenerator) {
        write(configuredGenerator.getIdentifier() + ".tier", configuredGenerator.getTier());
        configuredGenerator.getMaterials().forEach((material, chance) -> {
            write(configuredGenerator.getIdentifier() + ".materials." + material.name(), chance);
        });
    }
}
