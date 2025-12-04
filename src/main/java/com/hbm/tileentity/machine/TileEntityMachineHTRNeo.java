package com.hbm.tileentity.machine;


import java.util.List;
import java.util.Map.Entry;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Rocket;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.machine.fusion.IFusionPowerReceiver;
import com.hbm.tileentity.machine.fusion.TileEntityFusionTorus;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.KlystronNetwork;
import com.hbm.uninos.networkproviders.KlystronNetworkProvider;
import com.hbm.uninos.networkproviders.PlasmaNetwork;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ParticleUtil;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import api.hbm.tile.IPropulsion;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import io.netty.buffer.ByteBuf;

public class TileEntityMachineHTRNeo extends TileEntityMachineBase
implements IPropulsion, IFluidStandardTransceiverMK2, IFluidStandardReceiver, IEnergyReceiverMK2, IFusionPowerReceiver {

	//i smushed these together because i need you so bad
	protected GenNode plasmaNode;

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

			
			if(plasmaNode == null || plasmaNode.expired) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);
				plasmaNode = UniNodespace.getNode(worldObj, xCoord + dir.offsetX * -10, yCoord , zCoord + dir.offsetZ * -10, PlasmaNetworkProvider.THE_PROVIDER);
				if(plasmaNode == null) {

					plasmaNode = new GenNode(PlasmaNetworkProvider.THE_PROVIDER, 
							new BlockPos(xCoord + dir.offsetX * -10, yCoord , zCoord + dir.offsetZ * -10))
							.setConnections(new DirPos(xCoord + dir.offsetX * -11, yCoord , zCoord + dir.offsetZ * -11,dir));
					

					UniNodespace.createNode(worldObj, plasmaNode);
				}
				


			}
			if(plasmaNode != null && plasmaNode.hasValidNet()) plasmaNode.net.addReceiver(this);
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);

			
			if(plasmaNode != null && plasmaNode.net != null) {
				PlasmaNetwork net = (PlasmaNetwork) plasmaNode.net;

				for(Object o : net.receiverEntries.entrySet()) {
					
					Entry e = (Entry) o;
					if(e.getKey() instanceof TileEntityFusionTorus) { // replace this with an interface should we ever get more acceptors
						TileEntityFusionTorus torus = (TileEntityFusionTorus) e.getKey();

						if(torus.isLoaded() && !torus.isInvalid()) { // check against zombie network members

							plasmaEnergy += torus.plasmaEnergy;
							break; 
						}
					}
				}
			}
			
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
	public void invalidate() {
		super.invalidate();
		if(!worldObj.isRemote) {
			if(this.plasmaNode != null) UniNodespace.destroyNode(worldObj, plasmaNode);
		}
	}
	@Override
	public boolean receivesFusionPower() {
		return true;
	}

	@Override
	public void receiveFusionPower(long fusionPower, double neutronPower) {
		plasmaEnergy = fusionPower;
		
	}

	private DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
			new DirPos(xCoord - rot.offsetX * 10 + dir.offsetX, yCoord, zCoord - rot.offsetZ * 10 + dir.offsetZ, rot),
			new DirPos(xCoord - rot.offsetX * 10 - dir.offsetX, yCoord, zCoord - rot.offsetZ * 10 - dir.offsetZ, rot), //SAVING FOR LATER
		};
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
	
	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) bb = AxisAlignedBB.getBoundingBox(xCoord - 11, yCoord - 2, zCoord - 11, xCoord + 12, yCoord + 3, zCoord + 12);
		return bb;
	}

	@Override
	public String getName() {
		return "container.htrfneo";
	}

	public boolean isFacingPrograde() {
		return ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset) == ForgeDirection.SOUTH;
	}

}
