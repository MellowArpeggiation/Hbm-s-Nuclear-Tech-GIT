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
import java.util.function.Function;
import java.util.function.Predicate;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockWand;
import com.hbm.config.GeneralConfig;
import com.hbm.config.StructureConfig;
import com.hbm.handler.ThreeInts;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.Tuple.Quartet;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.util.Constants.NBT;

public class NBTStructure {

	/**
	 * Now with structure support!
	 *
	 * the type of structure to generate is saved into the Component,
	 * meaning this can generate all sorts of different structures,
	 * without having to define and register each structure manually
	 */

	protected static Map<Integer, List<SpawnCondition>> weightedMap = new HashMap<>();

	// serialization data
	protected static Map<String, JigsawPiece> jigsawMap = new HashMap<>();

	private String name;

	private boolean isLoaded;
	private ThreeInts size;
	private BlockDefinition[] palette;
	private List<Pair<Short, String>> itemPalette;
	private BlockState[][][] blockArray;
	private List<JigsawConnection> connections; // when starting a new connection
	private Map<String, List<JigsawConnection>> connectionMap; // when receiving a connection from another structure piece

	public NBTStructure(ResourceLocation resource) {
		// Can't use regular resource loading, servers don't know how!
		InputStream stream = NBTStructure.class.getResourceAsStream("/assets/" + resource.getResourceDomain() + "/" + resource.getResourcePath());
		if(stream != null) {
			name = resource.getResourcePath();
			loadStructure(stream);
		} else {
			MainRegistry.logger.error("NBT Structure not found: " + resource.getResourcePath());
		}
	}

	public static void register() {
		MapGenStructureIO.registerStructure(Start.class, "NBTStructures");
		MapGenStructureIO.func_143031_a(Component.class, "NBTComponents");
	}

	// Register a new structure for a given dimension
	public static void registerStructure(int dimensionId, SpawnCondition spawn) {
		List<SpawnCondition> weightedList = weightedMap.computeIfAbsent(dimensionId, integer -> new ArrayList<SpawnCondition>());
		for(int i = 0; i < spawn.spawnWeight; i++) {
			weightedList.add(spawn);
		}
	}

	// Add a chance for nothing to spawn at a given valid spawn location
	public static void registerNullWeight(int dimensionId, int weight) {
		registerNullWeight(dimensionId, weight, null);
	}

