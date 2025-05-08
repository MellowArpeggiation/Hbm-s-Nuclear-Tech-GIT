package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityHydroponic;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class MachineHydroponic extends BlockDummyable {

	public MachineHydroponic(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityHydroponic();
		if(meta >= 6) return new TileEntityProxyCombo(true, false, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 0, 0, 2, 2};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		// Base
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {0, 0, 0, 0, 2, 2}, this, dir);

		// Top
		MultiblockHandlerXR.fillSpace(world, x + rot.offsetX * 2, y + 2, z + rot.offsetZ * 2, new int[] {0, 0, 0, 0, 0, 2}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - rot.offsetX * 2, y + 2, z - rot.offsetZ * 2, new int[] {0, 0, 0, 0, 3, 0}, this, dir);

		// Sides
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {2, 0, 0, 0, -2, 2}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {2, 0, 0, 0, 2, -2}, this, dir);
	}

	@Override
	public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction, IPlantable plantable) {
		return true;
	}

	// TEMP
	@Override
	public int getRenderType() {
		return 0;
	}
	// TEMP

}
