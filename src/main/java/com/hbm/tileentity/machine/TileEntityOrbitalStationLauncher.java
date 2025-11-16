package com.hbm.tileentity.machine;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.entity.missile.EntityRideableRocket;
import com.hbm.entity.missile.EntityRideableRocket.RocketState;
import com.hbm.handler.RocketStruct;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.SlotRocket.IStage;
import com.hbm.inventory.container.ContainerOrbitalStationLauncher;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIOrbitalStationLauncher;
import com.hbm.items.ISatChip;
import com.hbm.items.ItemVOTVdrive;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityOrbitalStationLauncher extends TileEntityMachineBase implements IGUIProvider, IControlReceiver, IStage {

	public RocketStruct rocket;

	private OrbitalStation station;
	private EntityRideableRocket docked;

	public FluidTank[] tanks;

	public float rot;
	public float prevRot;

	public int currentStage;

	public boolean isBreaking;

	public TileEntityOrbitalStationLauncher() {
		// launch:			drive + fuel in + fuel out
		// construction:	capsule + stages + program drives
		super(
			1 + 1 + 1 +
			1 + RocketStruct.MAX_STAGES * 3 + RocketStruct.MAX_STAGES * 2
		);

		tanks = new FluidTank[RocketStruct.MAX_STAGES * 2]; // enough tanks for any combination of rocket stages
		for(int i = 0; i < tanks.length; i++) tanks[i] = new FluidTank(Fluids.NONE, 64_000);
	}

	@Override
	public String getName() {
		return "container.orbitalStationLauncher";
	}

	@Override
	public void updateEntity() {
		if(!CelestialBody.inOrbit(worldObj)) return;

		if(!worldObj.isRemote) {
			// This TE acts almost entirely like a port, except doesn't register itself so nothing actually tries to dock here
			station = OrbitalStation.getStationFromPosition(xCoord, zCoord);
			if(station != null) {

			}

			// Setup the constructed rocket
			ItemStack fromStack = slots[slots.length - (RocketStruct.MAX_STAGES - currentStage) * 2];
			ItemStack toStack = slots[slots.length - (RocketStruct.MAX_STAGES - currentStage) * 2 + 1];

			// updates the orbital station information and syncs it to the client, if necessary
			ItemVOTVdrive.getTarget(fromStack, worldObj);
			ItemVOTVdrive.getTarget(toStack, worldObj);

			rocket = new RocketStruct(slots[3]);
			if(slots[3] != null && slots[3].getItem() instanceof ISatChip) {
				rocket.satFreq = ISatChip.getFreqS(slots[3]);
			}
			for(int i = 4; i < RocketStruct.MAX_STAGES * 3 + 3; i += 3) {
				if(slots[i] == null && slots[i+1] == null && slots[i+2] == null) {
					// Check for later stages and shift them up into empty stages
					if(i + 3 < RocketStruct.MAX_STAGES * 3 && (slots[i+3] != null || slots[i+4] != null || slots[i+5] != null)) {
						slots[i] = slots[i+3];
						slots[i+1] = slots[i+4];
						slots[i+2] = slots[i+5];
						slots[i+3] = null;
						slots[i+4] = null;
						slots[i+5] = null;
					} else {
						break;
					}
				}
				rocket.addStage(slots[i], slots[i+1], slots[i+2]);
			}

			networkPackNT(250);
		}
	}

	public void enterCapsule(EntityPlayer player) {
		if(docked == null || docked.riddenByEntity != null) return;
		docked.interactFirst(player);
	}

	public void dockRocket(EntityRideableRocket rocket) {
		docked = rocket;
	}

	public void undockRocket() {
		docked = null;
	}

	public void spawnRocket(ItemStack stack) {
		EntityRideableRocket rocket = new EntityRideableRocket(worldObj, xCoord + 0.5F, yCoord + 1.5F, zCoord + 0.5F, stack);
		rocket.posY -= rocket.height;
		rocket.setState(RocketState.LANDED);
		worldObj.spawnEntityInWorld(rocket);

		dockRocket(rocket);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if(stack == null) return true;
		if(index == 0 && !(stack.getItem() instanceof ItemVOTVdrive)) return false;
		return true;
	}

	@Override
	public void serialize(ByteBuf buf) {
		rocket.writeToByteBuffer(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		rocket = RocketStruct.readFromByteBuffer(buf);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {

	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerOrbitalStationLauncher(player.inventory, this);
	}

	@Override
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIOrbitalStationLauncher(player.inventory, this);
	}

	@Override
	public void setCurrentStage(int stage) {
		currentStage = stage;
	}

}
