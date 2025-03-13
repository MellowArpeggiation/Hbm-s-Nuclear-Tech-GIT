package com.hbm.items.tool;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.BlockConveyorBase;
import com.hbm.blocks.network.BlockCraneBase;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent;

public class ItemConveyorWand extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
		if(stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = stack.stackTagCompound;

		if(!nbt.getBoolean("placing")) {
			// Starting placement

			nbt.setInteger("x", x);
			nbt.setInteger("y", y);
			nbt.setInteger("z", z);
			nbt.setInteger("side", side);

			nbt.setBoolean("placing", true);
		} else {
			// Constructing conveyor

			int sx = nbt.getInteger("x");
			int sy = nbt.getInteger("y");
			int sz = nbt.getInteger("z");
			int sSide = nbt.getInteger("side");

			if(!world.isRemote) {
				if(construct(world, sx, sy, sz, sSide, x, y, z, side)) {
					player.addChatMessage(new ChatComponentText("Conveyor built!"));
				} else {
					player.addChatMessage(new ChatComponentText("Conveyor obstructed, build cancelled"));
				}
			}

			nbt.setBoolean("placing", false);
		}

		return true; // always eat interactions
	}

	// In creative, auto delete connected conveyors
	@Override
	public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer playerEntity) {
		World world = playerEntity.worldObj;
		Block block = world.getBlock(x, y, z);

		if(!playerEntity.capabilities.isCreativeMode) return false;
		if(!(playerEntity instanceof EntityPlayerMP)) return false;

		EntityPlayerMP player = (EntityPlayerMP) playerEntity;

		if(!world.isRemote && block instanceof BlockConveyorBase) {
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				breakExtra(world, player, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, 32);
			}
		}

		return false;
	}

	private void breakExtra(World world, EntityPlayerMP player, int x, int y, int z, int depth) {
		depth--;
		if(depth <= 0) return;

		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		if(!(block instanceof BlockConveyorBase)) return;

		BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(world, player.theItemInWorldManager.getGameType(), player, x, y, z);
		if(event.isCanceled())
			return;

		block.onBlockHarvested(world, x, y, z, meta, player);
		if(block.removedByPlayer(world, player, x, y, z, false)) {
			block.onBlockDestroyedByPlayer(world, x, y, z, meta);
		}

		player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			breakExtra(world, player, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, depth);
		}
	}

	// attempts to construct a conveyor between two points, including bends, lifts, and chutes
	private static boolean construct(World world, int x1, int y1, int z1, int side1, int x2, int y2, int z2, int side2) {
		boolean isFromCrane = world.getBlock(x1, y1, z1) instanceof BlockCraneBase;
		// boolean isTargetCrane = world.getBlock(x2, y2, z2) instanceof BlockCraneBase;

		ForgeDirection target = getTargetDirection(x1, y1, z1, x2, y2, z2);
		ForgeDirection dir = ForgeDirection.getOrientation(side1);

		ForgeDirection targetDir = ForgeDirection.getOrientation(side2);
		x2 += targetDir.offsetX;
		y2 += targetDir.offsetY;
		z2 += targetDir.offsetZ;

		int x = x1 + dir.offsetX;
		int y = y1 + dir.offsetY;
		int z = z1 + dir.offsetZ;

		if(!isFromCrane) dir = target.getOpposite();

		for(int loopDepth = 0; loopDepth < 64; loopDepth++) {
			if(!world.getBlock(x, y, z).isReplaceable(world, x, y, z)) return false;

			Block block = getConveyorForDirection(dir);
			int meta = block != ModBlocks.conveyor ? target.getOpposite().ordinal() : dir.getOpposite().ordinal();

			int ox = x + dir.offsetX;
			int oy = y + dir.offsetY;
			int oz = z + dir.offsetZ;

			// check if we should turn before continuing
			int fromDistance = distance(x, y, z, x2, y2, z2);
			int toDistance = distance(ox, oy, oz, x2, y2, z2);
			boolean willBeObstructed = !world.getBlock(ox, oy, oz).isReplaceable(world, ox, oy, oz);
			boolean shouldTurn = toDistance >= fromDistance || fromDistance == 0 || willBeObstructed;

			if(shouldTurn) {
				ForgeDirection newDir = getTargetDirection(x, y, z, x2 - targetDir.offsetX, y2 - targetDir.offsetY, z2 - targetDir.offsetZ, willBeObstructed ? dir : null);

				if(dir.getRotation(ForgeDirection.UP) == newDir) {
					meta += 8;
				} else if(dir.getRotation(ForgeDirection.DOWN) == newDir) {
					meta += 4;
				}

				dir = newDir;
			}

			world.setBlock(x, y, z, block, meta, 3);

			if(x == x2 && y == y2 && z == z2) return true;

			x += dir.offsetX;
			y += dir.offsetY;
			z += dir.offsetZ;
		}

		return false;
	}

	private static Block getConveyorForDirection(ForgeDirection dir) {
		if(dir == ForgeDirection.UP) return ModBlocks.conveyor_lift;
		if(dir == ForgeDirection.DOWN) return ModBlocks.conveyor_chute;
		return ModBlocks.conveyor;
	}

	private static ForgeDirection getTargetDirection(int x1, int y1, int z1, int x2, int y2, int z2) {
		return getTargetDirection(x1, y1, z1, x2, y2, z2, null);
	}

	private static ForgeDirection getTargetDirection(int x1, int y1, int z1, int x2, int y2, int z2, ForgeDirection blockedDirection) {
		if(x1 == x2 && z1 == z2) return y1 > y2 ? ForgeDirection.DOWN : ForgeDirection.UP;

		if(Math.abs(x1 - x2) > Math.abs(z1 - z2) && blockedDirection != ForgeDirection.EAST && blockedDirection != ForgeDirection.WEST) {
			return x1 > x2 ? ForgeDirection.WEST : ForgeDirection.EAST;
		} else {
			return z1 > z2 ? ForgeDirection.NORTH : ForgeDirection.SOUTH;
		}
	}

	private static int distance(int x1, int y1, int z1, int x2, int y2, int z2) {
		return Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2);
	}

}
