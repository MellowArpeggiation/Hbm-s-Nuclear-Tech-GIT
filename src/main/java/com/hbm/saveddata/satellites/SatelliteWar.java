package com.hbm.saveddata.satellites;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Destroyed;
import com.hbm.dim.trait.CBT_War;
import com.hbm.dim.trait.CBT_War.Projectile;
import com.hbm.dim.trait.CBT_War.ProjectileType;
import com.hbm.entity.logic.EntityDeathBlast;
import com.hbm.handler.ThreeInts;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.saveddata.satellites.Satellite.InterfaceActions;
import com.hbm.saveddata.satellites.Satellite.Interfaces;
import com.hbm.util.BufferUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModelCustom;

public class SatelliteWar extends Satellite {
	//time to clean up this shit and make it PROPER.

	public SatelliteWar() {
		
	}
	
	private boolean canFire = false;
	private boolean hasTarget = false;

	public long lastOp;
	public float interp;
	public int cooldown;
	private CelestialBody target;
	
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("lastOp", lastOp);
	}
	
	public void readFromNBT(NBTTagCompound nbt) {
		lastOp = nbt.getLong("lastOp");
	}
	
	public void onClick(World world, int x, int z) {

	}	

	public void fire() {

		
	}
	
	public void setTarget(CelestialBody body) {

	}
	
	public void fireAtTarget(CelestialBody body) {

	}
	public void playsound() {
        Minecraft.getMinecraft().thePlayer.playSound("hbm:misc.fireflash", 10F, 1F);
	}
	
	@Override
	public float[] getColor() {
		return new float[] { 0.0F, 0.0F, 0.0F };
	}
	
	public float getInterp() {
		return interp;
	}
	
	public int magSize() {
		return 0;
	}
	@Override
	public void serialize(ByteBuf buf) {
		buf.writeFloat(interp);

	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.interp = buf.readFloat();
	}

	
	
	public IModelCustom getModel() {
		return null;
	}
	
	public void render(World world, int x, int z, float interp, Minecraft mc) {

	}


}