	public static void registerNullWeight(int dimensionId, int weight, Predicate<BiomeGenBase> predicate) {
		SpawnCondition spawn = new SpawnCondition() {{
			spawnWeight = weight;
			canSpawn = predicate;
		}};

		List<SpawnCondition> weightedList = weightedMap.computeIfAbsent(dimensionId, integer -> new ArrayList<SpawnCondition>());
		for(int i = 0; i < spawn.spawnWeight; i++) {
			weightedList.add(spawn);
		}
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

					if(block.key instanceof BlockWand) {
						block.key = ((BlockWand) block.key).exportAs;
					}

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

						nbt.removeTag("x");
						nbt.removeTag("y");
						nbt.removeTag("z");

						nbtBlock.setTag("nbt", nbt);

						String itemKey = null;
						if(nbt.hasKey("items")) itemKey = "items";
						if(nbt.hasKey("Items")) itemKey = "Items";

						if(nbt.hasKey(itemKey)) {
							NBTTagList items = nbt.getTagList(itemKey, NBT.TAG_COMPOUND);
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

	private void loadStructure(InputStream inputStream) {
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
			blockArray = new BlockState[size.x][size.y][size.z];

			for(int i = 0; i < blockData.tagCount(); i++) {
				NBTTagCompound block = blockData.getCompoundTagAt(i);
				int state = block.getInteger("state");
				ThreeInts pos = parsePos(block.getTagList("pos", NBT.TAG_INT));

				BlockState blockState = new BlockState(palette[state]);

				if(block.hasKey("nbt")) {
					NBTTagCompound nbt = block.getCompoundTag("nbt");
					blockState.nbt = nbt;

					// Load in connection points for jigsaws
					if(blockState.definition.block == ModBlocks.wand_jigsaw) {
						// int priority = nbt.getInteger("priority");
						ForgeDirection direction = ForgeDirection.getOrientation(nbt.getInteger("direction"));
						String poolName = nbt.getString("pool");
						String ourName = nbt.getString("name");
						String targetName = nbt.getString("target");

						JigsawConnection connection = new JigsawConnection(pos, direction, poolName, ourName, targetName);

						// List<DirPos> positions = connections.computeIfAbsent(priority, integer -> new ArrayList<>());
						if(connections == null) connections = new ArrayList<>();
						connections.add(connection);

						if(connectionMap == null) connectionMap = new HashMap<>();
						List<JigsawConnection> namedConnections = connectionMap.computeIfAbsent(ourName, name -> new ArrayList<>());
						namedConnections.add(connection);
					}
				}

				blockArray[pos.x][pos.y][pos.z] = blockState;
			}


			isLoaded = true;

		} catch(Exception e) {
			MainRegistry.logger.error("Exception reading NBT Structure format", e);
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

	private TileEntity buildTileEntity(World world, Block block, HashMap<Short, Short> worldItemPalette, NBTTagCompound nbt, int coordBaseMode) {
		nbt = (NBTTagCompound)nbt.copy();

		if(worldItemPalette != null) relinkItems(worldItemPalette, nbt);

		TileEntity te = TileEntity.createAndLoadEntity(nbt);

		if(te instanceof INBTTileEntityTransformable) {
			((INBTTileEntityTransformable) te).transformTE(world, coordBaseMode);
		}

		return te;
	}

	public void build(World world, int x, int y, int z) {
		build(world, x, y, z, 0);
	}

	public void build(World world, int x, int y, int z, int coordBaseMode) {
		if(!isLoaded) {
			MainRegistry.logger.info("NBTStructure is invalid");
			return;
		}

		HashMap<Short, Short> worldItemPalette = getWorldItemPalette();

		boolean swizzle = coordBaseMode == 1 || coordBaseMode == 3;
		x -= (swizzle ? size.z : size.x) / 2;
		z -= (swizzle ? size.x : size.z) / 2;

		int maxX = size.x;
		int maxZ = size.z;

		for(int bx = 0; bx < maxX; bx++) {
			for(int bz = 0; bz < maxZ; bz++) {
				int rx = rotateX(bx, bz, coordBaseMode) + x;
				int rz = rotateZ(bx, bz, coordBaseMode) + z;

				for(int by = 0; by < size.y; by++) {
					BlockState state = blockArray[bx][by][bz];
					if(state == null) continue;

					int ry = by + y;

					Block block = transformBlock(state.definition, null, world.rand);
					int meta = coordBaseMode != 0 ? transformMeta(state.definition, coordBaseMode) : state.definition.meta;

					world.setBlock(rx, ry, rz, block, meta, 2);

					if(state.nbt != null) {
						TileEntity te = buildTileEntity(world, block, worldItemPalette, state.nbt, coordBaseMode);
						world.setTileEntity(rx, ry, rz, te);
					}
				}
			}
		}
	}

	protected boolean build(World world, JigsawPiece piece, StructureBoundingBox totalBounds, StructureBoundingBox generatingBounds, int coordBaseMode, int heightOffset) {
		if(!isLoaded) {
			MainRegistry.logger.info("NBTStructure is invalid");
			return false;
		}

		HashMap<Short, Short> worldItemPalette = getWorldItemPalette();

		int sizeX = totalBounds.maxX - totalBounds.minX - 1;
		int sizeZ = totalBounds.maxZ - totalBounds.minZ - 1;

		// voxel grid transforms can fuck you up
		// you have my respect, vaer
		int absMinX = Math.max(generatingBounds.minX - totalBounds.minX, 0);
		int absMaxX = Math.min(generatingBounds.maxX - totalBounds.minX, sizeX);
		int absMinZ = Math.max(generatingBounds.minZ - totalBounds.minZ, 0);
		int absMaxZ = Math.min(generatingBounds.maxZ - totalBounds.minZ, sizeZ);

		// A check to see that we're actually inside the generating area at all
		if(absMinX > sizeX || absMaxX < 0 || absMinZ > sizeZ || absMaxZ < 0) return true;

		int rotMinX = unrotateX(absMinX, absMinZ, coordBaseMode);
		int rotMaxX = unrotateX(absMaxX, absMaxZ, coordBaseMode);
		int rotMinZ = unrotateZ(absMinX, absMinZ, coordBaseMode);
		int rotMaxZ = unrotateZ(absMaxX, absMaxZ, coordBaseMode);

		int minX = Math.min(rotMinX, rotMaxX);
		int maxX = Math.max(rotMinX, rotMaxX);
		int minZ = Math.min(rotMinZ, rotMaxZ);
		int maxZ = Math.max(rotMinZ, rotMaxZ);

		for(int bx = minX; bx <= maxX; bx++) {
			for(int bz = minZ; bz <= maxZ; bz++) {
				int rx = rotateX(bx, bz, coordBaseMode) + totalBounds.minX;
				int rz = rotateZ(bx, bz, coordBaseMode) + totalBounds.minZ;
				int oy = piece.conformToTerrain ? world.getTopSolidOrLiquidBlock(rx, rz) + heightOffset : totalBounds.minY;

				for(int by = 0; by < size.y; by++) {
					BlockState state = blockArray[bx][by][bz];
					if(state == null) continue;

					int ry = by + oy;

					Block block = transformBlock(state.definition, piece.blockTable, world.rand);
					int meta = coordBaseMode != 0 ? transformMeta(state.definition, coordBaseMode) : state.definition.meta;

					world.setBlock(rx, ry, rz, block, meta, 2);

					if(state.nbt != null) {
						TileEntity te = buildTileEntity(world, block, worldItemPalette, state.nbt, coordBaseMode);
						world.setTileEntity(rx, ry, rz, te);
					}
				}
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

	private Block transformBlock(BlockDefinition definition, Map<Block, BlockSelector> blockTable, Random rand) {
		if(blockTable != null && blockTable.containsKey(definition.block)) {
			final BlockSelector selector = blockTable.get(definition.block);
			selector.selectBlocks(rand, 0, 0, 0, false); // fuck the vanilla shit idc
			return selector.func_151561_a();
		}

		if(definition.block instanceof INBTTransformable) return ((INBTTransformable) definition.block).transformBlock(definition.block);

		return definition.block;
	}

	private int transformMeta(BlockDefinition definition, int coordBaseMode) {
		// Our shit
		if(definition.block instanceof INBTTransformable) return ((INBTTransformable) definition.block).transformMeta(definition.meta, coordBaseMode);

		// Vanilla shit
		if(definition.block instanceof BlockStairs) return INBTTransformable.transformMetaStairs(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockRotatedPillar) return INBTTransformable.transformMetaPillar(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockDirectional) return INBTTransformable.transformMetaDirectional(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockTorch) return INBTTransformable.transformMetaTorch(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockButton) return INBTTransformable.transformMetaTorch(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockDoor) return INBTTransformable.transformMetaDoor(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockLever) return INBTTransformable.transformMetaLever(definition.meta, coordBaseMode);

		return definition.meta;
	}

	private int rotateX(int x, int z, int coordBaseMode) {
		switch(coordBaseMode) {
		case 1: return size.z - 1 - z;
		case 2: return size.x - 1 - x;
		case 3: return z;
		default: return x;
		}
	}

	private int rotateZ(int x, int z, int coordBaseMode) {
		switch(coordBaseMode) {
		case 1: return x;
		case 2: return size.z - 1 - z;
		case 3: return size.x - 1 - x;
		default: return z;
		}
	}

	private int unrotateX(int x, int z, int coordBaseMode) {
		switch(coordBaseMode) {
		case 3: return size.x - 1 - z;
		case 2: return size.x - 1 - x;
		case 1: return z;
		default: return x;
		}
	}

	private int unrotateZ(int x, int z, int coordBaseMode) {
		switch(coordBaseMode) {
		case 3: return x;
		case 2: return size.z - 1 - z;
		case 1: return size.z - 1 - x;
		default: return z;
		}
	}

	private static class BlockState {

		final BlockDefinition definition;
		NBTTagCompound nbt;

		BlockState(BlockDefinition definition) {
			this.definition = definition;
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

	public static class SpawnCondition {

		// If defined, will spawn a single jigsaw piece, for single nbt structures
		public JigsawPiece structure;

		// If defined, will spawn in a non-nbt structure component
		public Function<Quartet<World, Random, Integer, Integer>, StructureStart> start;

		public Predicate<BiomeGenBase> canSpawn;
		public int spawnWeight = 1;

		// Named jigsaw pools that are referenced within the structure
		public Map<String, JigsawPool> pools;
		public String startPool;

		// Maximum amount of components in this structure
		public int sizeLimit = 8;

		// Height modifiers, will apply offset and clamp height that the start generates at, allowing for:
		//  * Submarines that must spawn under the ocean surface
		//  * Bunkers that sit underneath the ground
		//  * Conforming structures will adjust height to match terrain automatically
		public int minHeight = 1;
		public int maxHeight = 128;
		public int heightOffset = 0;

		// Can this spawn in the current biome
		protected boolean isValid(BiomeGenBase biome) {
			if(canSpawn == null) return true;
			return canSpawn.test(biome);
		}

	}

	// A set of pieces with weights
	public static class JigsawPool {

		// Weighted list of pieces to pick from
		private List<JigsawPiece> pieces = new ArrayList<>();

		public void add(JigsawPiece piece, int weight) {
			for(int i = 0; i < weight; i++) {
				pieces.add(piece);
			}
		}

		public JigsawPiece get(Random rand) {
			return pieces.get(rand.nextInt(pieces.size()));
		}

	}

	// Assigned to a Component to build
	public static class JigsawPiece {

		public final String name;

		public NBTStructure structure;

		public boolean conformToTerrain = false;

		// Block modifiers, for randomization
		public Map<Block, BlockSelector> blockTable;

		public JigsawPiece(String name) {
			if(jigsawMap.containsKey(name)) throw new IllegalStateException("A severe error has occurred in NBTStructure! A jigsaw piece has been registered with the same name as another: " + name);

			this.name = name;
			jigsawMap.put(name, this);
		}

	}

	// Each jigsaw block in a structure will instance one of these
	private static class JigsawConnection {

		private final ThreeInts pos;
		private final ForgeDirection dir;

		// what pool should we look through to find a connection
		private final String poolName;

		// when we successfully find a pool, what connections in that jigsaw piece can we target
		// private final String ourName;
		private final String targetName;

		private JigsawConnection(ThreeInts pos, ForgeDirection dir, String poolName, String ourName, String targetName) {
			this.pos = pos;
			this.dir = dir;
			this.poolName = poolName;
			// this.ourName = ourName;
			this.targetName = targetName;
		}

	}

	public static class Component extends StructureComponent {

		JigsawPiece piece;

		int heightOffset;
		int minHeight = 1;
		int maxHeight = 128;

		public Component() {}

		public Component(SpawnCondition spawn, JigsawPiece piece, Random rand, int x, int z) {
			this(spawn, piece, rand, x, z, rand.nextInt(4));
		}

		public Component(SpawnCondition spawn, JigsawPiece piece, Random rand, int x, int z, int coordBaseMode) {
			super(0);
			this.coordBaseMode = coordBaseMode;
			this.piece = piece;
			this.heightOffset = spawn.heightOffset;
			this.minHeight = spawn.minHeight;
			this.maxHeight = spawn.maxHeight;

			switch(this.coordBaseMode) {
			case 1:
			case 3:
				this.boundingBox = new StructureBoundingBox(x, 0, z, x + piece.structure.size.z, piece.structure.size.y, z + piece.structure.size.x);
				break;
			default:
				this.boundingBox = new StructureBoundingBox(x, 0, z, x + piece.structure.size.x, piece.structure.size.y, z + piece.structure.size.z);
				break;
			}
		}

		// Save to NBT
		@Override
		protected void func_143012_a(NBTTagCompound nbt) {
			nbt.setString("piece", piece.name);
			nbt.setInteger("offset", heightOffset);
			nbt.setInteger("min", minHeight);
			nbt.setInteger("max", maxHeight);
		}

		// Load from NBT
		@Override
		protected void func_143011_b(NBTTagCompound nbt) {
			piece = jigsawMap.get(nbt.getString("piece"));
			heightOffset = nbt.getInteger("offset");
			minHeight = nbt.getInteger("min");
			maxHeight = nbt.getInteger("max");
		}

		@Override
		public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
			if(piece == null) return false;

			// now we're in the world, update minY/maxY
			if(!piece.conformToTerrain && boundingBox.minY == 0) {
				int y = MathHelper.clamp_int(getAverageHeight(world, box) + heightOffset, minHeight, maxHeight);
				boundingBox.minY = y;
				boundingBox.maxY = y + piece.structure.size.y;
			}

			return piece.structure.build(world, piece, boundingBox, box, coordBaseMode, heightOffset);
		}

		// Overrides to fix Mojang's fucked rotations which FLIP instead of rotating in two instances
		// vaer being in the mines doing this the hard way for years was absolutely not for naught
		@Override
		public int getXWithOffset(int x, int z) {
			return boundingBox.minX + piece.structure.rotateX(x, z, coordBaseMode);
		}

		@Override
		public int getZWithOffset(int x, int z) {
			return boundingBox.minZ + piece.structure.rotateZ(x, z, coordBaseMode);
		}

		private ForgeDirection rotateDir(ForgeDirection dir) {
			if(dir == ForgeDirection.UP || dir == ForgeDirection.DOWN) return dir;
			switch(coordBaseMode) {
				default: return dir;
				case 1: return dir.getRotation(ForgeDirection.UP);
				case 2: return dir.getOpposite();
				case 3: return dir.getRotation(ForgeDirection.DOWN);
			}
		}

		private int getAverageHeight(World world, StructureBoundingBox box) {
			int total = 0;
			int iterations = 0;

			for(int z = box.minZ; z <= box.maxZ; z++) {
				for(int x = box.minX; x <= box.maxX; x++) {
					total += world.getTopSolidOrLiquidBlock(x, z);
					iterations++;
				}
			}

			if(iterations == 0)
				return 64;

			return total / iterations;
		}

		private int directionOffsetToCoordBase(ForgeDirection from, ForgeDirection to) {
			for(int i = 0; i < 4; i++) {
				if(from == to) return (i + coordBaseMode) % 4;
				from = from.getRotation(ForgeDirection.DOWN);
			}
			return 0;
		}

	}

	public static class Start extends StructureStart {

		private List<Component> queuedComponents = new ArrayList<>();

		public Start() {}

		@SuppressWarnings("unchecked")
		public Start(World world, Random rand, SpawnCondition spawn, int chunkX, int chunkZ) {
			super(chunkX, chunkZ);

			int x = chunkX << 4;
			int z = chunkZ << 4;

			JigsawPiece startPiece = spawn.structure != null ? spawn.structure : spawn.pools.get(spawn.startPool).get(rand);

			Component startComponent = new Component(spawn, startPiece, rand, x, z);

			this.components.add(startComponent);
			if(spawn.structure == null) queuedComponents.add(startComponent);

			// Iterate through and build out all the components we intend to spawn
			while(!queuedComponents.isEmpty()) {
				final int i = rand.nextInt(queuedComponents.size());
				Component fromComponent = queuedComponents.remove(i);

				if(this.components.size() >= spawn.sizeLimit) continue;
				if(fromComponent.piece.structure.connections == null) continue;

				for(JigsawConnection fromConnection : fromComponent.piece.structure.connections) {
					JigsawPiece nextPiece = spawn.pools.get(fromConnection.poolName).get(rand);

					List<JigsawConnection> connectionPool = nextPiece.structure.connectionMap.get(fromConnection.targetName);
					JigsawConnection toConnection = connectionPool.get(rand.nextInt(connectionPool.size()));

					// The direction this component is extending towards in ABSOLUTE direction
					ForgeDirection extendDir = fromComponent.rotateDir(fromConnection.dir);

					// Rotate our incoming piece to plug it in
					int nextCoordBase = fromComponent.directionOffsetToCoordBase(fromConnection.dir.getOpposite(), toConnection.dir);

					// Set the starting point for the next structure to the location of the connector block
					int nextX = fromComponent.getXWithOffset(fromConnection.pos.x, fromConnection.pos.z) + extendDir.offsetX;
					int nextZ = fromComponent.getZWithOffset(fromConnection.pos.x, fromConnection.pos.z) + extendDir.offsetZ;

					// offset the starting point to the connecting point
					nextX -= nextPiece.structure.rotateX(toConnection.pos.x, toConnection.pos.z, nextCoordBase);
					nextZ -= nextPiece.structure.rotateZ(toConnection.pos.x, toConnection.pos.z, nextCoordBase);

					// Build the new component and validate that it fits
					Component nextComponent = new Component(spawn, nextPiece, rand, nextX, nextZ, nextCoordBase);
					StructureComponent intersects = StructureComponent.findIntersecting(components, nextComponent.getBoundingBox());
					if(intersects == null || intersects == fromComponent) {
						this.components.add(nextComponent);
						queuedComponents.add(nextComponent);
					}

				}
			}

			if(GeneralConfig.enableDebugMode) {
				MainRegistry.logger.info("[Debug] Spawning NBT structure at: " + chunkX * 16 + ", " + chunkZ * 16);
				String componentList = "[Debug] Components: ";
				for(Object component : this.components) {
					componentList += ((Component) component).piece.structure.name + " ";
				}
				MainRegistry.logger.info(componentList);
			}

			updateBoundingBox();
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
			if(!weightedMap.containsKey(worldObj.provider.dimensionId)) return false;

			int x = chunkX;
			int z = chunkZ;

			if(x < 0) x -= StructureConfig.structureMaxChunks - 1;
			if(z < 0) z -= StructureConfig.structureMaxChunks - 1;

			x /= StructureConfig.structureMaxChunks;
			z /= StructureConfig.structureMaxChunks;
			rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L + this.worldObj.getWorldInfo().getSeed() + (long)996996996 - worldObj.provider.dimensionId);
			x *= StructureConfig.structureMaxChunks;
			z *= StructureConfig.structureMaxChunks;
			x += rand.nextInt(StructureConfig.structureMaxChunks - StructureConfig.structureMinChunks);
			z += rand.nextInt(StructureConfig.structureMaxChunks - StructureConfig.structureMinChunks);

			if(chunkX == x && chunkZ == z) {
				BiomeGenBase biome = this.worldObj.getWorldChunkManager().getBiomeGenAt(chunkX * 16 + 8, chunkZ * 16 + 8);

				nextSpawn = findSpawn(biome);

				return nextSpawn != null && (nextSpawn.pools != null || nextSpawn.start != null);
			}

			return false;
		}

		@Override
		protected StructureStart getStructureStart(int chunkX, int chunkZ) {
			if(nextSpawn.start != null) return nextSpawn.start.apply(new Quartet<World, Random, Integer, Integer>(this.worldObj, this.rand, chunkX, chunkZ));
			return new Start(this.worldObj, this.rand, nextSpawn, chunkX, chunkZ);
		}

		private SpawnCondition findSpawn(BiomeGenBase biome) {
			List<SpawnCondition> spawnList = weightedMap.get(worldObj.provider.dimensionId);

			for(int i = 0; i < 64; i++) {
				SpawnCondition spawn = spawnList.get(rand.nextInt(spawnList.size()));
				if(spawn.isValid(biome)) return spawn;
			}

			return null;
		}

	}

}
