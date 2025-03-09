package com.hbm.dim.duna;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.duna.biome.BiomeGenBaseDuna;
import com.hbm.dim.mapgen.ExperimentalCaveGenerator;

import net.minecraft.world.World;
public class ChunkProviderDuna extends ChunkProviderCelestial {

	private ExperimentalCaveGenerator caveGenSmall = new ExperimentalCaveGenerator(2, 12, 0.12F);
	private ExperimentalCaveGenerator caveGenV2 = new ExperimentalCaveGenerator(2, 40, 3.0F);

    public ChunkProviderDuna(World world, long seed, boolean hasMapFeatures) {
        super(world, seed, hasMapFeatures);
		stoneBlock = ModBlocks.duna_rock;

        caveGenV2.lavaBlock = ModBlocks.basalt;
        caveGenV2.stoneBlock = ModBlocks.duna_rock;
        caveGenSmall.lavaBlock = ModBlocks.duna_sands;
        caveGenSmall.stoneBlock = ModBlocks.duna_rock;

        caveGenSmall.smallCaveSize = 0.1F;

        caveGenV2.onlyBiome = BiomeGenBaseDuna.dunaLowlands;
        caveGenSmall.ignoreBiome = BiomeGenBaseDuna.dunaLowlands;
    }

    @Override
	public BlockMetaBuffer getChunkPrimer(int x, int z) {
		BlockMetaBuffer buffer = super.getChunkPrimer(x, z);

        this.caveGenV2.func_151539_a(this, worldObj, x, z, buffer.blocks);
        this.caveGenSmall.func_151539_a(this, worldObj, x, z, buffer.blocks);

		return buffer;
	}

}