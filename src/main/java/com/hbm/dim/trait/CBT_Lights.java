package com.hbm.dim.trait;

import com.hbm.main.ModEventHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class CBT_Lights extends CelestialBodyTrait{
	
	public int lights;
	

	public CBT_Lights() {}

	public CBT_Lights(int light) {
		this.lights = light;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("lights", lights);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		lights = nbt.getInteger("lights");
	}

	@Override
	public void writeToBytes(ByteBuf buf) {
		buf.writeInt(lights);
	}

	@Override
	public void readFromBytes(ByteBuf buf) {
		lights = buf.readInt();
	}
	
}
