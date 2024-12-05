package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDysonConverterAnatmogenesis;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class MachineDysonConverterAnatmogenesis extends BlockDummyable implements ILookOverlay {

	public MachineDysonConverterAnatmogenesis(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityDysonConverterAnatmogenesis();
		if(meta >= 6) return new TileEntityProxyCombo(false, false, false);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);
		
		if(pos == null) return;
		
		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);
		
		if(!(te instanceof TileEntityDysonConverterAnatmogenesis)) return;
		
		TileEntityDysonConverterAnatmogenesis converter = (TileEntityDysonConverterAnatmogenesis) te;
		
		CBT_Atmosphere atmosphere = CelestialBody.getTrait(world, CBT_Atmosphere.class);
		double pressure = atmosphere != null ? atmosphere.getPressure(converter.fluid) : 0;
		if(pressure < 0.0001) pressure = 0;
		pressure = Math.round(pressure * 1_000.0) / 1_000.0;

		List<String> text = new ArrayList<String>();

		text.add("Current rate: " + ((double)converter.gasProduced * 20 * 60 * 60 / AstronomyUtil.MB_PER_ATM) + "atm per hour");
		text.add("Current gas: " + converter.fluid.getLocalizedName() + " - " + pressure);
		text.add("Current mode: " + (converter.isEmitting ? "EMITTING" : "CAPTURING"));
		
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	//. TEMP .//
	@Override
	public int getRenderType() {
		return 0;
	}
	@Override
	public boolean isOpaqueCube() {
		return true;
	}
	@Override
	public boolean renderAsNormalBlock() {
		return true;
	}
	//. TEMP .//
	
}

