package com.hbm.dim.trait;

import com.hbm.main.ModEventHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class CBT_Lights extends CelestialBodyTrait{
	
	public int lights;
	public boolean isCivilized;
	
	public CBT_Lights() {}

	public CBT_Lights(int light) {
		this.lights = light;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("lights", lights);
		nbt.setBoolean("isCiv", isCivilized);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		lights = nbt.getInteger("lights");
		isCivilized = nbt.getBoolean("isCiv");
	}

	@Override
	public void writeToBytes(ByteBuf buf) {
		buf.writeInt(lights);
		buf.writeBoolean(isCivilized);
	}

	@Override
	public void readFromBytes(ByteBuf buf) {
		lights = buf.readInt();
		isCivilized = buf.readBoolean();
	}
	
	
	
}
