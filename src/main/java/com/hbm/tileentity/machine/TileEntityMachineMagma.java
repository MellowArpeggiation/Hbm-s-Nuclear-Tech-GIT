package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.CrucibleUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardTransceiver;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMachineMagma extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiver {

	public long power;
	public FluidTank[] tanks;
	
	public static final int maxLiquid = MaterialShapes.BLOCK.q(16);
	public List<MaterialStack> liquids = new ArrayList<>();

	public float drillExtension;

	public TileEntityMachineMagma() {
		super(0);
		tanks = new FluidTank[0];
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			// baby drill, baby
			int totalLiquid = 0;
			for(MaterialStack mat : liquids) totalLiquid += mat.amount;

			int toAdd = MaterialShapes.QUANTUM.q(1);

			if(totalLiquid + toAdd <= maxLiquid) {
				addToStack(new MaterialStack(Mats.MAT_RICH_MAGMA, toAdd));
			}

			// pour me a drink, barkeep
			if(!liquids.isEmpty()) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
				
				Vec3 impact = Vec3.createVectorHelper(0, 0, 0);
				MaterialStack didPour = CrucibleUtil.pourFullStack(worldObj, xCoord + 0.5D + dir.offsetX * 3.875D, yCoord - 1.75D, zCoord + 0.5D + dir.offsetZ * 3.875D, 6, true, liquids, MaterialShapes.INGOT.q(1), impact);

				if(didPour != null) {
					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "foundry");
					data.setInteger("color", didPour.material.moltenColor);
					data.setByte("dir", (byte) dir.ordinal());
					data.setFloat("off", 0.625F);
					data.setFloat("base", 0.625F);
					data.setFloat("len", Math.max(1F, yCoord - 2 - (float) (Math.ceil(impact.yCoord) - 0.875)));
					PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, xCoord + 0.5D + dir.offsetX * 3.875D, yCoord - 2, zCoord + 0.5D + dir.offsetZ * 3.875D), new TargetPoint(worldObj.provider.dimensionId, xCoord + 0.5, yCoord + 1, zCoord + 0.5, 50));
				}
			}
			
			liquids.removeIf(o -> o.amount <= 0);
		}
	}

	public void addToStack(MaterialStack matStack) {
		for(MaterialStack mat : liquids) {
			if(mat.material == matStack.material) {
				mat.amount += matStack.amount;
				return;
			}
		}
		
		liquids.add(matStack.copy());
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return 0;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return tanks;
	}

	@Override
	public String getName() {
		return "container.machineMagma";
	}

	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 4,
				yCoord - 3,
				zCoord - 4,
				xCoord + 5,
				yCoord + 3,
				zCoord + 5
			);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

}
