package com.hbm.dim.laythe;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbm.main.StructureManager;
import com.hbm.world.feature.OilBubble;
import com.hbm.world.gen.NBTStructure;
import com.hbm.world.gen.NBTStructure.SpawnCondition;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorLaythe implements IWorldGenerator {

	public WorldGeneratorLaythe() {
		NBTStructure.registerStructure(SpaceConfig.laytheDimension, new SpawnCondition() {{
			structure = StructureManager.nuke_sub;
			canSpawn = biome -> biome == BiomeGenBaseLaythe.laytheOcean;
			maxHeight = 54;
		}});
		NBTStructure.registerStructure(SpaceConfig.laytheDimension, new SpawnCondition() {{
			structure = StructureManager.vertibird;
			canSpawn = biome -> biome.rootHeight > 0;
			heightOffset = -3;
		}});
		NBTStructure.registerStructure(SpaceConfig.laytheDimension, new SpawnCondition() {{
			structure = StructureManager.crashed_vertibird;
			canSpawn = biome -> biome.rootHeight > 0;
			heightOffset = -10;
		}});
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.laytheDimension) {
			generateLaythe(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateLaythe(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);

		if(WorldConfig.laytheOilSpawn > 0 && rand.nextInt(WorldConfig.laytheOilSpawn) == 0) {
			int randPosX = i + rand.nextInt(16);
			int randPosY = rand.nextInt(25);
			int randPosZ = j + rand.nextInt(16);

			OilBubble.spawnOil(world, randPosX, randPosY, randPosZ, 10 + rand.nextInt(7), ModBlocks.ore_oil, meta, Blocks.stone);
		}

		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.asbestosSpawn, 4, 16, 16, ModBlocks.ore_asbestos, meta);
	}

}
