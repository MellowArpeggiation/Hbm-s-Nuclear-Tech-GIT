package com.hbm.dim.thatmo;

import java.util.HashMap;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.main.StructureManager;
import com.hbm.world.gen.NBTStructure;
import com.hbm.world.gen.NBTStructure.JigsawPiece;
import com.hbm.world.gen.NBTStructure.JigsawPool;
import com.hbm.world.gen.NBTStructure.SpawnCondition;
import com.hbm.world.gen.component.Component.ConcreteBricks;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;

public class WorldGeneratorThatmo implements IWorldGenerator {

	public WorldGeneratorThatmo() {
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition() {{
			structure = new JigsawPiece("thatmotest", StructureManager.THATMOTESTMO, -1);
			canSpawn = biome -> biome.heightVariation < 0.1F;
		}});
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition() {{
			structure = new JigsawPiece("thatmotest2", StructureManager.thatmo2, 1);
			canSpawn = biome -> biome.heightVariation < 0.1F;
		}});
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition() {{
			JigsawPiece rupture = new JigsawPiece("trenches", StructureManager.trenches, -1);
			rupture.conformToTerrain = true;
			spawnWeight = 2;
			canSpawn = biome -> biome.heightVariation < 0.1F;
			rupture.blockTable = new HashMap<Block, BlockSelector>() {{
				put(ModBlocks.brick_concrete_cracked, new C());
			}};
		}});
		/*
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition() {{
			sizeLimit = 128;
			canSpawn = biome -> biome.heightVariation < 0.1F;
			startPool = "default";
			pools = new HashMap<String, NBTStructure.JigsawPool>() {{
				put("default", new JigsawPool() {{
					add(new JigsawPiece("tr1", StructureManager.tr1, -3){{ conformToTerrain = true; }}, 2);
					add(new JigsawPiece("tr2", StructureManager.tr2, -3){{ conformToTerrain = true; }}, 2);
					add(new JigsawPiece("tr3", StructureManager.tr3, -3){{ conformToTerrain = true; }}, 2);
					add(new JigsawPiece("tr4", StructureManager.tr4, -3){{ conformToTerrain = true; }}, 2);
				}});

			}};
		}});
		*/

		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition() {{
			sizeLimit = 128;
			canSpawn = biome -> biome.heightVariation < 0.1F;
			startPool = "default";
			pools = new HashMap<String, NBTStructure.JigsawPool>() {{
				put("default", new JigsawPool() {{
					add(new JigsawPiece("intersection", StructureManager.intersection, 0){{ alignToTerrain = true; }}, 1);
					add(new JigsawPiece("road_1", StructureManager.road, 0){{ conformToTerrain = true; }}, 1);
					add(new JigsawPiece("curve_1", StructureManager.curve, 0){{ conformToTerrain = true; }}, 1);
					add(new JigsawPiece("tshape", StructureManager.tshape, 0){{ conformToTerrain = true; }}, 1);
					add(new JigsawPiece("block1", StructureManager.block1, 0){{ alignToTerrain = true; }}, 1);
					add(new JigsawPiece("block2", StructureManager.block2, 0){{ alignToTerrain = true; }}, 1);
					add(new JigsawPiece("pfmfac", StructureManager.pfmfac, 0){{ alignToTerrain = true; }}, 1);
				}});
				put("roadsonly", new JigsawPool() {{
					add(new JigsawPiece("road_2", StructureManager.road, 0){{ conformToTerrain = true; }}, 1);
					add(new JigsawPiece("curve_2", StructureManager.curve, 0){{ conformToTerrain = true; }}, 1);
				}});

			}};
		}});


	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.thatmoDimension) {
			generateThatmo(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateThatmo(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);

	}
}