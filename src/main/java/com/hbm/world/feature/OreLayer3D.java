package com.hbm.world.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.hbm.dim.WorldProviderCelestial;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.world.WorldEvent;

public class OreLayer3D {

	public static int counter = 0;
	public int id;

	NoiseGeneratorPerlin noiseX;
	NoiseGeneratorPerlin noiseY;
	NoiseGeneratorPerlin noiseZ;

	double scaleH;
	double scaleV;
	double threshold;

	Block block;
	int meta;
	int dim = 0;
	boolean allCelestials = false;

	Map<Integer, Set<ChunkCoordIntPair>> alreadyDecorated = new HashMap<>();

	public OreLayer3D(Block block, int meta) {
		this.block = block;
		this.meta = meta;
		MinecraftForge.EVENT_BUS.register(this);
		this.id = counter;
		counter++;
	}

	public OreLayer3D setDimension(int dim) {
		this.dim = dim;
		return this;
	}

	// If enabled, this vein will spawn on all celestial bodies
	public OreLayer3D setGlobal(boolean value) {
		this.allCelestials = value;
		return this;
	}

	public OreLayer3D setScaleH(double scale) {
		this.scaleH = scale;
		return this;
	}

	public OreLayer3D setScaleV(double scale) {
		this.scaleV = scale;
		return this;
	}

	public OreLayer3D setThreshold(double threshold) {
		this.threshold = threshold;
		return this;
	}

	@SubscribeEvent
	public void onDecorate(DecorateBiomeEvent.Pre event) {
		World world = event.world;
		int cX = event.chunkX;
		int cZ = event.chunkZ;

		if(world.provider == null) return;

		Block replace = Blocks.stone;
		if(world.provider instanceof WorldProviderCelestial) {
			replace = ((WorldProviderCelestial)world.provider).getStone();
		}

		if(allCelestials) {
			if(!(world.provider instanceof WorldProviderCelestial) && world.provider.dimensionId != 0) return;
		} else {
			if(world.provider.dimensionId != this.dim) return;
		}

		ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(cX, cZ);
		Set<ChunkCoordIntPair> decoratedChunks = alreadyDecorated.computeIfAbsent(world.provider.dimensionId, n -> new HashSet<>());

		// Stop early if we've already generated this chunk in this dimension
		if(decoratedChunks.contains(chunkPos)) return;
		decoratedChunks.add(chunkPos);

		if(this.noiseX == null) this.noiseX = new NoiseGeneratorPerlin(new Random(event.world.getSeed() + 101 + id), 4);
		if(this.noiseY == null) this.noiseY = new NoiseGeneratorPerlin(new Random(event.world.getSeed() + 102 + id), 4);
		if(this.noiseZ == null) this.noiseZ = new NoiseGeneratorPerlin(new Random(event.world.getSeed() + 103 + id), 4);

		for(int x = cX + 8; x < cX + 24; x++) {
			for(int z = cZ + 8; z < cZ + 24; z++) {
				double nY = this.noiseY.func_151601_a(x * scaleH, z * scaleH);
				for(int y = 64; y > 5; y--) {
					double nX = this.noiseX.func_151601_a(y * scaleV, z * scaleH);
					double nZ = this.noiseZ.func_151601_a(x * scaleH, y * scaleV);

					if(nX * nY * nZ > threshold) {
						Block target = world.getBlock(x, y, z);

						if(target.isNormalCube() && target.isReplaceableOreGen(world, x, y, z, replace)) {
							world.setBlock(x, y, z, block, meta, 2);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		alreadyDecorated.put(event.world.provider.dimensionId, new HashSet<>());
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		alreadyDecorated.remove(event.world.provider.dimensionId);
	}

}
