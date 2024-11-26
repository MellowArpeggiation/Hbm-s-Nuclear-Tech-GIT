package com.hbm.dim.thatmo;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorThatmo implements IWorldGenerator {

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