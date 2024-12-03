package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineMagma;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

public class MachineMagma extends BlockDummyable implements ILookOverlay {

	public MachineMagma() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityMachineMagma();
		if(meta >= 6) return new TileEntityProxyCombo().power().fluid();
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		return this.standardOpenBehavior(world, x, y, z, player, 0);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {3, 3, 3, 3, 3, 3};
	}

	@Override
	public int getOffset() {
		return 3;
	}

	@Override
	public int getHeightOffset() {
		return 3;
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x += dir.offsetX * o;
		y += dir.offsetY * o;
		z += dir.offsetZ * o;
		
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		this.makeExtra(world, x - dir.offsetX * 3, y - 1, z + dir.offsetZ * 3);
		this.makeExtra(world, x - dir.offsetX * 3, y - 2, z + dir.offsetZ * 3);
		this.makeExtra(world, x - dir.offsetX * 3 + rot.offsetX, y - 1, z - dir.offsetZ * 3 + rot.offsetZ);
		this.makeExtra(world, x - dir.offsetX * 3 - rot.offsetX, y - 1, z - dir.offsetZ * 3 - rot.offsetZ);
		this.makeExtra(world, x - dir.offsetX * 3 + rot.offsetX, y - 2, z - dir.offsetZ * 3 + rot.offsetZ);
		this.makeExtra(world, x - dir.offsetX * 3 - rot.offsetX, y - 2, z - dir.offsetZ * 3 - rot.offsetZ);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		
	}

}
