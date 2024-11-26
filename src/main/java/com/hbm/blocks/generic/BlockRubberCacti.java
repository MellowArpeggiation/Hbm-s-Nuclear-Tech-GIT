package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.BlockMulti;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockNTMFlower.EnumFlowerType;
import com.hbm.entity.effect.EntityMist;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockRubberCacti extends BlockEnumMulti {
	
	//@SideOnly(Side.CLIENT) protected IIcon[] icons;

	public BlockRubberCacti(Material material) {
		super(Material.plants, EnumBushType.class, false, true);
	}
	public static enum EnumBushType {
		CACT,
		BUSH,
		FLOWER
	}
	public static int renderIDcact = RenderingRegistry.getNextAvailableRenderId();
	
	@Override
	public int getRenderType(){
		return renderIDcact;
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
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return super.canPlaceBlockAt(world, x, y, z) && this.canBlockStay(world, x, y, z);
	}

	protected boolean canPlaceBlockOn(Block block) {
		return block == ModBlocks.vinyl_sand || block == ModBlocks.rubber_grass;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		this.checkAndDropBlock(world, x, y, z);
	}
	
	protected void checkAndDropBlock(World world, int x, int y, int z) {
		if(!this.canBlockStay(world, x, y, z)) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlock(x, y, z, getBlockById(0), 0, 2);
		}
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		return canPlaceBlockOn(world.getBlock(x, y - 1, z));
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		int meta = world.getBlockMetadata(x, y, z);
		if(meta == EnumBushType.CACT.ordinal()) {
			if (!world.isRemote && entity instanceof EntityPlayer) {
				world.createExplosion(entity, x, y, z, 4.0F, false); 
				world.setBlockToAir(x, y, z); 
				EntityMist mist = new EntityMist(world);
				mist.setType(Fluids.CHLORINE);
				mist.setPosition(x, y, z);
				mist.setArea(8, 5.5F);
				world.spawnEntityInWorld(mist);
			}
	    
	    }
	}
}
