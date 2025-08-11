package com.hbm.items.block;


import com.hbm.blocks.BlockNTSapling;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemBlockNamedMetaSapling extends ItemBlockMeta {
	private final Block block;
	public ItemBlockNamedMetaSapling(Block block) {
		super(block);
		this.block = block;
	}
	
	//OG class didn't have getUnlocalizedName and I didn't want to add it for fear of fucking shit up
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + stack.getItemDamage();
	}
	
	    @Override
	    @SideOnly(Side.CLIENT)
	    public IIcon getIconFromDamage(int meta) {
	        if (meta < 0 || meta >= 2) { // assuming 2 variants
	            meta = 0;
	        }
	        return ((BlockNTSapling) block).getIcon(0, meta);
	    }
	
}

