package com.hbm.dim;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerRiver extends GenLayer {

	private final BiomeGenBase riverBiome;

	public GenLayerRiver(long seed, GenLayer parent, BiomeGenBase riverBiome) {
		super(seed);
		this.parent = parent;
		this.riverBiome = riverBiome;
	}

	public int[] getInts(int x, int z, int width, int depth) {
		int i1 = x - 1;
		int j1 = z - 1;
		int k1 = width + 2;
		int l1 = depth + 2;
		int[] aint = parent.getInts(i1, j1, k1, l1);
		int[] aint1 = IntCache.getIntCache(width * depth);

		for(int i2 = 0; i2 < depth; ++i2) {
			for(int j2 = 0; j2 < width; ++j2) {
				int k2 = clampish(aint[j2 + 0 + (i2 + 1) * k1]);
				int l2 = clampish(aint[j2 + 2 + (i2 + 1) * k1]);
				int i3 = clampish(aint[j2 + 1 + (i2 + 0) * k1]);
				int j3 = clampish(aint[j2 + 1 + (i2 + 2) * k1]);
				int k3 = clampish(aint[j2 + 1 + (i2 + 1) * k1]);

				if(k3 == k2 && k3 == i3 && k3 == l2 && k3 == j3) {
					aint1[j2 + i2 * width] = aint[j2 + i2 * width];
				} else {
					aint1[j2 + i2 * width] = riverBiome.biomeID;
				}
			}
		}

		return aint1;
	}

	private int clampish(int value) {
		return value >= 2 ? 2 + (value & 1) : value;
	}

}
