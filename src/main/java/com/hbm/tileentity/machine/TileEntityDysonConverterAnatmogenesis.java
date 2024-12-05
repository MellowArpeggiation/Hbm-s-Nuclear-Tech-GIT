package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Gaseous;
import com.hbm.tileentity.IDysonConverter;
import com.hbm.tileentity.TileEntityMachineBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityDysonConverterAnatmogenesis extends TileEntityMachineBase implements IDysonConverter {

	// what the FUCK is anatmogenesis you ask?
	// I made it the fuck up

	// from:
	// * an-		= without
	// * atmo-		= vapour, air (from atmosphere)
	// * genesis	= creation

	// similar to abiogenesis (life from non-life/nothing)
	// anatmogenesis is the creation of an atmosphere from nothing

	// this is effectively the survival version of the creative atmosphere editor,
	// turning absolutely ridiculous amounts of energy into any gas you please,
	// or remove a gas entirely, if you so desire.

	public FluidType fluid = Fluids.OXYGEN;
	public boolean isEmitting = true;

	public long gasProduced;

	// 100THE/s will produce 0.1atm in 8 hours
	private static final long HE_TO_MB = 28_800_000;

	public TileEntityDysonConverterAnatmogenesis() {
		super(0);
	}

	@Override
	public String getName() {
		return "container.dysonConverterAnatmogenesis";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			networkPackNT(15);
			gasProduced = 0;
		}
	}

	@Override
	public void provideEnergy(int x, int y, int z, long energy) {
		long volume = energy / HE_TO_MB;
		gasProduced += volume;

		if(isEmitting) {
			FT_Gaseous.release(worldObj, fluid, volume);
		} else {
			FT_Gaseous.capture(worldObj, fluid, volume);
		}
	}

	@Override
	public long maximumEnergy() {
		return Long.MAX_VALUE;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(gasProduced);
		buf.writeInt(fluid.getID());
		buf.writeBoolean(isEmitting);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		gasProduced = buf.readLong();
		fluid = Fluids.fromID(buf.readInt());
		isEmitting = buf.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		fluid = Fluids.fromID(nbt.getInteger("fluid"));
		isEmitting = nbt.getBoolean("emit");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("fluid", fluid.getID());
		nbt.setBoolean("emit", isEmitting);
	}
	
}
