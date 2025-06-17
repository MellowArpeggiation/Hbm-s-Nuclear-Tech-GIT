package com.hbm.dim;

import java.util.HashMap;
import java.util.Map;

import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteRailgun;
import com.hbm.saveddata.satellites.SatelliteWar;

import net.minecraft.client.Minecraft;

public class WorldProviderEarth extends WorldProviderCelestial {

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = terrainType.getChunkManager(worldObj);
	}

	@Override
	public String getDimensionName() {
		return "Earth";
	}

	@Override
	public boolean hasLife() {
		return true;
	}

	@Override
	public boolean canRespawnHere() {
		return true;
	}

	@Override
	public void updateWeather() {
		HashMap<Integer, Satellite> sats = SatelliteSavedData.getData(worldObj).sats;
		for(Map.Entry<Integer, Satellite> entry : sats.entrySet()) {
			if(entry.getValue() instanceof SatelliteWar) {
				SatelliteRailgun war = (SatelliteRailgun) entry.getValue();
				war.fire();
			}

		}

		for(Map.Entry<Integer, Satellite> entry : SatelliteSavedData.getClientSats().entrySet()) {
			if(entry.getValue() instanceof SatelliteWar) {

				SatelliteRailgun war = (SatelliteRailgun) entry.getValue();

				if(war.getInterp() >= 1 && war.interp <= 10) {
					Minecraft.getMinecraft().thePlayer.playSound("hbm:misc.fireflash", 10F, 1F);
				}
			}
		}
	}

}
