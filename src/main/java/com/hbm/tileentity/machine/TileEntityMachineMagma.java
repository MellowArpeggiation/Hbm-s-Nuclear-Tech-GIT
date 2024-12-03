package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardTransceiver;

public class TileEntityMachineMagma extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiver {

	public TileEntityMachineMagma(int slotCount) {
		super(slotCount);
		// TODO Auto-generated constructor stub
	}

	@Override
	public long getPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPower(long power) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getMaxPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FluidTank[] getAllTanks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateEntity() {
		// TODO Auto-generated method stub

	}

}
