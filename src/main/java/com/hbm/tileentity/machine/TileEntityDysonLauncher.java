package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Dyson;
import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityDysonLauncher extends TileEntityMachineBase implements IEnergyReceiverMK2 {

	public int swarmId;
	public int swarmCount;

	public long power;
	public static final long MAX_POWER = 20_000_000;

	private static final int SPIN_UP_TIME = 132;
	private static final int SPIN_DOWN_TIME = 68;
	private static final long POWER_PER_TICK = MAX_POWER / SPIN_UP_TIME;

	private static final int MEMBERS_PER_LAUNCH = 4;

	public boolean isOperating;
	public boolean isSpinningDown;
	public int operatingTime;

	public float rotation;
	public float lastRotation;
	public float speed;

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
			swarmCount = CBT_Dyson.count(worldObj, swarmId);

			isOperating = !isSpinningDown && power >= POWER_PER_TICK && slots[0] != null && slots[0].getItem() == ModItems.swarm_member && swarmId > 0;

			if(isSpinningDown) {
				operatingTime++;

				if(operatingTime > SPIN_DOWN_TIME) {
					isSpinningDown = false;
					operatingTime = 0;
				}
			} else if(isOperating) {
				operatingTime++;
				power -= POWER_PER_TICK;

				if(operatingTime > SPIN_UP_TIME) {
					int toLaunch = Math.min(slots[0].stackSize, MEMBERS_PER_LAUNCH);
					CBT_Dyson.launch(worldObj, swarmId, toLaunch);

					CBT_Atmosphere atmosphere = CelestialBody.getTrait(worldObj, CBT_Atmosphere.class);
					double pressure = atmosphere != null ? atmosphere.getPressure() : 0;
					double scaledPressure = 1.0 - Math.pow(1.0 - pressure, 3);

					float volume = Math.min((float)scaledPressure * 16.0F, 4.0F);

					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
					ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

					worldObj.playSoundEffect(xCoord + rot.offsetX * 6, yCoord + 8, zCoord + rot.offsetZ * 6, "hbm:misc.spinshot", volume, 0.9F + worldObj.rand.nextFloat() * 0.3F);
					worldObj.playSoundEffect(xCoord + rot.offsetX * 6, yCoord + 8, zCoord + rot.offsetZ * 6, "hbm:misc.spinshot", volume, 1F + worldObj.rand.nextFloat() * 0.3F);

					int count = Math.min(20, (int)(pressure * 80));

					NBTTagCompound data = new NBTTagCompound();
					data.setInteger("count", count);
					data.setDouble("posX", xCoord + rot.offsetX * 9);
					data.setDouble("posY", yCoord + 12);
					data.setDouble("posZ", zCoord + rot.offsetZ * 9);
					data.setString("type", "spinlaunch");
					data.setFloat("scale", 3);
					data.setDouble("moX", dir.offsetX * 10);
					data.setDouble("moY", 10);
					data.setDouble("moZ", dir.offsetZ * 10);
					data.setInteger("maxAge", 10 + count / 2 + worldObj.rand.nextInt(5));
					MainRegistry.proxy.effectNT(data);

					slots[0].stackSize -= toLaunch;
					if(slots[0].stackSize <= 0) slots[0] = null;

					operatingTime = 0;
					isSpinningDown = true;
				}
			} else {
				operatingTime = 0;
			}

			networkPackNT(250);
		} else {
			// SHAKE IT LIKE IT'S HEAT, OVERDRIVE
			boolean sunsetOverdrive = false;

			float acceleration = sunsetOverdrive ? 2.5F : 0.75F;
			float deceleration = sunsetOverdrive ? 15.0F : 3.0F;
			float resetSpeed = sunsetOverdrive ? 30.0F : 8.0F;

			if(isOperating) {
				speed += acceleration;
				if(speed > 90) speed = 90;
			} else if(speed > 0.1F) {
				speed -= deceleration;
				if(speed < resetSpeed) speed = resetSpeed;
			}

			lastRotation = rotation;
			if(!isOperating && speed <= resetSpeed && rotation > 360 - resetSpeed * 1.5) {
				lastRotation -= 360;
				rotation = 0;
				speed = 0;
			} else {
				rotation += speed;
			}

			if(rotation >= 360) {
				rotation -= 360;
				lastRotation -= 360;
			}
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
		buf.writeBoolean(isOperating);
		buf.writeInt(swarmCount);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		swarmId = buf.readInt();
		power = buf.readLong();
		isOperating = buf.readBoolean();
		swarmCount = buf.readInt();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setBoolean("spinDown", isSpinningDown);
		nbt.setInteger("time", operatingTime);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		power = nbt.getLong("power");
		isSpinningDown = nbt.getBoolean("spinDown");
		operatingTime = nbt.getInteger("time");
	}

	@Override
	public int getInventoryStackLimit() {
		return 4;
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
	@Override public long getMaxPower() { return MAX_POWER; }

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
