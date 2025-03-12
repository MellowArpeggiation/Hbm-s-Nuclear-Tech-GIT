package com.hbm.dim.moho.biome;

import com.hbm.blocks.ModBlocks;

public class BiomeGenMohoRiver extends BiomeGenBaseMoho {

	public BiomeGenMohoRiver(int id) {
		super(id);
		this.setBiomeName("Lava River");

		this.setHeight(height_ShallowWaters);

		this.topBlock = ModBlocks.moho_regolith;
		this.fillerBlock = ModBlocks.moho_regolith;
	}

}