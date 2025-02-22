package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerAutocrafter;
import com.hbm.inventory.container.ContainerMachineWarController;
import com.hbm.inventory.gui.GUIAutocrafter;
import com.hbm.inventory.gui.GUIWarController;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IEnergyReceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityMachineWarController extends TileEntityMachineBase implements IEnergyReceiverMK2, IGUIProvider {

		
	public TileEntityMachineWarController() {
		super(4);
	}
	@Override
	public String getName() {
		return "container.warController";
	}

	@Override
	public long getPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPower(long power) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getMaxPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isLoaded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineWarController(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIWarController(player.inventory, this);
	}
	@Override
	public void updateEntity() {
		// TODO Auto-generated method stub
		
	}

}
