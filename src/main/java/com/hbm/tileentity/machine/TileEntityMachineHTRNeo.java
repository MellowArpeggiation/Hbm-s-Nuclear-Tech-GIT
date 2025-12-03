package com.hbm.tileentity.machine;


import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Rocket;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.machine.fusion.IFusionPowerReceiver;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import api.hbm.tile.IPropulsion;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import io.netty.buffer.ByteBuf;

public class TileEntityMachineHTRNeo extends TileEntityMachineBase
implements IPropulsion, IFluidStandardTransceiverMK2, IFluidStandardReceiver, IEnergyReceiverMK2, IFusionPowerReceiver {

	//i smushed these together because i need you so bad

	public long plasmaEnergy;
	public long plasmaEnergySync;
	public long power;

	public static long maxPower = 1_000_000_000L;

	public static final int COOLANT_USE = 50;
	public static final double PLASMA_EFFICIENCY = 1.35D;
	public static long MINIMUM_PLASMA = 5_000_000L;


	public FluidTank[] tanks = new FluidTank[]{
		new FluidTank(Fluids.PERFLUOROMETHYL_COLD, 4_000),
		new FluidTank(Fluids.PERFLUOROMETHYL, 4_000)
	};



	public boolean isOn;
	public int fuelCost;

	public TileEntityMachineHTRNeo() {
		super(0);
	}

	public boolean hasMinimumPlasma() {
		return plasmaEnergy >= MINIMUM_PLASMA;
	}

	public boolean isCool() {
		return tanks[0].getFill() >= COOLANT_USE &&
			   tanks[1].getFill() + COOLANT_USE <= tanks[1].getMaxFill();
	}


	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {

			plasmaEnergySync = plasmaEnergy;

			if(isCool()) {
				this.power = (long)(plasmaEnergy * PLASMA_EFFICIENCY);
				if(!hasMinimumPlasma()) this.power /= 2;

				tanks[0].setFill(-COOLANT_USE);
				tanks[1].setFill(+COOLANT_USE);
			}

			this.networkPackNT(200);
			this.plasmaEnergy = 0;   
		}
	}



	@Override
	public boolean receivesFusionPower() {
		return true;
	}

	@Override
	public void receiveFusionPower(long fusionPower, double neutronPower) {
		this.plasmaEnergy = fusionPower;
	}



	@Override
	public boolean canPerformBurn(int shipMass, double deltaV) {


		fuelCost = com.hbm.dim.SolarSystem.getFuelCost(deltaV, shipMass, 3000); //static temporary lolegaloge 

		if(plasmaEnergy < fuelCost) return false;

		if(!isCool()) return false;

		return true;
	}

	@Override
	public void addErrors(List<String> errors) {

		if(plasmaEnergy < fuelCost) {
			errors.add(EnumChatFormatting.RED + "Insufficient power: needs " + BobMathUtil.getShortNumber(fuelCost) + " HE");
		}

		if(!isCool()) {
			errors.add(EnumChatFormatting.RED + "Coolant loop not operational!");
		}
	}

	@Override
	public float getThrust() {
		return 1_600_000_000.0F; 
	}

	@Override
	public int startBurn() {
		isOn = true;
		power -= fuelCost;
		return 180;
	}

	@Override
	public int endBurn() {
		isOn = false;
		return 180;
	}



	@Override public FluidTank[] getAllTanks() { return tanks; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[]{ tanks[0] }; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[]{ tanks[1] }; }

	@Override public long getPower() { return power; }
	@Override public void setPower(long p) { power = p; }
	@Override public long getMaxPower() { return maxPower; }

	/* -------------------------
	 *   NBT / Sync
	 * ------------------------- */

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(plasmaEnergySync);
		buf.writeBoolean(isOn);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		plasmaEnergy = buf.readLong();
		isOn = buf.readBoolean();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("plasma", plasmaEnergy);
		nbt.setBoolean("on", isOn);

		tanks[0].writeToNBT(nbt, "t0");
		tanks[1].writeToNBT(nbt, "t1");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		plasmaEnergy = nbt.getLong("plasma");
		isOn = nbt.getBoolean("on");

		tanks[0].readFromNBT(nbt, "t0");
		tanks[1].readFromNBT(nbt, "t1");
	}

	@Override
	public TileEntity getTileEntity() {
		return this;
	}


	@Override
	public String getName() {
		return "container.htrfneo";
	}

	public boolean isFacingPrograde() {
		return ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset) == ForgeDirection.SOUTH;
	}

}
