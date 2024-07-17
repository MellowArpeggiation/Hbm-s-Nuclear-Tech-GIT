package com.hbm.dim.laythe;

import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.laythe.GenLayerLaythe.GenLayerDiversifyLaythe;
import com.hbm.dim.laythe.GenLayerLaythe.GenLayerLaytheBiomes;
import com.hbm.dim.laythe.GenLayerLaythe.GenLayerLaytheIslands;
import com.hbm.dim.laythe.GenLayerLaythe.GenLayerLaytheOceans;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerSmooth;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraftforge.client.IRenderHandler;

public class WorldProviderLaythe extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerCelestial(createBiomeGenerators(worldObj.getSeed()));
	}

	@Override
	public String getDimensionName() {
		return "Laythe";
	}
	
	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderLaythe(this.worldObj, this.getSeed(), false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		return new SkyProviderLaytheSunset();
	}

	@Override
	public boolean hasLife() {
		return true;
	}

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer biomes = new GenLayerLaytheBiomes(seed);

		biomes = new GenLayerFuzzyZoom(2000L, biomes);
		biomes = new GenLayerZoom(2001L, biomes);
		biomes = new GenLayerDiversifyLaythe(1000L, biomes);
		biomes = new GenLayerZoom(1000L, biomes);
		biomes = new GenLayerZoom(1001L, biomes);
		biomes = new GenLayerLaytheOceans(4000L, biomes);
		biomes = new GenLayerLaytheOceans(4000L, biomes);
		biomes = new GenLayerLaytheOceans(4000L, biomes);
		biomes = new GenLayerLaytheOceans(4000L, biomes);
		biomes = new GenLayerZoom(1003L, biomes);
		biomes = new GenLayerSmooth(700L, biomes);
		biomes = new GenLayerLaytheIslands(200L, biomes);

		biomes = new GenLayerZoom(1006L, biomes);
			
		GenLayer genLayerVoronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

		return new BiomeGenLayers(biomes, genLayerVoronoiZoom, seed);
	}

}