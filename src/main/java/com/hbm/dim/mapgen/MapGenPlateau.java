package com.hbm.dim.mapgen;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class MapGenPlateau extends MapGenBase {

	public Block stoneBlock;
	public Block surfrock;
	private final NoiseGeneratorPerlin plateauNoise;
	private final double noiseScale = 0.1;
	private final int maxPlateauAddition = 10;
	private final int stepHeight = 4;
	private final int topsoilThickness = 2;

	public MapGenPlateau(World world) {
		this.plateauNoise = new NoiseGeneratorPerlin(world.rand, 4);
	}

	@Override
	public void func_151539_a(IChunkProvider provider, World world, int chunkX, int chunkZ, Block[] blocks) {
		for(int localX = 0; localX < 16; localX++) {
			for(int localZ = 0; localZ < 16; localZ++) {
				int baseHeight = getSurfaceHeight(blocks, localX, localZ);
				double noiseVal = plateauNoise.func_151601_a((chunkX * 16 + localX) * noiseScale, (chunkZ * 16 + localZ) * noiseScale);
				int plateauAddition = (int) (((noiseVal + 1) / 2.0) * maxPlateauAddition);
				plateauAddition = (plateauAddition / stepHeight) * stepHeight;
				int plateauTop = baseHeight + plateauAddition;

				for(int y = baseHeight + 1; y < 256; y++) {
					int index = (localX * 16 + localZ) * 256 + y;
					if(y < plateauTop - topsoilThickness) {
						blocks[index] = stoneBlock;
					} else if(y < plateauTop) {
						blocks[index] = surfrock;
					} else {
						blocks[index] = Blocks.air;
					}
				}
			}
		}
	}

	private int getSurfaceHeight(Block[] blocks, int localX, int localZ) {
		int baseHeight = 0;
		for(int y = 255; y >= 0; y--) {
			int index = (localX * 16 + localZ) * 256 + y;
			if(blocks[index] != Blocks.air) {
				baseHeight = y;
				break;
			}
		}
		return baseHeight;
	}

}
