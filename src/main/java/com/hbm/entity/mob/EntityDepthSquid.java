package com.hbm.entity.mob;

import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.world.World;

public class EntityDepthSquid extends EntitySquid implements IEntityEnumMulti {

	public enum DepthSquid {
		AQUA,
		BLACK,
		ORANGE,
		OURPLE,
		RED,
		SILVER,
		VICIOUS,
	}

	public DepthSquid type;

	public EntityDepthSquid(World world) {
		super(world);

		type = DepthSquid.values()[world.rand.nextInt(DepthSquid.values().length)];
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum getEnum() {
		return type;
	}

}
