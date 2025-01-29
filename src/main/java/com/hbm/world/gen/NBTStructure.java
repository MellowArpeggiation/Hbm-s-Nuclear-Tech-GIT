package com.hbm.world.gen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.blocks.generic.BlockBobble.TileEntityBobble;
import com.hbm.config.GeneralConfig;
import com.hbm.config.StructureConfig;
import com.hbm.handler.ThreeInts;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple.Pair;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.client.model.ModelFormatException;
import net.minecraftforge.common.util.Constants.NBT;

public class NBTStructure {

	/**
	 * Now with structure support!
	 * 
	 * the type of structure to generate is saved into the Component,
	 * meaning this can generate all sorts of different structures,
	 * without having to define and register each structure manually
	 */
	
	// TODO: add rotation support

	protected static Map<Integer, List<SpawnCondition>> dimensionMap = new HashMap<>();

	public String structureName;

	private boolean isLoaded;
	private ThreeInts size;
	private BlockDefinition[] palette;
	private List<Pair<Short, String>> itemPalette;
	private BlockState[] blocks;

	public NBTStructure(ResourceLocation resource) {
		try {
			IResource res = Minecraft.getMinecraft().getResourceManager().getResource(resource);
			loadStructure(res.toString(), res.getInputStream());
		} catch(IOException e) {
			throw new ModelFormatException("IO Exception loading NBT resource", e);
		}
	}

	public static void register() {
		MapGenStructureIO.registerStructure(Start.class, "NBTStructures");
		MapGenStructureIO.func_143031_a(Component.class, "NBTComponents");
	}

	// REGISTRATION ORDER MATTERS, make sure new structures are registered AFTER older ones
	public static void registerStructureForDimension(int dimensionId, SpawnCondition spawn) {
		List<SpawnCondition> list = dimensionMap.computeIfAbsent(dimensionId, integer -> new ArrayList<SpawnCondition>());
		spawn.dimensionId = dimensionId;
		spawn.conditionId = list.size();
		list.add(spawn);
	}

	// Saves a selected area into an NBT structure (+ some of our non-standard stuff to support 1.7.10)
	public static void saveArea(String filename, World world, int x1, int y1, int z1, int x2, int y2, int z2, Set<Pair<Block, Integer>> exclude) {
		NBTTagCompound structure = new NBTTagCompound();
		NBTTagList nbtBlocks = new NBTTagList();
		NBTTagList nbtPalette = new NBTTagList();
		NBTTagList nbtItemPalette = new NBTTagList();

		// Quick access hash slinging slashers
		Map<Pair<Block, Integer>, Integer> palette = new HashMap<>();
		Map<Short, Integer> itemPalette = new HashMap<>();

		structure.setInteger("version", 1);

		int ox = Math.min(x1, x2);
		int oy = Math.min(y1, y2);
		int oz = Math.min(z1, z2);

		for(int x = ox; x <= Math.max(x1, x2); x++) {
			for(int y = oy; y <= Math.max(y1, y2); y++) {
				for(int z = oz; z <= Math.max(z1, z2); z++) {
					Pair<Block, Integer> block = new Pair<Block, Integer>(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));

					if(exclude.contains(block)) continue;

					int paletteId = palette.size();
					if(palette.containsKey(block)) {
						paletteId = palette.get(block);
					} else {
						palette.put(block, paletteId);

						NBTTagCompound nbtBlock = new NBTTagCompound();
						nbtBlock.setString("Name", GameRegistry.findUniqueIdentifierFor(block.key).toString());
						
						NBTTagCompound nbtProp = new NBTTagCompound();
						nbtProp.setString("meta", block.value.toString());

						nbtBlock.setTag("Properties", nbtProp);

						nbtPalette.appendTag(nbtBlock);
					}

					NBTTagCompound nbtBlock = new NBTTagCompound();
					nbtBlock.setInteger("state", paletteId);

					NBTTagList nbtPos = new NBTTagList();
					nbtPos.appendTag(new NBTTagInt(x - ox));
					nbtPos.appendTag(new NBTTagInt(y - oy));
					nbtPos.appendTag(new NBTTagInt(z - oz));

					nbtBlock.setTag("pos", nbtPos);

					TileEntity te = world.getTileEntity(x, y, z);
					if(te != null) {
						NBTTagCompound nbt = new NBTTagCompound();
						te.writeToNBT(nbt);

						nbt.setInteger("x", nbt.getInteger("x") - ox);
						nbt.setInteger("y", nbt.getInteger("y") - oy);
						nbt.setInteger("z", nbt.getInteger("z") - oz);

						nbtBlock.setTag("nbt", nbt);

						String itemKey = null;
						if(nbt.hasKey("items")) itemKey = "items";
						if(nbt.hasKey("Items")) itemKey = "Items";

						if(nbt.hasKey(itemKey)) {
							NBTTagList items = nbt.getTagList("items", NBT.TAG_COMPOUND);
							for(int i = 0; i < items.tagCount(); i++) {
								NBTTagCompound item = items.getCompoundTagAt(i);
								short id = item.getShort("id");
								String name = GameRegistry.findUniqueIdentifierFor(Item.getItemById(id)).toString();

								if(!itemPalette.containsKey(id)) {
									int itemPaletteId = itemPalette.size();
									itemPalette.put(id, itemPaletteId);

									NBTTagCompound nbtItem = new NBTTagCompound();
									nbtItem.setShort("ID", id);
									nbtItem.setString("Name", name);

									nbtItemPalette.appendTag(nbtItem);
								}
							}
						}
					}

					nbtBlocks.appendTag(nbtBlock);
				}
			}
		}

