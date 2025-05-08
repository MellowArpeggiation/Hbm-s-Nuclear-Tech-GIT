package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.TileEntityMachineBase;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityHydroponic extends TileEntityMachineBase {

	FluidTank[] tanks;

	public TileEntityHydroponic() {
		super(4);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.CARBONDIOXIDE, 16_000);
		tanks[1] = new FluidTank(Fluids.OXYGEN, 16_000);
	}

	@Override
	public String getName() {
		return "container.hydroponic";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

			Block testPlant = Blocks.wheat;
			IGrowable testGrow = (IGrowable) Blocks.wheat;

			for(int i = -1; i <= 1; i++) {
				int x = xCoord + rot.offsetX * i;
				int y = yCoord + 1;
				int z = zCoord + rot.offsetZ * i;

				Block currentPlant = worldObj.getBlock(x, y, z);

				if(currentPlant != testPlant) {
					worldObj.setBlock(x, y, z, testPlant);
				}

				testPlant.updateTick(worldObj, x, y, z, worldObj.rand);

				boolean bonemeal = false;
				if(testGrow.func_149851_a(worldObj, x, y, z, worldObj.isRemote)) { // should consume bonemeal, if not, assume fully grown
					if(bonemeal) {
						if(testGrow.func_149852_a(worldObj, worldObj.rand, x, y, z)) { // does bonemeal apply
							testGrow.func_149853_b(worldObj, worldObj.rand, x, y, z); // apply bonemeal
						}

						// now consume the bonemeal
					}
				} else {
					worldObj.setBlockToAir(x, y, z);
				}
			}
		} else {

		}
	}

}
