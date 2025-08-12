package com.hbm.dim.tekto;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.MapGenGreg;
import com.hbm.dim.mapgen.MapGenVolcano;
import com.hbm.dim.tekto.biome.BiomeGenBaseTekto;

import net.minecraft.block.Block;
import net.minecraft.world.World;


public class ChunkProviderTekto extends ChunkProviderCelestial {
	private MapGenVolcano volcano = new MapGenVolcano(12);
	private MapGenGreg caveGenV3 = new MapGenGreg();
	public ChunkProviderTekto(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);
		reclamp = false;
		caveGenV3.stoneBlock = ModBlocks.basalt;

		stoneBlock = ModBlocks.basalt;
		seaBlock = ModBlocks.ccl_block;
		volcano.setSize(8, 16);
		volcano.setMaterial(ModBlocks.geysir_chloric, ModBlocks.vinyl_sand);
	}

	@Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);
		
		if(biomesForGeneration[0] == BiomeGenBaseTekto.vinylsands) {
			volcano.func_151539_a(this, worldObj, x, z, buffer.blocks);
			
		}
		caveGenV3.func_151539_a(this, worldObj, x, z, buffer.blocks);

		// how many times do I gotta say BEEEEG
		return buffer;
	}
	
	@Override
	protected void generateBlocks(int x, int z, Block[] blocks) {
	    super.generateBlocks(x, z, blocks);
	    


	    /*
	    for (int localX = 0; localX < 16; ++localX) {
	        for (int localZ = 0; localZ < 16; ++localZ) {
	            int index = (localX * 16 + localZ) * 256 + 32;

	            blocks[index] = ModBlocks.block_starmetal;
	        }
	    }
	    */
	}
}