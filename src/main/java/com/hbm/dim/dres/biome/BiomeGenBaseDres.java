/*******************************************************************************
 * Copyright 2015 SteveKunG - More Planets Mod
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/

package com.hbm.dim.dres.biome;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.BiomeDecoratorCelestial;

import net.minecraft.world.biome.BiomeGenBase;

public abstract class BiomeGenBaseDres extends BiomeGenBase {

    public static final BiomeGenBase dresPlains = new BiomeGenDresPlains(SpaceConfig.dresBiome).setTemperatureRainfall(-1.0F, 0.0F);
    public static final BiomeGenBase dresCanyon = new BiomeGenDresCanyon(SpaceConfig.dreBasins).setTemperatureRainfall(-1.0F, 0.0F);
    
    public BiomeGenBaseDres(int id) {
        super(id);
		this.setDisableRain();
		
        this.spawnableCreatureList.clear();
        this.spawnableMonsterList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
        
        this.theBiomeDecorator = new BiomeDecoratorCelestial(ModBlocks.dres_rock);
        this.theBiomeDecorator.generateLakes = false;
        
        this.topBlock = ModBlocks.sellafield_slaked;
        this.fillerBlock = ModBlocks.sellafield_slaked;
    }

}