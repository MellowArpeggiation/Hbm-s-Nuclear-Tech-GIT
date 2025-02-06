package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockSideRotation;
import com.hbm.lib.RefStrings;
import com.hbm.world.gen.INBTTransformable;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWandJigsaw extends BlockContainer implements IBlockSideRotation, INBTTransformable {

	private IIcon iconTop;
	private IIcon iconSide;
	private IIcon iconBack;

	public BlockWandJigsaw() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityWandJigsaw();
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		int l = BlockPistonBase.determineOrientation(world, x, y, z, player);
		world.setBlockMetadataWithNotify(x, y, z, l, 2);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":wand_jigsaw");
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":wand_jigsaw_top");
		this.iconSide = iconRegister.registerIcon(RefStrings.MODID + ":wand_jigsaw_side");
		this.iconBack = iconRegister.registerIcon(RefStrings.MODID + ":wand_jigsaw_back");
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		if(side == meta) return blockIcon;
		if(IBlockSideRotation.isOpposite(side, meta)) return iconBack;
		if(side <= 1) return iconTop;
		if(side > 3 && meta <= 1) return iconTop;
		return iconSide;
	}

	@Override
	public int getRotationFromSide(IBlockAccess world, int x, int y, int z, int side) {
		if(side == 0) return IBlockSideRotation.topToBottom(getRotationFromSide(world, x, y, z, 1));

		int meta = world.getBlockMetadata(x, y, z);
		if(side == meta || IBlockSideRotation.isOpposite(side, meta)) return 0;

		// downwards facing has no changes, upwards flips anything not handled already
		if(meta == 0) return 0;
		if(meta == 1) return 3;

		// top (and bottom) is rotated fairly normally
		if(side == 1) {
			switch(meta) {
			case 2: return 3;
			case 3: return 0;
			case 4: return 1;
			case 5: return 2;
			}
		}

		// you know what I aint explaining further, it's a fucking mess here
		if(meta == 2) return side == 4 ? 2 : 1;
		if(meta == 3) return side == 4 ? 1 : 2;
		if(meta == 4) return side == 2 ? 1 : 2;
		if(meta == 5) return side == 2 ? 2 : 1;

		return 0;
	}

	@Override
	public int getRenderType() {
		return IBlockSideRotation.getRenderType();
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		return INBTTransformable.transformMetaDeco(meta, coordBaseMode);
	}


	public static class TileEntityWandJigsaw extends TileEntity {

		private int priority;
		private String pool = "default";
		private String name = "default";
		private String target = "default";

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("direction", this.getBlockMetadata());

			nbt.setInteger("priority", priority);
			nbt.setString("pool", pool);
			nbt.setString("name", name);
			nbt.setString("target", target);
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);

			priority = nbt.getInteger("priority");
			pool = nbt.getString("pool");
			name = nbt.getString("name");
			target = nbt.getString("target");
		}

	}

}
