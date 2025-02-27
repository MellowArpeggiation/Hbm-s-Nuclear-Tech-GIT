package com.hbm.dim;

import java.util.HashMap;
import java.util.Map;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Destroyed;
import com.hbm.dim.trait.CBT_War;
import com.hbm.dim.trait.CelestialBodyTrait;
import com.hbm.dim.trait.CBT_War.ProjectileType;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteWar;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

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
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);
		World world = DimensionManager.getWorld(worldObj.provider.dimensionId);
	    SatelliteSavedData data = (SatelliteSavedData)world.perWorldStorage.loadData(SatelliteSavedData.class, "satellites");




		HashMap<Integer, Satellite> sats = SatelliteSavedData.getData(world).sats;
		for(Map.Entry<Integer, Satellite> entry : sats.entrySet()) {
				if(entry.getValue() instanceof SatelliteWar) {
					SatelliteWar war = (SatelliteWar) entry.getValue();
					war.fire();	
				}
			
			}

			for(Map.Entry<Integer, Satellite> entry : SatelliteSavedData.getClientSats().entrySet()) {
				if(entry.getValue() instanceof SatelliteWar) {

					SatelliteWar war = (SatelliteWar) entry.getValue();

					if(war.getInterp() >= 1 && war.interp <= 10) {
				       Minecraft.getMinecraft().thePlayer.playSound("hbm:misc.fireflash", 10F, 1F);
					}
				}
			}
		}


	
    
}
