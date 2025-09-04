package com.hbm.sound;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class AudioDynamicEntity extends AudioDynamic {

	protected Entity entity;

	protected AudioDynamicEntity(ResourceLocation loc, Entity entity) {
		super(loc);
		this.entity = entity;
	}

	@Override
	public void update() {
		super.update();

		if(entity == null || entity.isDead) {
			stop();
			entity = null;
			return;
		}
	}

	@Override
	public float getXPosF() {
		return (float)entity.posX;
	}

	@Override
	public float getYPosF() {
		return (float)entity.posY;
	}

	@Override
	public float getZPosF() {
		return (float)entity.posZ;
	}

}
