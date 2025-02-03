package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.itempool.ItemPool;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.I18nUtil;
import com.hbm.util.LootGenerator;
import com.hbm.world.gen.INBTTileEntityTransformable;

import api.hbm.block.IToolable;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class BlockWandLoot extends BlockContainer implements ILookOverlay, IToolable, ITooltipProvider {

	public BlockWandLoot() {
		super(Material.iron);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);

		if(!(te instanceof TileEntityWandLoot)) return;

		TileEntityWandLoot loot = (TileEntityWandLoot) te;

		List<String> text = new ArrayList<String>();
		text.add("Will replace with: " + loot.replaceBlock.getUnlocalizedName());
		text.add("   meta: " + loot.replaceMeta);
		text.add("Loot pool: " + loot.poolName);
		text.add("Minimum items: " + loot.minItems);
		text.add("Maximum items: " + loot.maxItems);

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add("Define loot crates/piles in .nbt structures");
		list.add(EnumChatFormatting.GOLD + "Use screwdriver to increase/decrease minimum loot");
		list.add(EnumChatFormatting.GOLD + "Use hand drill to increase/decrease maximum loot");
		list.add(EnumChatFormatting.GOLD + "Use defuser to cycle loot types");
		list.add(EnumChatFormatting.GOLD + "Use container block to set the block that spawns with loot inside");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(x, y, z);

		if(!(te instanceof TileEntityWandLoot)) return false;

		TileEntityWandLoot loot = (TileEntityWandLoot) te;

		if(!player.isSneaking()) {

			Block block = getLootableBlock(world, player.getHeldItem());

			if(block != null) {
				loot.replaceBlock = block;
				loot.replaceMeta = player.getHeldItem().getItemDamage();

				List<String> poolNames = loot.getPoolNames(block == ModBlocks.deco_loot);
				if(!poolNames.contains(loot.poolName)) {
					loot.poolName = poolNames.get(0);
				}

				return true;
			}
		}

		return false;
	}

	private Block getLootableBlock(World world, ItemStack stack) {
		if(stack == null) return null;

		if(stack.getItem() instanceof ItemBlock) {
			Block block = ((ItemBlock) stack.getItem()).field_150939_a;

			if(block == ModBlocks.deco_loot) return block;

			if(block instanceof ITileEntityProvider) {
				TileEntity te = ((ITileEntityProvider) block).createNewTileEntity(world, 12);
				if(te instanceof IInventory) return block;
			}
		}

		return null;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		TileEntity te = world.getTileEntity(x, y, z);

		if(!(te instanceof TileEntityWandLoot)) return false;

		TileEntityWandLoot loot = (TileEntityWandLoot) te;

		switch(tool) {
		case SCREWDRIVER:
			if(player.isSneaking()) {
				loot.minItems--;
				if(loot.minItems < 0) loot.minItems = 0;
			} else {
				loot.minItems++;
				loot.maxItems = Math.max(loot.minItems, loot.maxItems);
			}

			return true;

		case HAND_DRILL:
			if(player.isSneaking()) {
				loot.maxItems--;
				if(loot.maxItems < 0) loot.maxItems = 0;
				loot.minItems = Math.min(loot.minItems, loot.maxItems);
			} else {
				loot.maxItems++;
			}

			return true;

		case DEFUSER:
			List<String> poolNames = loot.getPoolNames(loot.replaceBlock == ModBlocks.deco_loot);
			int index = poolNames.indexOf(loot.poolName);

			index += player.isSneaking() ? -1 : 1;
			index = MathHelper.clamp_int(index, 0, poolNames.size() - 1);

			loot.poolName = poolNames.get(index);

			return true;

		default: return false;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityWandLoot();
	}

	public static class TileEntityWandLoot extends TileEntityLoadedBase implements INBTTileEntityTransformable {

		private boolean triggerReplace;

		private Block replaceBlock = ModBlocks.deco_loot;
		private int replaceMeta;

		private String poolName = "";
		private int minItems;
		private int maxItems;

		@Override
		public void updateEntity() {
			if(!worldObj.isRemote) {
				// On the first tick of this TE, replace with intended block and fill with loot
				if(triggerReplace) replace();

				networkPackNT(15);
			}
		}

		private void replace() {
			WeightedRandomChestContent[] pool = ItemPool.getPool(poolName);

			worldObj.setBlock(xCoord, yCoord, zCoord, replaceBlock, replaceMeta, 2);

			TileEntity te = worldObj.getTileEntity(xCoord, yCoord, zCoord);

			if(te instanceof IInventory) {
				WeightedRandomChestContent.generateChestContents(worldObj.rand, pool, (IInventory) te, worldObj.rand.nextInt(maxItems - minItems) + minItems);
			} else if(te instanceof BlockLoot.TileEntityLoot) {
				LootGenerator.applyLoot(worldObj, xCoord, yCoord, zCoord, poolName);
			}
		}

		private List<String> getPoolNames(boolean loot) {
			if(loot) return Arrays.asList(LootGenerator.getLootNames());

			List<String> names = new ArrayList<>();
			names.addAll(ItemPool.pools.keySet());
			return names;
		}

		@Override
		public void transformTE(World world) {
			triggerReplace = true;
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("block", Block.getIdFromBlock(replaceBlock));
			nbt.setInteger("meta", replaceMeta);
			nbt.setInteger("min", minItems);
			nbt.setInteger("max", maxItems);
			nbt.setString("pool", poolName);
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			replaceBlock = Block.getBlockById(nbt.getInteger("block"));
			replaceMeta = nbt.getInteger("meta");
			minItems = nbt.getInteger("min");
			maxItems = nbt.getInteger("max");
			poolName = nbt.getString("pool");
		}

		@Override
		public void serialize(ByteBuf buf) {
			buf.writeInt(Block.getIdFromBlock(replaceBlock));
			buf.writeInt(replaceMeta);
			buf.writeInt(minItems);
			buf.writeInt(maxItems);
			BufferUtil.writeString(buf, poolName);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			replaceBlock = Block.getBlockById(buf.readInt());
			replaceMeta = buf.readInt();
			minItems = buf.readInt();
			maxItems = buf.readInt();
			poolName = BufferUtil.readString(buf);
		}

	}
}
