package com.hbm.dim.thatmo;

import java.util.HashMap;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsComponent;
import com.hbm.main.StructureManager;
import com.hbm.world.gen.NBTStructure;
import com.hbm.world.gen.NBTStructure.Loot;
import com.hbm.world.gen.NBTStructure.SpawnCondition;
import com.hbm.world.generator.DungeonToolbox;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldGeneratorThatmo implements IWorldGenerator {

	public WorldGeneratorThatmo() {
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition() {{
			structure = StructureManager.THATMOTESTMO;
			canSpawn = biome -> biome.heightVariation < 0.1F;
			lootTable = new HashMap<Block, Loot>() {{
				put(ModBlocks.crate_iron, new Loot(ItemPool.getPool(ItemPoolsComponent.POOL_MACHINE_PARTS), 8, 12));
				put(ModBlocks.filing_cabinet, new Loot(ItemPool.getPool(ItemPoolsComponent.POOL_OFFICE_TRASH), 0, 6));
			}};
		}});
		NBTStructure.registerNullWeight(SpaceConfig.thatmoDimension, 2);
		NBTStructure.registerStructure(SpaceConfig.thatmoDimension, new SpawnCondition() {{
			structure = StructureManager.thatmo2;
			canSpawn = biome -> biome.heightVariation < 0.1F;
			lootTable = new HashMap<Block, Loot>() {{
				put(ModBlocks.crate_iron, new Loot(ItemPool.getPool(ItemPoolsComponent.POOL_MACHINE_PARTS), 8, 12));
				put(ModBlocks.filing_cabinet, new Loot(ItemPool.getPool(ItemPoolsComponent.POOL_OFFICE_TRASH), 0, 6));
			}};
		}});
		NBTStructure.registerNullWeight(SpaceConfig.thatmoDimension, 2);
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