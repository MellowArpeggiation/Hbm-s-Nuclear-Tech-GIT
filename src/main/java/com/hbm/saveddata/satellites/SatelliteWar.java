package com.hbm.saveddata.satellites;

import java.util.ArrayList;

import com.hbm.dim.CelestialBody;

import com.hbm.entity.logic.EntityDeathBlast;
import com.hbm.handler.ThreeInts;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.satellites.Satellite.InterfaceActions;
import com.hbm.saveddata.satellites.Satellite.Interfaces;
import com.hbm.util.BufferUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class SatelliteWar extends Satellite {
	

	public SatelliteWar() {
		this.ifaceAcs.add(InterfaceActions.HAS_MAP);
		this.ifaceAcs.add(InterfaceActions.SHOW_COORDS);
		this.ifaceAcs.add(InterfaceActions.CAN_CLICK);
		this.satIface = Interfaces.SAT_PANEL;
	}
	
	
	public long lastOp;
	public float interp;
	public int cooldown;
	
	
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("lastOp", lastOp);
	}
	
	public void readFromNBT(NBTTagCompound nbt) {
		lastOp = nbt.getLong("lastOp");
	}
	
	public void onClick(World world, int x, int z) {
	
	}	

	public void fire() {
		interp += 0.5f;
		interp = Math.min(100.0f, interp + 0.3f * (100.0f - interp) * 0.15f);

        if (interp >= 100) {
        	interp = 0;
        }
	}
	
	public void playsound(World world) {
        Minecraft.getMinecraft().thePlayer.playSound("hbm:misc.fireflash", 10F, 1F);

	}
	
	@Override
	public float[] getColor() {
		return new float[] { 0.0F, 0.0F, 0.0F };
	}
	
	public float getInterp() {
		return interp;
	}
	
	@Override
	public void serialize(ByteBuf buf) {
		buf.writeFloat(interp);

	}

	@Override
	public void deserialize(ByteBuf buf) {

		this.interp = buf.readFloat();
	}

}
