package com.hbm.render.model;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;

public class ModelScuttlecrab extends ModelBase {

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float rotationYaw, float rotationHeadYaw, float rotationPitch, float scale) {
		super.render(entity, limbSwing, limbSwingAmount, rotationYaw, rotationHeadYaw, rotationPitch, scale);

		GL11.glPushMatrix();
		{

            GL11.glRotatef(180.0F, 0, 0, 1);
            GL11.glTranslatef(0, -1.5F, 0);

			ResourceManager.scuttlecrab.renderAll();

        }
        GL11.glPopMatrix();
    }

}
