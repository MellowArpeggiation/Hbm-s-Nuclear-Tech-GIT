package com.hbm.blocks.network;

import java.util.Random;

import com.hbm.items.ModItems;

import net.minecraft.item.Item;

public class BlockConveyor extends BlockConveyorBendable {

	public BlockConveyor() {
		super();
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return ModItems.conveyor_wand;
	}

}
