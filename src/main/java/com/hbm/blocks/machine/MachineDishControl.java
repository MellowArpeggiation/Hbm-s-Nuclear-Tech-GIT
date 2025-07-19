package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityAirScrubber;
import com.hbm.tileentity.machine.TileEntityDishControl;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ChatBuilder;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.ArrayList;
import java.util.List;

public class MachineDishControl extends BlockDummyable implements ITooltipProvider {
	public MachineDishControl(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int meta) {
		if(meta >= 12) return new TileEntityDishControl();
		return new TileEntityProxyCombo(false, false, false); // no need for extra atm, it's just two blocks
	}

	@Override
	public int[] getDimensions() { return new int[] {0, 0, 0, 0, 0, 1}; }

	@Override
	public int getOffset() { return 0; }

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		}
		else if(!player.isSneaking())
		{
			// Find the controller TileEntity
			int[] pos = findCore(world, x, y, z);

			TileEntity e = world.getTileEntity(pos[0], pos[1], pos[2]);
			if( e instanceof TileEntityDishControl ) {
				TileEntityDishControl entityDishControl = (TileEntityDishControl) e;
				// Check if a dish was not assigned
				if (entityDishControl.dish == null)
				{
					player.addChatMessage(ChatBuilder.start("[").color(EnumChatFormatting.DARK_AQUA)
						.nextTranslation(this.getUnlocalizedName() + ".name").color(EnumChatFormatting.DARK_AQUA)
						.next("] ").color(EnumChatFormatting.DARK_AQUA)
						.next("Dish not linked!").color(EnumChatFormatting.RED).flush());

					return false;
				}

				// Get reference to the stardar
				MachineStardar stardar = (MachineStardar) world.getBlock(
					entityDishControl.dish.xCoord,
					entityDishControl.dish.yCoord,
					entityDishControl.dish.zCoord
				);

				// Trigger the StarDar UI to open
				return stardar.onBlockActivated(
					world,
					entityDishControl.dish.xCoord,
					entityDishControl.dish.yCoord,
					entityDishControl.dish.zCoord,
					player, side, hitX, hitY, hitZ
				);
			}

			return false;

		}
		else
		{
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}
}
