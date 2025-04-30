package com.hbm.entity.mob;

import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class EntityScuttlecrab extends EntityWaterMob implements IEntityEnumMulti {

	public enum Scuttlecrab {
		TROPICAL,
		CLAYINFUSED,
	}

	public Scuttlecrab type;

	public EntityScuttlecrab(World world) {
		super(world);

		type = Scuttlecrab.TROPICAL;
		if(world.rand.nextInt(8) == 0) type = Scuttlecrab.CLAYINFUSED;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum getEnum() {
		return type;
	}


	// Walk on the seafloor, to do so, we override the isInWater check when attempting movement
	private boolean seafloorWalking = false;

	@Override
	public boolean isInWater() {
		if(seafloorWalking) return false;
		return this.inWater;
	}

	@Override
	public void onLivingUpdate() {
		seafloorWalking = true;
		super.onLivingUpdate();
		seafloorWalking = false;
	}

	@Override
	protected Item getDropItem() {
		return Items.clay_ball;
	}

}
