package tv.quaint.stratosphere.world;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SkyblockChunkGenerator extends ChunkGenerator {
    @Override
    public @NotNull ChunkData generateChunkData(World world, Random random, int x, int z, ChunkGenerator.BiomeGrid biome) {
        return createSkyblockChunkData(world, x, z);
    }

    public ChunkData createSkyblockChunkData(World world, int x, int z) {
        ChunkData chunkData = createChunkData(world);
        chunkData.setRegion(x * 16, 0, z * 16, 16, chunkData.getMaxHeight(), 16, Material.AIR);
        return chunkData;
    }

    @Override
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return Collections.emptyList();
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }
}
