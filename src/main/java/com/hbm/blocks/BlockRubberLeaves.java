package com.hbm.blocks;

import java.util.Random;

import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class BlockRubberLeaves extends Block {

	public BlockRubberLeaves(Material mat) {
		super(mat);
		this.setTickRandomly(true);
	}

	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		if (this == ModBlocks.pet_leaves) {
			return ModItems.leaf_pet;
		}
		return ModItems.leaf_rubber;
	}

	public boolean renderAsNormalBlock() {
		return true;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	protected boolean canSilkHarvest() {
		return false;
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int x, int y, int z, int metadata, float chance,
			int fortune) {
		super.dropBlockAsItemWithChance(world, x, y, z, metadata, chance, fortune);

		if (!world.isRemote) {
			Random rand = world.rand;
			if (this == ModBlocks.rubber_leaves && rand.nextFloat() < 0.3F) {
				this.dropBlockAsItem(world, x, y, z, new ItemStack(ModItems.leaf_rubber));

				if (rand.nextFloat() < 0.5F) {
					this.dropBlockAsItem(world, x, y, z,
							new ItemStack(ModBlocks.sapling_pvc, 1, 1));
				}
			}
			if (this == ModBlocks.pet_leaves && rand.nextFloat() < 0.3F) {
				this.dropBlockAsItem(world, x, y, z, new ItemStack(ModItems.leaf_pet));

				if (rand.nextFloat() < 0.5F) {
					this.dropBlockAsItem(world, x, y, z,
							new ItemStack(ModBlocks.sapling_pvc, 1, 0));
				}
			}


		}
	}
}