		structure.setTag("blocks", nbtBlocks);
		structure.setTag("palette", nbtPalette);
		structure.setTag("itemPalette", nbtItemPalette);

		NBTTagList nbtSize = new NBTTagList();
		nbtSize.appendTag(new NBTTagInt(Math.abs(x1 - x2) + 1));
		nbtSize.appendTag(new NBTTagInt(Math.abs(y1 - y2) + 1));
		nbtSize.appendTag(new NBTTagInt(Math.abs(z1 - z2) + 1));
		structure.setTag("size", nbtSize);

		structure.setTag("entities", new NBTTagList());

		try {
			File structureDirectory = new File(Minecraft.getMinecraft().mcDataDir, "structures");
			structureDirectory.mkdir();

			File structureFile = new File(structureDirectory, filename);

			CompressedStreamTools.writeCompressed(structure, new FileOutputStream(structureFile));
		} catch (Exception ex) {
			MainRegistry.logger.warn("Failed to save NBT structure", ex);
		}
	}

	private void loadStructure(String inputName, InputStream inputStream) {
		try {
			NBTTagCompound data = CompressedStreamTools.readCompressed(inputStream);


			// GET SIZE (for offsetting to center)
			size = parsePos(data.getTagList("size", NBT.TAG_INT));

			
			// PARSE BLOCK PALETTE
			NBTTagList paletteList = data.getTagList("palette", NBT.TAG_COMPOUND);
			palette = new BlockDefinition[paletteList.tagCount()];

			for(int i = 0; i < paletteList.tagCount(); i++) {
				NBTTagCompound p = paletteList.getCompoundTagAt(i);

				String blockName = p.getString("Name");
				NBTTagCompound prop = p.getCompoundTag("Properties");

				int meta = 0;
				try {
					meta = Integer.parseInt(prop.getString("meta"));
				} catch(NumberFormatException ex) {
					MainRegistry.logger.info("Failed to parse: " + prop.getString("meta"));
					meta = 0;
				}

				palette[i] = new BlockDefinition(blockName, meta);
			}
			

			// PARSE ITEM PALETTE (custom shite)
			if(data.hasKey("itemPalette")) {
				NBTTagList itemPaletteList = data.getTagList("itemPalette", NBT.TAG_COMPOUND);
				itemPalette = new ArrayList<>(itemPaletteList.tagCount());

				for(int i = 0; i < itemPaletteList.tagCount(); i++) {
					NBTTagCompound p = itemPaletteList.getCompoundTagAt(i);

					short id = p.getShort("ID");
					String name = p.getString("Name");

					itemPalette.add(new Pair<>(id, name));
				}
			} else {
				itemPalette = null;
			}


			// LOAD IN BLOCKS
			NBTTagList blockData = data.getTagList("blocks", NBT.TAG_COMPOUND);
			blocks = new BlockState[blockData.tagCount()];

			for(int i = 0; i < blockData.tagCount(); i++) {
				NBTTagCompound block = blockData.getCompoundTagAt(i);
				int state = block.getInteger("state");
				ThreeInts pos = parsePos(block.getTagList("pos", NBT.TAG_INT));

				blocks[i] = new BlockState(palette[state], pos);

				if(block.hasKey("nbt")) {
					blocks[i].nbt = block.getCompoundTag("nbt");
				}
			}


			isLoaded = true;

			structureName = inputName;

		} catch(IOException e) {
			throw new ModelFormatException("IO Exception reading NBT Structure format", e);
		} finally {
			try {
				inputStream.close();
			} catch(IOException e) {
				// hush
			}
		}
	}

	private HashMap<Short, Short> getWorldItemPalette() {
		if(itemPalette == null) return null;

		HashMap<Short, Short> worldItemPalette = new HashMap<>();

		for(Pair<Short, String> entry : itemPalette) {
			Item item = (Item)Item.itemRegistry.getObject(entry.getValue());

			worldItemPalette.put(entry.getKey(), (short)Item.getIdFromItem(item));
		}

		return worldItemPalette;
	}

	private TileEntity buildTileEntity(World world, Block block, Map<Block, Loot> lootTable, HashMap<Short, Short> worldItemPalette, NBTTagCompound nbt) {
		nbt = (NBTTagCompound)nbt.copy();

		if(worldItemPalette != null) relinkItems(worldItemPalette, nbt);

		TileEntity te = TileEntity.createAndLoadEntity(nbt);

		if(lootTable != null && te instanceof IInventory && lootTable.containsKey(block)) {
			Loot entry = lootTable.get(block);
			WeightedRandomChestContent.generateChestContents(world.rand, entry.table, (IInventory) te, world.rand.nextInt(entry.maxLoot - entry.minLoot) + entry.minLoot);
		}

		if(te instanceof TileEntityBobble) {
			((TileEntityBobble) te).type = BobbleType.values()[world.rand.nextInt(BobbleType.values().length - 1) + 1];
		}

		return te;
	}

	public void build(World world, int x, int y, int z) {
		build(world, x, y, z, null);
	}

	public void build(World world, int x, int y, int z, Map<Block, Loot> lootTable) {
		if(!isLoaded) {
			MainRegistry.logger.info("NBTStructure is invalid");
			return;
		}

		x -= size.x / 2;
		z -= size.z / 2;

		HashMap<Short, Short> worldItemPalette = getWorldItemPalette();

		for(BlockState block : blocks) {
			world.setBlock(x + block.pos.x, y + block.pos.y, z + block.pos.z, block.definition.block, block.definition.meta, 2);

			if(block.nbt != null) {
				TileEntity te = buildTileEntity(world, block.definition.block, lootTable, worldItemPalette, block.nbt);
				world.setTileEntity(x + block.pos.x, y + block.pos.y, z + block.pos.z, te);
			}
		}
	}

	protected boolean build(World world, Map<Block, Loot> lootTable, StructureBoundingBox totalBounds, StructureBoundingBox generatingBounds) {
		if(!isLoaded) {
			MainRegistry.logger.info("NBTStructure is invalid");
			return false;
		}

		HashMap<Short, Short> worldItemPalette = getWorldItemPalette();

		for(BlockState block : blocks) {
			int bx = totalBounds.minX + block.pos.x;
			int by = totalBounds.minY + block.pos.y;
			int bz = totalBounds.minZ + block.pos.z;

			// Check that this block is inside the currently generating area, preventing cascades
			if(!generatingBounds.isVecInside(bx, by, bz)) continue;

			world.setBlock(bx, by, bz, block.definition.block, block.definition.meta, 2);

			if(block.nbt != null) {
				TileEntity te = buildTileEntity(world, block.definition.block, lootTable, worldItemPalette, block.nbt);
				world.setTileEntity(bx, by, bz, te);
			}
		}

		return true;
	}

	// What a fucken mess, why even implement the IntArray NBT if ye aint gonna use it Moe Yang?
	private ThreeInts parsePos(NBTTagList pos) {
		NBTBase xb = (NBTBase)pos.tagList.get(0);
		int x = ((NBTTagInt)xb).func_150287_d();
		NBTBase yb = (NBTBase)pos.tagList.get(1);
		int y = ((NBTTagInt)yb).func_150287_d();
		NBTBase zb = (NBTBase)pos.tagList.get(2);
		int z = ((NBTTagInt)zb).func_150287_d();

		return new ThreeInts(x, y, z);
	}

	// NON-STANDARD, items are serialized with IDs, which will differ from world to world!
	// So our fixed exporter adds an itemPalette, please don't hunt me down for fucking with the spec
	private void relinkItems(HashMap<Short, Short> palette, NBTTagCompound nbt) {
		NBTTagList items = null;
		if(nbt.hasKey("items"))
			items = nbt.getTagList("items", NBT.TAG_COMPOUND);
		if(nbt.hasKey("Items"))
			items = nbt.getTagList("Items", NBT.TAG_COMPOUND);

		if(items == null) return;

		for(int i = 0; i < items.tagCount(); i++) {
			NBTTagCompound item = items.getCompoundTagAt(i);
			item.setShort("id", palette.get(item.getShort("id")));
		}
	}

	private static class BlockState {

		final BlockDefinition definition;
		final ThreeInts pos;
		NBTTagCompound nbt;

		BlockState(BlockDefinition definition, ThreeInts pos) {
			this.definition = definition;
			this.pos = pos;
		}

	}

	private static class BlockDefinition {

		final Block block;
		final int meta;

		BlockDefinition(String name, int meta) {
			this.block = Block.getBlockFromName(name);
			this.meta = meta;
		}

	}

	public static class Loot {

		private final WeightedRandomChestContent[] table;
		private final int minLoot;
		private final int maxLoot;

		public Loot(WeightedRandomChestContent[] table, int minLoot, int maxLoot) {
			this.table = table;
			this.minLoot = minLoot;
			this.maxLoot = maxLoot;
		}

	}
	
	public static class SpawnCondition {

		public NBTStructure structure;
		public Map<Block, Loot> lootTable;

		public List<BiomeGenBase> validBiomes;
		public int minHeight = 0;
		public int maxHeight = 128;

		// Used for serializing/deserializing in Component
		private int dimensionId;
		private int conditionId;

		// Can this spawn in the current biome
		protected boolean isValid(BiomeGenBase biome) {
			if(validBiomes == null) return true;
			return validBiomes.contains(biome);
		}

	}

	public static class Component extends StructureComponent {

		SpawnCondition spawn;

		public Component() {}
		
		public Component(SpawnCondition spawn, Random rand, int x, int y, int z) {
			super(0);
			this.boundingBox = new StructureBoundingBox(x, y, z, x + spawn.structure.size.x, 255, z + spawn.structure.size.z);
			this.spawn = spawn;
		}

		// Save to NBT
		@Override
		protected void func_143012_a(NBTTagCompound nbt) {
			nbt.setInteger("dim", spawn.dimensionId);
			nbt.setInteger("con", spawn.conditionId);
		}

		// Load from NBT
		@Override
		protected void func_143011_b(NBTTagCompound nbt) {
			spawn = dimensionMap.get(nbt.getInteger("dim")).get(nbt.getInteger("con"));
		}

		@Override
		public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
			return spawn.structure.build(world, spawn.lootTable, boundingBox, box);
		}
		
	}

	public static class Start extends StructureStart {
		
		public Start() {}
		
		public Start(World world, Random rand, SpawnCondition spawn, int chunkX, int chunkZ) {
			super(chunkX, chunkZ);
			
			int x = (chunkX << 4);
			int z = (chunkZ << 4);

			int y = MathHelper.clamp_int(getAverageHeight(world, chunkX, chunkZ), spawn.minHeight, spawn.maxHeight);

			// testing, just grab the first loaded structure and generate that (martian base)
			addComponent(new Component(spawn, rand, x, y, z));

			updateBoundingBox();
		}

		@SuppressWarnings("unchecked")
		private void addComponent(StructureComponent component) {
			this.components.add(component);
		}

		private int getAverageHeight(World world, int chunkX, int chunkZ) {
			int total = 0;
			int iterations = 0;

			int minX = chunkX << 4;
			int minZ = chunkZ << 4;
			int maxX = minX + 16;
			int maxZ = minZ + 16;
			
			for(int z = minZ; z <= maxZ; z++) {
				for(int x = minX; x <= maxX; x++) {
					total += world.getTopSolidOrLiquidBlock(x, z);
					iterations++;
				}
			}
			
			if(iterations == 0)
				return 64;
			
			return total / iterations;
		}

	}

	public static class GenStructure extends MapGenStructure {

		private SpawnCondition nextSpawn;

		public void generateStructures(World world, Random rand, IChunkProvider chunkProvider, int chunkX, int chunkZ) {
			Block[] ablock = new Block[65536];

			func_151539_a(chunkProvider, world, chunkX, chunkZ, ablock);
			generateStructuresInChunk(world, rand, chunkX, chunkZ);
		}

		@Override
		public String func_143025_a() {
			return "NBTStructures";
		}
	
		@Override
		protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
			if(!dimensionMap.containsKey(worldObj.provider.dimensionId)) return false;

			int x = chunkX;
			int z = chunkZ;
			
			if(x < 0) x -= StructureConfig.structureMaxChunks - 1;
			if(z < 0) z -= StructureConfig.structureMaxChunks - 1;
			
			x /= StructureConfig.structureMaxChunks;
			z /= StructureConfig.structureMaxChunks;
			Random random = this.worldObj.setRandomSeed(x, z, 996996996 - worldObj.provider.dimensionId);
			x *= StructureConfig.structureMaxChunks;
			z *= StructureConfig.structureMaxChunks;
			x += random.nextInt(StructureConfig.structureMaxChunks - StructureConfig.structureMinChunks);
			z += random.nextInt(StructureConfig.structureMaxChunks - StructureConfig.structureMinChunks);
			
			if(chunkX == x && chunkZ == z) {
				BiomeGenBase biome = this.worldObj.getWorldChunkManager().getBiomeGenAt(chunkX * 16 + 8, chunkZ * 16 + 8);

				nextSpawn = findSpawn(biome);

				if(GeneralConfig.enableDebugMode)
					MainRegistry.logger.info("[Debug] Spawning NBT structure at: " + chunkX * 16 + ", " + chunkZ * 16);
				
				return nextSpawn != null;
			}

			return false;
		}
	
		@Override
		protected StructureStart getStructureStart(int chunkX, int chunkZ) {
			return new Start(this.worldObj, this.rand, nextSpawn, chunkX, chunkZ);
		}

		private SpawnCondition findSpawn(BiomeGenBase biome) {
			List<SpawnCondition> spawnList = dimensionMap.get(worldObj.provider.dimensionId);

			for(int i = 0; i < 64; i++) {
				SpawnCondition spawn = spawnList.get(rand.nextInt(spawnList.size()));
				if(spawn.isValid(biome)) return spawn;
			}

			return null;
		}

	}

}
