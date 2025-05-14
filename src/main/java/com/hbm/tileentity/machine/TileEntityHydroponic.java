package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityHydroponic extends TileEntityMachineBase implements IFluidStandardTransceiverMK2 {

	public FluidTank[] tanks;

	private int[] prevMeta = new int[3];

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

			for(DirPos pos : getConPos()) {
				trySubscribe(tanks[0].getTankType(), worldObj, pos);
				tryProvide(tanks[1].getTankType(), worldObj, pos);
			}

			Block testPlant = Blocks.wheat;
			IGrowable testGrow = (IGrowable) Blocks.wheat;

			for(int i = 0; i < 3; i++) {
				int x = xCoord + rot.offsetX * (i - 1);
				int y = yCoord + 1;
				int z = zCoord + rot.offsetZ * (i - 1);

				Block currentPlant = worldObj.getBlock(x, y, z);

				// Minimum CO2 pressure required to start growing
				if(tanks[0].getFill() >= 100) {
					if(currentPlant != testPlant) {
						worldObj.setBlock(x, y, z, testPlant);
						prevMeta[i] = worldObj.getBlockMetadata(x, y, z);
					}

					// a 3/(16^3) chance of ticking, multiplied by 10
					// if(worldObj.rand.nextInt(136) == 0) testPlant.updateTick(worldObj, x, y, z, worldObj.rand);
					if(worldObj.rand.nextInt(20) == 0) testPlant.updateTick(worldObj, x, y, z, worldObj.rand);

					boolean fullyGrown = false;
					boolean bonemeal = false;
					if(testGrow.func_149851_a(worldObj, x, y, z, worldObj.isRemote)) { // should consume bonemeal, if not, assume fully grown
						if(bonemeal) {
							if(testGrow.func_149852_a(worldObj, worldObj.rand, x, y, z)) { // does bonemeal apply
								testGrow.func_149853_b(worldObj, worldObj.rand, x, y, z); // apply bonemeal
							}

							// now consume the bonemeal
						}
					} else {
						fullyGrown = true;
					}

					int newMeta = worldObj.getBlockMetadata(x, y, z);

					if(newMeta != prevMeta[i]) {
						// each growth stage sequesters 5mb of carbon
						int toProduce = Math.max(newMeta - prevMeta[i], 0) * 5;
						tanks[0].setFill(Math.max(tanks[0].getFill() - toProduce, 0));
						tanks[1].setFill(Math.min(tanks[1].getFill() + toProduce, tanks[1].getMaxFill()));

						prevMeta[i] = newMeta;
					}

					// after collecting produced O2, break any fully grown plants
					if(fullyGrown) worldObj.setBlockToAir(x, y, z);
				} else if(currentPlant instanceof IGrowable) {
					// pause growth until sufficient CO2 added
					worldObj.setBlockMetadataWithNotify(x, y, z, prevMeta[i], 2);
				}
			}

			networkPackNT(15);
		} else {

		}
	}

	private DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
			new DirPos(xCoord + rot.offsetX * 2 + dir.offsetX, yCoord, zCoord + rot.offsetZ * 2 + dir.offsetZ, dir),
			new DirPos(xCoord - rot.offsetX * 2 + dir.offsetX, yCoord, zCoord - rot.offsetZ * 2 + dir.offsetZ, dir),
			new DirPos(xCoord + rot.offsetX * 2 - dir.offsetX, yCoord, zCoord + rot.offsetZ * 2 - dir.offsetZ, dir.getOpposite()),
			new DirPos(xCoord - rot.offsetX * 2 - dir.offsetX, yCoord, zCoord - rot.offsetZ * 2 - dir.offsetZ, dir.getOpposite()),
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		for(int i = 0; i < tanks.length; i++) tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		for(int i = 0; i < tanks.length; i++) tanks[i].deserialize(buf);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
		for(int i = 0; i < 3; i++) nbt.setInteger("p" + i, prevMeta[i]);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
		for(int i = 0; i < 3; i++) prevMeta[i] = nbt.getInteger("p" + i);
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 2,
				yCoord,
				zCoord - 2,
				xCoord + 3,
				yCoord + 2,
				zCoord + 3
			);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tanks[0] }; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tanks[1] }; }
	@Override public FluidTank[] getAllTanks() { return tanks; }

}
