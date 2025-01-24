package com.hbm.dim.laythe.biome;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenLaytheCoast extends BiomeGenBaseLaythe {

	public BiomeGenLaytheCoast(int id) {
		super(id);
		this.setBiomeName("Laythe Reef");

		this.setHeight(new BiomeGenBase.Height(-0.4F, 0.01F));
		this.setTemperatureRainfall(0.2F, 0.2F);
	}
}
