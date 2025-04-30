package com.hbm.entity.mob;

import net.minecraft.world.World;

public class EntityShifterEel extends EntityFish implements IEntityEnumMulti {

	public enum ShifterEel {
		PLAIN,
		FAST,
		EXOTIC,
		PHASED,
		ELEMENTAL,
		PERFECT,
	}

	public ShifterEel type;

	public EntityShifterEel(World world) {
		super(world, 1.8, 8.0F);

		type = ShifterEel.values()[world.rand.nextInt(ShifterEel.values().length)];
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum getEnum() {
		return type;
	}

}
