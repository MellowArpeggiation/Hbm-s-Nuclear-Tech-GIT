package com.hbm.dim;

import java.util.HashMap;
import java.util.Map;

import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteWar;

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
		super.updateWeather();

	}
    
}
