package com.hbm.saveddata.satellites;

import java.util.ArrayList;

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
import com.hbm.saveddata.satellites.Satellite.InterfaceActions;
import com.hbm.saveddata.satellites.Satellite.Interfaces;
import com.hbm.util.BufferUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class SatelliteWar extends Satellite {
	

	public SatelliteWar() {
		this.ifaceAcs.add(InterfaceActions.HAS_MAP);
		this.ifaceAcs.add(InterfaceActions.SHOW_COORDS);
		this.ifaceAcs.add(InterfaceActions.CAN_CLICK);
		this.satIface = Interfaces.SAT_PANEL;
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
		fireAtTarget(target);

		if(!hasTarget) {
			canFire = false;
		}
		else {
			canFire = true;
		}
	}	

	public void fire() {
	    if (canFire) {
	        interp += 0.5f;
	        interp = Math.min(100.0f, interp + 0.3f * (100.0f - interp) * 0.15f);

	        if (interp >= 100) {
	            interp = 0; 
	            canFire = false;
	        }

	    }
		
	}
	
	public void setTarget(CelestialBody body) {
		target = CelestialBody.getBody(body.dimensionId);
		if(target != null) {
			hasTarget = true;
		}

	}
	
	public void fireAtTarget(CelestialBody body) {
		if(hasTarget) {
			if(!target.hasTrait(CBT_War.class)) {
				target.modifyTraits(new CBT_War(100, 0));
			} else {
				CBT_War war = target.getTrait(CBT_War.class);
				if(war != null) {
					float rand = Minecraft.getMinecraft().theWorld.rand.nextFloat();
					//TODO: be able to choose projectile types 
					Projectile projectile = new Projectile(100, 20, 50, 28 * rand * 5, 55, 20, ProjectileType.SMALL, body.dimensionId);
					projectile.GUIangle = (int) (rand * 360);
					war.launchProjectile(projectile);
					System.out.println(war.health);
					
				}
			}			
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

	
	public void setCanFire(boolean canFire) {
		    this.canFire = canFire;
	}

}
