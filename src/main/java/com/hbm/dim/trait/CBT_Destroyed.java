package com.hbm.dim.trait;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class CBT_Destroyed extends CelestialBodyTrait {
	
	public float interp;

	public CBT_Destroyed() {}

	public CBT_Destroyed(float bees) {
		this.interp = bees;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("interp", interp);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		interp = nbt.getFloat("interp");
	}

	@Override
	public void writeToBytes(ByteBuf buf) {
		buf.writeFloat(interp);
	}

	@Override
	public void readFromBytes(ByteBuf buf) {
		interp = buf.readFloat();
	}
	
	public void updatefloat() {
		interp += 0.1f;
        interp = Math.min(100.0f,interp + 0.1f * (100.0f - interp) * 0.15f);

        if (interp >= 100) {
        	interp = 0;
        }		
	}

}
