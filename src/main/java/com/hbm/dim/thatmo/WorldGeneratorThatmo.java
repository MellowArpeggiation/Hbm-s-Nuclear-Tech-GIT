package com.hbm.dim.thatmo;

import java.util.HashMap;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.main.StructureManager;
import com.hbm.world.gen.NBTStructure;
import com.hbm.world.gen.NBTStructure.SpawnCondition;
import com.hbm.world.gen.component.Component.ConcreteBricks;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;

public class WorldGeneratorThatmo implements IWorldGenerator {

	public WorldGeneratorThatmo() {
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition() {{
			structure = StructureManager.THATMOTESTMO;
			canSpawn = biome -> biome.heightVariation < 0.1F;
		}});
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition() {{
			structure = StructureManager.thatmo2;
			canSpawn = biome -> biome.heightVariation < 0.1F;
		}});
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition() {{
			structure = StructureManager.trenches;
			conformToTerrain = true;
			heightOffset = -2;
			spawnWeight = 2;
			blockTable = new HashMap<Block, BlockSelector>() {{
				put(ModBlocks.brick_concrete_cracked, new ConcreteBricks());
			}};
		}});
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.thatmoDimension) {
			generateThatmo(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateThatmo(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);

	}
}