package com.hbm.items.tool;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.BlockConveyorBase;
import com.hbm.blocks.network.BlockCraneBase;
import com.hbm.render.util.RenderOverhead;
import com.hbm.wiaj.WorldInAJar;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IBlockAccess;
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
				// pretend to construct, if it doesn't fail, actually construct
				if(construct(world, null, sx, sy, sz, sSide, x, y, z, side, 0, 0, 0)) {
					construct(world, world, sx, sy, sz, sSide, x, y, z, side, 0, 0, 0);
					player.addChatMessage(new ChatComponentText("Conveyor built!"));
				} else {
					player.addChatMessage(new ChatComponentText("Conveyor obstructed, build cancelled"));
				}
			}

			nbt.setBoolean("placing", false);
		}

		return true; // always eat interactions
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inHand) {
		if(!inHand && stack.hasTagCompound() && stack.stackTagCompound.getBoolean("placing")) {
			stack.stackTagCompound.setBoolean("placing", false);
		}

		// clientside prediction only
		if(!world.isRemote) {
			if(!(entity instanceof EntityPlayer)) return;
			// EntityPlayer player = (EntityPlayer) entity;

			if(!stack.hasTagCompound() || !stack.stackTagCompound.getBoolean("placing")) {
				RenderOverhead.clearActionPreview();
				return;
			}

			MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
			if(mop == null || mop.typeOfHit != MovingObjectType.BLOCK) {
				RenderOverhead.clearActionPreview();
				return;
			}

			int x = mop.blockX;
			int y = mop.blockY;
			int z = mop.blockZ;
			int side = mop.sideHit;

			NBTTagCompound nbt = stack.stackTagCompound;

			int sx = nbt.getInteger("x");
			int sy = nbt.getInteger("y");
			int sz = nbt.getInteger("z");
			int sSide = nbt.getInteger("side");

			// Size has a one block buffer on both sides, for overshooting conveyors
			int sizeX = Math.abs(sx - x) + 3;
			int sizeY = Math.abs(sy - y) + 3;
			int sizeZ = Math.abs(sz - z) + 3;

			int minX = Math.min(sx, x) - 1;
			int minY = Math.min(sy, y) - 1;
			int minZ = Math.min(sz, z) - 1;

			WorldInAJar wiaj = new WorldInAJar(sizeX, sizeY, sizeZ);
			boolean pathSuccess = construct(world, wiaj, sx, sy, sz, sSide, x, y, z, side, minX, minY, minZ);

			RenderOverhead.setActionPreview(wiaj, minX, minY, minZ, pathSuccess);
		}
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
	private static boolean construct(World routeWorld, IBlockAccess buildWorld, int x1, int y1, int z1, int side1, int x2, int y2, int z2, int side2, int box, int boy, int boz) {
		boolean isFromCrane = routeWorld.getBlock(x1, y1, z1) instanceof BlockCraneBase;
		boolean isTargetCrane = routeWorld.getBlock(x2, y2, z2) instanceof BlockCraneBase;

		ForgeDirection dir = ForgeDirection.getOrientation(side1);

		ForgeDirection targetDir = ForgeDirection.getOrientation(side2);
		int tx = x2 + targetDir.offsetX;
		int ty = y2 + targetDir.offsetY;
		int tz = z2 + targetDir.offsetZ;

		int x = x1 + dir.offsetX;
		int y = y1 + dir.offsetY;
		int z = z1 + dir.offsetZ;

		if(!isFromCrane) {
			dir = getTargetDirection(x, y, z, x2, y2, z2);
		}

		ForgeDirection horDir = dir == ForgeDirection.UP || dir == ForgeDirection.DOWN ? ForgeDirection.NORTH : dir;

		for(int loopDepth = 0; loopDepth < 64; loopDepth++) {
			if(!routeWorld.getBlock(x, y, z).isReplaceable(routeWorld, x, y, z)) return false;

			Block block = getConveyorForDirection(dir);
			int meta = getConveyorMetaForDirection(block, dir, targetDir, horDir);

			int ox = x + dir.offsetX;
			int oy = y + dir.offsetY;
			int oz = z + dir.offsetZ;

			// check if we should turn before continuing
			int fromDistance = taxiDistance(x, y, z, tx, ty, tz);
			int toDistance = taxiDistance(ox, oy, oz, tx, ty, tz);
			int finalDistance = taxiDistance(ox, oy, oz, x2, y2, z2);
			boolean notAtTarget = (isTargetCrane ? finalDistance : fromDistance) > 0;
			boolean willBeObstructed = notAtTarget && !routeWorld.getBlock(ox, oy, oz).isReplaceable(routeWorld, ox, oy, oz);
			boolean shouldTurn = (toDistance >= fromDistance && notAtTarget) || willBeObstructed;

			if(shouldTurn) {
				ForgeDirection newDir = getTargetDirection(x, y, z, isTargetCrane ? x2 : tx, isTargetCrane ? y2 : ty, isTargetCrane ? z2 : tz, tx, ty, tz, dir);

				if(newDir == ForgeDirection.UP) {
					block = ModBlocks.conveyor_lift;
				} else if(newDir == ForgeDirection.DOWN) {
					block = ModBlocks.conveyor_chute;
				} else if(dir.getRotation(ForgeDirection.UP) == newDir) {
					meta += 8;
				} else if(dir.getRotation(ForgeDirection.DOWN) == newDir) {
					meta += 4;
				}

				dir = newDir;
				if(dir != ForgeDirection.UP && dir != ForgeDirection.DOWN) horDir = dir;
			}

			if(buildWorld instanceof World) {
				((World) buildWorld).setBlock(x - box, y - boy, z - boz, block, meta, 3);
			} else if(buildWorld instanceof WorldInAJar) {
				((WorldInAJar) buildWorld).setBlock(x - box, y - boy, z - boz, block, meta);
			}

			if(x == tx && y == ty && z == tz) return true;

			x += dir.offsetX;
			y += dir.offsetY;
			z += dir.offsetZ;
		}

		return false;
	}

	private static int getConveyorMetaForDirection(Block block, ForgeDirection dir, ForgeDirection targetDir, ForgeDirection horDir) {
		if(block == ModBlocks.conveyor) return dir.getOpposite().ordinal();
		if(targetDir == ForgeDirection.UP || targetDir == ForgeDirection.DOWN) return horDir.getOpposite().ordinal();
		return targetDir.ordinal();
	}

	private static Block getConveyorForDirection(ForgeDirection dir) {
		if(dir == ForgeDirection.UP) return ModBlocks.conveyor_lift;
		if(dir == ForgeDirection.DOWN) return ModBlocks.conveyor_chute;
		return ModBlocks.conveyor;
	}

	private static ForgeDirection getTargetDirection(int x1, int y1, int z1, int x2, int y2, int z2) {
		return getTargetDirection(x1, y1, z1, x2, y2, z2, x2, y2, z2, null);
	}

	private static ForgeDirection getTargetDirection(int x1, int y1, int z1, int x2, int y2, int z2, int tx, int ty, int tz, ForgeDirection heading) {
		if(y1 != y2 && ((x1 == x2 && z1 == z2) || (x1 == tx && z1 == tz))) return y1 > y2 ? ForgeDirection.DOWN : ForgeDirection.UP;

		if(Math.abs(x1 - x2) > Math.abs(z1 - z2) && heading != ForgeDirection.EAST && heading != ForgeDirection.WEST) {
			return x1 > x2 ? ForgeDirection.WEST : ForgeDirection.EAST;
		} else {
			return z1 > z2 ? ForgeDirection.NORTH : ForgeDirection.SOUTH;
		}
	}

	private static int taxiDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
		return Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2);
	}

}
