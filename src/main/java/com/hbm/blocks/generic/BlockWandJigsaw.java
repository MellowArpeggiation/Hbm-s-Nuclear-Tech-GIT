package com.hbm.blocks.generic;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockWandJigsaw extends BlockContainer {

	public BlockWandJigsaw() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityWandJigsaw();
	}

	public static class TileEntityWandJigsaw extends TileEntity {

	}

}
