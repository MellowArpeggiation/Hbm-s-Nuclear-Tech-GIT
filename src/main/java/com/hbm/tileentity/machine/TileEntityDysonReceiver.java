package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.trait.CBT_Dyson;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IDysonConverter;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityDysonReceiver extends TileEntityMachineBase {

	// Connects to a dyson swarm via ID, receiving energy during the day
	// also receives energy at night if a satellite relay is in orbit around the planet

	// The energy received is fired as a violently powerful beam,
	// converters can collect this beam and turn it into HE/TU or used for analysis, crafting, etc.

	public int swarmCount;
	public int swarmConsumers;
	public int beamLength;

	private AudioWrapper audio;

	public TileEntityDysonReceiver() {
		super(1);
	}

	// Sun luminosity is 4*10^26, which we can't represent in any Java integer primitive
	// therefore the upper bound for power generation is higher than a FEnSU, effectively
	// reality doesn't provide any interesting solutions that make the system engaging to use
	// so we're going to build our own power curve.
	// We need to encourage players to build large swarms, so single satellites must suck but together they produce enormous power
	// Gompertz is a funne name
	public static long getEnergyOutput(int swarmCount) {
		double adjustedDensity = (double)swarmCount / 1024.0D;
		long maxOutput = Long.MAX_VALUE / 10;
		double b = 32.0D;
		double c = 1.3D;
		double gompertz = Math.exp(-b * Math.exp(-c * adjustedDensity));
		return (long)(maxOutput * gompertz) / 20;
	}

	@Override
	public String getName() {
		return "container.machineDysonReceiver";
	}

	@Override
	public void updateEntity() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
		
		if(!worldObj.isRemote) {
			int swarmId = 12345;

			swarmCount = CBT_Dyson.count(worldObj, swarmId);
			swarmConsumers = CBT_Dyson.consumers(worldObj, swarmId);

			if(swarmCount > 0 && swarmConsumers > 0) {
				long energyOutput = getEnergyOutput(swarmCount) / swarmConsumers;

				beamLength = 24;
				for(int i = 3; i < 24; i++) {
					int x = xCoord + dir.offsetX * i;
					int y = yCoord;
					int z = zCoord + dir.offsetZ * i;

					Block block = worldObj.getBlock(x, y, z);
					
					TileEntity te;
					if(block instanceof BlockDummyable) {
						int[] pos = ((BlockDummyable) block).findCore(worldObj, x, y, z);
						te = worldObj.getTileEntity(pos[0], pos[1], pos[2]);
					} else {
						te = worldObj.getTileEntity(x, y, z);
					}

					if(te instanceof IDysonConverter) {
						((IDysonConverter) te).provideEnergy(x, y, z, energyOutput);
						beamLength = i;
						break;
					}
				}
			}

			networkPackNT(20);			
		} else {
			if(swarmCount > 0) {
				if(audio == null) {
					audio = MainRegistry.proxy.getLoopedSound("hbm:block.dysonBeam", xCoord + dir.offsetX * 8, yCoord, zCoord + dir.offsetZ * 8, 0.75F, 20F, 1.0F);
					audio.startSound();
				}

				audio.updatePitch(0.85F);
			} else {
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(swarmCount);
		buf.writeInt(swarmConsumers);
		buf.writeInt(beamLength);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		swarmCount = buf.readInt();
		swarmConsumers = buf.readInt();
		beamLength = buf.readInt();
	}
	
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 25,
				yCoord,
				zCoord - 25,
				xCoord + 25,
				yCoord + 19,
				zCoord + 25
			);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	public static void runTests() {
		for(int i = 1; i < 5000; i *= 2) {
			MainRegistry.logger.info(i + " dyson swarm members produces: " + BobMathUtil.getShortNumber(getEnergyOutput(i) * 20) + "HE/s");
		}
	}
	
}
