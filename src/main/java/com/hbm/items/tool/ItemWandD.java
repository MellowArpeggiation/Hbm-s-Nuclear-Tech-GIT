package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.DebugTeleporter;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbm.dim.trait.CBT_War;
import com.hbm.dim.trait.CBT_War.Projectile;
import com.hbm.dim.trait.CBT_War.ProjectileType;
import com.hbm.dim.trait.CBT_Destroyed;
import com.hbm.lib.Library;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class ItemWandD extends Item {

	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		
		if(world.isRemote)
			return stack;
		
		MovingObjectPosition pos = Library.rayTrace(player, 500, 1, false, true, false);
		
		if(pos != null) {

			if(stack.stackTagCompound == null)
				stack.stackTagCompound = new NBTTagCompound();
			
			if(!player.isSneaking()) {
				int targetId = stack.stackTagCompound.getInteger("dim");

				if(targetId == 0) {
					DebugTeleporter.teleport(player, SpaceConfig.orbitDimension, player.posX, 128, player.posZ, false);
					player.addChatMessage(new ChatComponentText("Teleported to: ORBIT"));
				} else {
					SolarSystem.Body target = SolarSystem.Body.values()[targetId];
	
					DebugTeleporter.teleport(player, target.getBody().dimensionId, player.posX, 300, player.posZ, true);
					player.addChatMessage(new ChatComponentText("Teleported to: " + target.getBody().getUnlocalizedName()));
				}

			} else {
				int targetId = stack.stackTagCompound.getInteger("dim");
				targetId++;

				if(targetId >= SolarSystem.Body.values().length) {
					targetId = 0;
				}
				
				stack.stackTagCompound.setInteger("dim", targetId);

				if(targetId == 0) {
					player.addChatMessage(new ChatComponentText("Set teleport target to: ORBIT"));
				} else {
					SolarSystem.Body target = SolarSystem.Body.values()[targetId];
					player.addChatMessage(new ChatComponentText("Set teleport target to: " + target.getBody().getUnlocalizedName()));
				}
			}
		} else if(!(world.provider instanceof WorldProviderOrbit)) {
			if(!player.isSneaking()) {
				// TESTING: View atmospheric data
				CBT_Atmosphere atmosphere = CelestialBody.getTrait(world, CBT_Atmosphere.class);
	
				boolean isVacuum = true;
				if(atmosphere != null) {
					for(FluidEntry entry : atmosphere.fluids) {
						// if(entry.pressure > 0.001) {
							player.addChatMessage(new ChatComponentText("Atmosphere: " + entry.fluid.getUnlocalizedName() + " - " + entry.pressure + "bar"));
							isVacuum = false;
						// }
					}
				}
	
				if(isVacuum)
					player.addChatMessage(new ChatComponentText("Atmosphere: NEAR VACUUM"));
			} else {
				
				//something broke here....
				//i might split the war trait and the projectiles trait and have it be its own.
				//when all projectiles return zero and finish damaging the planet the trait deletes itself
				//problem right now is that if both planets have war data the projectile list conflates the two
				
				
				// TESTING: END OF LIFE
				//World targetdBody = DimensionManager.getWorld(SpaceConfig.dunaDimension);
				World targetdBody = MinecraftServer.getServer().worldServerForDimension(SpaceConfig.dunaDimension);
				CelestialBody target = CelestialBody.getBody(targetdBody);
				System.out.println(target);

				if(!target.hasTrait(targetdBody, CBT_War.class)) {
					// TESTING: END OF TIME
					target.modifyTraits(targetdBody, new CBT_War());		
				} else {
					CBT_War war = CelestialBody.getTrait(targetdBody, CBT_War.class);
					if(war != null) {
						float rand = Minecraft.getMinecraft().theWorld.rand.nextFloat();
						System.out.println(rand);
						//war.launchProjectile(100, 20, 1, 28 * rand * 5, 33, 20, ProjectileType.SPLITSHOT);
						Projectile projectile = new Projectile(100, 20, 50, 28 * rand * 5, 55, 20, ProjectileType.SMALL, SpaceConfig.dunaDimension);
						projectile.GUIangle = (int) (rand * 360);
						war.launchProjectile(projectile);
						System.out.println(war.health);

						player.addChatMessage(new ChatComponentText("projectile launched"));
					}
				} 
			}
		}
		return stack;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add("Dimension teleporter and atmosphere debugger.");

		if(stack.stackTagCompound != null) {
			int targetId = stack.stackTagCompound.getInteger("dim");
			if(targetId == 0) {
				list.add("Teleportation target: ORBIT");
			} else {
				SolarSystem.Body target = SolarSystem.Body.values()[targetId];
				list.add("Teleportation target: " + target.getBody().getUnlocalizedName());
			}
		}
	}
}