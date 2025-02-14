package com.hbm.dim.trait;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import scala.reflect.internal.Trees.This;

public class CBT_Destroyed extends CelestialBodyTrait {

	public float interp;

	public CBT_Destroyed() {}

	public CBT_Destroyed(float interp) {
		this.interp = interp;
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
		//buf.writeFloat(interp);
	}

	@Override
	public void readFromBytes(ByteBuf buf) {
		//interp = buf.readFloat();
	}

	public void updatefloat() {
        interp = Math.min(201.0f,interp + 0.01f * (201.0f - interp) * 0.15f);
        if (interp >= 200) {
        	interp = 0;
        }		

	}
	
	@Override
	public void update(boolean isremote) {
		if(isremote) {
			updatefloat();
		}
	}

}
