package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.IDysonConverter;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyProviderMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityDysonConverterHE extends TileEntityMachineBase implements IDysonConverter, IEnergyProviderMK2 {

	public long power;

	public TileEntityDysonConverterHE() {
		super(0);
	}

	@Override
	public String getName() {
		return "container.machineDysonConverterHE";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();

			DirPos output = new DirPos(xCoord + dir.offsetX * 5, yCoord, zCoord + dir.offsetZ * 5, dir);
			tryProvide(worldObj, output.getX(), output.getY(), output.getZ(), output.getDir());

			// To prevent this machine acting like an endgame battery, but still be able to transmit every drop of power
			// this machine will clear its buffers immediately after transmitting power
			power = 0;
		}
	}

	@Override
	public boolean provideEnergy(int x, int y, int z, long energy) {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		int rx = xCoord + dir.offsetX * 4;
		int ry = yCoord + 1;
		int rz = zCoord + dir.offsetZ * 4;

		if(x != rx || y != ry || z != rz) return false;

		power += energy;
		if(power < 0) power = Long.MAX_VALUE; // prevent overflow

		return true;
	}

	@Override
	public long maximumEnergy() {
		return Long.MAX_VALUE;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return Long.MAX_VALUE;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 6,
				yCoord,
				zCoord - 6,
				xCoord + 7,
				yCoord + 6,
				zCoord + 7
			);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

}
