package com.hbm.entity.effect;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EntityDepress extends Entity {

	public final ForgeDirection dir;
	public int timeToLive;

	public EntityDepress(World world) {
		this(world, ForgeDirection.DOWN, 100);
	}

	public EntityDepress(World world, ForgeDirection dir, int timeToLive) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.noClip = true;

		this.timeToLive = timeToLive;
		this.dir = dir;
	}

	@Override
	public void onUpdate() {
		NBTTagCompound data = new NBTTagCompound();
		data.setDouble("posX", posX + worldObj.rand.nextGaussian() * 0.25);
		data.setDouble("posY", posY + worldObj.rand.nextGaussian() * 0.25);
		data.setDouble("posZ", posZ + worldObj.rand.nextGaussian() * 0.25);
		data.setString("type", "depress");
		data.setFloat("scale", 0.5f);
		data.setDouble("moX", dir.offsetX * 0.5 + worldObj.rand.nextGaussian() * 0.25);
		data.setDouble("moY", dir.offsetY * 0.5 + worldObj.rand.nextGaussian() * 0.25);
		data.setDouble("moZ", dir.offsetZ * 0.5 + worldObj.rand.nextGaussian() * 0.25);
		data.setInteger("maxAge", 100 + worldObj.rand.nextInt(20));
		data.setInteger("color", 0xFFFFFF);
		MainRegistry.proxy.effectNT(data);

		timeToLive--;

		if(timeToLive <= 0) {
			setDead();
		}
	}

	@Override
	protected void entityInit() {

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		timeToLive = nbt.getInteger("ttl");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("ttl", timeToLive);
	}

}
