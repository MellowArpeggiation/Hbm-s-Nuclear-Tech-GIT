package com.hbm.blocks.generic;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockWandJigsaw extends BlockContainer {

	public BlockWandJigsaw() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityWandJigsaw();
	}

	public static class TileEntityWandJigsaw extends TileEntity {

		private int priority;
		private String pool = "default";

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("priority", priority);
			nbt.setString("pool", pool);
			nbt.setInteger("direction", ForgeDirection.NORTH.ordinal());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			priority = nbt.getInteger("priority");
			pool = nbt.getString("pool");
		}

	}

}
