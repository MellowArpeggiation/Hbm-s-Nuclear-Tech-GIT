

package com.hbm.dim.eve.biome;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeDecoratorDead;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeGenBaseEve extends BiomeGenBase {

	public static final BiomeGenBase evePlains = new BiomeGenEvePlains(SpaceConfig.eveBiome).setTemperatureRainfall(1.0F, 0.5F);
	public static final BiomeGenBase eveOcean = new BiomeGenEveOcean(SpaceConfig.eveOceanBiome).setTemperatureRainfall(1.0F, 0.5F);
	public static final BiomeGenBase eveMountains = new BiomeGenEveMountains(SpaceConfig.eveMountainsBiome).setTemperatureRainfall(1.0F, 0.5F);
	public static final BiomeGenBase eveSeismicPlains = new BiomeGenEveSeismicPlains(SpaceConfig.eveSeismicBiome).setTemperatureRainfall(1.0F, 0.5F);
	public static final BiomeGenBase eveRiver = new BiomeGenEveRiver(128).setTemperatureRainfall(1.0F, 0.5F);

	public BiomeGenBaseEve(int id) {
		super(id);
		this.waterColorMultiplier = 0x5b009a;

		this.theBiomeDecorator = new BiomeDecoratorDead(ModBlocks.eve_rock);

		this.spawnableCreatureList.clear();
		this.spawnableMonsterList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCaveCreatureList.clear();
		
		this.topBlock = ModBlocks.eve_silt;
		this.fillerBlock = ModBlocks.eve_rock;
		BiomeDictionary.registerBiomeType(this, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.SPOOKY);
	}
}