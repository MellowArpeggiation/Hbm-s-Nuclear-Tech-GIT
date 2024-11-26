package com.hbm.saveddata.satellites;

import com.hbm.main.MainRegistry;
import com.hbm.saveddata.satellites.Satellite.Interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class SatelliteWar extends Satellite {

	public SatelliteWar() {
		this.satIface = Interfaces.NONE;
	}

	public void onOrbit(World world, double x, double y, double z) {

		for(Object p : world.playerEntities)
			((EntityPlayer)p).triggerAchievement(MainRegistry.achFOEQ);
	}

	@Override
	public float[] getColor() {
		return new float[] { 0.0F, 0.0F, 0.0F };
	}

}
