package tv.quaint.stratosphere.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import tv.quaint.storage.resources.flat.simple.SimpleConfiguration;
import tv.quaint.stratosphere.Stratosphere;
import tv.quaint.stratosphere.config.bits.ConfiguredGenerator;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class GeneratorConfig extends SimpleConfiguration {
    @Getter @Setter
    private ConcurrentSkipListSet<ConfiguredGenerator> loadedGenerators;

    public GeneratorConfig() {
        super("generators.yml", Stratosphere.getInstance(), false);

        reloadTheConfig();
    }

    public static void initialize() {
        // Set up default generator.
        if (Stratosphere.getGeneratorConfig().getGenerator("1") == null) {
            ConfiguredGenerator def = new ConfiguredGenerator("1", 1);
            def.save();
            def.load();
        }
        if (Stratosphere.getGeneratorConfig().getGenerator("2") == null) {
            ConfiguredGenerator def = new ConfiguredGenerator("2", 2, ConfiguredGenerator.getDefaultGenerator2());
            def.save();
            def.load();
        }
        if (Stratosphere.getGeneratorConfig().getGenerator("3") == null) {
            ConfiguredGenerator def = new ConfiguredGenerator("3", 3, ConfiguredGenerator.getDefaultGenerator3());
            def.save();
            def.load();
        }
        if (Stratosphere.getGeneratorConfig().getGenerator("4") == null) {
            ConfiguredGenerator def = new ConfiguredGenerator("4", 4, ConfiguredGenerator.getDefaultGenerator4());
            def.save();
            def.load();
        }
        if (Stratosphere.getGeneratorConfig().getGenerator("5") == null) {
            ConfiguredGenerator def = new ConfiguredGenerator("5", 5, ConfiguredGenerator.getDefaultGenerator5());
            def.save();
            def.load();
        }
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
            if (key.equalsIgnoreCase("ensureDefaults")) return;

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

    public ConfiguredGenerator getByTierAbsolute(int tier) {
        AtomicReference<ConfiguredGenerator> generator = new AtomicReference<>();

        getLoadedGenerators().forEach(gen -> {
            if (gen.getTier() == tier) {
                generator.set(gen);
            }
        });

        return generator.get();
    }

    public boolean isTierLoaded(int tier) {
        return getByTierAbsolute(tier) != null;
    }


    public ConfiguredGenerator getByTier(int tier) {
        ConfiguredGenerator generator = getByTierAbsolute(tier);

        while (generator == null && tier > 0) {
            tier --;
            generator = getByTierAbsolute(tier);
        }

        return generator;
    }
}
