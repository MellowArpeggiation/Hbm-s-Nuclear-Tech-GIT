package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.trait.CBT_Dyson;
import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityDysonLauncher extends TileEntityMachineBase implements IEnergyReceiverMK2 {

	public int swarmId;

	public long power;
	public long maxPower = 1_000_000;

	public TileEntityDysonLauncher() {
		super(2);
	}

	@Override
	public String getName() {
		return "container.machineDysonLauncher";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			for(DirPos pos : getConPos()) trySubscribe(worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());

			swarmId = ISatChip.getFreqS(slots[1]);

			if(power == maxPower && slots[0] != null && slots[0].getItem() == ModItems.swarm_member && swarmId > 0) {
				CBT_Dyson.launch(worldObj, swarmId);

				worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hbm:misc.spinshot", 4.0F, 0.9F + worldObj.rand.nextFloat() * 0.3F);
				worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hbm:misc.spinshot", 4.0F, 1F + worldObj.rand.nextFloat() * 0.3F);

				slots[0].stackSize--;
				power = 0;

				if(slots[0].stackSize <= 0) slots[0] = null;
			}

			networkPackNT(15);
		}
	}

	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
			new DirPos(xCoord - dir.offsetX * 0 - rot.offsetX * 3, yCoord, zCoord - dir.offsetZ * 0 - rot.offsetZ * 3, rot.getOpposite()),
			new DirPos(xCoord - dir.offsetX * 1 - rot.offsetX * 3, yCoord, zCoord - dir.offsetZ * 1 - rot.offsetZ * 3, rot.getOpposite()),
			new DirPos(xCoord - dir.offsetX * 2 - rot.offsetX * 3, yCoord, zCoord - dir.offsetZ * 2 - rot.offsetZ * 3, rot.getOpposite()),
			new DirPos(xCoord - dir.offsetX * 3 - rot.offsetX * 3, yCoord, zCoord - dir.offsetZ * 3 - rot.offsetZ * 3, rot.getOpposite()),
			new DirPos(xCoord - dir.offsetX * 4 - rot.offsetX * 3, yCoord, zCoord - dir.offsetZ * 4 - rot.offsetZ * 3, rot.getOpposite()),
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(swarmId);
		buf.writeLong(power);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		swarmId = buf.readInt();
		power = buf.readLong();
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		if(slot == 0) return itemStack.getItem() == ModItems.swarm_member;
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] {0};
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord - 11,
				yCoord,
				zCoord - 11,
				xCoord + 12,
				yCoord + 18,
				zCoord + 12
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
