package com.hbm.world.gen;

import java.util.Random;

import com.hbm.config.StructureConfig;

import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import static net.minecraftforge.common.BiomeDictionary.*;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import static net.minecraftforge.event.terraingen.TerrainGen.*;
import net.minecraftforge.event.world.WorldEvent;

public class NTMWorldGenerator implements IWorldGenerator {

	private MapGenNTMFeatures scatteredFeatureGen = new MapGenNTMFeatures();
	private NBTStructure.GenStructure nbtGen = new NBTStructure.GenStructure();
	
	private final Random rand = new Random(); //A central random, used to cleanly generate our stuff without affecting vanilla or modded seeds.
	
	/** Inits all MapGen upon the loading of a new world. Hopefully clears out structureMaps and structureData when a different world is loaded. */
	@SubscribeEvent
	public void onLoad(WorldEvent.Load event) {
		scatteredFeatureGen = (MapGenNTMFeatures) getModdedMapGen(new MapGenNTMFeatures(), EventType.CUSTOM);
		nbtGen = (NBTStructure.GenStructure) getModdedMapGen(new NBTStructure.GenStructure(), EventType.CUSTOM);
		
		hasPopulationEvent = false;
	}
	
	/** Called upon the initial population of a chunk. Called in the pre-population event first; called again if pre-population didn't occur (flatland) */
	private void setRandomSeed(World world, int chunkX, int chunkZ) {
		rand.setSeed(world.getSeed() + world.provider.dimensionId);
		final long i = rand.nextLong() / 2L * 2L + 1L;
		final long j = rand.nextLong() / 2L * 2L + 1L;
		rand.setSeed((long)chunkX * i + (long)chunkZ * j ^ world.getSeed());
	}
	
	/*
	 * Pre-population Events / Structure Generation
	 * Used to generate structures without unnecessary intrusion by biome decoration, like trees.
	 */
	
	private boolean hasPopulationEvent = false; // Does the given chunkGenerator have a population event? If not (flatlands), default to using generate.
	
	@SubscribeEvent
	public void generateStructures(PopulateChunkEvent.Pre event) {
		hasPopulationEvent = true;
		
		if(StructureConfig.enableStructures == 0) return;
		if(StructureConfig.enableStructures == 2 && !event.world.getWorldInfo().isMapFeaturesEnabled()) return;

		setRandomSeed(event.world, event.chunkX, event.chunkZ); //Set random for population down the line.

		nbtGen.generateStructures(event.world, rand, event.chunkProvider, event.chunkX, event.chunkZ);
		
		switch (event.world.provider.dimensionId) {
		case -1:
			break;
		case 0:
			generateOverworldStructures(event.world, event.chunkProvider, event.chunkX, event.chunkZ);
			break;
		case 1:
			break;
		}
	}
	
	protected void generateOverworldStructures(World world, IChunkProvider chunkProvider, int chunkX, int chunkZ) {
		Block[] ablock = new Block[65536]; //ablock isn't actually used for anything in MapGenStructure
		
		this.scatteredFeatureGen.func_151539_a(chunkProvider, world, chunkX, chunkZ, ablock);
		this.scatteredFeatureGen.generateStructuresInChunk(world, rand, chunkX, chunkZ);
	}
	
	/*
	 * Post-Vanilla / Modded Generation
	 * Used to generate features that don't care about intrusions (ores, craters, caves, etc.)
	 */
	
	@Override
	public void generate(Random unusedRandom, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(hasPopulationEvent) return; //If we've failed to generate any structures (flatlands)
		
		if(StructureConfig.enableStructures == 0) return;
		if(StructureConfig.enableStructures == 2 && !world.getWorldInfo().isMapFeaturesEnabled()) return;

		setRandomSeed(world, chunkX, chunkZ); //Reset the random seed to compensate

		nbtGen.generateStructures(world, rand, chunkProvider, chunkX, chunkZ);
		
		switch (world.provider.dimensionId) {
		case -1:
			generateNetherStructures(world, chunkGenerator, chunkX, chunkZ); break;
		case 0:
			generateOverworldStructures(world, chunkGenerator, chunkX, chunkZ); break;
		case 1:
			generateEndStructures(world, chunkGenerator, chunkX, chunkZ); break;
		}
	}
	
	private void generateNetherStructures(World world, IChunkProvider chunkGenerator, int chunkX, int chunkZ) { }
	private void generateEndStructures(World world, IChunkProvider chunkGenerator, int chunkX, int chunkZ) { }
	
	/** Utility method for biome checking multiple types exclusively. Not sure why it wasn't already present. */
	public static boolean isBiomeOfTypes(BiomeGenBase biome, Type... types) { //If new biomes are implemented, move this to any biome-related utility class.
		for(Type type : types) {
			if(!isBiomeOfType(biome, type)) return false;
		}
		
		return true;
	}
	
	/** utility method, same as above but inclusive. useful for catch-alls, like the dirty glass structures have */
	public static boolean doesBiomeHaveTypes(BiomeGenBase biome, Type... types) {
		for(Type type : types) {
			if(isBiomeOfType(biome, type)) return true;
		}
		
		return false;
	}
}