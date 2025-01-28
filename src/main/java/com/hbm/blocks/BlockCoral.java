package com.hbm.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockCoral extends BlockEnumMulti {

	public BlockCoral() {
		super(Material.water, EnumCoral.class, false, true);
	}

	public static enum EnumCoral {
		TUBE,
		BRAIN,
		BUBBLE,
		FIRE,
		HORN,
	}

	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public int getRenderType() {
		return renderID;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
    
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

}
