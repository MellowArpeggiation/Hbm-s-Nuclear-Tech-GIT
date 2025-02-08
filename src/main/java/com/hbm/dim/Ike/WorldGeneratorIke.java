package com.hbm.dim.Ike;

import java.util.HashMap;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.main.StructureManager;
import com.hbm.world.dungeon.AncientTomb;
import com.hbm.world.gen.NBTStructure;
import com.hbm.world.gen.NBTStructure.JigsawPiece;
import com.hbm.world.gen.NBTStructure.JigsawPool;
import com.hbm.world.gen.NBTStructure.SpawnCondition;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorIke implements IWorldGenerator {

	public WorldGeneratorIke() {
		NBTStructure.registerStructure(SpaceConfig.ikeDimension, new SpawnCondition() {{
			minHeight = 32;
			maxHeight = 32;
			sizeLimit = 32;
			startPool = "start";
			pools = new HashMap<String, NBTStructure.JigsawPool>() {{
				put("start", new JigsawPool() {{
					add(new JigsawPiece("ike_meteor_core", StructureManager.meteor_core), 1);
				}});
				put("default", new JigsawPool() {{
					add(new JigsawPiece("ike_meteor_corner", StructureManager.meteor_corner), 2);
					add(new JigsawPiece("ike_meteor_t", StructureManager.meteor_t), 2);
					add(new JigsawPiece("ike_meteor_stairs", StructureManager.meteor_stairs), 1);
					add(new JigsawPiece("ike_meteor_room_base_thru", StructureManager.meteor_room_base_thru), 2);
					add(new JigsawPiece("ike_meteor_room_base_end", StructureManager.meteor_room_base_end), 3);
					fallback = "fallback";
				}});
				put("10room", new JigsawPool() {{
					add(new JigsawPiece("ike_meteor_room_basic", StructureManager.meteor_room_basic), 1);
					add(new JigsawPiece("ike_meteor_room_balcony", StructureManager.meteor_room_balcony), 1);
					add(new JigsawPiece("ike_meteor_room_dragon", StructureManager.meteor_room_dragon), 1);
					fallback = "roomback";
				}});
				put("3x3loot", new JigsawPool() {{
					add(new JigsawPiece("ike_meteor_3_bale", StructureManager.meteor_3_bale), 1);
					add(new JigsawPiece("ike_meteor_3_blank", StructureManager.meteor_3_blank), 1);
					add(new JigsawPiece("ike_meteor_3_block", StructureManager.meteor_3_block), 1);
					add(new JigsawPiece("ike_meteor_3_crab", StructureManager.meteor_3_crab), 1);
					add(new JigsawPiece("ike_meteor_3_crab_tesla", StructureManager.meteor_3_crab_tesla), 1);
					add(new JigsawPiece("ike_meteor_3_crate", StructureManager.meteor_3_crate), 1);
					add(new JigsawPiece("ike_meteor_3_dirt", StructureManager.meteor_3_dirt), 1);
					add(new JigsawPiece("ike_meteor_3_lead", StructureManager.meteor_3_lead), 1);
					add(new JigsawPiece("ike_meteor_3_ooze", StructureManager.meteor_3_ooze), 1);
					add(new JigsawPiece("ike_meteor_3_pillar", StructureManager.meteor_3_pillar), 1);
					add(new JigsawPiece("ike_meteor_3_star", StructureManager.meteor_3_star), 1);
					add(new JigsawPiece("ike_meteor_3_tesla", StructureManager.meteor_3_tesla), 1);
				}});
				put("headloot", new JigsawPool() {{
					add(new JigsawPiece("ike_meteor_dragon_chest", StructureManager.meteor_dragon_chest), 1);
					add(new JigsawPiece("ike_meteor_dragon_tesla", StructureManager.meteor_dragon_tesla), 1);
					add(new JigsawPiece("ike_meteor_dragon_trap", StructureManager.meteor_dragon_trap), 1);
					add(new JigsawPiece("ike_meteor_dragon_crate_crab", StructureManager.meteor_dragon_crate_crab), 1);
				}});
				put("fallback", new JigsawPool() {{
					add(new JigsawPiece("ike_meteor_fallback", StructureManager.meteor_fallback), 1);
				}});
				put("roomback", new JigsawPool() {{
					add(new JigsawPiece("ike_meteor_room_fallback", StructureManager.meteor_room_fallback), 1);
				}});
			}};
		}});
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.dimensionId == SpaceConfig.ikeDimension) {
			generateIke(world, random, chunkX * 16, chunkZ * 16);
		}
	}

	private void generateIke(World world, Random rand, int i, int j) {
		int meta = CelestialBody.getMeta(world);

		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.asbestosSpawn, 8, 3, 22, ModBlocks.ore_asbestos, meta, ModBlocks.ike_stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.copperSpawn, 9, 4, 27, ModBlocks.ore_copper, meta, ModBlocks.ike_stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.ironClusterSpawn,  8, 1, 33, ModBlocks.ore_iron, meta, ModBlocks.ike_stone);
		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.lithiumSpawn,  6, 4, 8, ModBlocks.ore_lithium, meta, ModBlocks.ike_stone);
		DungeonToolbox.generateOre(world, rand, i, j, 2, 4, 15, 40, ModBlocks.ore_coltan, meta, ModBlocks.ike_stone);

		//okay okay okay, lets say on duna you DO make solvent, this is now awesome because you can now make gallium arsenide to then head to
		//dres and the likes :)


		DungeonToolbox.generateOre(world, rand, i, j, WorldConfig.mineralSpawn, 10, 12, 32, ModBlocks.ore_mineral, meta, ModBlocks.ike_stone);



		if(WorldConfig.pyramidStructure > 0 && rand.nextInt(WorldConfig.pyramidStructure) == 0) {
			int x = i + rand.nextInt(16);
			int z = j + rand.nextInt(16);
			int y = world.getHeightValue(x, z);

			new AncientTomb().build(world, rand, x, y, z);
		}
	}
}