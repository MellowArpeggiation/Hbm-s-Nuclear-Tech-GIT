package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDysonReceiver;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

public class MachineDysonReceiver extends BlockDummyable implements ILookOverlay {
	
	public MachineDysonReceiver(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityDysonReceiver();
		if(meta >= 6) return new TileEntityProxyCombo(false, false, false);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 4, 2, 2, 2};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		// Main structure
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {1, 0, 8, -4, 1, 1}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {3, 0, 4, 0, 1, 1}, this, dir);

		// Dish
		MultiblockHandlerXR.fillSpace(world, x, y + 10, z, new int[] {1, 0, 6, 6, 6, 6}, this, dir);

		// Tower
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {17, 0, 1, 1, 1, 1}, this, dir);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);
		
		if(pos == null) return;
		
		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);
		
		if(!(te instanceof TileEntityDysonReceiver)) return;
		
		TileEntityDysonReceiver receiver = (TileEntityDysonReceiver) te;

		long energyOutput = 0;
		if(receiver.swarmConsumers > 0) {
			energyOutput = TileEntityDysonReceiver.getEnergyOutput(receiver.swarmCount) / receiver.swarmConsumers * 20;
		}
		
		List<String> text = new ArrayList<String>();
		text.add("Swarm: " + receiver.swarmCount + " members");
		text.add("Consumers: " + receiver.swarmConsumers + " consumers");
		text.add("Power: " + BobMathUtil.getShortNumber(energyOutput) + "HE/s");
		
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

}
