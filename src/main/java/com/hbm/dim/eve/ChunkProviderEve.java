package com.hbm.dim.eve;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.ChunkProviderCelestial.BlockMetaBuffer;
import com.hbm.dim.eve.biome.BiomeGenBaseEve;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.NoiseGeneratorPerlin;


public class ChunkProviderEve extends ChunkProviderCelestial {
	private final NoiseGeneratorPerlin crackNoise;
	
	public ChunkProviderEve(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);
		reclamp = false;
		stoneBlock = ModBlocks.eve_rock;
		seaBlock = ModBlocks.mercury_block;
		this.crackNoise = new NoiseGeneratorPerlin(world.rand, 4);  

	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);
		generateCracks(x, z, buffer);
		// how many times do I gotta say BEEEEG
		return buffer;
	}
	
	
	private void generateCracks(int chunkX, int chunkZ, BlockMetaBuffer buffer) {
		    BiomeGenBase biome = worldObj.getBiomeGenForCoords(chunkX * 16, chunkZ * 16);
		    if(biome == BiomeGenBaseEve.eveSeismicPlains) {
		        for (int x = 0; x < 16; x++) {
		               for (int z = 0; z < 16; z++) {
		                   double crackValue = crackNoise.func_151601_a((chunkX * 16 + x) * 0.3, (chunkZ * 16 + z) * 0.3);  // Lower scale value for more spread-out cracks

		                   if (crackValue > 0.8) {  
		                       int bedrockY = -1;
		                       for (int y = 0; y < 256; y++) {
		                           int index = (x * 16 + z) * 256 + y;
		                           if (buffer.blocks[index] == Blocks.bedrock) {
		                               if (bedrockY == -1)
		                                   bedrockY = y;
		                           } else {
		                               buffer.blocks[index] = Blocks.air;
		                           }
		                       }
		                       if (bedrockY != -1) {
		                           for (int y = bedrockY + 1; y < Math.min(bedrockY + 10, 256); y++) {
		                               int index = (x * 16 + z) * 256 + y;
		                               buffer.blocks[index] = Blocks.lava;
		                           }
		                       }
		                   }
		               }
		           }
		       }
		}
		

		
		
		
}