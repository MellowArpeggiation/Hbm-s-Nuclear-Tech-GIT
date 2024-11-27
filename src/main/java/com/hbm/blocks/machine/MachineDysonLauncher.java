package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDysonLauncher;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

// swarm shitter
public class MachineDysonLauncher extends BlockDummyable implements ILookOverlay {

	public MachineDysonLauncher(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityDysonLauncher();
		if(meta >= 6) return new TileEntityProxyCombo(true, true, false);
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);
	
			if(pos == null)
				return false;

			TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

			if(!(te instanceof TileEntityDysonLauncher))
				return false;

			TileEntityDysonLauncher launcher = (TileEntityDysonLauncher) te;

			ItemStack heldStack = player.getHeldItem();

			if(heldStack != null && heldStack.getItem() == ModItems.sat_chip) {
				if(launcher.slots[1] != null)
					return false;

				launcher.slots[1] = heldStack.copy();
				heldStack.stackSize = 0;
				world.playSoundEffect(x, y, z, "hbm:item.upgradePlug", 1.0F, 1.0F);
			} else if(heldStack == null && launcher.slots[1] != null) {
				if(player.inventory.addItemStackToInventory(launcher.slots[1].copy())) {
					launcher.slots[1] = null;
					launcher.markChanged();
					world.playSoundEffect(x, y, z, "hbm:item.upgradePlug", 1.0F, 1.0F);
				}
			}
		}

		return true;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);
		
		if(pos == null) return;
		
		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);
		
		if(!(te instanceof TileEntityDysonLauncher)) return;
		
		TileEntityDysonLauncher launcher = (TileEntityDysonLauncher) te;
		
		List<String> text = new ArrayList<String>();

		if(launcher.swarmId > 0) {
			text.add("ID: " + launcher.swarmId);
			text.add((launcher.power < launcher.maxPower ? EnumChatFormatting.RED : EnumChatFormatting.GREEN) + "Power: " + BobMathUtil.getShortNumber(launcher.power) + "HE");
		} else {
			text.add("No Satellite ID-Chip installed!");
		}
		
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
