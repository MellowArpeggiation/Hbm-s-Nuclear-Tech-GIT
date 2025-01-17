package com.hbm.dim;

import java.util.HashMap;
import java.util.Map;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Destroyed;
import com.hbm.dim.trait.CBT_War;
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
	    
		if(!worldObj.isRemote) {

		HashMap<Integer, Satellite> sats = SatelliteSavedData.getData(world).sats;
		for(Map.Entry<Integer, Satellite> entry : sats.entrySet()) {
				if(entry.getValue() instanceof SatelliteWar) {
					SatelliteWar war = (SatelliteWar) entry.getValue();
					//war.fire();	
					war.interp = 100;
				}
			
			}
        } else {
			for(Map.Entry<Integer, Satellite> entry : SatelliteSavedData.getClientSats().entrySet()) {
				if(entry.getValue() instanceof SatelliteWar) {

					SatelliteWar war = (SatelliteWar) entry.getValue();
					if(war.getInterp() <= 1) {
				        Minecraft.getMinecraft().thePlayer.playSound("hbm:misc.fireflash", 10F, 1F);
					}
				}
			}
		}

        CBT_War war = CelestialBody.getTrait(worldObj, CBT_War.class);
        if(!worldObj.isRemote) {
        	
	        if (war != null) {
	            for (int i = 0; i < war.getProjectiles().size(); i++) {
	                CBT_War.Projectile projectile = war.getProjectiles().get(i);
	                
	                projectile.update();
	                float travel = projectile.getTravel();
	                System.out.println(travel);
		            if (projectile.getTravel() >= 18 && projectile.getTravel() <= 18 && worldObj ==  MinecraftServer.getServer().worldServerForDimension(projectile.getTarget())) {
		            	System.out.println("323");
		            	  Minecraft.getMinecraft().thePlayer.playSound("hbm:misc.impact", 10F, 1F);

	                }
		            if (projectile.getTravel() <= 0) {
		                projectile.impact();
		            }
		            
	                if(projectile.getAnimtime() >= 100) {
		                    war.destroyProjectile(projectile);
		    				World targetBody = MinecraftServer.getServer().worldServerForDimension(SpaceConfig.moonDimension);
		                    i--;
		                    System.out.println("damaged: " + targetBody + " health left: " + war.health);
		                    if(war.health > 0) {
			    				CelestialBody.damage(projectile.getDamage(), targetBody);		                    
		                    } else if(war.health <= 0) {
		        				CelestialBody target = CelestialBody.getPlanet(targetBody);
		        				target.modifyTraits(targetBody, new CBT_Destroyed());
		        				war.health = 0;
		                    }
	                }
	                if(projectile.getType() == ProjectileType.SPLITSHOT) {
	                	if (projectile.getTravel() <= 0) {
	                		war.split(worldObj, 4, projectile, ProjectileType.SMALL);
	                		war.destroyProjectile(projectile);
	                		i--;
	                	}
	                	
	                }
	            }
	        }
        }
	}
    
}
